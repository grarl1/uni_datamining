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
package es.uam.eps.bmi.recommendation.data;

import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

/**
 * Class representing the data in a table of contents.
 *
 * @author Guillermo Ruiz Álvarez
 * @author Enrique Cabrerizo Fernández
 */
public abstract class ContentData {

    /**
     * Load the data from file.
     *
     * @param dataPath Path to the file containing the data.
     * @param ratedMoviesSet Set of rated movies.
     * @throws java.io.IOException
     */
    public abstract void load(String dataPath, Set<Integer> ratedMoviesSet) throws IOException;

    /**
     * Returns the array of identifiers of rows.
     *
     * @return the array of identifiers of rows.
     */
    public abstract Integer[] getRowIDs();

    /**
     * Returns the array of identifiers of columns
     *
     * @return the array of identifiers of columns
     */
    public abstract Integer[] getColumnIDs();

    /**
     * Returns the map of indexes of rows.
     *
     * @return the map of indexes of rows.
     */
    public abstract HashMap<Integer, Integer> getRowIndexesMap();

    /**
     * Returns the map of indexes of columns.
     *
     * @return the map of indexes of column.
     */
    public abstract HashMap<Integer, Integer> getColumnIndexesMap();

    /**
     * Returns the matrix of data.
     *
     * @return the matrix of data.
     */
    public abstract SparseDoubleMatrix2D getData();
}
