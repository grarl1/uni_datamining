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
package es.uam.eps.bmi.recommendation.model;

/**
 * Class representing a neighbor.
 *
 * @author Guillermo Ruiz Álvarez
 * @author Enrique Cabrerizo Fernández
 */
public class Neighbor implements Comparable<Neighbor> {

    /* Attributes */
    private final int neighborID;
    private final double neighborSim;

    /**
     * Default constructor.
     *
     * @param neighborID ID of the neighbor.
     * @param neighborSim Similarity of the neighbor.
     */
    public Neighbor(int neighborID, double neighborSim) {
        this.neighborID = neighborID;
        this.neighborSim = neighborSim;
    }

    /**
     * Returns the neighbor ID.
     *
     * @return the neighbor ID.
     */
    public int getNeighborID() {
        return neighborID;
    }

    /**
     * Returns the neighbor similarity.
     *
     * @return the neighbor similarity.
     */
    public double getNeighborSim() {
        return neighborSim;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param t Object to be compared to.
     * @return Returns a negative integer, zero, or a positive integer as this
     * object is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(Neighbor t) {
        return Double.compare(this.neighborSim, t.neighborSim);
    }

}
