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
import es.uam.eps.bmi.util.Pair;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Class representing the data in a table of contents.
 *
 * @author Guillermo Ruiz Álvarez
 * @author Enrique Cabrerizo Fernández
 */
public class MovieContentData extends ContentData{

    /* Data */
    private Integer[] movieID;
    private Integer[] tagID;
    private final HashMap<Integer, Integer> movieMap = new HashMap<>();
    private final HashMap<Integer, Integer> tagMap = new HashMap<>();
    private SparseDoubleMatrix2D tfidfData;

    /**
     * Load the data from file.
     *
     * @param dataPath Path to the file containing the data.
     * @param ratedMoviesSet Set of rated movies.
     * @throws java.io.IOException
     */
    @Override
    public void load(String dataPath, Set<Integer> ratedMoviesSet) throws IOException {
        // Maps for compute the tf idf
        HashMap<Integer, ArrayList<Pair<Integer, Integer>>> movieTagMap = new HashMap<>();
        HashMap<Integer, Integer> tagFrecMap = new HashMap<>();

        // Read the file.
        BufferedReader reader = new BufferedReader(new FileReader(new File(dataPath)));

        // Read the first line
        String line = reader.readLine();
        // Get headers
        if (line != null) {
            line = reader.readLine();
        }

        // Array indexes
        ArrayList<Integer> movieArray = new ArrayList<>();
        ArrayList<Integer> tagArray = new ArrayList<>();
        int movieIndex = 0, tagIndex = 0;

        // Read file
        while (line != null) {
            String[] split = line.split("\t");

            // Store data
            int movie = Integer.valueOf(split[0]);
            int tag = Integer.valueOf(split[1]);
            Integer tagWeight = Integer.valueOf(split[2]);

            // Store rows and columns IDs.
            if (!movieMap.containsKey(movie)) {
                movieArray.add(movie);
                movieMap.put(movie, movieIndex++);
            }
            if (!tagMap.containsKey(tag)) {
                tagArray.add(tag);
                tagMap.put(tag, tagIndex++);
            }

            // Add tags
            ArrayList<Pair<Integer, Integer>> pairs = movieTagMap.get(movie);
            if (pairs == null) {
                pairs = new ArrayList<>();
                movieTagMap.put(movie, pairs);
            }
            pairs.add(new Pair(tag, tagWeight));

            // Add frequency
            Integer frec = tagFrecMap.get(tag);
            if (frec == null) {
                tagFrecMap.put(tag, 1);
            } else {
                tagFrecMap.put(tag, frec + 1);
            }

            // Read next line
            line = reader.readLine();
        }

        // Add movies that are not in the file
        for (Integer movie : ratedMoviesSet) {
            if (!movieMap.containsKey(movie)) {
                movieArray.add(movie);
                movieMap.put(movie, movieIndex++);
            }
        }

        // Create sparse matrix
        this.movieID = movieArray.toArray(new Integer[movieIndex]);
        this.tagID = tagArray.toArray(new Integer[tagIndex]);
        this.tfidfData = new SparseDoubleMatrix2D(movieIndex, tagIndex);

        // Compute tf-idf and add to the table.
        double absD = (double) movieIndex;

        // Iterate the movies
        movieTagMap.forEach((Integer movie, ArrayList<Pair<Integer, Integer>> pairs) -> {
            // Iterate the tags
            pairs.forEach((Pair<Integer, Integer> pair) -> {
                Integer tag = pair.getX();
                Integer tagWeight = pair.getY();
                double absDt = tagFrecMap.get(tag);

                // tf-idf
                double tf = 1 + Math.log((double) tagWeight) / Math.log(2);
                double idf = Math.log(absD / absDt) / Math.log(2);
                this.tfidfData.set(movieMap.get(movie), tagMap.get(tag), tf * idf);
            });
            pairs.clear();
        });

        // Clear the data
        movieTagMap.clear();
        tagFrecMap.clear();
    }

    /**
     * Returns the array of identifiers of rows.
     *
     * @return the array of identifiers of rows.
     */
    @Override
    public Integer[] getRowIDs() {
        return movieID;
    }

    /**
     * Returns the array of identifiers of columns
     *
     * @return the array of identifiers of columns
     */
    @Override
    public Integer[] getColumnIDs() {
        return tagID;
    }

    /**
     * Returns the map of indexes of rows.
     *
     * @return the map of indexes of rows.
     */
    @Override
    public HashMap<Integer, Integer> getRowIndexesMap() {
        return movieMap;
    }

    /**
     * Returns the map of indexes of columns.
     *
     * @return the map of indexes of column.
     */
    @Override
    public HashMap<Integer, Integer> getColumnIndexesMap() {
        return tagMap;
    }

    /**
     * Returns the matrix of data.
     *
     * @return the matrix of data.
     */
    @Override
    public SparseDoubleMatrix2D getData() {
        return tfidfData;
    }
}
