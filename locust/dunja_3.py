import random
from locust import HttpUser, task, between

class AdminRealEstateUser(HttpUser):
    wait_time = between(0.5, 2)
    
    def on_start(self):
        """Called when a simulated user starts. Login as admin to get JWT token."""
        self.login()
    
    def login(self):
        """Login as admin and store the JWT token for authenticated requests."""
        # Only admins can access this endpoint
        username = f"admin{random.randint(1, 10)}@example.com"
        
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
    def get_all_requests_for_admin(self):
        """Test the get all real estate requests for admin endpoint."""
        if not self.token:
            return
        
        headers = {
            "Authorization": f"Bearer {self.token}"
        }
        
        self.client.get(
            "/api/real-estate-request/admin/requests",
            headers=headers,
            name="/api/real-estate-request/admin/requests"
        )