import random
from locust import HttpUser, task, between
from urllib.parse import quote

class HouseholdSearchUser(HttpUser):
    wait_time = between(0.1, 1)
    
    # Define possible values
    municipalities = ['Novi Sad', 'Zvezdara', 'Sombor', 'Golubac', 'Topola', 
                     'Kraljevo', 'Aleksandrovac', 'Leskovac', 'Ruma', 'Ada']
    apartment_numbers = [None, 1, 2, 3, 4, 5]
    
    def on_start(self):
        """Called when a simulated user starts. Login to get JWT token."""
        self.login()
    
    def login(self):
        """Login and store the JWT token for authenticated requests."""
        # Simulate different user types
        rand_val = random.random()
        if rand_val < 0.00001:  # ~10 admins
            username = f"admin{random.randint(1, 10)}@example.com"
        elif rand_val < 0.00021:  # ~200 employees
            username = f"employee{random.randint(1, 200)}@example.com"
        else:  # citizens
            username = f"citizen{random.randint(1, 3000)}@example.com"
        
        payload = {
            "email": username,
            "password": "12345678"
        }
        
        response = self.client.post(
            "/api/users/login",
            json=payload,
            name="/api/users/login"
        )
        
        if response.status_code == 200:
            data = response.json()
            self.token = data.get("accessToken")  # Adjust key name based on your UserTokenState
        else:
            self.token = None
    
    @task
    def search_households(self):
        """Test the household search endpoint."""
        if not self.token:
            return
        
        # Select random municipality
        municipality = random.choice(self.municipalities)
        
        # Always use "Street" as the address
        address = "Street"
        
        # Randomly select apartment number (including None)
        apartment_number = random.choice(self.apartment_numbers)
        
        # Random pagination parameters
        page = random.randint(0, 5)  # Pages 0-5
        size = random.choice([10, 20, 50])  # Common page sizes
        
        # URL encode the path parameters
        encoded_municipality = quote(municipality)
        encoded_address = quote(address)
        
        # Build query parameters
        params = {
            "page": page,
            "size": size
        }
        
        # Add apartment number if not None
        if apartment_number is not None:
            params["apartmentNumber"] = apartment_number
        
        headers = {
            "Authorization": f"Bearer {self.token}"
        }
        
        self.client.get(
            f"/api/household/search/{encoded_municipality}/{encoded_address}",
            params=params,
            headers=headers,
            name="/api/household/search/{municipality}/{address}"
        )