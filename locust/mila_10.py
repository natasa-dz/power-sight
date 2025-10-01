import random
from locust import HttpUser, task, between

class GraphDataUser(HttpUser):
    wait_time = between(0.1, 1)
    
    # Define possible values
    time_ranges = ["12", "24", "week", "month", "3months"]
    
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
    def get_graph_data(self):
        """Test the get data for graph endpoint."""
        if not self.token:
            return
        
        # Name is always "consumption"
        name = "consumption"
        
        # Select random time range
        time_range = random.choice(self.time_ranges)
        
        headers = {
            "Authorization": f"Bearer {self.token}"
        }
        
        self.client.get(
            f"/api/household/graph/{name}/{time_range}",
            headers=headers,
            name="/api/household/graph/{name}/{timeRange}"
        )