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

import es.uam.eps.bmi.search.parsing.StemParser;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Stem index class. Filters stopwords preventing them from getting being
 * indexed and performs stemming on every term.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class StemIndex extends BasicIndex {

    /**
     * Main class for Stem index.
     *
     * It indexes a set of documents, creating an index in the output directory.
     * Parses the content of each file removing stopwords and stemming terms.
     *
     * @param args The following arguments are used: "docs_path": Path to the
     * directory containing the documents to be indexed. "index_path": Path to
     * the directory to store the index.
     */
    public static void main(String[] args) {

        // Input control
        if (args.length != 2) {
            System.err.printf("Usage: %s docs_path index_path\n"
                    + "\tdocs_path: Path to the directory containing the documents to be used.\n"
                    + "\tindex_path: Path to a directory to store the index.\n",
                    BasicIndex.class.getSimpleName());
            return;
        }

        // Build the index
        StemIndex stopwordIndex = new StemIndex();
        stopwordIndex.build(args[0], args[1], new StemParser());

        System.out.print("Getting index stats...");
        stopwordIndex.load(args[1]);
        int totalDocuments = stopwordIndex.getDocIds().size();

        File f = new File(args[1] + "/" + "indexstats");
        try (FileWriter fw = new FileWriter(f, false)) {
            // Get stats from index and write them to the file.
            List<String> terms = stopwordIndex.getTerms();
            for (String term : terms) {
                long frequency = 0;
                long nDocs = 0;
                List<Posting> lp = stopwordIndex.getTermPostings(term);
                for (Posting p : lp) {
                    frequency += p.getTermFrequency();
                    nDocs++;
                }
                double tf = 1 + (Math.log(frequency) / Math.log(2));
                double idf = Math.log(totalDocuments / nDocs);
                String outputString = String.format("%s %d %d %.2f %.2f\n", term, frequency, nDocs, tf, idf);
                fw.write(outputString);
            }
        } catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
        }
        System.out.println("Done");

    }

}
