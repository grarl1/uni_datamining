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
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import com.google.common.collect.HashBasedTable;
import es.uam.eps.bmi.recommendation.data.MovieRatingData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.swing.SwingUtilities;

/**
 * Class to test accuraccy of userbased kNN recommendation method
 *
 * @author Guillermo Ruiz Álvarez
 * @author Enrique Cabrerizo Fernández
 */
public class Metrics {

    //Erased ratings from ratings table
    private static HashBasedTable<Integer, Integer, Double> erasedRatings;
    private static HashBasedTable<Integer, Integer, Double> newRatings;
    private static List<Double> ratingDiffs;
    
    /**
     * Main function.
     *
     * @param args user_rating_path movie_tags_path userID
     */
    public static void main(String[] args) {
        
        int neighbors = 25;
        // Input control
        if (args.length != 2) {
            System.out.printf("Usage: %s user_rating_path trainingPercentage\n", Metrics.class.getSimpleName());
            System.out.println("");
            return;
        }
        // Load the data.
        MovieRatingData ratingData = new MovieRatingData();
        Double trainPercentage = 0.0;
        try {
            ratingData.load(args[0]);
            trainPercentage = Double.parseDouble(args[1]);
        } catch (IOException ex) {
            System.err.println("Error while loading the files.");
            System.err.println(ex.getMessage());
            return;
        }
        MovieRatingData ratingCopy = ratingData.clone();
        removeTestEntrances(ratingCopy, trainPercentage);
        CollaborativeUserBased cub = new CollaborativeUserBased(ratingCopy);
        cub.generateAllSimilarities(neighbors);
        generateAllRatings(cub);
        computeDifferences();
        System.out.println("MAE Score: " + maeScore());
        System.out.println("RMSE Score: " + rmseScore());
    }

    /**
     * Generates all ratings removed to test the recommendation system
     * @param cub 
     */
    private static void generateAllRatings(CollaborativeUserBased cub) {
        newRatings = HashBasedTable.create();
        Set<Integer> userColumns = erasedRatings.rowKeySet();
        for (Integer userIndex: userColumns) {
            cub.generateProfileFromUserIndex(userIndex);
            cub.generateRecommendations(1);
            HashMap<Integer,Double> movieRating = cub.getReccomendation();
            for (Integer movieID: movieRating.keySet()) {
                Integer movieCol = cub.getRatingData().getColumnIndexesMap().get(movieID);
                if (erasedRatings.get(userIndex, movieCol) != null){
                    newRatings.put(userIndex, movieCol, movieRating.get(movieID));
                }
            }
        }        
    }
    
    /**
     * Computes rating differences between predicted rating and rating given by user.
     */
    private static void computeDifferences() {
        ratingDiffs = new ArrayList<>();
        erasedRatings.cellSet().stream().forEach(entry -> {
            Double newRatingValue = newRatings.get(entry.getRowKey(), entry.getColumnKey());
            if (newRatingValue == null)
                newRatingValue = 0.0;
            ratingDiffs.add(entry.getValue()-newRatingValue);
        });
    }
    
    /**
     * Sets to 0 a specified percentage of the non zero values of the ratingData passed (randomly).
     * stores the position and previous values on class variables.
     * @param ratingData variable that stores the matrix of ratings
     * @param trainPercentage 
     */
    private static void removeTestEntrances(MovieRatingData ratingData, Double trainPercentage) {
        erasedRatings = HashBasedTable.create();
        SparseDoubleMatrix2D matrix = ratingData.getData();
        IntArrayList rowsFromMatrix = new IntArrayList();
        IntArrayList columnsFromMatrix = new IntArrayList();
        DoubleArrayList valuesFromMatrix = new DoubleArrayList();
        matrix.getNonZeros(rowsFromMatrix, columnsFromMatrix, valuesFromMatrix);        
        
        //get list of rows, columns and values of the matrix
        List<Integer> rows = rowsFromMatrix.toList();
        List<Integer> columns = columnsFromMatrix.toList();
        List<Double> values = valuesFromMatrix.toList();
        
        //shuffle every list in the same way
        long seed = System.currentTimeMillis();
        Collections.shuffle(rows, new Random(seed));
        Collections.shuffle(columns, new Random(seed));
        Collections.shuffle(values, new Random(seed));
        
        //trim lists to (1-trainPercentage) of actual size
        Integer maxSize = (int) Math.round( Math.floor((1-trainPercentage)*rows.size()) );
        rows = rows.subList(0, maxSize);
        columns = columns.subList(0, maxSize);
        values = values.subList(0, maxSize);
        //Remove these values from matrix
        Iterator<Integer> rowIt = rows.iterator();
        Iterator<Integer> colIt = columns.iterator();
        Iterator<Double> valIt = values.iterator(); //TODO REMOVE
        while (rowIt.hasNext() && colIt.hasNext() && valIt.hasNext()) {
            Integer r = rowIt.next();
            Integer c = colIt.next();
            Double v = valIt.next();
            //matrix.set(r,c,0);
            erasedRatings.put(r, c, v);
        }
    }
    
    /**
     * returns MAE score
     * @return MAE score
     */
    private static double maeScore(){
        Double maeScore = 0.0;
        for (Double r : ratingDiffs) {
            maeScore+=Math.abs(r);
        }
        maeScore/=ratingDiffs.size();
        return maeScore;
    }
    
    /**
     * returns RMSE score
     * @return RMSE score
     */
    private static double rmseScore(){
        Double rmseScore = 0.0;
        for (Double r : ratingDiffs) {
            rmseScore+=Math.pow(r, 2);
        }
        rmseScore/=ratingDiffs.size();
        return Math.sqrt(rmseScore);
    }
}
