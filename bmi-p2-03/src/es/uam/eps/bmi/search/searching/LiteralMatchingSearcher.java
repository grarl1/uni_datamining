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
import es.uam.eps.bmi.search.parsing.BasicParser;
import es.uam.eps.bmi.search.parsing.TextParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Class implementing literal searcher.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class LiteralMatchingSearcher implements Searcher {

    // Maximum number of results to retrieve.
    int TOP_RESULTS_NUMBER = 5;

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

        // If no terms, return an empty string.
        if (terms.length == 0) {
            return new ArrayList<>();
        }

        // Final list of terms
        List<Posting> finalPostingList = index.getTermPostings(terms[0]);
        if (finalPostingList == null || finalPostingList.isEmpty()) {
            return new ArrayList<>();
        }

        if (terms.length > 0) {
            terms = Arrays.copyOfRange(terms, 1, terms.length);
        }

        // Iterate the terms
        for (String term : terms) {
            List<Posting> nextPostingList = index.getTermPostings(term);
            if (finalPostingList != null && nextPostingList != null && !finalPostingList.isEmpty() && !nextPostingList.isEmpty()) {
                finalPostingList = concatPostings(finalPostingList, nextPostingList);
            } else {
                return new ArrayList<>();
            }
        }

        // Build the list of results
        List<ScoredTextDocument> resultList = new ArrayList<>();
        int sentenceDocsCount = finalPostingList.size();
        int docsCount = index.getDocIds().size();

        // Compute the score of each document
        for (Posting posting : finalPostingList) {
            // Get document attributes
            int docID = posting.getDocID();
            double docMod = index.getDocModule(docID);

            // Get term attributes
            int sentenceFrequency = posting.getTermFrequency();

            // Compute the tf-idf
            double tf = 1 + (Math.log(sentenceFrequency) / Math.log(2));
            double idf = Math.log((double) docsCount / (double) sentenceDocsCount) / Math.log(2);
            double tf_idf = tf * idf;

            // Compute the ranking
            double score = (1 / docMod) * tf_idf;
            resultList.add(new ScoredTextDocument(docID, score));
        }

        // Sort the list and return
        Collections.sort(resultList, Collections.reverseOrder());

        if (resultList.size() > TOP_RESULTS_NUMBER) {
            return resultList.subList(1, TOP_RESULTS_NUMBER + 1);
        }
        return resultList;
    }

    /**
     * Takes <code>previousPostingList</code> and find postings in
     * <code>currentPostingsList</code> having the same document ID and
     * consecutive positions
     *
     * The postings must be sorted by document ID, and the list of positions
     * must be sorted too.
     *
     * @param previousPostingList First list of postings.
     * @param currentPostingsList Second list of postings in which to find the
     * postings with same document ID and consecutive positions.
     * @return A list filled with the found postings.
     */
    private List<Posting> concatPostings(List<Posting> previousPostingList, List<Posting> currentPostingsList) {
        // Result list.
        List<Posting> resultPostings = new ArrayList<>();

        // Iterate the second list of postings.
        for (Posting currPosting : currentPostingsList) {
            // Search for an element with the same document ID.
            int prevPostingPos = Collections.binarySearch(previousPostingList, currPosting, (Posting t1, Posting t2) -> {
                return Integer.compare(t1.getDocID(), t2.getDocID());
            });

            // Check if found
            if (prevPostingPos >= 0) {
                Posting resultPosting = new Posting(currPosting.getTerm(), currPosting.getDocID(), new ArrayList<>());
                Posting prevPosting = previousPostingList.get(prevPostingPos);

                // Build the new posting.
                for (int currPosition : currPosting.getTermPositions()) {
                    for (int prevPosition : prevPosting.getTermPositions()) {
                        if (currPosition == prevPosition + 1) {
                            resultPosting.addPosition(currPosition);
                        }
                        if (currPosition <= prevPosition) {
                            break;
                        }
                    }
                }

                // Add the result posting in case that the positions are consecutive.
                if (resultPosting.getTermFrequency() > 0) {
                    resultPostings.add(resultPosting);
                }
            }

        }
        return resultPostings;
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
     * Main class for a literal searcher.
     *
     * Builds a searcher and asks the user for queries, showing the TOP 5
     * results. Reads the configuration from XML_INPUT file.
     *
     * @param args ignored.
     */
    public static void main(String[] args) {
        // Top results
        int TOP = 5;

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
            return;
        } catch (IOException ex) {
            System.err.println("Exception caught while performing IO operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
            return;
        }
        if (!outPath.endsWith("/")) {
            outPath += "/";
        }

        // Create a LuceneIndex instance.
        Index index = new BasicIndex();
        index.load(outPath + IndexBuilder.BASIC_I_APPEND);
        TextParser parser = new BasicParser();

        // Check if the index has been correctly loaded.
        if (index.isLoaded()) {
            LiteralMatchingSearcher literalMatchingSearcher = new LiteralMatchingSearcher();
            literalMatchingSearcher.build(index);

            // Ask for queries.
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter a query (press enter to finish): ");
            String query = scanner.nextLine();
            while (!query.equals("")) {
                // Show results.
                long fromTime = System.nanoTime();
                List<ScoredTextDocument> resultList = literalMatchingSearcher.search(parser.parse(query));
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
