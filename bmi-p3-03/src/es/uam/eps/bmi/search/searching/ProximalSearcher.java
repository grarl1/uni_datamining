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
import es.uam.eps.bmi.search.indexing.IndexBuilder;
import es.uam.eps.bmi.search.indexing.Posting;
import es.uam.eps.bmi.search.indexing.StemIndex;
import es.uam.eps.bmi.search.indexing.StopwordIndex;
import es.uam.eps.bmi.search.parsing.BasicParser;
import es.uam.eps.bmi.search.parsing.StemParser;
import es.uam.eps.bmi.search.parsing.StopwordParser;
import es.uam.eps.bmi.search.parsing.TextParser;
import es.uam.eps.bmi.util.MinHeap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.tartarus.snowball.ext.englishStemmer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Class implementing a proximal searcher.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class ProximalSearcher implements Searcher {

    // Maximum number of results to retrieve.
    private int TOP_RESULTS_NUMBER = 5;

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

        // If no terms, return an empty list.
        if (terms.length == 0) {
            return new ArrayList<>();
        }
        // If there is only one term, search using the TF-IDF searcher.
        if (terms.length == 1) {
            TFIDFSearcher searcher = new TFIDFSearcher();
            searcher.build(index);
            searcher.setTopResultsNumber(TOP_RESULTS_NUMBER);
            return searcher.search(query);
        }

        // Load list of postings
        List<Posting>[] postingsArray = loadPostings(terms);
        // If any of the terms does not have postings, return empty list.
        if (postingsArray == null) {
            return new ArrayList<>();
        }

        // Sequential indexes array
        int[] indexesArray = new int[terms.length];
        // Min heap to sort the results
        MinHeap<ScoredTextDocument> minHeap = new MinHeap<>(TOP_RESULTS_NUMBER);

        // Get documents that contains every term and process them
        Posting[] matchingPostings = getNextMatchingPostings(postingsArray, indexesArray);
        while (matchingPostings != null) {
            int docID = matchingPostings[0].getDocID();
            double docScore = processPostings(matchingPostings);
            // Add to the heap if it's possible.
            minHeap.add(new ScoredTextDocument(docID, docScore));
            // Get the next array of postings.
            matchingPostings = getNextMatchingPostings(postingsArray, indexesArray);
        }

        // Resturn the results.
        List<ScoredTextDocument> result = minHeap.asList();
        Collections.sort(result, Collections.reverseOrder());
        return result;
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
     * Returns an array filled with the list of postings of the given terms.
     *
     * @param terms Terms used to load their lists of postings.
     * @return an array filled with the list of postings of the given terms.
     * Returns null if any of the given terms does not have postings.
     */
    private List<Posting>[] loadPostings(String[] terms) {
        List<Posting>[] postingArray = new List[terms.length];
        for (int i = 0; i < terms.length; ++i) {
            List<Posting> termPosting = index.getTermPostings(terms[i]);
            // Check there are postings of the given term.
            if (termPosting == null) {
                return null;
            }
            postingArray[i] = termPosting;
        }
        return postingArray;
    }

    /**
     * Returns an array that contains one posting of the same document per term.
     * Updates the indexes array so that next time this function is called,
     * another list is returned until there are no more matching postings.
     *
     * @param postingsArray Array of the lists of postings of each term.
     * @param indexesArray Array containing the indexes of the list of postings
     * that have been already processed.
     * @return an array that contains one posting per term. Returns null if
     * there are not more postings whose documents match.
     */
    private Posting[] getNextMatchingPostings(List<Posting>[] postingsArray, int[] indexesArray) {

        Posting[] result = new Posting[postingsArray.length];
        int currentDocID = 0;

        // Indexes
        int i = 0;
        int j = indexesArray[i];

        while (true) {

            // Check if we reach the end of any list of posting.
            if (j == postingsArray[i].size()) {
                return null;
            }

            // Start traversing the posting lists in search of any matching posting.
            if (result[0] == null) {
                result[0] = postingsArray[i].get(j);
                currentDocID = result[0].getDocID();
                ++i;
                j = indexesArray[i];
            } else {
                // Get the posting and the docID
                Posting posting = postingsArray[i].get(j);
                int docID = posting.getDocID();

                // Check if it is lower.
                if (docID < currentDocID) {
                    j = ++indexesArray[i];
                } // Check if it is greater.
                else if (docID > currentDocID) {
                    i = 0;
                    j = ++indexesArray[i];
                    Arrays.fill(result, null);
                } // Check if it is equal.
                else {
                    result[i++] = posting;
                    if (i == indexesArray.length) {
                        ++indexesArray[0];
                        return result;
                    } else {
                        j = indexesArray[i];
                    }
                }
            }
        }
    }

    /**
     * Given an array of postings of one document, returns the document's score
     * according to the score function for proximal searching.
     *
     * @param matchingPostings Array of postings of the same document.
     * @return the document's score according to the score function for proximal
     * searching.
     */
    private double processPostings(Posting[] matchingPostings) {

        // Value to be returned.
        double score = 0;

        // Begin the algorithm
        int a = Integer.MIN_VALUE;
        int b;

        // List of ranges
        ArrayList<int[]> pairs = new ArrayList<>();

        while (true) {
            // Used to find a or b.
            Integer[] auxArray = new Integer[matchingPostings.length];

            // Finding b
            Arrays.fill(auxArray, Integer.MAX_VALUE);
            for (int i = 0; i < matchingPostings.length; ++i) {
                List<Integer> termPositions = matchingPostings[i].getTermPositions();
                for (int j = 0; j < termPositions.size(); ++j) {
                    if (termPositions.get(j) > a) {
                        auxArray[i] = termPositions.get(j);
                        break;
                    }
                }
                if (auxArray[i] == Integer.MAX_VALUE) {
                    break;
                }
            }
            b = Collections.max(Arrays.asList(auxArray));

            // Check if b is infinite.
            if (b == Integer.MAX_VALUE) {
                break;
            }

            // Finding a
            Arrays.fill(auxArray, Integer.MAX_VALUE);
            for (int i = 0; i < matchingPostings.length; ++i) {
                List<Integer> termPositions = matchingPostings[i].getTermPositions();
                for (int j = 0; j < termPositions.size(); ++j) {
                    if (termPositions.get(j) <= b) {
                        auxArray[i] = termPositions.get(j);
                    } else {
                        break;
                    }
                }
            }
            a = Collections.min(Arrays.asList(auxArray));

            // Add the range to the list.
            pairs.add(new int[]{a, b});
        }

        // Calculate the score
        for (int[] pair : pairs) {
            score += 1.0 / ((double) (pair[1] - pair[0] - matchingPostings.length + 2));
        }

        return score;
    }

    /**
     * Main class for a proximal searcher.
     *
     * Builds a searcher and asks the user for queries, showing the TOP 5
     * results. Reads the configuration from XML_INPUT file.
     *
     * @param args ignored.
     */
    public static void main(String[] args) {
        // Top results
        final int TOP = 5;

        // Read configuration from XML.
        String outPath = getIndexPath();

        if (outPath == null) {
            return;
        }

        // Ask for type of searcher
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose the index type: ");
        System.out.println("\tBasic: 1");
        System.out.println("\tStopword: 2");
        System.out.println("\tStemming: 3");
        System.out.print("Enter the option (1, 2, 3): ");
        String optionStr = scanner.nextLine();

        // Create index instance.
        Index index;
        TextParser parser;
        try {
            int option = Integer.valueOf(optionStr);
            switch (option) {
                case 1:
                    index = new BasicIndex();
                    parser = new BasicParser();
                    index.load(outPath + IndexBuilder.BASIC_I_APPEND);
                    break;
                case 2:
                    index = new StopwordIndex();
                    parser = new StopwordParser();
                    index.load(outPath + IndexBuilder.STOP_I_APPEND);
                    break;
                case 3:
                    index = new StemIndex();
                    parser = new StemParser(2, new englishStemmer());
                    index.load(outPath + IndexBuilder.STEM_I_APPEND);
                    break;
                default:
                    System.err.println(optionStr + ": not an option.");
                    return;
            }
        } catch (NumberFormatException ex) {
            System.err.println(optionStr + ": not an option.");
            return;
        }

        // Check if the index has been correctly loaded.
        if (index.isLoaded()) {
            ProximalSearcher proximalSearcher = new ProximalSearcher();
            proximalSearcher.build(index);
            proximalSearcher.setTopResultsNumber(TOP);

            // Ask for queries.
            System.out.print("Enter a query (press enter to finish): ");
            String query = scanner.nextLine();
            while (!query.equals("")) {
                // Show results.
                long fromTime = System.nanoTime();
                List<ScoredTextDocument> resultList = proximalSearcher.search(parser.parse(query));
                long toTime = System.nanoTime();
                // If there were no errors, show the results.
                if (resultList != null) {
                    if (resultList.isEmpty()) {
                        System.out.println("No results.");
                    } else {
                        System.out.println("Search time: " + ((toTime - fromTime) / 1e6) + " milliseconds");
                        System.out.println("Showing top " + TOP + " documents:");
                        resultList.stream().forEach((t) -> {
                            TextDocument document = index.getDocument(t.getDocID());
                            if (document != null) {
                                System.out.println("ID: " + document.getId() + "\tName: " + document.getName() + "\tScore: " + t.getScore());
                                System.out.println();
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

    /**
     * Returns the path where the three indexes are stored.
     *
     * @return the path where the three indexes are stored.
     */
    private static String getIndexPath() {
        // Read configuration from XML.
        String outPath;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(IndexBuilder.XML_INPUT);
            doc.getDocumentElement().normalize();
            outPath = doc.getElementsByTagName(IndexBuilder.OUTPATH_TAG_NAME).item(0).getTextContent();
        } catch (ParserConfigurationException | SAXException ex) {
            System.err.println("Exception caught while configurating XML parser: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
            return null;
        } catch (IOException ex) {
            System.err.println("Exception caught while performing IO operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
            return null;
        }
        if (!outPath.endsWith("/")) {
            outPath += "/";
        }
        return outPath;
    }
}
