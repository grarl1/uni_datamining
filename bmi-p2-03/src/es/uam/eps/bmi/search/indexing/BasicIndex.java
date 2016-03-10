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
package es.uam.eps.bmi.search.indexing;

import es.uam.eps.bmi.search.TextDocument;
import es.uam.eps.bmi.search.parsing.BasicParser;
import es.uam.eps.bmi.search.parsing.TextParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Basic index class.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class BasicIndex implements Index {

    /* Attributes */
    protected String indexPath; // Path where the index is stored
    protected IndexWriter writer = null;
    protected IndexReader reader = null;

    /**
     * Builds an index from a collection of text documents.
     *
     * @param inputCollectionPath Path to the directory containing the
     * collection of documents to be indexed.
     * @param outputIndexPath Path to the directory to store the indexes.
     * @param textParser Parser for document processing.
     */
    @Override
    public void build(String inputCollectionPath, String outputIndexPath, TextParser textParser) {
        // Input control
        File docsPath = new File(inputCollectionPath);
        if (!docsPath.exists() || !docsPath.canRead()) {
            System.err.printf("%s does not exist or is not readable.\n", docsPath.getAbsolutePath());
            return;
        }

        // Attributes
        this.indexPath = outputIndexPath;

        // Start timing.
        Date start = new Date();
        System.out.println("Indexing documents from '" + inputCollectionPath + "', this may take a while...");

        // Create writer.
        writer = new IndexWriter(outputIndexPath, 10000000);
        // Start indexing.
        indexDocuments(writer, new File(inputCollectionPath), textParser);

        try {
            writer.close();
        } catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
            return;
        }

        // Stop timing and print elapsed time.
        Date end = new Date();
        System.out.println(end.getTime() - start.getTime() + " total milliseconds");
        reader = null;
    }

    /**
     * Stores (partially or completely) a previously created index in memory.
     *
     * @param indexPath Path to the directory where the index is stored.
     */
    @Override
    public void load(String indexPath) {
        if (writer != null) { //writer already in RAM, build reader from its data.
            try {
                reader = new IndexReader(writer);
            } catch (FileNotFoundException ex) {
                System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
                System.err.println(ex.getMessage());
                return;
            }
        } else {
            try {
                reader = new IndexReader(indexPath);
            } catch (IOException ex) {
                System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
                System.err.println(ex.getMessage());
                return;
            } catch (ClassNotFoundException ex) {
                System.err.println("Exception reading class from file: " + ex.getClass().getSimpleName());
                System.err.println(ex.getMessage());
                return;
            }
        }

        writer = null;
    }

    /**
     * Returns the path where the index is stored.
     *
     * @return the path where the index is stored.
     */
    @Override
    public String getPath() {
        return indexPath;
    }

    /**
     * Returns a list of the IDs of indexed documents.
     *
     * @return a list of the IDs of indexed documents.
     */
    @Override
    public List<Integer> getDocIds() {
        return reader.getDocIds();
    }

    /**
     * Returns a document given its Id.
     *
     * @param docId Id of the document to retrieve.
     * @return a <code>TextDocument</code> instance matching the given Id.
     */
    @Override
    public TextDocument getDocument(int docId) {
        return reader.getDocument(docId);
    }

    /**
     * Returns the list of terms extracted from the indexed documents.
     *
     * @return the list of terms extracted from the indexed documents.
     */
    @Override
    public List<String> getTerms() {
        try {
            return reader.getTerms();
        } catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
        }
        return null;
    }

    /**
     * Returns a list of postings of the given term.
     *
     * @param term Given term used to get the list of postings.
     * @return a list of the postings of the given term.
     */
    @Override
    public List<Posting> getTermPostings(String term) {
        try {
            return reader.getTermPostings(term);
        } catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
        }
        return null;
    }

    /**
     * Returns a true if the index is loaded, false otherwise.
     *
     * @return true if index is loaded, false otherwise.
     */
    @Override
    public boolean isLoaded() {
        return reader != null;
    }

    /**
     * Returns the module of the document corresponding to the id passed as
     * argument
     *
     * @param docId numeric id of the document to retrieve it's module.
     * @return the module of the document corresponding to the id passed as
     * argument.
     */
    @Override
    public double getDocModule(int docId) {
        return reader.getDocModule(docId);
    }

    /**
     * Indexes the given file using the given writer.
     *
     * @param writer Writer to the path where the index will be stored.
     * @param file The file or directory whose documents will be index.
     * @param textParser the parser used to process documents.
     * @throws IOException If the file or directory cannot be indexed.
     */
    private void indexDocuments(IndexWriter writer, File file, TextParser textParser) {

        // Make the index if the file/directory is readable.
        if (file.canRead()) {

            // If the file represents a directory, call this function recursively
            // for each file within.
            if (file.isDirectory()) {
                // Files within the directory
                String[] files = file.list();
                // Avoid IO errors.
                if (files != null) {
                    for (String fileInside : files) {
                        indexDocuments(writer, new File(file, fileInside), textParser);
                    }
                }
            } // The file object represents a file (not a directory).
            else // Test if file is a zip.
             if (isZipFile(file)) {
                    indexZip(writer, file, textParser);
                } else {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        String docname = file.getPath();
                        // Read the whole content from file.
                        byte[] byteContent = new byte[(int) file.length()];
                        fis.read(byteContent);
                        //Add document to the index
                        writer.add(docname, textParser.parse(new String(byteContent), "\\s+"));

                    } catch (IOException ex) {
                        System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
                        System.err.println(ex.getMessage());
                    }
                }
        }
    }

    /**
     * Indexes every file inside zip file passed. Behavior undefined for zip
     * files containing folders.
     *
     * @param writer Writer to the path where the index will be stored.
     * @param zipfile Zip file reading.
     * @param zis ZipInputStream to index
     * @param textParser the parser used to process documents.
     * @throws IOException If the file or directory cannot be indexed.
     */
    private void indexZip(IndexWriter writer, File zipFile, TextParser textParser) {
        ZipEntry ze;
        try (FileInputStream fis = new FileInputStream(zipFile);
                ZipInputStream zis = new ZipInputStream(fis)) {
            //Iterate over every file inside zip.
            while ((ze = zis.getNextEntry()) != null) {

                String docname = zipFile.getPath() + "/" + ze.getName();

                // read file
                byte[] byteContent = new byte[2048];
                int justRead;
                String s = "";
                while ((justRead = zis.read(byteContent)) > 0) {
                    s += new String(byteContent, 0, justRead);
                }
                //add document to the index
                Date st = new Date();
                writer.add(docname, textParser.parse(s));
                Date end = new Date();

            }
        } catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Returns true if file passed is a zip file, false otherwise.
     *
     * @param file file to test
     * @return true if file passed is a zip file, false otherwise.
     */
    private boolean isZipFile(File file) {
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

    /**
     * Main class for Basic index.
     *
     * It indexes a set of documents, creating an index in the output directory.
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
                    BasicIndex.class.getSimpleName());
            return;
        }

        // Build the index
        BasicIndex basicIndex = new BasicIndex();
        basicIndex.build(args[0], args[1], new BasicParser());

        System.out.print("Getting index stats...");
        basicIndex.load(args[1]);
        int totalDocuments = basicIndex.getDocIds().size();

        File f = new File(args[1] + "/" + "indexstats");
        try (FileWriter fw = new FileWriter(f, false)) {
            // Get stats from index and write them to the file.
            List<String> terms = basicIndex.getTerms();
            for (String term : terms) {
                long frequency = 0;
                long nDocs = 0;
                List<Posting> lp = basicIndex.getTermPostings(term);
                for (Posting p : lp) {
                    frequency += p.getTermFrequency();
                    nDocs++;
                }
                double tf = 1 + (Math.log(frequency) / Math.log(2));
                double idf = Math.log(totalDocuments / nDocs) / Math.log(2) ;
                String outputString = String.format("%s %d %d %.2f %.2f\n", term, frequency, nDocs, tf, idf);
                fw.write(outputString);
            }
        } catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
        }
        System.out.println("Done");
    }
}
