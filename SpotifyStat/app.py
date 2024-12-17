from flask import Flask, request, jsonify
from transformers import pipeline
import requests

app = Flask(__name__)

# NLP model for mood detection
classifier = pipeline("text-classification", model="distilbert-base-uncased-finetuned-sst-2-english")

@app.route('/generate_playlist', methods=['POST'])
def generate_playlist():
    data = request.get_json()
    prompt = data.get("prompt")
    spotify_token = data.get("spotify_token")

    if not prompt or not spotify_token:
        return jsonify({"error": "Missing required fields"}), 400

    # Analyze mood/intent
    mood_analysis = classifier(prompt)
    mood = mood_analysis[0]['label'].lower()  # Example: 'positive', 'negative'

    # Fetch Spotify recommendations
    genre = "pop" if mood == "positive" else "acoustic"
    recommendations = fetch_spotify_recommendations(genre, spotify_token)

    return jsonify({"mood": mood, "tracks": recommendations})

def fetch_spotify_recommendations(genre, token):
    url = f"https://api.spotify.com/v1/recommendations?seed_genres={genre}&limit=5"
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(url, headers=headers)
    if response.status_code == 200:
        return response.json().get("tracks", [])
    return []

if __name__ == '__main__':
    app.run(port=5000)
