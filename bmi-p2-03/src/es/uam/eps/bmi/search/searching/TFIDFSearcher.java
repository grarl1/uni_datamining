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
import es.uam.eps.bmi.search.indexing.Posting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Class implementing an information retrieval vector model with TF-IDF terms
 * weighing.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class TFIDFSearcher implements Searcher {

    // Thread pool to perform a multi thread search.
    private ExecutorService threadPoolExecutor;
    //Index used to search
    private Index index;

    /**
     * Creates a searcher using the given index.
     *
     * @param index Index used to create the searcher, must be loaded.
     */
    @Override
    public void build(Index index) {
        // Store the index object.
        this.index = index;

        // Create a thread pool.
        int cores = Runtime.getRuntime().availableProcessors();
        this.threadPoolExecutor = Executors.newFixedThreadPool(cores);
    }

    /**
     * Returns a ranking of documents sorted by the score value.
     *
     * @param query String query used to search.
     * @return a ranking of documents sorted by the decrementing score value.
     */
    @Override
    public List<ScoredTextDocument> search(String query) {

        List<Callable<List<ScoredTextDocument>>> callableList = new ArrayList<>();

        // Separate the query string by spaces
        String[] terms = query.split(" ");

        // Number of documents
        int docsCount = index.getDocIds().size();

        // Iterate the query terms
        for (String term : terms) {

            // Get the list of postings.
            List<Posting> termPosting = index.getTermPostings(term);

            // Create a task for execution (one per term in the query)
            Callable<List<ScoredTextDocument>> callable = () -> {
                // Map to store the documents.
                HashMap<Integer, ScoredTextDocument> docMap = new HashMap<>();

                // Iterate the term postings
                termPosting.stream().forEach((posting) -> {
                    // Get document attributes
                    int docID = posting.getDocID();
                    double docMod = index.getDocModule(docID);

                    // Get term attributes
                    int termDocsCount = termPosting.size();
                    int termFrequency = posting.getTermFrequency();

                    // Compute the tf-idf
                    double idf = Math.log((double) docsCount / (double) termDocsCount);
                    double tf = 1 + Math.log(termFrequency);
                    double tf_idf = tf * idf;

                    // Compute the partial ranking
                    double score = (1 / docMod) * tf_idf;

                    // Store the ranking
                    ScoredTextDocument std = docMap.get(docID);
                    // If it does not exists, add to the map.
                    if (std == null) {
                        std = new ScoredTextDocument(docID, score);
                        docMap.put(docID, std);
                    } else {
                        // Uptade the score.
                        std.sumScore(score);
                    }
                });

                List<ScoredTextDocument> returnList = new ArrayList<>(docMap.values());
                Collections.sort(returnList, Collections.reverseOrder());
                return returnList;
            };

            // Add the future object to the list.
            callableList.add(callable);
        }

        // Wait for the threads to finish all the tasks.
        try {
            // Execute all the tasks.
            List<Future<List<ScoredTextDocument>>> futureList = this.threadPoolExecutor.invokeAll(callableList);

            // Wait for the task to be finished and store the results.
            List<List<ScoredTextDocument>> results = new ArrayList<>();
            for (Future<List<ScoredTextDocument>> future : futureList) {
                results.add(future.get());
            }

            // Shut down the executor service.
            this.threadPoolExecutor.shutdown();

            // Merge the results and retrieve.
            return mergeResults(results);
        } catch (InterruptedException | ExecutionException ex) {
            System.err.println("The executor service has been interrupted:");
            System.err.println(ex.getMessage());
            return null;
        }
    }

    /**
     * Merges the result lists retrieved by all the threads.
     *
     * @param results List of list to be merged.
     * @return A list of documents sorted by the decrementing score value.
     */
    private List<ScoredTextDocument> mergeResults(List<List<ScoredTextDocument>> results) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
