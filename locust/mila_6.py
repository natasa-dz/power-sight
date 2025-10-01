import random
from locust import HttpUser, task, between
from urllib.parse import quote

class EmployeeSearchUser(HttpUser):
    wait_time = between(0.1, 1)
    
    # Common username patterns to search for
    username_patterns = [
        "employee",
        "admin",
        "marko",
        "ana",
        "stefan",
        "nikola",
        "jovana",
        "aleksandar",
        "milica",
        "petrovic",
        "jovanovic",
        "nikolic",
        "djordjevic",
        "example.com",
        "test",
        "user"
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
    def search_employees(self):
        """Test the employee search endpoint."""
        if not self.token:
            return
        
        # Select random search term
        search_term = random.choice(self.username_patterns)
        
        # Random pagination parameters
        page = 0
        size = random.choice([5, 10, 20])
        
        # URL encode the search term
        encoded_username = quote(search_term)
        
        params = {
            "page": page,
            "size": size
        }
        
        headers = {
            "Authorization": f"Bearer {self.token}"
        }
        
        self.client.get(
            f"/api/employee/search/{encoded_username}",
            params=params,
            headers=headers,
            name="/api/employee/search/{username}"
        )