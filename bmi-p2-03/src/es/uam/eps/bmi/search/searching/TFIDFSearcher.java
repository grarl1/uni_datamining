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
import es.uam.eps.bmi.search.indexing.Posting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
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
    
    //Index used to search
    private Index index;
    // Cores
    private int cores;

    /**
     * Creates a searcher using the given index.
     *
     * @param index Index used to create the searcher, must be loaded.
     */
    @Override
    public void build(Index index) {
        // Store the index object.
        this.index = index;

        // Get the number of cores
        this.cores = Runtime.getRuntime().availableProcessors();
    }

    /**
     * Returns a ranking of documents sorted by the score value.
     *
     * @param query String query used to search.
     * @return a ranking of documents sorted by the decrementing score value.
     */
    @Override
    public List<ScoredTextDocument> search(String query) {

        // Thread pool to perform a multi thread search.
        ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(cores);
        List<Callable<Map<Integer, ScoredTextDocument>>> callableList = new ArrayList<>();

        // Separate the query string by spaces
        String[] terms = query.split(" ");

        // Number of documents
        int docsCount = index.getDocIds().size();

        // Iterate the query terms
        for (String term : terms) {

            // Get the list of postings.
            List<Posting> termPosting = index.getTermPostings(term);

            // Create a task for execution (one per term in the query)
            Callable<Map<Integer, ScoredTextDocument>> callable = () -> {
                // Map to store the documents.
                HashMap<Integer, ScoredTextDocument> docMap = new HashMap<>();
                
                // Get the 
                int termDocsCount = termPosting.size();

                // Iterate the term postings
                termPosting.stream().forEach((posting) -> {
                    // Get document attributes
                    int docID = posting.getDocID();
                    double docMod = index.getDocModule(docID);

                    // Get term attributes
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

                return docMap;
            };

            // Add the future object to the list.
            callableList.add(callable);
        }

        // Wait for the threads to finish all the tasks.
        try {
            // Execute all the tasks.
            List<Future<Map<Integer, ScoredTextDocument>>> futureList = threadPoolExecutor.invokeAll(callableList);

            // Wait for the task to be finished and store the results.
            List<Map<Integer, ScoredTextDocument>> results = new ArrayList<>();
            for (Future<Map<Integer, ScoredTextDocument>> future : futureList) {
                results.add(future.get());
            }

            // Shut down the executor service.
            threadPoolExecutor.shutdown();

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
    private List<ScoredTextDocument> mergeResults(List<Map<Integer, ScoredTextDocument>> results) {

        // Result map
        HashMap<Integer, ScoredTextDocument> resultMap = new HashMap<>();

        // Merge all the maps
        results.stream().forEach((map) -> {
            map.forEach((Integer docID, ScoredTextDocument scoredDoc) -> {
                ScoredTextDocument retrieved = resultMap.get(docID);
                if (retrieved == null) {
                    resultMap.put(docID, scoredDoc);
                } else {
                    retrieved.sumScore(scoredDoc.getScore());
                }
            });
        });

        // Sort the result list.
        ArrayList<ScoredTextDocument> resultList = new ArrayList<>(resultMap.values());
        Collections.sort(resultList, Collections.reverseOrder());
        return resultList;
    }
    
    
    /**
     * Main class for TF-IDF searcher.
     *
     * Builds a searcher and asks the user for queries, showing the TOP 5
     * results.
     *
     * @param args The following arguments are used: index_path: Path to a
     * directory containing an index.
     */
    public static void main(String[] args) {
        int TOP = 5;
        
        // Input control
        if (args.length != 1) {
            System.err.printf("Usage: %s index_path\n"
                    + "\tindex_path: Path to a directory containing a Lucene index.\n",
                    TFIDFSearcher.class.getSimpleName());
            return;
        }

        // Create a LuceneIndex instance.
        Index index = new LuceneIndex();
        index.load(args[0]);

        // Check if the index has been correctly loaded.
        if (index.isLoaded()) {
            TFIDFSearcher tfidfSearcher = new TFIDFSearcher();
            tfidfSearcher.build(index);

            // Ask for queries.
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter a query (press enter to finish): ");
            String query = scanner.nextLine();
            while (!query.equals("")) {
                // Show results.
                List<ScoredTextDocument> resultList = tfidfSearcher.search(query);
                // If there were no errors, show the results.
                if (resultList != null) {
                    if (resultList.isEmpty()) {
                        System.out.println("No results.");
                    } else {
                        System.out.println("Showing top " + TOP + " documents:");
                        // Get sublist.
                        if (resultList.size() >= TOP) {
                            resultList = resultList.subList(0, TOP);
                        }
                        resultList.forEach((ScoredTextDocument t) -> {
                            TextDocument document = index.getDocument(t.getDocID());
                            if (document != null) {
                                System.out.println(document.getName());
                            }
                        });
                    }
                    System.out.print("Enter a query (press enter to finish): ");
                    query = scanner.nextLine();
                } else {
                    return;
                }
            }
        }
    }
}
