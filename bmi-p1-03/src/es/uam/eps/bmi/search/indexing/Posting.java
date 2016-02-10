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
package es.uam.eps.bmi.search.indexing;

import java.util.List;

/**
 * Posting basic structure used by an index.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class Posting {

    /*Associated term*/
    private String term;

    /*Associated document id*/
    private String docID;

    /*Position of the term within the document*/
    private List<Long> termPositions;

    /**
     * Returns the associated term.
     *
     * @return the associated term.
     */
    public String getTerm() {
        return term;
    }

    /**
     * Returns the associated document id.
     *
     * @return the associated document id.
     */
    public String getDocID() {
        return docID;
    }

    /**
     * Returns a list containing the term position within the associated
     * document.
     *
     * @return a list containing the term position within the associated
     * document.
     */
    public List<Long> getTermPositions() {
        return termPositions;
    }
}
