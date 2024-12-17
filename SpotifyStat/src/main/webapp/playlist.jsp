<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.json.JSONArray" %>
<!DOCTYPE html>
<html>
<head>
    <title>Generated Playlist</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #191414;
            color: #fefefe;
        }
        h1 {
            color: #1db954;
            margin-bottom: 30px;
        }
        .player-container {
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Your Playlist</h1>
        <%
            org.json.JSONObject playlist = (org.json.JSONObject) request.getAttribute("playlist");
            if (playlist != null) {
                org.json.JSONArray tracks = playlist.getJSONArray("tracks");
        %>
            <% for (int i = 0; i < tracks.length(); i++) {
                org.json.JSONObject track = tracks.getJSONObject(i);
            %>
                <% if (!track.isNull("uri")) { %>
                    <div class="player-container">
                        <iframe 
                            src="https://open.spotify.com/embed/track/<%= track.getString("uri").split(":")[2] %>" 
                            width="100%" height="80" frameborder="0" 
                            allowtransparency="true" allow="encrypted-media">
                        </iframe>
                    </div>
                <% } %>
            <% } %>
        <% } else { %>
            <p>No playlist generated.</p>
        <% } %>
    </div>
</body>
</html>
