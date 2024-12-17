package com.stat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@WebServlet("/generatePlaylist")
public class GeneratePlaylistServlet extends HttpServlet {

    private static final String FLASK_API_URL = "http://localhost:5000/generatePlaylist";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String prompt = request.getParameter("prompt");

        if (prompt == null || prompt.isEmpty()) {
            response.getWriter().write("No prompt provided");
            return;
        }

        // Call Flask API and get the playlist
        String playlistJson = callFlaskAPI(prompt);

        if (playlistJson != null) {
            // Pass the playlist data to the JSP
            JSONObject playlist = new JSONObject(playlistJson);
            request.setAttribute("playlist", playlist);
            request.getRequestDispatcher("playlist.jsp").forward(request, response);
        } else {
            response.getWriter().write("Error generating playlist");
        }
    }

    private String callFlaskAPI(String prompt) throws IOException {
        String encodedPrompt = URLEncoder.encode(prompt, "UTF-8");
        URL url = new URL(FLASK_API_URL + "?prompt=" + encodedPrompt);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() == 200) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } else {
            System.out.println("Error: HTTP " + conn.getResponseCode());
        }
        return null;
    }
}
