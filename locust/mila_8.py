import random
from datetime import datetime, timedelta
from locust import HttpUser, task, between

class AvailableSlotsUser(HttpUser):
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
    
    def generate_random_date(self):
        """Generate a random date for appointment slots."""
        # Most people book appointments within the next 30 days
        today = datetime.now()
        days_ahead = random.randint(0, 30)
        random_date = today + timedelta(days=days_ahead)
        
        # Format as yyyy-MM-dd (ISO date format)
        return random_date.strftime('%Y-%m-%d')
    
    @task
    def get_available_slots(self):
        """Test the get available slots endpoint."""
        if not self.token:
            return
        
        # Generate random employee ID between 1 and 200
        employee_id = random.randint(1, 200)
        
        # Generate random date
        date = self.generate_random_date()
        
        # Build query parameters
        params = {
            "date": date
        }
        
        headers = {
            "Authorization": f"Bearer {self.token}"
        }
        
        self.client.get(
            f"/api/appointments/available-slots/{employee_id}",
            params=params,
            headers=headers,
            name="/api/appointments/available-slots/{employeeId}"
        )