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
package es.uam.eps.bmi.util;

/**
 * Class representing a pair of data.
 *
 * @author Guillermo Ruiz Álvarez
 * @author Enrique Cabrerizo Fernández
 * @param <X> Type of the first datum.
 * @param <Y> Type of the second datum
 */
public class Pair<X,Y> {
    
    /* Attributes */
    private final X x;
    private final Y y;

    /**
     * Default constructor.
     * @param x First datum.
     * @param y Second datum.
     */
    public Pair(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    /* Getters */
    public X getX() {
        return x;
    }

    public Y getY() {
        return y;
    }
}
