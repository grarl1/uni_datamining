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
package es.uam.eps.bmi.search.ranking.graph;

import es.uam.eps.bmi.search.ScoredTextDocument;

/**
 * Class used to store a document ranked with PageRank.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class RankedDocument extends ScoredTextDocument {
    
    /* Attributes */
    private final String docName;

    /**
     * Default constructor for <code>RankedDocument</code> class.
     *
     * @param docID numeric id of the document.
     * @param score PageRank score of the text document.
     * @param docName name of the document.
     */
    public RankedDocument(Integer docID, double score, String docName) {
        super(docID, score);
        this.docName = docName;
    }

    /**
     * Returns the document name.
     *
     * @return the document name.
     */
    public String getDocName() {
        return docName;
    }
    
}
