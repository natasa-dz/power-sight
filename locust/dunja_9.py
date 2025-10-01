import random
from locust import HttpUser, task, between
from urllib.parse import quote

class CitizenSearchUser(HttpUser):
    wait_time = between(0.1, 1)
    
    # Common username patterns to search for
    username_patterns = [
        "",  # Empty search (get all)
        "citizen",
        "user",
        "test",
        "marko",
        "ana",
        "stefan",
        "nikola",
        "jovana",
        "milica",
        "aleksandar",
        "petrovic",
        "jovanovic",
        "nikolic",
        "djordjevic",
        "example.com",
        "@example"
    ]
    
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
    def search_citizens(self):
        """Test the citizen search endpoint."""
        if not self.token:
            return
        
        # Select random search term
        search_term = random.choice(self.username_patterns)
        
        # Random pagination parameters
        page = 0
        size = random.choice([5, 10, 20])  # Common page sizes
        
        # Build query parameters
        params = {
            "page": page,
            "size": size
        }
        
        # Add username parameter if not empty
        if search_term:
            params["username"] = search_term
        
        headers = {
            "Authorization": f"Bearer {self.token}"
        }
        
        self.client.get(
            "/api/citizen/search",
            params=params,
            headers=headers,
            name="/api/citizen/search [citizen]"
        )