<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONException" %>
<!DOCTYPE html>
<html>
<head>
    <title>Spotify User Details</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #191414;
            color: #fefefe;
        }
        h1, h2 {
            color: #1db954;
        }
        .card {
            background-color: #222;
            border: none;
        }
        .card img {
            height: 150px;
            object-fit: cover;
        }
        .genre-badge {
            background: #1db954;
            color: #fff;
            padding: 10px 15px;
            border-radius: 20px;
            font-size: 14px;
            font-weight: bold;
            transition: background 0.3s ease;
        }
        .genre-badge:hover {
            background: #14893c;
        }
        footer {
            background: #111;
            color: #aaa;
            padding: 20px;
            text-align: center;
        }
        footer a {
            color: #1db954;
            text-decoration: none;
        }
        footer a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="container my-1">
        <h1 class="text-center mb-4">Spotify User Insights</h1>

        <%
            String userDetailsJson = (String) request.getAttribute("userDetailsJson");
            try {
                JSONObject userDetails = new JSONObject(userDetailsJson);

                // Existing sections (Top Artists, Top Albums, etc.)
                // High-Level Artists
                out.println("<div class='mb-5'>");
                out.println("<h3 class='text-center mb-4'>Top 5 High-Level Artists</h3>");
                out.println("<div class='row g-4' style='gap: 50px;'>");
                JSONArray highLevelArtists = userDetails.getJSONArray("highLevelArtists");
                for (int i = 0; i < highLevelArtists.length(); i++) {
                    JSONObject artist = highLevelArtists.getJSONObject(i);
                    out.println("<div class='col-md-1 col-lg-2'>");
                    
                    if (!artist.isNull("image")) {
                        out.println("<img src='" + artist.getString("image") + "' class='card-img-top mb-2' alt='Artist Image'>");
                    }
                    out.println("<div class='card-body text-center'>");
                    out.println("<h5 class='card-title' style='color: #FFFFFF;'>" + artist.getString("name") + "</h5>");
                    
                    out.println("</div>");
                    out.println("</div>");
                }
                out.println("</div>");
                out.println("</div>");

                // Middle-Level Artists
                out.println("<div class='mb-5'>");
                out.println("<h3 class='text-center mb-4'>Top 5 Middle-Level Artists</h3>");
                out.println("<div class='row g-4' style='gap: 50px;'>");
                JSONArray middleLevelArtists = userDetails.getJSONArray("middleLevelArtists");
                for (int i = 0; i < middleLevelArtists.length(); i++) {
                    JSONObject artist = middleLevelArtists.getJSONObject(i);
                    out.println("<div class='col-md-2 col-lg-2'>");
                    
                    if (!artist.isNull("image")) {
                        out.println("<img src='" + artist.getString("image") + "'class='card-img-top mb-2' alt='Artist Image'>");
                    }
                    out.println("<div class='card-body text-center'>");
                    out.println("<h5 class='card-title' style='color: #FFFFFF;'>" + artist.getString("name") + "</h5>");
                    
                    out.println("</div>");
                    out.println("</div>");
                }
                out.println("</div>");
                out.println("</div>");

                // Top Albums
                out.println("<div class='mb-5'>");
                out.println("<h3 class='text-center mb-4'>Top 5 Albums</h3>");
                out.println("<div class='row g-4' style='gap: 50px;'>");
                JSONArray topAlbums = userDetails.getJSONArray("topAlbums");
                for (int i = 0; i < topAlbums.length(); i++) {
                    JSONObject album = topAlbums.getJSONObject(i);
                    out.println("<div class='col-md-3 col-lg-2'>");
                    
                    if (!album.isNull("image")) {
                        out.println("<img src='" + album.getString("image") + "' class='card-img-top' alt='Album Image'>");
                    }
                    out.println("<div class='card-body text-center'>");
                    out.println("<h5 class='card-title' style='color: #FFFFFF;'>" + album.getString("name") + "</h5>");
                    
                    
                    out.println("</div>");
                    out.println("</div>");
                }
                out.println("</div>");
                out.println("</div>");
                
             // NEW SECTION: Top Tracks
                out.println("<div class='mb-5'>");
                out.println("<h3 class='text-center mb-4'>Top 20 Tracks</h3>");
                out.println("<div class='row g-4' style='gap: 24px;'>");
                JSONArray topTracks = userDetails.getJSONArray("topTracks");
                for (int i = 0; i < topTracks.length(); i++) {
                    JSONObject track = topTracks.getJSONObject(i);

                    // Add a new row every 10 items
                    if (i % 10 == 0 && i != 0) {
                        out.println("</div><div class='row g-4' style='gap: 24px;'>");
                    }

                    out.println("<div class='col-1 d-flex flex-column align-items-center mb-3'>");

                    if (!track.isNull("image")) {
                        out.println("<img src='" + track.getString("image") + "' class='card-img-top mb-2' alt='Track Image' style='width: 90px; height: 90px; object-fit: cover;'>");
                    }
                    out.println("<h5 class='card-title text-center' style='color: #FFFFFF; font-size: 12px;'>" + track.getString("name") + "</h5>");
                    out.println("<p class='card-text text-center' style='font-size: 10px; color: #aaa;'>Artist: " + track.getString("artist") + "</p>");
                    out.println("<p class='card-text text-center' style='font-size: 10px; color: #aaa;'>Album: " + track.getString("album") + "</p>");
                    out.println("</div>");
                }
                out.println("</div>");
                out.println("</div>");

                // Top Genres
                out.println("<div class='mb-5'>");
                out.println("<h3 class='text-center mb-4'>Top 5 Genres</h3>");
                out.println("<div class='d-flex flex-wrap justify-content-center gap-3'>");
                JSONArray topGenres = userDetails.getJSONArray("topGenres");
                for (int i = 0; i < topGenres.length(); i++) {
                    out.println("<span class='genre-badge'>" + topGenres.getString(i) + "</span>");
                }
                out.println("</div>");
                out.println("</div>");

                

            } catch (JSONException e) {
                out.println("<div class='alert alert-danger text-center'>Error parsing user details: " + e.getMessage() + "</div>");
            }
        %>
    </div>
    <div class="mb-5">
    <h3 class="text-center mb-4">Generate AI Playlist</h3>
	    <form method="post" action="generatePlaylist" class="d-flex flex-column align-items-center">
		    <div class="mb-3 w-50">
		        <input type="text" name="prompt" class="form-control" placeholder="Enter your mood or music preference (e.g., 'Play happy music')" required>
		    </div>
		    <button type="submit" class="btn btn-success">Generate Playlist</button>
		</form>


	</div>
    

    <footer>
        &copy; 2024 Spotify User Insights | Developed by Hiran Rathnayake
    </footer>

    <!-- Bootstrap JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
