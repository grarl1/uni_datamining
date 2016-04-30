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
package es.uam.eps.bmi.recommendation.data;

import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class representing the data in a rating table.
 *
 * @author Guillermo Ruiz Álvarez
 * @author Enrique Cabrerizo Fernández
 */
public class MovieRatingData extends RatingData {

    /* Data */
    private Integer[] userID;
    private Integer[] movieID;
    private final HashMap<Integer, Integer> userMap = new HashMap<>();
    private final HashMap<Integer, Integer> movieMap = new HashMap<>();
    private SparseDoubleMatrix2D ratingData;

    /**
     * Load the data from file.
     *
     * @param dataPath Path to the file to be read.
     * @throws java.io.IOException
     */
    @Override
    public void load(String dataPath) throws IOException {
        // Table for reading
        Table<Integer, Integer, Double> table = HashBasedTable.create();

        // Read the file.
        BufferedReader reader = new BufferedReader(new FileReader(new File(dataPath)));

        // Read the first line
        String line = reader.readLine();
        // Get headers
        if (line != null) {
            line = reader.readLine();
        }

        // Array indexes
        ArrayList<Integer> userArray = new ArrayList<>();
        ArrayList<Integer> movieArray = new ArrayList<>();
        int userIndex = 0, movieIndex = 0;

        // Read file
        while (line != null) {
            // Split data
            String[] split = line.split("\t");
            // Get data
            int user = Integer.valueOf(split[0]);
            int movie = Integer.valueOf(split[1]);
            double rating = Double.valueOf(split[2]);

            // Store rows and columns IDs.
            if (!userMap.containsKey(user)) {
                userArray.add(user);
                userMap.put(user, userIndex++);
            }
            if (!movieMap.containsKey(movie)) {
                movieArray.add(movie);
                movieMap.put(movie, movieIndex++);
            }

            // Add data
            table.put(user, movie, rating);
            // Read next line
            line = reader.readLine();
        }

        // Create sparse matrix
        this.userID = userArray.toArray(new Integer[userIndex]);
        this.movieID = movieArray.toArray(new Integer[movieIndex]);
        this.ratingData = new SparseDoubleMatrix2D(userIndex, movieIndex);

        // Fill sparse matrix
        table.rowMap().forEach((Integer row, Map<Integer, Double> rowVector) -> {
            rowVector.forEach((Integer column, Double cellData) -> {
                ratingData.set(userMap.get(row), movieMap.get(column), cellData);
            });
        });

        table.clear();
    }

    /**
     * Returns the array of identifiers of rows.
     *
     * @return the array of identifiers of rows.
     */
    @Override
    public Integer[] getRowIDs() {
        return userID;
    }

    /**
     * Returns the array of identifiers of columns
     *
     * @return the array of identifiers of columns
     */
    @Override
    public Integer[] getColumnIDs() {
        return movieID;
    }

    /**
     * Returns the map of indexes of rows.
     *
     * @return the map of indexes of rows.
     */
    @Override
    public HashMap<Integer, Integer> getRowIndexesMap() {
        return userMap;
    }

    /**
     * Returns the map of indexes of columns.
     *
     * @return the map of indexes of column.
     */
    @Override
    public HashMap<Integer, Integer> getColumnIndexesMap() {
        return movieMap;
    }

    /**
     * Returns the matrix of data.
     *
     * @return the matrix of data.
     */
    @Override
    public SparseDoubleMatrix2D getData() {
        return ratingData;
    }
}
