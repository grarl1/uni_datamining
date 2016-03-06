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
package es.uam.eps.bmi.search;

/**
 * Class representation of a basic document structure.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class TextDocument {

    /*Id of the document*/
    private final int id;

    /*Absolute path of the document*/
    private final String name;

    /**
     * Default constructor for <code>TextDocument</code> class.
     *
     * @param id Unique ID for the text document.
     * @param name Text document absolute path.
     */
    public TextDocument(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Returns the id of the text document.
     *
     * @return the id of the text document.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the name of the text document.
     *
     * @return the name of the text document.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        return id + name.hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one by comparing
     * their ids.
     *
     * @param obj to compare to.
     * @return true if both objects have the same Id.
     */
    @Override
    public boolean equals(Object obj) {
        /*Returns true only if the given object's class is TextDocument and its
        id is the same as this one's.*/
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TextDocument textDocument = (TextDocument) obj;
        return id == textDocument.getId();
    }
}
