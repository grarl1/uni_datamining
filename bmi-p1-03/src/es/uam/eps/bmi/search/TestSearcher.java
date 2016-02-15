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
import es.uam.eps.bmi.search.searching.LuceneSearcher;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class used to test the searcher.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class TestSearcher {

    private static final String SEARCH_STATS_FILE = "output/searchstats";

    /**
     * Main class for testing.
     *
     * @param args The following arguments are used: index_path: Path to a
     * directory containing a Lucene index. queries_file_path: Path to the file
     * containing the queries to test. relevance_file_path: Path to the file
     * containing the relevant documents.
     */
    public static void main(String[] args) {
        // Input control
        if (args.length != 3) {
            System.err.printf("Usage: %s index_path queries_file_path relevance_file_path\n"
                    + "\tindex_path: Path to a directory containing a Lucene index.\n"
                    + "\tqueries_file_path: Path to the file containing the queries to test.\n"
                    + "\trelevance_file_path: Path to the file containing the relevant documents.\n",
                    TestSearcher.class.getSimpleName());
            return;
        }

        // Create the files.
        File queriesFile = new File(args[1]);
        File relevanceFile = new File(args[2]);

        // Check the files.
        if (!areReadable(queriesFile, relevanceFile)) {
            return;
        }

        // Create a LuceneIndex instance.
        LuceneIndex luceneIndex = new LuceneIndex();
        luceneIndex.load(args[0]);

        // Check if the index has been correctly loaded.
        if (!luceneIndex.isLoaded()) {
            System.err.printf("Couldn't load the index.\n");
            return;
        }

        // Create a LuceneSearcher instance.
        LuceneSearcher luceneSearcher = new LuceneSearcher();
        luceneSearcher.build(luceneIndex);

        // Construct the readers and the writer.
        BufferedReader queriesReader;
        BufferedReader relevanceReader;
        File outputFile = new File(SEARCH_STATS_FILE);
        outputFile.getParentFile().mkdirs();
        FileWriter fileWriter;
        try {
            queriesReader = new BufferedReader(new FileReader(queriesFile));
            relevanceReader = new BufferedReader(new FileReader(relevanceFile));
            fileWriter = new FileWriter(outputFile);
        } catch (FileNotFoundException ex) {
            System.err.println("Couldn't find the file:" + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
            return;
        } catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
            return;
        }

        // List for storing the queries and relevance list.
        ArrayList<String> queriesList = new ArrayList<>();

        // Store the queries
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

        // Make the queries and store the results.
        for (int i = 0; i < queriesList.size(); ++i) {
            // Search and get the document list.
            List<ScoredTextDocument> retrievedList = luceneSearcher.search(queriesList.get(i));
            try {
                String relevanceLine = relevanceReader.readLine();
                String[] relevanceDocuments = relevanceLine.split("\t");
                // Don't read the number
                List<String> relevanceDocumentsList = Arrays.asList(relevanceDocuments).subList(1, relevanceDocuments.length);

                // Calculate results.
                double pAt5 = precision(retrievedList, relevanceDocumentsList, 5);
                double pAt10 = precision(retrievedList, relevanceDocumentsList, 10);

                // Print results.
                fileWriter.write((i + 1) + "\t" + pAt5 + "\t" + pAt10 + "\n");
            } catch (IOException ex) {
                System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
                System.err.println(ex.getMessage());
                return;
            }
        }
        try {
            fileWriter.close();
        } catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Checks whether the files passed as arguments can be read.
     *
     * @param queriesFile File containing queries.
     * @param relevanceFile File containing relevance documents.
     * @return True if the files are readable, false otherwise.
     */
    private static boolean areReadable(File queriesFile, File relevanceFile) {
        // Check queries files
        if (!queriesFile.exists() || !queriesFile.isFile()) {
            System.err.printf("%s: not a file.\n", queriesFile.getAbsolutePath());
            return false;
        } else if (!queriesFile.canRead()) {
            System.err.printf("Couldn't read %s.\n", queriesFile.getAbsolutePath());
            return false;
        }

        // Check relevance files
        if (!relevanceFile.exists() || !relevanceFile.isFile()) {
            System.err.printf("%s: not a file.\n", relevanceFile.getAbsolutePath());
            return false;
        }
        if (!relevanceFile.canRead()) {
            System.err.printf("Couldn't read %s.\n", relevanceFile.getAbsolutePath());
            return false;
        }
        return true;
    }

    /**
     * Returns the fraction of the documents retrieved that are relevant to the
     * user's information need.
     *
     * @param retrievedList List of retrieved documents.
     * @param relevantList List of relevant documents.
     * @param n n value for the precision to be calculated with P@n measure.
     * @return the fraction of the documents retrieved that are relevant to the
     * user's information need.
     */
    public static double precision(List<ScoredTextDocument> retrievedList, List<String> relevantList, int n) {
        int intersectionCount = 0;

        // Get the intersection count of retrieved (up to n) and relevant documents.
        for (int i = 0; i < n && i < retrievedList.size(); ++i) {
            String[] nameArray = retrievedList.get(i).getDocID().split("/");
            String name = nameArray[nameArray.length - 1];
            name = name.substring(0, name.lastIndexOf("."));
            if (relevantList.contains(name)) {
                intersectionCount++;
            }
        }

        return ((double) intersectionCount / (double) n);
    }
}
