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
import es.uam.eps.bmi.search.indexing.StemIndex;
import es.uam.eps.bmi.search.indexing.StopwordIndex;
import es.uam.eps.bmi.search.parsing.BasicParser;
import es.uam.eps.bmi.search.parsing.StemParser;
import es.uam.eps.bmi.search.parsing.StopwordParser;
import es.uam.eps.bmi.search.parsing.TextParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.tartarus.snowball.ext.englishStemmer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Class for testing the searchers.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class SearcherTest {

    private static final int INDEX_N = 3;
    private static final int SEARCHER_N = 3;
    private static final String QUERIES_FILE_NAME = "queries.txt";
    private static final String RELEVANCE_FILE_NAME = "relevance.txt";

    /**
     * Main method: Run queries with the TF-IDF and the literal searcher using
     * the three indexes (basic, stopword and stem).
     *
     * @param args ignored.
     */
    public static void main(String[] args) {
        // Get both parhs
        String collectionPath;
        String outPath;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(IndexBuilder.XML_INPUT);
            doc.getDocumentElement().normalize();
            collectionPath = doc.getElementsByTagName(IndexBuilder.COLLECTION_TAG_NAME).item(0).getTextContent();
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

        // Get the queries file
        File queriesFile = new File(collectionPath).getParentFile();
        queriesFile = new File(queriesFile.getPath() + "/" + QUERIES_FILE_NAME);

        // Get the relevance file
        File relevanceFile = new File(collectionPath).getParentFile();
        relevanceFile = new File(relevanceFile.getPath() + "/" + RELEVANCE_FILE_NAME);

        if (!queriesFile.exists() || !relevanceFile.exists()) {
            System.err.println("The files " + QUERIES_FILE_NAME + " and "
                    + RELEVANCE_FILE_NAME + " must be in the parent directory "
                    + "where the documents collection is stored.");
            return;
        }

        /* Load indexes */
        String[] indexNames = new String[INDEX_N];
        Index[] indexes = new Index[INDEX_N];
        TextParser[] parsers = new TextParser[INDEX_N];

        // Load basic index.
        Index basicIndex = new BasicIndex();
        basicIndex.load(outPath + IndexBuilder.BASIC_I_APPEND);
        TextParser basicParser = new BasicParser();
        indexNames[0] = "Basic index";
        indexes[0] = basicIndex;
        parsers[0] = basicParser;
        // Load stopword index
        Index stopwordIndex = new StopwordIndex();
        stopwordIndex.load(outPath + IndexBuilder.STOP_I_APPEND);
        TextParser stopwordParser = new StopwordParser();
        indexNames[1] = "Stopword index";
        indexes[1] = stopwordIndex;
        parsers[1] = stopwordParser;
        // Load stem index
        Index stemIndex = new StemIndex();
        stemIndex.load(outPath + IndexBuilder.STEM_I_APPEND);
        TextParser stemParser = new StemParser(2, new englishStemmer());
        indexNames[2] = "Stem index";
        indexes[2] = stemIndex;
        parsers[2] = stemParser;
        // Store all

        /* Create searchers */
        String[] searcherNames = new String[SEARCHER_N];
        Searcher[] searchers = new Searcher[SEARCHER_N];
        // TF-IDF
        Searcher tfidfSearcher = new TFIDFSearcher();
        searcherNames[0] = "TF-IDF searcher";
        searchers[0] = tfidfSearcher;
        // Literal
        Searcher literarSearcher = new LiteralMatchingSearcher();
        searcherNames[1] = "Literal searcher";
        searchers[1] = literarSearcher;
        // Proximal
        Searcher proximalSearcher = new ProximalSearcher();
        searcherNames[2] = "Proximal searcher";
        searchers[2] = proximalSearcher;

        // Construct the readers and the writer.
        BufferedReader queriesReader;
        BufferedReader relevanceReader;
        try {
            queriesReader = new BufferedReader(new FileReader(queriesFile));
            relevanceReader = new BufferedReader(new FileReader(relevanceFile));
        } catch (FileNotFoundException ex) {
            System.err.println("Couldn't find the file:" + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
            return;
        }

        // Store the queries
        ArrayList<String> queriesList = new ArrayList<>();
        try {
            String queriesLine = queriesReader.readLine();
            while (queriesLine != null) {
                // Dont read the number
                queriesList.add(queriesLine.split(":")[1]);
                queriesLine = queriesReader.readLine();
            }
        } catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
            return;
        }

        // Print precisions
        printPrecision(indexNames, searcherNames, indexes, searchers, parsers, relevanceReader, queriesList);

        // Measure TF-IDF cost
        System.out.println("Measures for the basic index and TF-IDF searcher:");
        printCost(basicIndex, proximalSearcher, basicParser, queriesList);
    }

    /**
     * Do the search and print the P@5 and P@10 values with the given searchers
     * and indexes.
     *
     * @param indexNames Array with the indexes names.
     * @param searcherNames Array with the searcher names.
     * @param indexes Array with the indexes.
     * @param searchers Array with the searchers.
     * @param parsers Array with the parsers.
     * @param relevanceReader Reader used to read the relevance list.
     * @param queriesList A list filled with the queries.
     */
    private static void printPrecision(String[] indexNames, String[] searcherNames, Index[] indexes, Searcher[] searchers, TextParser[] parsers, BufferedReader relevanceReader, ArrayList<String> queriesList) {
        for (int queryIndex = 0; queryIndex < queriesList.size(); ++queryIndex) {
            List<String> relevanceDocumentsList;
            try {
                // Read the relevance documents
                String relevanceLine = relevanceReader.readLine();
                String[] relevanceDocuments = relevanceLine.split("\t");
                relevanceDocumentsList = Arrays.asList(relevanceDocuments).subList(1, relevanceDocuments.length);
            } catch (IOException ex) {
                System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
                System.err.println(ex.getMessage());
                return;
            }
            // Test for every index and every searcher.
            for (int searcher_count = 0; searcher_count < searchers.length; ++searcher_count) {
                searchers[searcher_count].setTopResultsNumber(10);
                System.out.println("Searcher: " + searcherNames[searcher_count]);
                for (int index_count = 0; index_count < indexes.length; ++index_count) {
                    searchers[searcher_count].build(indexes[index_count]);
                    List<ScoredTextDocument> retrievedList = searchers[searcher_count].search(parsers[index_count].parse(queriesList.get(queryIndex)));
                    double pAt5 = precision(indexes[index_count], retrievedList, relevanceDocumentsList, 5);
                    double pAt10 = precision(indexes[index_count], retrievedList, relevanceDocumentsList, 10);
                    System.out.println("Query: " + (queryIndex + 1) + " \tIndex: " + indexNames[index_count] + "\tP@5: " + pAt5 + "\tP@10: " + pAt10);
                }
                System.out.println();
            }
        }
    }

    /**
     * Returns the fraction of the documents retrieved that are relevant to the
     * user's information need.
     *
     * @param index Index used to retrieve the documents.
     * @param retrievedList List of retrieved documents.
     * @param relevantList List of relevant documents.
     * @param n n value for the precision to be calculated with P@n measure.
     * @return the fraction of the documents retrieved that are relevant to the
     * user's information need.
     */
    private static double precision(Index index, List<ScoredTextDocument> retrievedList, List<String> relevantList, int n) {
        int intersectionCount = 0;
        // Get the intersection count of retrieved (up to n) and relevant documents.
        for (int i = 0; i < n && i < retrievedList.size(); ++i) {
            TextDocument document = index.getDocument(retrievedList.get(i).getDocID());
            String[] nameArray = document.getName().split("/");
            String name = nameArray[nameArray.length - 1];
            name = name.substring(0, name.lastIndexOf("."));
            if (relevantList.contains(name)) {
                intersectionCount++;
            }
        }
        return ((double) intersectionCount / (double) n);
    }

    /**
     * Calculates and prints the cost of run the queries using a particular
     * index and searcher.
     * 
     * @param index Index to use.
     * @param searcher Searcher to use.
     * @param parser Parse to parse the queries.
     * @param queriesList List of queries.
     */
    private static void printCost(Index index, Searcher searcher, TextParser parser, ArrayList<String> queriesList) {
        
        // Initialize the searcher.
        searcher.setTopResultsNumber(10);
        searcher.build(index);
        
        // Start measuring
        Runtime runtime = Runtime.getRuntime();
        long start = System.nanoTime();
        queriesList.stream().forEach((query) -> {
            searcher.search(parser.parse(query));
        });
        long end = System.nanoTime();
        
        System.out.println("Elapsed time: " + (end-start)/1e6 + " milliseconds.");
    }
}
