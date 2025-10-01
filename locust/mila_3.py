import random
import io
from locust import HttpUser, task, between
from PIL import Image

class UserRegistrationUser(HttpUser):
    wait_time = between(0.5, 2)
    
    # Counter to generate unique usernames
    user_counter = 0
    
    def create_dummy_profile_image(self, size=(400, 400)):
        """Create a dummy profile image file in memory."""
        img = Image.new('RGB', size, color=(random.randint(0, 255), 
                                            random.randint(0, 255), 
                                            random.randint(0, 255)))
        img_bytes = io.BytesIO()
        img.save(img_bytes, format='JPEG')
        img_bytes.seek(0)
        return img_bytes
    
    @task
    def register_employee(self):
        """Test the register endpoint for creating an employee."""
        
        # Generate unique username using counter
        UserRegistrationUser.user_counter += 1
        timestamp = random.randint(10000, 99999)
        username = f"employee_{UserRegistrationUser.user_counter}_{timestamp}@example.com"
        
        # Generate random employee data
        first_names = ["Marko", "Jovana", "Stefan", "Ana", "Nikola", "Milica", 
                      "Aleksandar", "Tijana", "Lazar", "Jelena"]
        last_names = ["Petrović", "Jovanović", "Nikolić", "Đorđević", "Stanković",
                     "Ilić", "Pavlović", "Marković", "Popović", "Kostić"]
        
        name = random.choice(first_names)
        surname = random.choice(last_names)
        password = "SecurePass123!"  # Or generate random password
        
        # Create profile photo
        photo_bytes = self.create_dummy_profile_image()
        
        # Prepare multipart form data
        files = {
            'userPhoto': (f'{username}_profile.jpg', photo_bytes, 'image/jpeg')
        }
        
        data = {
            'username': username,
            'password': password,
            'role': 'EMPLOYEE',
            'name': name,
            'surname': surname
        }
        
        self.client.post(
            "/api/users/register",
            files=files,
            data=data,
            name="/api/users/register [EMPLOYEE]"
        )