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
package es.uam.eps.bmi.recommendation.function;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import es.uam.eps.bmi.recommendation.data.ContentData;
import es.uam.eps.bmi.recommendation.data.RatingData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Class representing a Rocchio similarity function.
 *
 * @author Guillermo Ruiz Álvarez
 * @author Enrique Cabrerizo Fernández
 */
public class RocchioSimilarity {

    /* Data */
    private final HashMap<Integer, Double> rankingTable = new HashMap<>();

    /**
     * Compute the similarity function values.
     *
     * @param userCoords Coordinates of the rating data user's row that have
     * been rated.
     * @param userRatingValues The user rating values for each coordinate.
     * @param ratingData Object containing the rating data where user's are rows
     * and items are columns.
     * @param contentData Object containing the content data where item's are
     * rows and contents are columns.
     */
    public void build(ArrayList<Integer> userCoords, ArrayList<Double> userRatingValues, RatingData ratingData, ContentData contentData) {

        // Create user vector
        Double[] userValues = userRatingValues.toArray(new Double[userRatingValues.size()]);
        SparseDoubleMatrix1D userRated = new SparseDoubleMatrix1D(Stream.of(userValues).mapToDouble(Double::doubleValue).toArray());

        // Movies
        Set<Integer> movieIDs = new HashSet<>();

        // Create content selection
        int[] contentCoords = new int[userCoords.size()];
        for (int i = 0; i < userCoords.size(); ++i) {
            int ratingCoord = userCoords.get(i);
            Integer movieID = ratingData.getColumnIDs()[ratingCoord];
            contentCoords[i] = contentData.getRowIndexesMap().get(movieID);
            movieIDs.add(movieID);
        }
        // Make content selection
        DoubleMatrix2D contentSelection = contentData.getData().viewSelection(contentCoords, null);
        // Compute the user centroid
        DoubleMatrix1D userCentroid = new DenseDoubleMatrix1D(contentSelection.columns());
        contentSelection.zMult(userRated, userCentroid, 1, 0, true);
        // Normalization
        userCentroid.assign((double d) -> d / userCoords.size());

        // Compute the cosines  
        for (int i = 0; i < contentData.getData().rows(); ++i) {
            if (!movieIDs.contains(contentData.getRowIDs()[i])) {
                // Compute the cosine
                DoubleMatrix1D row = contentData.getData().viewRow(i);
                double dotProduct = userCentroid.zDotProduct(row);

                if (dotProduct == 0) {
                    rankingTable.put(contentData.getRowIDs()[i], 0.0);
                } else {
                    double norms = Math.sqrt(userCentroid.zDotProduct(userCentroid) * row.zDotProduct(row));
                    rankingTable.put(contentData.getRowIDs()[i], dotProduct / norms);
                }
            }
        }
    }

    /**
     * Returns the map of data.
     *
     * @return the map of data.
     */
    public HashMap<Integer, Double> getRankingData() {
        return rankingTable;
    }
}
