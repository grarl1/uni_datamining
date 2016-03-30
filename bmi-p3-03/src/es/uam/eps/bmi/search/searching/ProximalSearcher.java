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
package es.uam.eps.bmi.search.searching;

import es.uam.eps.bmi.search.ScoredTextDocument;
import es.uam.eps.bmi.search.indexing.Index;
import java.util.List;

/**
 * Class implementing a proximal searcher.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class ProximalSearcher implements Searcher {

    /**
     * Creates a searcher using the given index.
     *
     * @param index Index used to create the searcher, must be loaded.
     */
    @Override
    public void build(Index index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns a ranking of documents sorted by the score value.
     *
     * @param query String query used to search.
     * @return a ranking of documents sorted by the decrementing score value.
     */
    @Override
    public List<ScoredTextDocument> search(String query) {
        
    }

    /**
     * Sets the maximum number of results to retrieve.
     *
     * @param topResultsNumber Maximum number of results to retrieve.
     */
    @Override
    public void setTopResultsNumber(int topResultsNumber) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
