from locust import HttpUser, task, between
import random
import datetime


def login(client, username, password):
    r = client.post("/api/users/login", json={"email": username, "password": password})
    if r.status_code == 200 and r.json():
        body = r.json()
        token = body.get("token") or body.get("accessToken")
        user_id = body.get("id")
        return token, user_id
    return None, None


class CitizenUser(HttpUser):
    wait_time = between(1, 3)

    def on_start(self):
        self.id = random.randint(1, 1000)
        self.username = f"citizen{self.id}@test.com"
        self.password = "test"
        self.token, self.user_id = login(self.client, self.username, self.password)


    @task(2)
    def login_citizen(self):
        self.token = login(self.client, self.username, self.password)

    @task(2)
    def list_owner_households(self):
        owner_id = random.randint(1, 1000)  # dummy seedovani vlasnik
        page = random.randint(0, 5)
        size = random.choice([5, 10, 20])
        self.client.get(
            "/api/households/owner",
            params={"page": page, "size": size, "ownerId": owner_id},
            headers={"Authorization": f"Bearer {self.token}"}
        )


    @task(2)
    def list_no_owner_households(self):
        page = random.randint(0, 5)
        size = random.choice([5, 10, 20])
        self.client.get(
            "/api/households/no-owner",
            params={"page": page, "size": size},
            headers={"Authorization": f"Bearer {self.token}"}
        )


    @task(2)
    def submit_ownership_request(self):
        files = {"files": ("ownership.pdf", b"locust dummy pdf")}
        self.client.post(
            "/api/ownership-requests/requestOwnership",
            params={"userId": self.user_id, "householdId": random.randint(1, 500)},
            files=files,
            headers={"Authorization": f"Bearer {self.token}"}
        )

    @task(1)
    def list_my_ownership_requests(self):
        self.client.get(
            f"/api/ownership-requests/{self.id}",
            headers={"Authorization": f"Bearer {self.token}"}
        )

    @task(2)
    def book_appointment(self):
        eid = random.randint(1, 4)
        user_id = random.randint(1, 1000)
        start_time = (datetime.datetime.now() + datetime.timedelta(days=1, hours=random.randint(8, 15))).isoformat()
        self.client.post(
            "/api/appointments/create",
            params={"employeeId": eid, "userId": user_id, "startTime": start_time, "timeSlotCount": 1},
            headers={"Authorization": f"Bearer {self.token}"}
        )

    @task(1)
    def list_available_slots(self):
        eid = random.randint(1, 4)
        date = (datetime.date.today() + datetime.timedelta(days=1)).isoformat()
        self.client.get(
            f"/api/appointments/available-slots/{eid}",
            params={"date": date},
            headers={"Authorization": f"Bearer {self.token}"}
        )


class AdminUser(HttpUser):
    wait_time = between(2, 5)

    def on_start(self):
        self.username = "admin1@test.com"
        self.password = "admin"
        self.token = login(self.client, self.username, self.password)

    @task(2)
    def list_pending_requests(self):
        self.client.get(
            "/api/ownership-requests/pending",
            headers={"Authorization": f"Bearer {self.token}"}
        )

    @task(1)
    def process_request(self):
        rid = random.randint(1, 500)
        approved = random.choice([True, False])
        note = "Approved by Locust" if approved else "Rejected by Locust"
        self.client.post(
            "/api/ownership-requests/process",
            json={"requestId": rid, "approved": approved, "reason": note},
            headers={"Authorization": f"Bearer {self.token}"}
        )


    @task(1)
    def employee_schedule(self):
        eid = random.randint(1, 4)
        date = (datetime.date.today() + datetime.timedelta(days=1)).isoformat()
        self.client.get(
            f"/api/appointments/get-employees-appointments-for-date/{eid}",
            params={"date": date},
            headers={"Authorization": f"Bearer {self.token}"}
        )
