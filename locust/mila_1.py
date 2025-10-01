import random
from locust import HttpUser, task, between

class HouseholdUser(HttpUser):
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
    def find_household_by_id(self):
        """Test the find household by id endpoint."""
        if not self.token:
            return
        
        # Generate random household ID between 1 and 5000
        household_id = random.randint(10000, 20000)
        
        headers = {
            "Authorization": f"Bearer {self.token}"
        }
        
        self.client.get(
            f"/api/household/find-by-id/{household_id}",
            headers=headers,
            name="/api/household/find-by-id/{id}"
        )