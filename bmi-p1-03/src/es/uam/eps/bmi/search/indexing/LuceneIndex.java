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
import es.uam.eps.bmi.search.parsing.HTMLSimpleParser;
import es.uam.eps.bmi.search.parsing.TextParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * This class represents a Lucene indexer built using the Lucene library.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class LuceneIndex implements Index {

    /* Attributes */
    private String indexPath; // Path where the index is indexed
    private IndexReader reader = null;
    private HashMap<String, Integer> namesMap = null;

    /**
     * Getter for IndexReader
     *
     * @return IndexReader.
     */
    public IndexReader getReader() {
        return reader;
    }
    
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
        if (!docsPath.exists() || !docsPath.isDirectory()) {
            System.err.printf("%s: not a directory.\n", docsPath.getAbsolutePath());
            return;
        } else if (!docsPath.canRead()) {
            System.err.printf("Couldn't read in %s.\n", docsPath.getAbsolutePath());
            return;
        }

        // Attributes
        this.indexPath = outputIndexPath;

        // Start timing.
        Date start = new Date();
        System.out.println("Indexing documents from '" + inputCollectionPath + "'...");

        // Create directory.
        Directory outputDir;
        try {
            outputDir = FSDirectory.open(new File(outputIndexPath));
        } catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
            return;
        }

        // Create analyzer.
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);

        // Create the writer configuration in order to create new documents.
        IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_36, analyzer);
        writerConfig.setOpenMode(OpenMode.CREATE);

        // Create writer.
        try (IndexWriter writer = new IndexWriter(outputDir, writerConfig)) {
            // Start indexing.
            indexDocuments(writer, new File(inputCollectionPath), textParser);
        } catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());            
            return;
        }

        // Stop timing and print elapsed time.
        Date end = new Date();
        System.out.println(end.getTime() - start.getTime() + " total milliseconds");
        reader = null;
        namesMap = null;
    }

    /**
     * Indexes the given file using the given writer.
     *
     * @param writer Writer to the path where the index will be stored.
     * @param file The file or directory whose documents will be index.
     * @param textParser the parser used to process documents.
     * @throws IOException If the file or directory cannot be indexed.
     */
    private void indexDocuments(IndexWriter writer, File file, TextParser textParser) throws IOException {

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
            else {

                try (FileInputStream fis = new FileInputStream(file);
                        ZipInputStream zis = new ZipInputStream(fis)) {

                    // If file is a ZIP, zis.getNextEntry() returns not null.
                    if ( zis.getNextEntry() != null ) { 
                        zis.close();
                        indexZip(writer, file, textParser);
                    }
                    else { // if file is not a ZIP
                        // Create a new empty document.
                        Document doc = new Document();

                        // Add the path of the file as a field named "docID".  Use a
                        // field that is indexed (i.e. searchable), but don't tokenize 
                        // the field into separate words and don't index term frequency
                        // or positional information:
                        Field idField = new Field("docID", file.getPath(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
                        idField.setIndexOptions(IndexOptions.DOCS_ONLY);
                        doc.add(idField);
                        // Do the same with the name
                        Field nameField = new Field("name", file.getPath(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
                        nameField.setIndexOptions(IndexOptions.DOCS_ONLY);
                        doc.add(nameField);

                        // Read the whole content from file.
                        byte[] byteContent = new byte[(int) file.length()];
                        fis.read(byteContent);
                        // Add the contents of the file to a field named "contents".
                        doc.add(new Field("contents", textParser.parse(new String(byteContent)), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));

                        // New index, so we just add the document (no old document can be there)
                        System.out.println("Adding " + file);
                        writer.addDocument(doc);
                    }

                } catch (FileNotFoundException ex) {
                    System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
    
    /**
     * Indexes every file inside zip file passed.
     * Behavior undefined for zip files containing folders.
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
            while ( (ze = zis.getNextEntry()) != null ) {

                // Index file read.
                // Create a new empty document.
                Document doc = new Document();

                // Add the path of the file as a field named "docID".  Use a
                // field that is indexed (i.e. searchable), but don't tokenize 
                // the field into separate words and don't index term frequency
                // or positional information:
                Field idField = new Field("docID", zipFile.getPath()+"/"+ze.getName(),
                        Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
                idField.setIndexOptions(IndexOptions.DOCS_ONLY);
                doc.add(idField);
                // Do the same with the name
                Field nameField = new Field("name", zipFile.getPath()+"/"+ze.getName(),
                        Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
                nameField.setIndexOptions(IndexOptions.DOCS_ONLY);
                doc.add(nameField);

                // read file
                byte[] byteContent = new byte[(int) ze.getSize()];
                zis.read(byteContent);
                // Add the contents of the file to a field named "contents".
                doc.add(new Field("contents", textParser.parse(new String(byteContent)), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));

                // New index, so we just add the document (no old document can be there)
                System.out.println("Adding " + ze);
                writer.addDocument(doc);

            }
        } catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
        }
    }
    
    /**
     * Stores (partially or completely) a previously created index in memory.
     *
     * @param indexPath Path to the directory where the index is stored.
     */
    @Override
    public void load(String indexPath) {
        // Store index path.
        this.indexPath = indexPath;

        // Read the index
        try {
            reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
        } catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
        }
        
        // Creating a HashMap to map document names with its position in Lucene array.
        namesMap = new HashMap<>();
        for (int i = 0; i < reader.numDocs(); i++) {
            try {
                namesMap.put(reader.document(i).getFieldable("docID").stringValue(), i);
            } catch (IOException ex) {
                System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
                System.err.println(ex.getMessage());
            }
        }
    }

    /**
     * Returns index path.
     *
     * @return index path.
     */
    @Override
    public String getPath() {
        return indexPath;
    }

    /**
     * Returns a list containing every document ID in the index.
     *
     * @return a list containing every document ID in the index.
     */
    @Override
    public List<String> getDocIds() {
        // If the index has not been loaded, return null.
        if (reader == null) {
            return null;
        }

        // Build the list.
        List<String> docIds = new ArrayList<>();
        for (int i = 0; i < reader.numDocs(); ++i) {
            try {
                docIds.add(reader.document(i).getFieldable("docID").stringValue());
            } catch (IOException ex) {
                System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
                System.err.println(ex.getMessage());
            }
        }
        return docIds;
    }

    /**
     * Returns a document given its Id.
     *
     * @param docId Id of the document to retrieve.
     * @return a <code>TextDocument</code> instance matching the given Id.
     */
    @Override
    public TextDocument getDocument(String docId) {
        if (reader == null) {
            return null;
        }
        Integer id = namesMap.get(docId);
        if (id == null) {
            return null;
        }
        Document doc;
        try {
            doc = reader.document(id);
        } catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
            return null;
        }
        return new TextDocument(docId, doc.getFieldable("name").stringValue());
    }

    /**
     * Returns the list of terms extracted from the indexed documents.
     *
     * @return the list of terms extracted from the indexed documents.
     */
    @Override
    public List<String> getTerms() {
        // If the index has not been loaded, return null.
        if (reader == null) {
            return null;
        }

        // Build the list without repeated elements.
        HashSet<String> termsSet = new HashSet<>();
        List<String> termsList = new ArrayList<>();

        // Iterate over the documents
        int j = reader.numDocs();
        for (int i = 0; i < reader.numDocs(); ++i) {
            try {
                TermFreqVector freqVector = reader.getTermFreqVector(i, "contents");
                termsSet.addAll(Arrays.asList(freqVector.getTerms()));
            } catch (IOException ex) {
                System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
                System.err.println(ex.getMessage());
            }
        }
        termsList.addAll(termsSet);
        return termsList;

    }

    /**
     * Returns the list of Postings associated to a term.
     *
     * @param term term to get Postings.
     * @return the list of Postings associated to a term.
     */
    @Override
    public List<Posting> getTermPostings(String term) {
        List<Posting> lp = new ArrayList<>();
        try {
            TermPositions tp = reader.termPositions(new Term("contents", term));
            //For every tuple in tp, get doc name and the list of positions for the term
            while (tp.next()) {
                List<Long> position_list = new ArrayList<>();
                for (int i = 0; i < tp.freq(); i++) {
                    position_list.add(new Long(tp.nextPosition()));
                }
                String docID = reader.document(tp.doc()).getFieldable("docID").stringValue();
                lp.add(new Posting(term, docID, position_list));
            }
        } catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
        }
        return lp;
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
     * Main class for Lucene index.
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
                    LuceneIndex.class.getSimpleName());
            return;
        }

        // Build the index
        LuceneIndex luceneIndex = new LuceneIndex();
        luceneIndex.build(args[0], args[1], new HTMLSimpleParser());
    }
}
