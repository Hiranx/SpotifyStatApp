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
import java.net.URLEncoder;
import java.util.Base64;

@WebServlet("/callback")
public class SpotifyCallbackServlet extends HttpServlet {
    private static final String CLIENT_ID = "#"; //add your one
    private static final String CLIENT_SECRET = "#"; //add your one
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

        String accessToken = getAccessToken(code);

        if (accessToken != null) {
        	 request.getSession().setAttribute("accessToken", accessToken);
            String userDetailsJson = fetchSpotifyUserDetails(accessToken);
            request.setAttribute("userDetailsJson", userDetailsJson);
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
                String jsonResponse = response.toString();
                return jsonResponse.split("\"access_token\":\"")[1].split("\"")[0];
            }
        }
        return null;
    }

    private String fetchSpotifyUserDetails(String accessToken) throws IOException {
        JSONObject userDetails = new JSONObject();

        String topArtistsJson = fetchSpotifyData("https://api.spotify.com/v1/me/top/artists?limit=15", accessToken);
        JSONArray artists = parseArtistsWithLevels(topArtistsJson);
        userDetails.put("highLevelArtists", getSubArray(artists, 0, 5));
        userDetails.put("middleLevelArtists", getSubArray(artists, 5, 10));

        String topAlbumsJson = fetchSpotifyData("https://api.spotify.com/v1/me/top/tracks?limit=5", accessToken);
        userDetails.put("topAlbums", parseAlbums(topAlbumsJson));

        String topGenresJson = fetchSpotifyData("https://api.spotify.com/v1/me/top/artists?limit=50", accessToken);
        userDetails.put("topGenres", parseGenres(topGenresJson));
        
        String topTracksJson = fetchSpotifyData("https://api.spotify.com/v1/me/top/tracks?limit=20", accessToken);
        userDetails.put("topTracks", parseTracks(topTracksJson));

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
    
    private JSONArray parseTracks(String jsonResponse) {
        JSONArray tracksArray = new JSONArray();
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray items = jsonObject.getJSONArray("items");

        for (int i = 0; i < items.length(); i++) {
            JSONObject track = items.getJSONObject(i);
            JSONObject trackDetails = new JSONObject();
            trackDetails.put("name", track.getString("name"));
            trackDetails.put("artist", track.getJSONArray("artists").getJSONObject(0).getString("name"));
            trackDetails.put("album", track.getJSONObject("album").getString("name"));
            trackDetails.put("image", track.getJSONObject("album").getJSONArray("images").length() > 0
                    ? track.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url")
                    : null);
            tracksArray.put(trackDetails);
        }

        return tracksArray;
    }

    private JSONArray parseArtistsWithLevels(String jsonResponse) {
        JSONArray artistsArray = new JSONArray();
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray items = jsonObject.getJSONArray("items");

        for (int i = 0; i < items.length(); i++) {
            JSONObject artist = items.getJSONObject(i);
            JSONObject artistDetails = new JSONObject();
            artistDetails.put("name", artist.getString("name"));
            artistDetails.put("image", artist.getJSONArray("images").length() > 0 ? artist.getJSONArray("images").getJSONObject(0).getString("url") : null);
            artistsArray.put(artistDetails);
        }

        return artistsArray;
    }
    
    



    private JSONArray parseAlbums(String jsonResponse) {
        JSONArray albumsArray = new JSONArray();
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray items = jsonObject.getJSONArray("items");

        for (int i = 0; i < items.length(); i++) {
            JSONObject track = items.getJSONObject(i);
            JSONObject album = track.getJSONObject("album");
            JSONObject albumDetails = new JSONObject();
            albumDetails.put("name", album.getString("name"));
            albumDetails.put("image", album.getJSONArray("images").length() > 0 ? album.getJSONArray("images").getJSONObject(0).getString("url") : null);
            albumsArray.put(albumDetails);
        }

        return albumsArray;
    }

    private JSONArray parseGenres(String jsonResponse) {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray items = jsonObject.getJSONArray("items");
        JSONArray genres = new JSONArray();

        for (int i = 0; i < items.length(); i++) {
            JSONArray artistGenres = items.getJSONObject(i).getJSONArray("genres");
            for (int j = 0; j < artistGenres.length(); j++) {
                if (!genres.toList().contains(artistGenres.getString(j))) {
                    genres.put(artistGenres.getString(j));
                }
            }
        }

        return new JSONArray(genres.toList().subList(0, Math.min(5, genres.length())));
    }
    
    


    private JSONArray getSubArray(JSONArray array, int start, int end) {
        JSONArray subArray = new JSONArray();
        for (int i = start; i < end && i < array.length(); i++) {
            subArray.put(array.get(i));
        }
        return subArray;
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String prompt = request.getParameter("prompt");

        // Call Flask API to generate playlist
        String playlistJson = generatePlaylistFromAPI(prompt);

        if (playlistJson != null) {
            // Send playlist back to frontend or store it as needed
            request.setAttribute("playlistJson", playlistJson);
            request.getRequestDispatcher("displayPlaylist.jsp").forward(request, response);
        } else {
            response.getWriter().write("Failed to generate playlist!");
        }
    }

    private String generatePlaylistFromAPI(String prompt) throws IOException {
        String apiUrl = "http://localhost:8090/generatePlaylist?prompt=" + URLEncoder.encode(prompt, "UTF-8");
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

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
}
