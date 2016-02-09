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
import es.uam.eps.bmi.search.parsing.TextParser;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
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

    /**
     * Main class for Lucene index.
     *
     * It indexes a set of documents, creating an index in the output directory.
     * If the index already exists, then, new documents will be added to the
     * index, otherwise, a new index is built.
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

        // Input file control
        File docsPath = new File(args[0]);
        if (!docsPath.exists() || !docsPath.isDirectory()) {
            System.err.printf("%s: not a directory.\n", docsPath.getAbsolutePath());
        } else if (!docsPath.canRead()) {
            System.err.printf("Couldn't read in %s\n", docsPath.getAbsolutePath());
        }

        // Build the index
        LuceneIndex luceneIndex = new LuceneIndex();
        luceneIndex.build(args[0], args[1], null);
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
        // Exception control.
        try {
            // Start timing.
            Date start = new Date();
            System.out.println("Indexing documents from '" + inputCollectionPath
                    + "' to directory '" + outputIndexPath + "'...");

            // Create directory and analyzer.
            Directory outputDir = FSDirectory.open(new File(outputIndexPath));
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);

            // Create the writer in order to add new documents 
            // to an existing index or create a new one.
            IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_31, analyzer);
            writerConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
            IndexWriter writer = new IndexWriter(outputDir, writerConfig);

            // Start indexing.
            indexDocuments(writer, new File(inputCollectionPath));
        } catch (IOException ex) {
            System.err.printf("Error while performing an I/O operation: %s\n", ex.getMessage());
        }
    }

    private void indexDocuments(IndexWriter writer, File file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void load(String indexPath) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getPath() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getDocIds() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TextDocument getDocument(String docId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getTerms() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Posting> getTermPostings(String term) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
