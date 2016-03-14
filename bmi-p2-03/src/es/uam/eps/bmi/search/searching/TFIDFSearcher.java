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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.tartarus.snowball.ext.englishStemmer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Class implementing an information retrieval vector model with TF-IDF terms
 * weighing.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class TFIDFSearcher implements Searcher {

    // Maximum number of results to retrieve.
    private int TOP_RESULTS_NUMBER = 5;

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
            ScoredTextDocument nextDocument = heap.poll(); // Get the head document.
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
        List<ScoredTextDocument> result = minHeap.asList();
        Collections.sort(result, Collections.reverseOrder());
        return result;
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
        double tf = 1 + (Math.log(posting.getTermFrequency()) / Math.log(2));
        double idf = Math.log(((double) docsCount) / ((double) termPostingsSize)) / Math.log(2);
        double docMod = index.getDocModule(posting.getDocID());
        // Add the scored document to the heap
        return new ScoredTextDocument(posting.getDocID(), tf * idf / docMod);
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
     * results. Reads the configuration from XML_INPUT file.
     *
     * @param args ignored.
     */
    public static void main(String[] args) {
        // Top results
        final int TOP = 5;
        // Max content size
        final int MAX_READ = 64;

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
            TFIDFSearcher tfidfSearcher = new TFIDFSearcher();
            tfidfSearcher.build(index);
            tfidfSearcher.setTopResultsNumber(TOP);

            // Ask for queries.
            System.out.print("Enter a query (press enter to finish): ");
            String query = scanner.nextLine();
            while (!query.equals("")) {
                // Show results.
                long fromTime = System.nanoTime();
                List<ScoredTextDocument> resultList = tfidfSearcher.search(parser.parse(query));
                long toTime = System.nanoTime();
                // If there were no errors, show the results.
                if (resultList != null) {
                    if (resultList.isEmpty()) {
                        System.out.println("No results.");
                    } else {
                        System.out.println("Search time: " + ((toTime - fromTime) / 1e6) + " milliseconds");
                        System.out.println("Showing top " + TOP + " documents:");
                        for (ScoredTextDocument t : resultList) {
                            TextDocument document = index.getDocument(t.getDocID());
                            if (document != null) {
                                System.out.println("ID: " + document.getId() + "\tName: " + document.getName() + "\tScore: " + t.getScore());
                                String content = readContent(document.getName(), parser.parse(query), MAX_READ);
                                System.out.println("Content: " + content);
                                System.out.println();
                            }
                        }
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

    /**
     * Reads content from a file. It will show until <code>maxRead</code>
     * characters starting from the first occurrence from some of the terms in
     * the <code>query</code> string.
     *
     * @param path Path to the file.
     * @param query The query string.
     * @param maxRead Maximum characters to return.
     * @return The content of the file.
     */
    private static String readContent(String path, String query, int maxRead) {
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && parent.exists()) {
            // Zip file
            if (isZipFile(parent)) {
                return readZip(parent, path, query, maxRead);
            } // Normal file 
            else if (file.exists()) {
                return readFile(file, query, maxRead);
            }
        }
        return null;
    }

    /**
     * Reads content from a zip file. It will show until <code>maxRead</code>
     * characters starting from the first occurrence from some of the terms in
     * the <code>query</code> string.
     *
     * @param parent The parent file of the file to be read.
     * @param path Path to the file.
     * @param query The query string.
     * @param maxRead Maximum characters to return.
     * @return The content of the file.
     */
    private static String readZip(File parent, String path, String query, int maxRead) {
        try {
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            ZipFile zipFile = new ZipFile(parent);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                InputStream stream = zipFile.getInputStream(entry);
                if (entry.getName().equals(fileName)) {
                    byte[] byteContent = new byte[2048];
                    int justRead;
                    String s = "";
                    while ((justRead = stream.read(byteContent)) > 0) {
                        s += new String(byteContent, 0, justRead);
                    }
                    TextParser basicParser = new BasicParser();
                    String document = basicParser.parse(s);
                    String[] querySplit = query.split(" ");
                    for (String term : querySplit) {
                        if (document.contains(term)) {
                            int termIndex = document.indexOf(term);
                            int maxLength = (termIndex + maxRead) < document.length() ? (termIndex + maxRead) : document.length();
                            return document.substring(termIndex, maxLength);
                        }
                    }
                    int termIndex = 0;
                    int maxLength = (termIndex + maxRead) < document.length() ? (termIndex + maxRead) : document.length();
                    return document.substring(termIndex, maxLength);
                }
            }
        } catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
        }
        return null;
    }

    /**
     * * Reads content from a file. It will show until <code>maxRead</code>
     * characters starting from the first occurrence from some of the terms in
     * the <code>query</code> string.
     *
     * @param file The file to be read.
     * @param query The query string.
     * @param maxRead Maximum characters to return.
     * @return The content of the file.
     */
    private static String readFile(File file, String query, int maxRead) {
        try {
            FileReader stream = new FileReader(file);
            char[] charContent = new char[2048];
            int justRead;
            String s = "";
            while ((justRead = stream.read(charContent)) > 0) {
                s += new String(charContent, 0, justRead);
            }
            TextParser basicParser = new BasicParser();
            String document = basicParser.parse(s);
            String[] querySplit = query.split(" ");
            for (String term : querySplit) {
                if (document.contains(term)) {
                    int termIndex = document.indexOf(term);
                    int maxLength = (termIndex + maxRead) < document.length() ? (termIndex + maxRead) : document.length();
                    return document.substring(termIndex, maxLength);
                }
            }
            int termIndex = 0;
            int maxLength = (termIndex + maxRead) < document.length() ? (termIndex + maxRead) : document.length();
            return document.substring(termIndex, maxLength);
        } catch (FileNotFoundException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
        } catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
        }
        return null;
    }

    /**
     * Returns true if file passed is a zip file, false otherwise.
     *
     * @param file file to test
     * @return true if file passed is a zip file, false otherwise.
     */
    private static boolean isZipFile(File file) {
        try (FileInputStream fis = new FileInputStream(file);
                ZipInputStream zis = new ZipInputStream(fis)) {
            if (zis.getNextEntry() != null) {
                return true;
            }
        } catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
        }
        return false;
    }
}
