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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * IndexReader class.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class IndexReader {

    /* Doc modules id, position i of array will correspond to document with docid i */
    protected double[] docMod = null;

    /* Name of the file to store finished index */
    protected static final String INDEX_FILE_NAME = IndexWriter.INDEX_FILE_NAME;
    /* Name of the file which contains numeric docId and real name of documents 
        in the index*/
    protected static final String DOCMAP_FILE_NAME = IndexWriter.DOCMAP_FILE_NAME;
    /* Name of the file which contains the list of terms and its offset in the
        final index*/
    protected static final String TERMOFF_FILE_NAME = IndexWriter.TERMOFF_FILE_NAME;
    /* Name of the file which contains the list of terms and its offset in the
        final index*/
    protected static final String DOC_MODULES_FILE_NAME = IndexWriter.DOC_MODULES_FILE_NAME;
    /* Path to save index */
    protected String indexPath;

    /* TreeMap to store document names and given numeric id */
    protected TreeMap<Integer, String> docsmap;
    /* Map containing offsets of terms in final index */
    protected TreeMap<String, Integer> termsoffset;

    /* File to read index from */
    private RandomAccessFile raf;

    /* Indicates the number of terms gap in termMapFile so that
        only 1 of every termMapSize terms will be written to termMapFile file */
    private static final int TERM_MAP_SIZE = 100;

    /* last term read from index */
    private String lastRead = "";
    /* last term read offset in block */
    private int lastReadOffset = 0;

    /**
     * Default constructor.
     */
    public IndexReader(String indexPath) throws FileNotFoundException, IOException, ClassNotFoundException {
        this.indexPath = indexPath;
        if (!indexPath.endsWith("/")) {
            this.indexPath += "/";
        }
        raf = new RandomAccessFile(new File(this.indexPath + INDEX_FILE_NAME), "r");
        docsmap = (TreeMap<Integer, String>) (new ObjectInputStream(new FileInputStream(this.indexPath + DOCMAP_FILE_NAME))).readObject();
        termsoffset = (TreeMap<String, Integer>) (new ObjectInputStream(new FileInputStream(this.indexPath + TERMOFF_FILE_NAME))).readObject();
        docMod = (double[]) (new ObjectInputStream(new FileInputStream(this.indexPath + DOC_MODULES_FILE_NAME))).readObject();
    }

    /**
     * Constructor from an IndexWriter.
     *
     * @param iw IndexWriter to build reader from.
     * @throws java.io.FileNotFoundException
     *
     */
    public IndexReader(IndexWriter iw) throws FileNotFoundException {
        this.docMod = iw.docMod;
        this.docsmap = iw.docsmap;
        this.indexPath = iw.indexPath;
        this.termsoffset = iw.termsoffset;
        raf = new RandomAccessFile(new File(this.indexPath + INDEX_FILE_NAME), "r");
    }

    /**
     * Returns a <code>TextDocument</code> corresponding to id passed or null if
     * such document does not exist in index.
     *
     * @param docId document numeric ID to look for
     * @return TextDocument
     */
    public TextDocument getDocument(int docId) {
        String docname = docsmap.get(docId);
        if (docname == null) {
            return null;
        }
        return new TextDocument(docId, docname);
    }

    /**
     * Returns the module of the document corresponding to the id passed as
     * argument
     *
     * @param docId numeric id of the document to retrieve it's module.
     * @return the module of the document corresponding to the id passed as
     * argument.
     */
    public double getDocModule(int docId) {
        return Math.sqrt(docMod[docId]);
    }

    /**
     * Returns a list of terms in the index.
     *
     * @throws java.io.IOException
     */
    public List<String> getTerms() throws IOException {
        List<String> terms = new ArrayList<>();
        IndexEntry ie;
        raf.seek(0);

        while ((ie = IndexEntry.readEntry(raf)) != null) {
            terms.add(ie.getTerm());
        }
        return terms;
    }

    /**
     * Seeks for a term in the index and returns a list of its postings.
     *
     * @param term term to seek in the index.
     * @return List of Postings associated to the term if it exists in the
     * index.
     * @throws java.io.IOException
     */
    public List<Posting> getTermPostings(String term) throws IOException {
        Entry<String, Integer> lowerBound = termsoffset.floorEntry(term);
        if (lowerBound == null) //term string is less than the first entry in the map.
        {
            return null;
        }
        //find if term is after last read term but in the same block. In that
        //case, there is no need to move the reading pointer in the file.
        if ((lastRead.compareTo(lowerBound.getKey()) < 0) || (lastRead.compareTo(term) >= 0))
        {
            raf.seek(lowerBound.getValue());
            lastReadOffset = 0;
        }

        for (int i = lastReadOffset; i < TERM_MAP_SIZE; i++, lastReadOffset++) {
            IndexEntry ie = IndexEntry.readEntry(raf);
            if (ie == null) //end of file
            {
                break;
            }
            if (term.compareTo(ie.getTerm()) == 0) {
                lastRead = ie.getTerm();
                lastReadOffset++;
                return Posting.listFromBytes(term, ie.getRawPostingsData());
            }
        }
        lastRead = "";
        lastReadOffset=0;
        return null;
    }

    /**
     *
     * @return
     */
    List<Integer> getDocIds() {
        return new ArrayList<Integer>(docsmap.keySet());
    }
}
