package com.stat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


@WebServlet("/callback")
public class SpotifyCallbackServlet extends HttpServlet {
    private static final String CLIENT_ID = "61c8cd337f314036ae037122ceb92039";
    private static final String CLIENT_SECRET = "933d43292e054a93b9722e921b3e33e2";
    private static final String REDIRECT_URI = "http://localhost:8090/SpotifyStat/callback";
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String code = request.getParameter("code");
        String state = request.getParameter("state");

        if (code == null || state == null) {
            response.getWriter().write("Authorization failed!");
            return;
        }

        // Exchange code for access token
        String accessToken = getAccessToken(code);

        if (accessToken != null) {
            // Fetch user details using access token
            String userDetails = fetchSpotifyUserDetails(accessToken);
            request.setAttribute("userDetails", userDetails);
            request.getRequestDispatcher("home.jsp").forward(request, response);
        } else {
            response.getWriter().write("Failed to get access token!");
        }
    }

    private String getAccessToken(String code) throws IOException {
        URL url = new URL(TOKEN_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes()));
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String body = "grant_type=authorization_code&code=" + code + "&redirect_uri=" + REDIRECT_URI;
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes());
        }

        if (conn.getResponseCode() == 200) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                // Parse JSON response to extract access token
                String jsonResponse = response.toString();
                return jsonResponse.split("\"access_token\":\"")[1].split("\"")[0];
            }
        }
        return null;
    }

    private String fetchSpotifyUserDetails(String accessToken) throws IOException {
        StringBuilder userDetails = new StringBuilder();

        // Fetch Top Artists
        String topArtists = fetchSpotifyData("https://api.spotify.com/v1/me/top/artists?limit=5", accessToken);
        userDetails.append("Top Artists:\n").append(parseNames(topArtists, "name")).append("\n\n");

        // Fetch Top Tracks
        String topTracks = fetchSpotifyData("https://api.spotify.com/v1/me/top/tracks?limit=20", accessToken);
        userDetails.append("Top Tracks:\n").append(parseNames(topTracks, "name")).append("\n\n");

        // Fetch Top Albums (from Top Tracks)
        userDetails.append("Top Albums:\n").append(parseAlbums(topTracks)).append("\n\n");

        return userDetails.toString();
    }

    private String fetchSpotifyData(String endpoint, String accessToken) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        if (conn.getResponseCode() == 200) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        }
        return null;
    }

    private String parseNames(String jsonResponse, String key) {
        // Extract values from JSON (e.g., artist names or track names)
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray items = jsonObject.getJSONArray("items");
            StringBuilder names = new StringBuilder();
            for (int i = 0; i < items.length(); i++) {
                names.append(i + 1).append(". ").append(items.getJSONObject(i).getString(key)).append("\n");
            }
            return names.toString();
        } catch (Exception e) {
            return "Error parsing names: " + e.getMessage();
        }
    }

    private String parseAlbums(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray items = jsonObject.getJSONArray("items");
            StringBuilder albums = new StringBuilder();
            HashSet<String> uniqueAlbums = new HashSet<>();
            for (int i = 0; i < items.length(); i++) {
                JSONObject album = items.getJSONObject(i).getJSONObject("album");
                String albumName = album.getString("name");
                if (uniqueAlbums.add(albumName)) { // Avoid duplicates
                    albums.append(uniqueAlbums.size()).append(". ").append(albumName).append("\n");
                    if (uniqueAlbums.size() >= 5) break; // Limit to 5 albums
                }
            }
            return albums.toString();
        } catch (Exception e) {
            return "Error parsing albums: " + e.getMessage();
        }
    }
}
