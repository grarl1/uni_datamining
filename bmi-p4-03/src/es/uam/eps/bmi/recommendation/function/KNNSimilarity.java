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
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import es.uam.eps.bmi.recommendation.data.RatingData;
import es.uam.eps.bmi.recommendation.model.Neighbor;
import es.uam.eps.bmi.util.MinHeap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class representing a KNN similarity function with K neighbors.
 *
 * @author Guillermo Ruiz Álvarez
 * @author Enrique Cabrerizo Fernández
 */
public class KNNSimilarity {

    /* Data */
    private final int neighbors;
    private final HashMap<Integer, Double> ratingTable = new HashMap<>();

    // Similarity tables
    private Table<Integer, Integer, Double> similarityTable;
    private HashMap<Integer, Double> userSimilarityTable;

    /**
     * Default constructor.
     *
     * @param neighbors Number of neighbors.
     */
    public KNNSimilarity(int neighbors) {
        this.neighbors = neighbors;
    }

    /**
     * Compute the similarity values for all rows.
     *
     * @param ratingData Object containing the rating data where user's are rows
     * and items are columns.
     */
    public void buildSimilarityTable(RatingData ratingData) {

        // Initialize the table
        similarityTable = HashBasedTable.create();

        // Get the data
        SparseDoubleMatrix2D data = ratingData.getData();

        // Compute the dot products
        DoubleMatrix2D symmetric = new DenseDoubleMatrix2D(data.rows(), data.rows());
        ratingData.getData().zMult(ratingData.getData(), symmetric, 1, 0, false, true);

        // Compute the similarities
        for (int i = 0; i < symmetric.rows(); ++i) {
            for (int j = i + 1; j < symmetric.rows(); ++j) {
                double dotProduct = symmetric.get(i, j);
                if (dotProduct == 0) {
                    similarityTable.put(i, j, 0.0);
                } else {
                    double norms = Math.sqrt(symmetric.get(i, i) * symmetric.get(j, j));
                    similarityTable.put(i, j, dotProduct / norms);
                }
            }
        }
    }

    /**
     * Compute the similarity values for a given row.
     *
     * @param ratingData Object containing the rating data where user's are rows
     * and items are columns.
     * @param userRow The index of the user row.
     */
    public void buildSimilarityTable(RatingData ratingData, int userRow) {

        // Initialize the table
        userSimilarityTable = new HashMap<>();

        // Get the data
        SparseDoubleMatrix2D data = ratingData.getData();
        // Get the user vector
        DoubleMatrix1D userVector = data.viewRow(userRow);

        // Compute the similarities
        for (int i = 0; i < data.rows(); ++i) {
            DoubleMatrix1D dataRow = data.viewRow(i);
            double dotProduct = dataRow.zDotProduct(userVector);
            if (dotProduct == 0) {
                userSimilarityTable.put(i, 0.0);
            } else {
                double norms = Math.sqrt(userVector.zDotProduct(userVector) * dataRow.zDotProduct(dataRow));
                userSimilarityTable.put(i, dotProduct / norms);
            }
        }

    }

    /**
     * Builds the rating table of an user.
     *
     * @param mode 0 to use the user similarity table, 1 to use the complete
     * similarity table.
     * @param userRow The index of the user row.
     * @param userCoords Coordinates of the rating data user's row that have
     * been rated.
     * @param ratingData Object containing the rating data where user's are rows
     * and items are columns.
     */
    public void buildRatingTable(int mode, int userRow, ArrayList<Integer> userCoords, RatingData ratingData) {

        // Build movies coords
        HashSet<Integer> itemCoords = new HashSet<>(userCoords);

        MinHeap<Neighbor> minHeap = new MinHeap<>(neighbors);
        switch (mode) {
            case 0:
                // Get the neighbors
                this.userSimilarityTable.forEach((Integer neighborRow, Double neighborSim) -> {
                    if (userRow != neighborRow) {
                        minHeap.add(new Neighbor(neighborRow, neighborSim));
                    }
                });
                break;
            default:
                // Get the neighbors
                similarityTable.rowKeySet().forEach((Integer neighborRow) -> {
                    if (userRow < neighborRow) {
                        double neighborSim = similarityTable.get(userRow, neighborRow);
                        minHeap.add(new Neighbor(neighborRow, neighborSim));
                    } else if (userRow > neighborRow) {
                        double neighborSim = similarityTable.get(neighborRow, userRow);
                        minHeap.add(new Neighbor(neighborRow, neighborSim));
                    }
                });
        }

        // Fill the data
        ratingData.getColumnIndexesMap().values().stream().filter((itemCoord) -> (!itemCoords.contains(itemCoord))).forEach((itemCoord) -> {
            double product = 0;
            double norm = 0;
            for (Neighbor n : minHeap.asList()) {
                double itemRating = ratingData.getData().get(n.getNeighborID(), itemCoord);
                product += itemRating * n.getNeighborSim();
                norm += n.getNeighborSim();
            }
            this.ratingTable.put(ratingData.getColumnIDs()[itemCoord], product / norm);
        });
    }

    /**
     * Return the map with the rating values.
     *
     * @return the map with the rating values.
     */
    public HashMap<Integer, Double> getRatingData() {
        return ratingTable;
    }
}
