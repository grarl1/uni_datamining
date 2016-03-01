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
import java.util.List;

/**
 * Basic index class.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class BasicIndex implements Index {

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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Stores (partially or completely) a previously created index in memory.
     *
     * @param indexPath Path to the directory where the index is stored.
     */
    @Override
    public void load(String indexPath) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns the path where the index is stored.
     *
     * @return the path where the index is stored.
     */
    @Override
    public String getPath() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns a list of the IDs of indexed documents.
     *
     * @return a list of the IDs of indexed documents.
     */
    @Override
    public List<String> getDocIds() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns a document given its Id.
     *
     * @param docId Id of the document to retrieve.
     * @return a <code>TextDocument</code> instance matching the given Id.
     */
    @Override
    public TextDocument getDocument(String docId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns the list of terms extracted from the indexed documents.
     *
     * @return the list of terms extracted from the indexed documents.
     */
    @Override
    public List<String> getTerms() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns a list of postings of the given term.
     *
     * @param term Given term used to get the list of postings.
     * @return a list of the postings of the given term.
     */
    @Override
    public List<Posting> getTermPostings(String term) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns a true if the index is loaded, false otherwise.
     *
     * @return true if index is loaded, false otherwise.
     */
    @Override
    public boolean isLoaded() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns the module of the document with the given doc ID.
     *
     * @param docID ID of the document whose module will be returned.
     * @return the module of the document with the given doc ID.
     */
    @Override
    public double getDocModule(int docID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
