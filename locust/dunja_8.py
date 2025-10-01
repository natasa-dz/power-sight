import random
from locust import HttpUser, task, between

class OwnerHouseholdsUser(HttpUser):
    wait_time = between(0.1, 1)
    
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
        else:  # citizens (owners)
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
    def get_owner_households(self):
        """Test the get owner households endpoint."""
        if not self.token:
            return
        
        # Generate random owner ID between 1000 and 5000
        owner_id = random.randint(1000, 5000)
        
        # Random pagination parameters
        page = random.randint(0, 10)  # Pages 0-10
        size = random.choice([10, 20, 50, 100])  # Common page sizes
        
        # Optional: add sorting
        sort_options = [
            "id,asc",
            "id,desc",
            "floor,asc",
            "apartmentNumber,asc",
            "squareFootage,desc"
        ]
        sort = random.choice(sort_options)
        
        # Build query parameters
        params = {
            "ownerId": owner_id,
            "page": page,
            "size": size,
            "sort": sort
        }
        
        headers = {
            "Authorization": f"Bearer {self.token}"
        }
        
        self.client.get(
            "/api/household/owner",
            params=params,
            headers=headers,
            name="/api/household/owner"
        )