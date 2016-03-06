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
 * Interface for index definition.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public interface Index {

    /**
     * Builds an index from a collection of text documents.
     *
     * @param inputCollectionPath Path to the directory containing the
     * collection of documents to be indexed.
     * @param outputIndexPath Path to the directory to store the indexes.
     * @param textParser Parser for document processing.
     */
    public void build(String inputCollectionPath, String outputIndexPath, TextParser textParser);

    /**
     * Stores (partially or completely) a previously created index in memory.
     *
     * @param indexPath Path to the directory where the index is stored.
     */
    public void load(String indexPath);

    /**
     * Returns the path where the index is stored.
     *
     * @return the path where the index is stored.
     */
    public String getPath();

    /**
     * Returns a list of the IDs of indexed documents.
     *
     * @return a list of the IDs of indexed documents.
     */
    public List<Integer> getDocIds();

    /**
     * Returns a document given its Id.
     *
     * @param docId Id of the document to retrieve.
     * @return a <code>TextDocument</code> instance matching the given Id.
     */
    public TextDocument getDocument(int docId);

    /**
     * Returns the list of terms extracted from the indexed documents.
     *
     * @return the list of terms extracted from the indexed documents.
     */
    public List<String> getTerms();

    /**
     * Returns a list of postings of the given term.
     *
     * @param term Given term used to get the list of postings.
     * @return a list of the postings of the given term.
     */
    public List<Posting> getTermPostings(String term);

    /**
     * Returns a true if the index is loaded, false otherwise.
     *
     * @return true if index is loaded, false otherwise.
     */
    public boolean isLoaded();

    /**
     * Returns the module of the document with the given doc ID.
     *
     * @param docID ID of the document whose module will be returned.
     * @return the module of the document with the given doc ID.
     */
    public double getDocModule(int docID);

}
