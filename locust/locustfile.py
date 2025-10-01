import random
from locust import HttpUser, task, between

class LoginUser(HttpUser):
    wait_time = between(0.1, 1)  # adjust as needed

    @task
    def login(self):
        # Decide which user type to simulate
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

        self.client.post(
            "/api/users/login",
            json=payload
        )
