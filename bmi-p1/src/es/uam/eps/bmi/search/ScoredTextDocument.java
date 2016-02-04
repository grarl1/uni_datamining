/*
 * Copyright (C) 2016 rual
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
package es.uam.eps.bmi.search;

/**
 * Class used to store a text document score resulting from a query.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class ScoredTextDocument implements Comparable {

    private String docID;
    private double score;

    public String getDocID() {
        return docID;
    }

    public double getScore() {
        return score;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param t The object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is
     * less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(Object t) {
        return 0;
    }

}
