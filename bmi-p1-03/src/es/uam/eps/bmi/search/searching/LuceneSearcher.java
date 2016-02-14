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
import es.uam.eps.bmi.search.TextDocument;
import es.uam.eps.bmi.search.indexing.Index;
import es.uam.eps.bmi.search.indexing.LuceneIndex;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.Version;

/**
 * This class represents a Lucene searcher built using the Lucene library.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class LuceneSearcher implements Searcher {

    /* Constant */
    private static final int TOP = 5;
    private static final int N_SEARCH = 10;

    /* Attributes */
    private IndexSearcher indexSearcher;

    /**
     * Creates a searcher using the given index. This searcher only supports
     * <code>LuceneIndex</code> instances as index.
     *
     * @param index Index used to create the searcher.
     */
    @Override
    public void build(Index index) {
        // Only implemented for LuceneIndex objects.
        if (index instanceof LuceneIndex) {
            System.out.println("Building searcher...");
            LuceneIndex luceneIndex = (LuceneIndex) index;
            indexSearcher = new IndexSearcher(luceneIndex.getReader());
            System.out.println("Done.");
        } else {
            throw new UnsupportedOperationException("This searcher only suppport LuceneIndex instances.");
        }
    }

    /**
     * Returns a ranking of documents sorted by the score value or null if the
     * search couldn't be done.
     *
     * @param query Query used to search.
     * @return a ranking of documents sorted by the score value or null if the
     * search couldn't be done.
     */
    @Override
    public List<ScoredTextDocument> search(String query) {
        try {
            // Create analyzer
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
            // Create query parser
            QueryParser parser = new QueryParser(Version.LUCENE_36, "contents", analyzer);
            Query q = parser.parse(query);

            // Get results.
            ScoreDoc[] resultArray = indexSearcher.search(q, N_SEARCH).scoreDocs;

            // Build result list.
            TreeSet sortedSet = new TreeSet();
            for (ScoreDoc scoreDoc : resultArray) {
                sortedSet.add(new ScoredTextDocument(indexSearcher.doc(scoreDoc.doc).getFieldable("docID").stringValue(), scoreDoc.score));
            }

            // Sort the list and return it.
            ArrayList<ScoredTextDocument> resultList = new ArrayList(sortedSet);
            Collections.reverse(resultList);
            return resultList;
        } catch (ParseException ex) {
            System.err.println("Exception caught while parsing the query: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
            return null;
        } catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
            return null;
        }
    }

    /**
     * Main class for Lucene searcher.
     *
     * @param args The following arguments are used: index_path: Path to a
     * directory containing a Lucene index.
     */
    public static void main(String[] args) {
        // Input control
        if (args.length != 1) {
            System.err.printf("Usage: %s index_path\n"
                    + "\tindex_path: Path to a directory containing a Lucene index.\n",
                    LuceneSearcher.class.getSimpleName());
            return;
        }

        // Create a LuceneIndex instance.
        LuceneIndex luceneIndex = new LuceneIndex();
        luceneIndex.load(args[0]);

        // Check if the index has been correctly loaded.
        if (luceneIndex.isLoaded()) {
            LuceneSearcher luceneSearcher = new LuceneSearcher();
            luceneSearcher.build(luceneIndex);

            // Ask for queries.
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter a query (press enter to finish): ");
            String query = scanner.nextLine();
            while (!query.equals("")) {
                // Show results.
                List<ScoredTextDocument> resultList = luceneSearcher.search(query);
                // If there were no errors, show the results.
                if (resultList != null) {
                    if (resultList.isEmpty()) {
                        System.out.println("No results.");
                    } else {
                        System.out.println("Showing top " + TOP + " documents:");
                        resultList.subList(0, TOP).forEach((ScoredTextDocument t) -> {
                            TextDocument document = luceneIndex.getDocument(t.getDocID());
                            if (document != null) {
                                System.out.println(document.getName());
                            }
                        });
                    }
                    System.out.print("Enter a query (press enter to finish): ");
                    query = scanner.nextLine();
                } else{
                    return;
                }
            }
        }
    }
}
