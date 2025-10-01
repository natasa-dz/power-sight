import psycopg2
from influxdb_client import InfluxDBClient, Point
from influxdb_client.client.write_api import SYNCHRONOUS
from datetime import datetime, timedelta
import random
from tqdm import tqdm

# Configuration
INFLUX_URL = "http://192.168.42.199:8086"
INFLUX_TOKEN = "sy1rRDbAjVDMUE8ibzGupFgSolsJTnA23eQuIifG__G6Fz-bI_IET9hGKh38cVKdivK381JcUKzxrOQgSilSZA=="
INFLUX_ORG = "nwt"
INFLUX_BUCKET = "consumptions"

POSTGRES_HOST = "192.168.42.199"
POSTGRES_PORT = 5433
POSTGRES_USER = "nwt"
POSTGRES_PASSWORD = "epsnwtdockerpassword"
POSTGRES_DB = "eps_nwt"

# Serbian municipalities (matching your data)
MUNICIPALITIES = [
    'Barajevo', 'Voždovac', 'Vračar', 'Grocka', 'Zvezdara', 'Zemun', 'Lazarevac', 
    'Mladenovac', 'Novi Beograd', 'Obrenovac', 'Palilula Beograd', 'Rakovica', 
    'Savski Venac', 'Sopot', 'Stari Grad', 'Surčin', 'Čukarica', 'Bačka Topola', 
    'Mali Iđoš', 'Subotica', 'Žitište', 'Zrenjanin', 'Nova Crnja', 'Novi Bečej', 
    'Sečanj', 'Ada', 'Kanjiža', 'Kikinda', 'Novi Kneževac', 'Senta', 'Čoka', 
    'Alibunar', 'Bela Crkva', 'Vršac', 'Kovačica', 'Kovin', 'Opovo', 'Pančevo', 
    'Plandište', 'Apatin', 'Kula', 'Odžaci', 'Sombor', 'Bač', 'Bačka Palanka', 
    'Bački Petrovac', 'Beočin', 'Bečej', 'Vrbas', 'Žabalj', 'Novi Sad', 'Srbobran', 
    'Sremski Karlovci', 'Temerin', 'Titel', 'Inđija', 'Irig', 'Pećinci', 'Ruma', 
    'Sremska Mitrovica', 'Stara Pazova', 'Šid', 'Bogatić', 'Vladimirci', 'Koceljeva', 
    'Krupanj', 'Loznica', 'Ljubovija', 'Mali Zvornik', 'Šabac', 'Valjevo', 'Lajkovac', 
    'Ljig', 'Mionica', 'Osečina', 'Ub', 'Velika Plana', 'Smederevo', 'Smederevska Palanka',
    'Veliko Gradište', 'Golubac', 'Žabari', 'Žagubica', 'Kučevo', 'Malo Crniće', 
    'Petrovac', 'Požarevac', 'Aranđelovac', 'Batočina', 'Knić', 'Kragujevac', 'Lapovo', 
    'Rača', 'Topola', 'Despotovac', 'Jagodina', 'Paraćin', 'Rekovac', 'Svilajnac', 
    'Ćuprija', 'Bor', 'Kladovo', 'Majdanpek', 'Negotin', 'Boljevac', 'Zaječar', 
    'Knjaževac', 'Sokobanja', 'Arilje', 'Bajina Bašta', 'Kosjerić', 'Nova Varoš', 
    'Požega', 'Priboj', 'Prijepolje', 'Sjenica', 'Užice', 'Čajetina', 'Gornji Milanovac', 
    'Ivanjica', 'Lučani', 'Čačak', 'Vrnjačka Banja', 'Kraljevo', 'Novi Pazar', 'Raška', 
    'Tutin', 'Aleksandrovac', 'Brus', 'Varvarin', 'Kruševac', 'Trstenik', 'Ćićevac', 
    'Aleksinac', 'Gadžin Han', 'Doljevac', 'Merošina', 'Pantelej', 'Palilula Niš', 
    'Mediana', 'Niška Banja', 'Crveni Krst', 'Ražanj', 'Svrljig', 'Blace', 'Žitorađa', 
    'Kuršumlija', 'Prokuplje', 'Babušnica', 'Bela Palanka', 'Dimitrovgrad', 'Pirot', 
    'Bojnik', 'Vlasotince', 'Lebane', 'Leskovac', 'Medveđa', 'Crna Trava', 'Bosilegrad', 
    'Bujanovac', 'Vladičin Han', 'Vranje', 'Preševo', 'Surdulica', 'Trgovište'
]

def get_hourly_consumption(hour):
    """Calculate consumption based on hour of day (same as Go simulator)"""
    base_consumption = 0.35
    if 0 <= hour < 6:
        return base_consumption * 0.5
    elif 6 <= hour < 9:
        return base_consumption * 3.0
    elif 9 <= hour < 18:
        return base_consumption * 1.5
    elif 18 <= hour < 22:
        return base_consumption * 4.0
    else:
        return base_consumption * 2.0

def fetch_households():
    """Fetch households from PostgreSQL"""
    conn = psycopg2.connect(
        host=POSTGRES_HOST,
        port=POSTGRES_PORT,
        user=POSTGRES_USER,
        password=POSTGRES_PASSWORD,
        database=POSTGRES_DB
    )
    
    cursor = conn.cursor()
    query = """
        SELECT 
            h.id,
            COALESCE(re.municipality, 'unknown') as municipality
        FROM households h
        LEFT JOIN real_estates re ON h.real_estate_id = re.id
        ORDER BY h.id
        LIMIT 1000
    """
    
    cursor.execute(query)
    households = cursor.fetchall()
    
    cursor.close()
    conn.close()
    
    return households

def generate_and_insert_data():
    """Generate and insert 3 years of consumption data"""
    print("Connecting to PostgreSQL...")
    
    # Then fetch households
    households = fetch_households()
    print(f"✓ Found {len(households)} households")
    
    print("Connecting to InfluxDB...")
    client = InfluxDBClient(url=INFLUX_URL, token=INFLUX_TOKEN, org=INFLUX_ORG)
    write_api = client.write_api(write_options=SYNCHRONOUS)
    
    # Calculate simulated time (same logic as Go simulator)
    base_date = datetime(2025, 9, 29, 15, 56, 0)
    now = datetime.utcnow()
    time_passed = now - base_date
    simulation_hours_passed = int(round(time_passed.total_seconds() / 60)) + 60  # minutes passed converted to hours
    
    # Calculate current simulation time
    days_passed = simulation_hours_passed // 24
    current_simulation_date = base_date + timedelta(days=days_passed)
    
    # Generate data for previous 3 years in simulation time
    end_time = current_simulation_date
    start_time = end_time - timedelta(days=365*3)
    
    print(f"\nSimulated time range:")
    print(f"  Start: {start_time}")
    print(f"  End:   {end_time}")
    print(f"  (Current real time: {now})")
    print("This will take a few minutes...\n")
    
    total_points = 0
    batch_size = 5000
    batch = []
    
    # Process each household
    for household_id, municipality in tqdm(households, desc="Processing households"):
        meter_id = f"simulator-{household_id}"
        
        # Normalize municipality name (lowercase, remove spaces) to match simulator format
        municipality_normalized = municipality.lower().replace(' ', '')
        
        # Generate hourly data for 3 years
        current_time = start_time
        while current_time < end_time:
            hour = current_time.hour
            consumption = get_hourly_consumption(hour) + (random.random() * 0.1)
            
            # Create InfluxDB point
            point = Point("consumption") \
                .tag("id", meter_id) \
                .tag("municipality", municipality_normalized) \
                .field("consumption", consumption) \
                .time(current_time)
            
            batch.append(point)
            total_points += 1
            
            # Write batch when it reaches the batch size
            if len(batch) >= batch_size:
                write_api.write(bucket=INFLUX_BUCKET, org=INFLUX_ORG, record=batch)
                batch = []
            
            # Move to next hour
            current_time += timedelta(hours=1)
    
    # Write remaining points
    if batch:
        write_api.write(bucket=INFLUX_BUCKET, org=INFLUX_ORG, record=batch)
    
    client.close()
    
    print(f"\n✓ Successfully inserted {total_points:,} data points into InfluxDB")
    print(f"✓ Average points per household: {total_points // len(households):,}")

if __name__ == "__main__":
    try:
        generate_and_insert_data()
    except Exception as e:
        print(f"❌ Error: {e}")
        import traceback
        traceback.print_exc()