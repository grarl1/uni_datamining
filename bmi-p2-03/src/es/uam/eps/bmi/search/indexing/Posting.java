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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Posting basic structure used by an index.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class Posting {

    /*Associated term*/
    private final String term;

    /*Associated document id*/
    private final long docID;

    /*Position of the term within the document*/
    private List<Long> termPositions;

    /*Amount of times the term appears in the document*/
    private int termFrequency;
    
    /**
     * Default constructor for <code>TextDocument</code> class.
     *
     * @param term term associated to the posting.
     * @param docID ID of the document.
     * @param termPositions Array of term positions.
     */
    public Posting(String term, long docID, List<Long> termPositions) {
        this.term = term;
        this.docID = docID;
        this.termPositions = termPositions;
        this.termFrequency = this.termPositions.size();
    }
    
    /**
     * Returns the associated term.
     *
     * @return the associated term.
     */
    public String getTerm() {
        return term;
    }

    /**
     * Returns the associated document id.
     *
     * @return the associated document id.
     */
    public long getDocID() {
        return docID;
    }

    /**
     * Returns a list containing the term position within the associated
     * document.
     *
     * @return a list containing the term position within the associated
     * document.
     */
    public List<Long> getTermPositions() {
        return termPositions;
    }
    
    /**
     * Returns the amount of times the term appears in the document.
     *
     * @return the amount of times the term appears in the document.
     */
    public int getTermFrequency() {
        return termFrequency;
    }
    
    /**
     * Adds a new position to the posting
     * 
     * @param pos position to add
     */
     public void addPosition(long pos) {
         this.termPositions.add(pos);
         this.termFrequency++;
     }
     
     /**
      * Returns an array of bytes with postings information as follows: </br>
      * #docID,#positions,position1,position2,...,positionN
      * 
      * @throws java.io.IOException
      * @return array of bytes with format explained
      */
     public byte[] positionsToBytes() throws IOException {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         DataOutputStream dos = new DataOutputStream(baos);
         dos.writeLong(docID);
         dos.writeLong(termFrequency);
         for (long l: termPositions){
             dos.writeLong(l);
         }
         dos.flush();
         dos.close();
         return baos.toByteArray();
     }
     
     /**
      * Returns the size of the array returned by positionsToBytes
      * @return the size of the array returned by positionsToBytes
      */
     public long positionsToBytesSize() {
         return (termFrequency + 2)*Long.BYTES;
     }
     
     /**
      * Receives a byte array with consecutive packages formated as the output
      * of <code>positionToBytes</code> and builds a List of Postings from it.
      * @param term term String, every posting in the list will be associated to 
      *             this term.
      * @param array array containing postings.
      * @return a List of Postings recovered from array.
      */
     public static List<Posting> listFromBytes(String term, byte[] array) {
        List<Posting> lp = new ArrayList<>();
        
        if ((array.length % Long.BYTES) != 0) { //array is malformed
            return null;
        }
        LongBuffer lb = ByteBuffer.wrap(array).asLongBuffer();
        for (int i = 0; i<lb.limit(); ) {
            long docid = lb.get();
            long postingsSize = lb.get();
            i+=2;
            Posting p = new Posting(term, docid, new ArrayList());
            for (int j=0; j<postingsSize; j++) {
                p.addPosition(lb.get());
                i++;
            }
        }
        return lp;
     }
}
