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
package es.uam.eps.bmi.search;

/**
 * Class used to store a text document score resulting from a query.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class ScoredTextDocument implements Comparable {

    /* Attributes */
    private final String docID;
    private final double score;

    /**
     * Default constructor for <code>ScoredTextDocument</code> class.
     *
     * @param docID Unique ID for the text document.
     * @param score Score of the text document.
     */
    public ScoredTextDocument(String docID, double score) {
        this.docID = docID;
        this.score = score;
    }

    /**
     * Returns the document id.
     *
     * @return the document id.
     */
    public String getDocID() {
        return docID;
    }

    /**
     * Returns the score of the document.
     *
     * @return the score of the document.
     */
    public double getScore() {
        return score;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object. If the argument is
     * not a <code>ScoredTextDocument</code> then returns -1. When -1 is
     * received, the caller must discern whether the argument is a
     * <code>ScoredTextDocument</code> and the distance with this object is 1,
     * or the argument is not at <code>ScoredTextDocument</code> instance.
     *
     *
     * @param t The object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is
     * less than, equal to, or greater than the specified object. If the
     * argument is not a <code>ScoredTextDocument</code> then returns -1
     */
    @Override
    public int compareTo(Object t) {
        // Null argument.
        if (t == null) {
            return -1;
        }

        // Not an ScoredTextDocument
        if (t.getClass() != this.getClass()) {
            return -1;
        }

        // Return the values (if the difference is, for instance, 0.1, the
        // function returns Math.ceil(0.1) = 1, because the documents are not 
        // equal.
        ScoredTextDocument scoredTextDocument = (ScoredTextDocument) t;
        return Double.compare(this.score, scoredTextDocument.score);
    }
}
