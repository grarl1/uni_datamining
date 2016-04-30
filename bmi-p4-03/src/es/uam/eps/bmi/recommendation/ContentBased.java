/*
 * Copyright (C) 2016 Enrique Cabrerizo Fernández, Guillermo Ruiz Álvarez
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.uam.eps.bmi.recommendation;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import es.uam.eps.bmi.recommendation.data.ContentData;
import es.uam.eps.bmi.recommendation.data.MovieContentData;
import es.uam.eps.bmi.recommendation.data.MovieRatingData;
import es.uam.eps.bmi.recommendation.data.RatingData;
import es.uam.eps.bmi.recommendation.function.RocchioSimilarity;
import es.uam.eps.bmi.recommendation.model.RankedMovie;
import es.uam.eps.bmi.util.MinHeap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Class representing a content-based recommender system for movies.
 *
 * @author Guillermo Ruiz Álvarez
 * @author Enrique Cabrerizo Fernández
 */
public class ContentBased {

    // Attributes.
    private final RatingData ratingData;
    private final ContentData contentData;
    private final RocchioSimilarity rocchioSim = new RocchioSimilarity();

    // Profile
    private ArrayList<Integer> userCoords;
    private ArrayList<Double> userRatingValues;

    /**
     * Default constructor.
     *
     * @param ratingData MovieRatingData object with the data loaded.
     * @param contentData MovieContentData object with the data loaded.
     */
    public ContentBased(RatingData ratingData, ContentData contentData) {
        this.ratingData = ratingData;
        this.contentData = contentData;
    }

    /**
     * Generates the user profile for a given user ID.
     *
     * @param userID The ID of the user whose profile is going to be generated.
     */
    public void generateProfile(int userID) {
        // Get the user index
        Integer userIndex = this.ratingData.getRowIndexesMap().get(userID);
        if (userIndex == null) {
            return;
        }
        // Get rated movies
        IntArrayList coord = new IntArrayList();
        DoubleArrayList values = new DoubleArrayList();
        this.ratingData.getData().viewRow(userIndex).getNonZeros(coord, values);

        // Store the profile
        this.userCoords = coord.toList();
        this.userRatingValues = values.toList();
    }

    /**
     * Generates recommendations for a given user.
     */
    public void generateRecommendations() {
        this.rocchioSim.build(userCoords, userRatingValues, ratingData, contentData);
    }

    /**
     * Returns the user profile for a given user ID.
     *
     * @param userID The user ID whose profile is going to be returned.
     * @return the user profile or null if the user is not in the database.
     */
    private String getProfile(int userID) {
        // Check the profile is created
        if (this.userCoords == null || this.userRatingValues == null) {
            return null;
        }
        // Create profile string
        String profile = "User #" + userID + " Profile:\n\tMovieID\tRating\n";
        // Complete profile
        for (int i = 0; i < this.userCoords.size(); ++i) {
            profile += "\t" + this.ratingData.getColumnIDs()[this.userCoords.get(i)] + "\t" + this.userRatingValues.get(i) + "\n";
        }
        return profile;
    }

    /**
     * Returns the generated recommendations for a user.
     *
     * @param userID The ID of the user.
     * @return the generated recommendations.
     */
    private List<RankedMovie> getReccomendation(int top) {

        // Create min heap for ordering.
        MinHeap<RankedMovie> minHeap = new MinHeap<>(top);
        HashMap<Integer, Double> rankingData = this.rocchioSim.getRankingData();

        // Fill the min heap
        rankingData.forEach((Integer movieID, Double movieRank) -> {
            minHeap.add(new RankedMovie(movieID, movieRank));
        });

        // Return the list
        List<RankedMovie> retList = minHeap.asList();
        Collections.sort(retList, Collections.reverseOrder());
        return retList;
    }

    /**
     * Main function.
     *
     * @param args user_rating_path movie_tags_path userID
     */
    public static void main(String[] args) {

        // Top ranking
        final int TOP_RANKING = 10;

        // Input control
        if (args.length != 3) {
            System.out.printf("Usage: %s user_rating_path movie_tags_path userID\n", ContentBased.class.getSimpleName());
            System.out.println("");
            return;
        }

        // Load the data.
        MovieRatingData ratingData = new MovieRatingData();
        MovieContentData contentData = new MovieContentData();
        int userID;
        try {
            ratingData.load(args[0]);
            contentData.load(args[1], ratingData.getColumnIndexesMap().keySet());
            userID = Integer.valueOf(args[2]);
        } catch (IOException ex) {
            System.err.println("Error while loading the files.");
            System.err.println(ex.getMessage());
            return;
        } catch (NumberFormatException ex) {
            System.err.println(args[2] + ": Not an user ID, it must be a number.");
            return;
        }

        ContentBased contentBased = new ContentBased(ratingData, contentData);

        // Generate the user profile
        contentBased.generateProfile(userID);

        // Get and show the user profile
        String userProfile = contentBased.getProfile(userID);
        if (userProfile == null) {
            System.out.println("The given user is not in the database");
            return;
        }
        System.out.println(userProfile);

        // Generate recommendations
        System.out.println("Generating top " + TOP_RANKING + " recommendations.");
        contentBased.generateRecommendations();

        // Get and show the recommendations.
        System.out.println("Movie ID\tRanking");
        contentBased.getReccomendation(TOP_RANKING).stream().forEach((m) -> {
            System.out.println(m.getMovieID() + "\t" + m.getMovieRank());
        });
    }
}
