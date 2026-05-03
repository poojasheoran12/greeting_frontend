import firebase_admin
from firebase_admin import credentials, firestore, storage
import os
import uuid

# 1. INITIALIZATION
# Make sure you have downloaded 'serviceAccountKey.json' from Firebase Console
# and placed it in the project root folder.
cred = credentials.Certificate("greetings-b6728-firebase-adminsdk-fbsvc-b0027ac6e7.json")
firebase_admin.initialize_app(cred, {
    'storageBucket': 'greetings-b6728.firebasestorage.app'
})

db = firestore.client()
bucket = storage.bucket()

IMAGE_DIR = "greetings_images"

# 2. CATEGORY MAPPING (Optional cleanup of folder names)
category_map = {
    "hindi": "hindi quotes",
    "english": "english quotes",
    "days": "days",
    "birthday": "birthdays",
    "patriotic": "patrotic"
}

def upload_images():
    print("🚀 Starting Upload Process...")
    
    # Iterate through category folders
    for folder_name in os.listdir(IMAGE_DIR):
        folder_path = os.path.join(IMAGE_DIR, folder_name)
        
        if not os.path.isdir(folder_path):
            continue
            
        category = category_map.get(folder_name.lower(), folder_name.lower())
        print(f"\n📂 Processing Category: {category}")
        
        # Iterate through images in the category folder
        for filename in os.listdir(folder_path):
            if filename.lower().endswith(('.png', '.jpg', '.jpeg')):
                file_path = os.path.join(folder_path, filename)
                
                # A. Upload to Firebase Storage
                blob_path = f"templates/{folder_name}/{filename}"
                blob = bucket.blob(blob_path)
                
                print(f"  📤 Uploading {filename}...")
                blob.upload_from_filename(file_path)
                
                # Make the blob publicly viewable or get signed URL
                # In production, you might want to use signed URLs or Firebase Storage rules
                blob.make_public()
                image_url = blob.public_url
                
                # B. Create Firestore Document
                # Using a unique ID for each template
                template_id = str(uuid.uuid4())
                
                # Default slot data (centered top for name, top left for photo as per your request)
                doc_data = {
                    "imageUrl": image_url,
                    "category": category,
                    "isPremium": False, # Set some to True if you want to test the monetization flow
                    "photoSlot": {
                        "x": 150.0,
                        "y": 150.0,
                        "size": 280.0
                    },
                    "textSlot": {
                        "x": 540.0,
                        "y": 115.0
                    }
                }
                
                db.collection("templates").document(template_id).set(doc_data)
                print(f"  ✅ Added to Firestore with ID: {template_id}")

    print("\n✨ ALL IMAGES UPLOADED AND SEEDED SUCCESSFULLY!")

if __name__ == "__main__":
    upload_images()
