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
 * Class representing a ranked movie.
 *
 * @author Guillermo Ruiz Álvarez
 * @author Enrique Cabrerizo Fernández
 */
public class RankedMovie implements Comparable<RankedMovie> {

    // Attributes
    private final int movieID;
    private final double movieRank;

    /**
     * Default constructor.
     *
     * @param movieID Movie ID.
     * @param movieRank Ranking value.
     */
    public RankedMovie(int movieID, double movieRank) {
        this.movieID = movieID;
        this.movieRank = movieRank;
    }

    /**
     * Returns the movie ID.
     *
     * @return the movie ID.
     */
    public int getMovieID() {
        return movieID;
    }

    /**
     * Returns the movie ranking value.
     *
     * @return
     */
    public double getMovieRank() {
        return movieRank;
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
    public int compareTo(RankedMovie t) {
        return Double.compare(this.movieRank, t.movieRank);
    }

}
