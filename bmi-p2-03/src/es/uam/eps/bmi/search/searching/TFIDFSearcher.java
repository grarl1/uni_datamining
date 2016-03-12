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
import es.uam.eps.bmi.search.indexing.BasicIndex;
import es.uam.eps.bmi.search.indexing.Index;
import es.uam.eps.bmi.search.indexing.Posting;
import es.uam.eps.bmi.util.MinHeap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

/**
 * Class implementing an information retrieval vector model with TF-IDF terms
 * weighing.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class TFIDFSearcher implements Searcher {

    // Maximum number of results to retrieve.
    int TOP_RESULTS_NUMBER = 5;

    //Index used to search
    private Index index;

    // Attributes
    private double docsCount = 0;

    /**
     * Creates a searcher using the given index.
     *
     * @param index Index used to create the searcher, must be loaded.
     */
    @Override
    public void build(Index index) {
        // Store the index object.
        this.index = index;
    }

    /**
     * Returns a ranking of documents sorted by the score value.
     *
     * @param query String query used to search.
     * @return a ranking of documents sorted by the decrementing score value.
     */
    @Override
    public List<ScoredTextDocument> search(String query) {
        // Separate the query string by spaces
        String[] terms = query.split(" ");

        // If no terms, return an empty string.
        if (terms.length == 0) {
            return new ArrayList<>();
        }

        // Heap to traverse the postings by docID.
        PriorityQueue<ScoredTextDocument> heap = new PriorityQueue<>(terms.length, (ScoredTextDocument t1, ScoredTextDocument t2) -> Integer.compare(t1.getDocID(), t2.getDocID()));
        // Min-Heap to store the results.
        MinHeap<ScoredTextDocument> minHeap = new MinHeap<>(TOP_RESULTS_NUMBER);

        // Attributes for calculation
        docsCount = (double) index.getDocIds().size();

        // Load term postings
        int[] cosequentialIndexes = new int[terms.length];
        List<List<Posting>> termsPostings = new ArrayList<>();
        for (String term : terms) {
            termsPostings.add(index.getTermPostings(term));
        }

        // Fill the heap for the first time.
        for (int termIndex = 0; termIndex < terms.length; ++termIndex) {
            List<Posting> termPostings = termsPostings.get(termIndex);
            if (termPostings != null && !termPostings.isEmpty()) {
                // Get the posting
                Posting posting = termPostings.get(0);
                heap.add(docFromPosting(posting, termPostings.size()));
                // Fill the indexes
                ++cosequentialIndexes[termIndex];
            }
        }

        // If the heap is empty, there are not results.
        if (heap.isEmpty()) {
            return new ArrayList<>();
        }

        // Initialize the process
        ScoredTextDocument currentDocument = heap.poll();

        // Iterate the list of postings.
        Posting[] nextPostings = new Posting[terms.length];
        while (true) {
            // Get the next posting.
            for (int termIndex = 0; termIndex < terms.length; ++termIndex) {
                List<Posting> termPostings = termsPostings.get(termIndex);
                if (termPostings != null && !termPostings.isEmpty()) {
                    int postingIndex = cosequentialIndexes[termIndex];
                    if (postingIndex < termPostings.size()) {
                        nextPostings[termIndex] = termPostings.get(postingIndex);
                    } else {
                        nextPostings[termIndex] = null;
                    }
                } else {
                    nextPostings[termIndex] = null;
                }
            }

            // Break condition.
            if (isEmptyArray(nextPostings)) {
                break;
            }

            // Get the posting
            Posting nextPosting = minArrayElement(nextPostings, (Posting t1, Posting t2) -> Integer.compare(t1.getDocID(), t2.getDocID()));
            int termIndex = Arrays.asList(nextPostings).indexOf(nextPosting);
            cosequentialIndexes[termIndex] += 1;

            // Add the new document to the heap.
            heap.add(docFromPosting(nextPosting, termsPostings.get(termIndex).size()));

            // Update values
            ScoredTextDocument nextDocument = heap.poll();
            if (currentDocument.getDocID() == nextDocument.getDocID()) {
                nextDocument.setScore(currentDocument.getScore() + nextDocument.getScore());
            } else {
                minHeap.add(currentDocument);
            }
            currentDocument = nextDocument;
        }

        // Empty the heap
        currentDocument = heap.poll();
        while (currentDocument != null) {
            // Same routine until the heap is empty
            ScoredTextDocument nextDocument = heap.poll();
            if (nextDocument != null) {
                if (currentDocument.getDocID() == nextDocument.getDocID()) {
                    nextDocument.setScore(currentDocument.getScore() + nextDocument.getScore());
                } else {
                    minHeap.add(currentDocument);
                }
            } else {
                minHeap.add(currentDocument);
            }
            currentDocument = nextDocument;
        }

        // Resturn the results.
        return minHeap.asList();
    }

    /**
     * Returns a <code>ScoredTextDocument</code> object using a posting.
     *
     * @param posting Posting used to construct the object.
     * @param termPostingsSize The size of the term postings list.
     * @return a <code>ScoredTextDocument</code> object..
     */
    private ScoredTextDocument docFromPosting(Posting posting, int termPostingsSize) {
        // Attributes for calculation
        double tf = 1 + Math.log(posting.getTermFrequency());
        double idf = Math.log(((double) docsCount) / ((double) termPostingsSize));
        // Add the scored document to the heap
        return new ScoredTextDocument(posting.getDocID(), tf * idf / index.getDocModule(posting.getDocID()));
    }

    /**
     * Returns true if an array is full of null objects.
     *
     * @param <T> Type of element.
     * @param array Array to be checked.
     * @return true if an array is full of null objects, false otherwise.
     */
    private <T> boolean isEmptyArray(T[] array) {
        for (T t : array) {
            if (t != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the minimum element in an array.
     *
     * @param <T> Type of array elements.
     * @param array The array whose minimum element will be returned.
     * @param comparator Comparator to compare the elements.
     * @return the minimum element in an array.
     */
    private <T> T minArrayElement(T[] array, Comparator<? super T> comparator) {
        T min = array[0];
        for (int i = 1; i < array.length; ++i) {
            if (min != null && array[i] != null) {
                if (comparator.compare(array[i], min) < 0) {
                    min = array[i];
                }
            } else if (array[i] != null) {
                min = array[i];
            }
        }
        return min;
    }

    /**
     * Sets the maximum number of results to retrieve.
     *
     * @param topResultsNumber Maximum number of results to retrieve.
     */
    @Override
    public void setTopResultsNumber(int topResultsNumber) {
        this.TOP_RESULTS_NUMBER = topResultsNumber;
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
        // Top results
        int TOP = 5;

        // Input control
        if (args.length != 1) {
            System.err.printf("Usage: %s index_path\n"
                    + "\tindex_path: Path to a directory containing a Lucene index.\n",
                    TFIDFSearcher.class.getSimpleName());
            return;
        }

        // Create a LuceneIndex instance.
        Index index = new BasicIndex();
        index.load(args[0]);

        // Check if the index has been correctly loaded.
        if (index.isLoaded()) {
            TFIDFSearcher tfidfSearcher = new TFIDFSearcher();
            tfidfSearcher.build(index);
            tfidfSearcher.setTopResultsNumber(TOP);

            // Ask for queries.
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter a query (press enter to finish): ");
            String query = scanner.nextLine();
            while (!query.equals("")) {
                // Show results.
                long fromTime = System.nanoTime();
                List<ScoredTextDocument> resultList = tfidfSearcher.search(query);
                long toTime = System.nanoTime();
                // If there were no errors, show the results.
                if (resultList != null) {
                    if (resultList.isEmpty()) {
                        System.out.println("No results.");
                    } else {
                        System.out.println("Search time: " + ((toTime - fromTime) / 1e6) + " milliseconds");
                        System.out.println("Showing top " + TOP + " documents:");
                        resultList.forEach((ScoredTextDocument t) -> {
                            TextDocument document = index.getDocument(t.getDocID());
                            if (document != null) {
                                System.out.println("ID: " + document.getId() + "\tName: " + document.getName() + "\tScore: " + t.getScore());
                            }
                        });
                    }
                    System.out.println();
                    System.out.print("Enter a query (press enter to finish): ");
                    query = scanner.nextLine();
                } else {
                    return;
                }
            }
        }
    }
}
