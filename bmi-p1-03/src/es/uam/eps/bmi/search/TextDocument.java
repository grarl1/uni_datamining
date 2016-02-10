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
    private String id;

    /*Absolute path of the document*/
    private String name;

    public String getId() {
        return id;
    }

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
        return id.hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one by comparing
     * their Ids.
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
        final TextDocument textDocument = (TextDocument) obj;
        return id.equals(textDocument.id);
    }
}
