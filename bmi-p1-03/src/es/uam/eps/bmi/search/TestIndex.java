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

import es.uam.eps.bmi.search.indexing.LuceneIndex;
import es.uam.eps.bmi.search.parsing.HTMLSimpleParser;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;

/**
 * Class used to test the index.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class TestIndex {

    /**
     * Name of the output file containing stats about the index.
     */
    private final static String STATS_FILE = "output/indexstats";

    /**
     * Main class for Lucene index.
     *
     * Given a set of documents, creates an index in the output directory. Then,
     * loads the index and creates a document named <code>STATS_FILE</code> that
     * contains a line for each term, reporting: <br>
     * "'Term string' 'Term frequency' 'Number of files containing such term'
     * 'TF' 'IDF'"
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
                    TestIndex.class.getSimpleName());
            return;
        }

        // Build the index
        LuceneIndex luceneIndex = new LuceneIndex();
        luceneIndex.build(args[0], args[1], new HTMLSimpleParser());
        // Load it
        luceneIndex.load(args[1]);

        if (luceneIndex.isLoaded()) {
            //Open statistics output file
            File f = new File(STATS_FILE);
            f.getParentFile().mkdirs();
            try (FileWriter fw = new FileWriter(f, false)) {
                // Get stats from index and write them to the file.
                int totalDocuments = luceneIndex.getReader().numDocs();
                List<String> terms = luceneIndex.getTerms();
                for (String term : terms) {
                    long frequency = 0;
                    long nDocs = 0;
                    TermDocs td = luceneIndex.getReader().termDocs(new Term("contents", term));
                    while (td.next()) {
                        frequency += td.freq();
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
        } else {
            System.err.printf("ERROR: Could not load index.");
        }
    }
}
