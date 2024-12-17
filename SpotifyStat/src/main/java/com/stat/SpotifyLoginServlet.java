package com.stat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/SpotifyLoginServlet")
public class SpotifyLoginServlet extends HttpServlet {
    private static final String CLIENT_ID = "#"; //Add your one
    private static final String REDIRECT_URI = "http://localhost:8090/SpotifyStat/callback";
    private static final String SPOTIFY_AUTH_URL = "https://accounts.spotify.com/authorize";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String scope = "user-top-read"; // Modify scope as per your need
        String state = "some-random-string"; // Use a proper state for CSRF protection

        String authURL = SPOTIFY_AUTH_URL + 
                "?client_id=" + CLIENT_ID +
                "&response_type=code" +
                "&redirect_uri=" + REDIRECT_URI +
                "&scope=" + scope +
                "&state=" + state;

        response.sendRedirect(authURL);
    }
}
