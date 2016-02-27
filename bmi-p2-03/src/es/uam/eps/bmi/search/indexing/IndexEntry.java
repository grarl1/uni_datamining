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
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * IndexEntry class.
 * Represents the data structure that is written into index files for each term.
 * Such data has the following format:</br>
 * termString,delimiter,#postingsSize,docid,#postings1,position1,..positionN,docid2,#postings2,position1,...,positionM,...
 * written as binary to file, so each number after termString is a long.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class IndexEntry {
    
    /* delimiter for the end of the term string in byte array read from disc */
    public static final char delimiter = ' ';

    /* String representing the term */
    private String term;
    /* size in bytes of the data structure written for Postings */
    private long postingsSize;
    /* portion of the Entry that holds the list of postings for each docid */
    private byte[] rawPostingsData;
    
    /**
     * Default constructor.
     * @param term term string
     * @param postingsSize size of rawPostingsData
     * @param rawPostingsData raw data for postings, represents a stream of bytes </br>
     *              with following format: docid1,#postings1,position1,..positionN,docid2,#postings2,...
     */
    public IndexEntry(String term, long postingsSize, byte[] rawPostingsData){
        this.term = term;
        this.postingsSize = postingsSize;
        this.rawPostingsData = rawPostingsData;
    };

    /**
     * Returns the associated term.
     *
     * @return the associated term.
     */
    public String getTerm() {
        return term;
    }

    /**
     * Returns the postingsSize.
     *
     * @return the postingsSize.
     */
    public long getPostingsSize() {
        return postingsSize;
    }

    /**
     * Returns the byte array containing postings.
     *
     * @return the byte array containing postings.
     */
    public byte[] getRawPostingsData() {
        return rawPostingsData;
    }
    
    
    
    /**
     * Reads a IndexEntry from file. 
     * Assumes the cursor on DataInputStream is pointing to the first char of the term
     * and reads the format specified in class description. String term in file can't 
     * contain spaces as the class will assume a space as an end for the term string.
     * @param dis input stream to read from.
     * @return IndexEntry with term read.
     */
    public static IndexEntry readEntry(DataInputStream dis) throws IOException {
        String term = "";
        try {
            char read;
            while ( (read = dis.readChar()) != delimiter ) {
                term += Character.toString(read);
            }
            //read size of postings list
            long postingsSize = dis.readLong();
            byte[] postingList = new byte[(int)postingsSize];
            int amountRead = dis.read(postingList,0,(int)postingsSize);
            if (amountRead != (int)postingsSize) 
            {
                throw new IOException();
            }
            return new IndexEntry(term, postingsSize, postingList);
        }
        catch (EOFException ex) {
            return null;
        }
        catch (IOException ex) {
            throw new IOException("Error getting entry from index file. File might be corrputed");
        }
        
    }
    
    /**
     * Merges two IndexEntries into a single one concatenating respective postings
     * in the same order as the arguments passed. 
     * Assumes that string term is the same on both entries.
     * 
     * @param e1 first entry
     * @param e2 second entry
     * @return IndexEntry resulting from merging entries passed.
     */
    public static IndexEntry mergeEntries(IndexEntry e1, IndexEntry e2) {
        if ((e1 == null) && (e2 == null)) return null;
        if (e1 == null) return e2;
        if (e2 == null) return e1;
        
        long newSize = e1.getPostingsSize()+e2.getPostingsSize();
        byte[] newPosting = new byte[(int)newSize];
        System.arraycopy(e1.getRawPostingsData(), 0, newPosting, 0, (int)e1.getPostingsSize());
        System.arraycopy(e2.getRawPostingsData(), 0, newPosting, (int)e1.getPostingsSize(), (int)e2.getPostingsSize());
        return new IndexEntry(e1.getTerm(), newSize, newPosting);
    }
    
}
