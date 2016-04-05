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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * First test for PageRank
 *
 * @author Guillermo Ruiz Álvarez
 * @author Enrique Cabrerizo Fernández
 */
public class PageRankTest1 {
    
    /**
     * Main method.
     * @param args 
     */
    public static void main (String args[]) throws IOException, ClassNotFoundException {
        String outFolder = "./out/";
        String dataFolder = "./data/";
        String linkFile = "test1";
        String outFile = "test1rank";
        PageRank pr = new PageRank(dataFolder + linkFile, outFolder + outFile);

        System.out.println("Top Documents:");
        // 0.0 for tol ensures the execution of max iterations or until there are no
        // changes in scores
        if (pr.rank(0.15, 50, 0.0, true)) { 
            List<RankedDocument> lr = pr.getTopNIds(10);
            Collections.sort(lr);
            Collections.reverse(lr);
            for (RankedDocument rd : lr) {
                System.out.println(rd.getDocName() + " score: " + Double.toString(rd.getScore()));
            }
        }
        return;
    }
}
