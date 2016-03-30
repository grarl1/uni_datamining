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

import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;

/**
 * Priority min-heap with limited size.
 *
 * @param <T> Type of the elements inside the min-heap.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class MinHeap<T extends Comparable> {

    /* Attributes */
    private final PriorityQueue<T> minHeap;
    private final int maxSize;

    /**
     * Default constructor.
     *
     * @param size maximum size.
     */
    public MinHeap(int size) {
        // Build a min-heap
        this.minHeap = new PriorityQueue<>(size);
        this.maxSize = size;
    }

    /**
     * Inserts the specified element into this priority queue. The element will
     * not be inserted if it is less than the top element.
     *
     * @param element The element to add.
     * @return True if the element has been added, false otherwise.
     */
    public boolean add(T element) {
        // Insert if the maximum size has not been reached.
        if (this.minHeap.size() < this.maxSize) {
            this.minHeap.add(element);
            return true;
        }

        // The maximum size has been reached.
        if (this.minHeap.peek().compareTo(element) < 0) {
            // Remove the minimum element.
            this.minHeap.poll();
            this.minHeap.add(element);
            return true;
        }

        return false;
    }

    /**
     * Retrieves and removes the head of this queue, or returns null if this
     * queue is empty.
     *
     * @return the head of this queue, or null if this queue is empty.
     */
    public T poll() {
        return this.minHeap.poll();
    }

    /**
     * Retrieves, but does not remove, the head of this queue, or returns null
     * if this queue is empty.
     *
     * @return the head of this queue, or null if this queue is empty.
     *
     */
    public T peek() {
        return this.minHeap.peek();
    }

    /**
     * Returns the number of elements in this collection.
     *
     * @return the number of elements in this collection
     *
     */
    public int size() {
        return this.minHeap.size();
    }

    /**
     * Returns the maximum number of elements in this collections.
     *
     * @return the maximum number of elements in this collections.
     */
    public int maxSize() {
        return maxSize;
    }

    /**
     * Removes all of the elements from this priority queue. The queue will be
     * empty after this call returns.
     *
     */
    public void clear() {
        this.minHeap.clear();
    }

    /**
     * Returns the elements of this collections as a sorted list.
     *
     * @return the elements of this collections as a sorted list.
     */
    public List<T> asList() {
        return new ArrayList<>(this.minHeap);
    }
}
