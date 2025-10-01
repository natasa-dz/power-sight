import random
import io
from locust import HttpUser, task, between
from PIL import Image

class RealEstateRequestUser(HttpUser):
    wait_time = between(0.5, 2)
    
    def on_start(self):
        """Called when a simulated user starts. Login to get JWT token."""
        self.login()
    
    def login(self):
        """Login and store the JWT token for authenticated requests."""
        # Simulate mostly citizens creating real estate requests
        rand_val = random.random()
        if rand_val < 0.0001:  # ~0.01% admins
            username = f"admin{random.randint(1, 10)}@example.com"
        elif rand_val < 0.002:  # ~0.2% employees
            username = f"employee{random.randint(1, 200)}@example.com"
        else:  # citizens (owners of real estate)
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
            self.user_id = random.randint(1, 3000)  # Simulated owner ID
        else:
            self.token = None
            self.user_id = None
    
    def create_dummy_image(self, size=(800, 600)):
        """Create a dummy image file in memory."""
        img = Image.new('RGB', size, color=(random.randint(0, 255), 
                                            random.randint(0, 255), 
                                            random.randint(0, 255)))
        img_bytes = io.BytesIO()
        img.save(img_bytes, format='JPEG')
        img_bytes.seek(0)
        return img_bytes
    
    def create_dummy_pdf(self):
        """Create a dummy PDF-like file in memory."""
        pdf_content = b"%PDF-1.4\n%Mock PDF document for testing\n%%EOF"
        return io.BytesIO(pdf_content)
    
    @task
    def create_real_estate_request(self):
        """Test the create real estate request endpoint."""
        if not self.token:
            return
        
        # Generate random real estate data
        municipalities = ["Belgrade", "Novi Sad", "Niš", "Kragujevac", "Subotica"]
        towns = ["Center", "New Belgrade", "Zvezdara", "Voždovac", "Palilula"]
        
        floors = random.randint(1, 20)
        num_households = random.randint(1, 5)
        
        # Create household requests
        household_requests = []
        for i in range(num_households):
            household_requests.append({
                "floor": random.randint(0, floors),
                "squareFootage": round(random.uniform(30.0, 150.0), 2),
                "apartmentNumber": random.randint(1, 100)
            })
        
        # Create real estate request DTO
        real_estate_request = {
            "owner": self.user_id,
            "address": f"Street {random.randint(1, 100)}, No. {random.randint(1, 50)}",
            "municipality": random.choice(municipalities),
            "town": random.choice(towns),
            "floors": floors,
            "createdAt": None,  # Backend typically sets this
            "householdRequests": household_requests
        }
        
        # Create image files (2-5 images)
        num_images = random.randint(2, 5)
        image_files = []
        for i in range(num_images):
            img_bytes = self.create_dummy_image()
            image_files.append(
                ('images', (f'real_estate_{i}.jpg', img_bytes, 'image/jpeg'))
            )
        
        # Create documentation files (1-3 documents)
        num_docs = random.randint(1, 3)
        doc_files = []
        for i in range(num_docs):
            doc_bytes = self.create_dummy_pdf()
            doc_files.append(
                ('documentation', (f'document_{i}.pdf', doc_bytes, 'application/pdf'))
            )
        
        # Combine all files
        files = image_files + doc_files
        
        # Add JSON data as a file part
        import json
        files.append(
            ('realEstateRequest', (None, json.dumps(real_estate_request), 'application/json'))
        )
        
        # Make the request with authentication
        headers = {
            "Authorization": f"Bearer {self.token}"
        }
        
        self.client.post(
            "/api/real-estate-request/registration",
            files=files,
            headers=headers,
            name="/api/real-estate-request/registration"
        )