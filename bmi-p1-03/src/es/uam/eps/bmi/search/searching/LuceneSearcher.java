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
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class LuceneSearcher implements Searcher {

    private static final int TOP = 5;

    /**
     * Main class for Lucene searcher.
     *
     * @param args The following arguments are used: index_path: Path to a
     * directory containing a Lucene index.
     */
    public static void main(String[] args) {
        // Input control
        if (args.length != 2) {
            System.err.printf("Usage: %s tindex_path\n"
                    + "\tindex_path: Path to a directory containing a Lucene index.\n",
                    LuceneSearcher.class.getSimpleName());
            return;
        }
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void build(Index index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<ScoredTextDocument> search(String query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
