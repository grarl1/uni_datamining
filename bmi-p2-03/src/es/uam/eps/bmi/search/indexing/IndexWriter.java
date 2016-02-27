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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * IndexWriter class.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class IndexWriter {
    
    /* Attributes */
    /* default block size to keep in RAM */
    private static final long BLOCK_DEFAULT = 128 * 1024 * 1024; //128MB
    /* when block size is greater than this value, a block will be written to disc */
    private long maxBlockSize;
    /* maximum amount of RAM to use before writing to disc */
    private long currentBlockSize;
    /* Indicates the number of terms gap in termMapFile so that
        only 1 of every termMapSize terms will be written to termMapFile file */
    private static final int TERM_MAP_SIZE = 100;
    
    /* Incremental value every time a new document is added */
    private long currentDocId = 0;
    /* Incremental value every time a new block is written to disc */
    private long currentBlockId = 0;
    /* Doc modules id, position i of array will correspond to document with docid i */
    double[] docMod = null;
    
    
    /* Name of temporary files to store index */
    private static final String TMP_FILE_FORMAT = "%d_%d_index.tmp";
    /* Name of the file to store finished index */
    private static final String INDEX_FILE_NAME = "index";
    /* Name of the file which contains numeric docId and real name of documents 
        in the index*/
    private static final String DOCMAP_FILE_NAME  = "docids";
    /* Name of the file which contains the list of terms and its offset in the
        final index*/
    private static final String TERMMAP_FILE_NAME = "termsoffset";
    /* Name of the file which contains the list of terms and its offset in the
        final index*/
    private static final String DOC_MODULES_FILE_NAME = "modules";
    /* Path to save index */
    private String indexPath;
    
    /* TreeMap to store document names and given numeric id */
    private TreeMap<Long, String> docsmap;
    /* TreeMap to store current block dictionary */
    private TreeMap<String, List<Posting>> termmap;
    /* Map containing offsets of terms in final index */
    private TreeMap<String, Long> termsoffset;
    
    /* Checks if index is already merged so no new files can be added */
    private boolean closed = false;
            
    /**
     * Default constructor for <code>IndexWriter</code> class.
     *
     * @param indexPath path to save index to.
     */
    public IndexWriter(String indexPath) {
        this(indexPath, BLOCK_DEFAULT);
    }
    
    /**
     * Constructor for <code>IndexWriter</code> class.
     *
     * @param indexPath path to save index to.
     * @param maxBlockSize maximum block size.
     */
    public IndexWriter(String indexPath, long maxBlockSize) {
        this.indexPath = indexPath;
        this.maxBlockSize = maxBlockSize;
        this.docsmap = new TreeMap<>();
        this.termmap = new TreeMap<>();
        this.termsoffset = new TreeMap<>();
        if (!indexPath.endsWith("/"))
        {
            this.indexPath += "/";
        }
    }
    
    /**
     * Adds document passed to the index.</br>
     * Assumes that every term in content is separated by spaces.
     * 
     * @param docName name of the document.
     * @param content string with content of document.
     */
    public void add (String docName, String content) throws IOException {
        long termPosition = 0;
        for (String term: basicFilter(content).split("\\s+"))
        {
            List<Posting> lp;
            if (termmap.containsKey(term)) 
            { //get term list of postings and add this position
                lp = termmap.get(term);
                Posting lastposting = lp.get(lp.size() - 1); //get last postings and check docid
                if (lastposting.getDocID() == currentDocId)
                { //a list of postings for this document already exists, add position.
                    lastposting.addPosition(termPosition);
                    currentBlockSize += Long.BYTES;
                }
                else 
                { // create a new posting and add it to the list
                    Posting p = new Posting(term, currentDocId, new ArrayList<>());
                    p.addPosition(termPosition);
                    lp.add(p);
                    currentBlockSize += p.positionsToBytesSize();
                }
            }
            else 
            { //create a new entry in Dictionary and add a new posting for the term.
                Posting p = new Posting(term, currentDocId, new ArrayList<>());
                p.addPosition(termPosition);
                lp = new ArrayList<>();
                lp.add(p);
                currentBlockSize += ((term + Character.toString(IndexEntry.delimiter)).length())*Character.BYTES 
                        + Long.BYTES + (1 + p.positionsToBytesSize());
            }
            termmap.put(term, lp); 
            termPosition++;
        }
        
        if (currentBlockSize >= maxBlockSize) {
            writeBlock();
        }
        docsmap.put(currentDocId, docName);
        currentDocId++;
    }
    
    /**
     * Finishes the creation of the index.
     * This method must be called after last document is added so the index
     * can be closed and every temp file merged correctly.
     */
    public void close() throws IOException {
        if (currentBlockSize > 0) {
            writeBlock();
        }
        long remainingFiles = currentBlockId;
        int j=0;
        while (remainingFiles > 2) 
        { //merge files until only 2 are left
            for (int i=0 ; i < remainingFiles ; i+=2) {
                String name_f1 = indexPath + String.format(TMP_FILE_FORMAT, j, i);
                String name_f2 = indexPath + String.format(TMP_FILE_FORMAT, j, i+1);
                String name_out = indexPath + String.format(TMP_FILE_FORMAT, j+1, (i+1)/2);
                if ((remainingFiles - i) == 1) //odd number of files
                {
                    File f1 = new File(name_f1);
                    merge(f1, null, new File(name_out), false);
                    f1.delete();
                }
                else 
                {
                    File f1 = new File(name_f1);
                    File f2 = new File(name_f2);
                    merge(f1, f2, new File(name_out), false);
                    f1.delete();
                    f2.delete();
                }
            }
            j++;
            remainingFiles = remainingFiles/2 + remainingFiles%2;
        }
        //merge last two files updating document modules and termOffset
        String name_f1 = indexPath + String.format(TMP_FILE_FORMAT, j, 0);
        File f1 = new File(name_f1);
        String name_out = indexPath + INDEX_FILE_NAME;
        File f2;
        if (remainingFiles > 1) 
        {
            String name_f2 = indexPath + String.format(TMP_FILE_FORMAT, j, 1);
            f2 = new File(name_f2);
        }
        else {
            f2 = null;
        }
        merge(f1, f2, new File(name_out), true);
        f1.delete();
        if (f2 != null) 
        {
            f2.delete();
        }
        //TODO save maps to files
        closed = true;
    }

    /**
     * Merges files of the index.
     */
    private void merge (File src1, File src2, File dst, boolean lastMerge) throws IOException {
        int currentTermGap = TERM_MAP_SIZE; //ensures that the first term is added to map with offset 0
        long currentOffset = 0;
        long bytesWritten = 0;
        DataInputStream dis1=null ,dis2=null;
        DataOutputStream dos=null;
        IndexEntry e1=null, e2=null, entryOut=null;
        if ((src2 == null) && (lastMerge != true)) 
        { //odd number of files, just rename for next iteration and return
            src1.renameTo(dst);
            return;
        }
        dis1 = new DataInputStream(new FileInputStream(src1));
        dos = new DataOutputStream(new FileOutputStream(dst));
        e1 = IndexEntry.readEntry(dis1);
        if (src2 != null) {
            dis2 = new DataInputStream(new FileInputStream(src2));
            e2 = IndexEntry.readEntry(dis2);        
        }
        while (e1!=null && e2!=null) //compare terms read and write to the new file.
        {
            int comparison = e1.getTerm().compareTo(e2.getTerm());
            if (comparison < 0) //e2 term is greater than e1, write e1 to disc 
            {
                entryOut = e1;
                e1 = IndexEntry.readEntry(dis1);
            }
            else if (comparison > 0) //e1 term is greater than e2, write e2 to disc
            {
                entryOut = e2;
                e2 = IndexEntry.readEntry(dis2);
            }
            else 
            { //same term, concatenate postings
                entryOut = IndexEntry.mergeEntries(e1, e2);
                e1 = IndexEntry.readEntry(dis1);
                e2 = IndexEntry.readEntry(dis2);
            }
            dos.writeChars(entryOut.getTerm() + Character.toString(IndexEntry.delimiter));
            dos.writeLong(entryOut.getPostingsSize());
            dos.write(entryOut.getRawPostingsData());
            if (lastMerge) { //if merging last two files, update termOffset and document modules
                bytesWritten = (entryOut.getTerm() + Character.toString(IndexEntry.delimiter)).length()*Character.BYTES+
                                Long.BYTES + entryOut.getPostingsSize();
                if (currentTermGap == TERM_MAP_SIZE) { //save term to map
                    termsoffset.put(entryOut.getTerm(), currentOffset);
                    currentTermGap = 0;
                }
                currentOffset += bytesWritten;
                currentTermGap++;
                updateDocModules(Posting.listFromBytes(entryOut.getTerm(),entryOut.getRawPostingsData()));
            }
        }
        
        DataInputStream remainingStream = null;
        entryOut = null;
        if (e1 != null) 
        { //src2 file ended, copy rest of content from dis1 file to dos
            remainingStream = dis1;
            entryOut = e1;
        }
        else if (e2 != null) 
        { //src1 file ended, copy rest of content from dis2 to dos
            remainingStream = dis2;
            entryOut = e2;
        }
        while (entryOut != null) 
        {
            dos.writeChars(entryOut.getTerm() + Character.toString(IndexEntry.delimiter));
            dos.writeLong(entryOut.getPostingsSize());
            dos.write(entryOut.getRawPostingsData());
            if (lastMerge) { //if merging last two files, update termOffset and document modules
                bytesWritten = (entryOut.getTerm() + Character.toString(IndexEntry.delimiter)).length()*Character.BYTES+
                                Long.BYTES + entryOut.getPostingsSize();
                if (currentTermGap == TERM_MAP_SIZE) { //save term to map
                    termsoffset.put(entryOut.getTerm(), currentOffset);
                    currentTermGap = 0;
                }
                currentOffset += bytesWritten;
                currentTermGap++;
                updateDocModules(Posting.listFromBytes(entryOut.getTerm(),entryOut.getRawPostingsData()));
            }
            entryOut = IndexEntry.readEntry(remainingStream);
        }
        dis1.close();
        if (src2 != null) {
            dis2.close();
        }
        dos.flush();
        dos.close();
    }
    
    /**
     * Writes current block to a file.
     */
    private void writeBlock() throws IOException {
        String fileName = indexPath + String.format(TMP_FILE_FORMAT, 0, currentBlockId);
        File f = new File(fileName);
        
        //create path if this is the first block.
        if (currentBlockId == 0)
        {
            File parent = f.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
        }
        // for every term, write it followed by a ' ' and then, the size of the
        // postings list and the list itself.
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(f));
        for (String term : termmap.keySet()) {
            List<Posting> lp = termmap.get(term);
            long size = 0;
            for (Posting p: lp) 
            {
                size += p.positionsToBytesSize();
            }
            dos.writeChars(term + Character.toString(IndexEntry.delimiter));
            dos.writeLong(size);
            for (Posting p: lp)
            {
                dos.write(p.positionsToBytes());
            }
        }
        dos.flush();
        dos.close();
        currentBlockId++;
        currentBlockSize=0;
        termmap.clear();
    }

    /**
     * Calculates the module of documents as this function is called with every
     * Posting list in the index.
     * @param lp list of postings to update document modules with
     */
    private void updateDocModules(List<Posting> lp) {
        if (lp == null)
        {
            return;
        }
        if (docMod == null) {
            docMod = new double[(int)currentDocId];
        }
        
        long docNoAppearance = lp.size();
        for (Posting p: lp) {
            double tf = (1 + Math.log(p.getTermFrequency())/Math.log(2));
            double idf = (currentDocId*1.0) / docNoAppearance;
            docMod[(int)p.getDocID()] += Math.pow(tf, 2)*Math.pow(idf, 2);
        }
    }
    
    /**
     * Basic filtering, remove accents and symbols
     */
    private String basicFilter (String s) {
        String convertedString = Normalizer
                .normalize(s, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", " ");
        return convertedString;
    }
}
