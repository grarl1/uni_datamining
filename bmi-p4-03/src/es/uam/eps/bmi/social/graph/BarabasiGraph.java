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
package es.uam.eps.bmi.social.graph;

import edu.uci.ics.jung.algorithms.generators.random.BarabasiAlbertGenerator;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections15.Factory;

/**
 * Loads a graph and prints some output stats.
 *
 * @author Guillermo Ruiz Álvarez
 * @author Enrique Cabrerizo Fernández
 */
public class BarabasiGraph extends SocialGraph {
    
    /**
     * Constructor for a random barabasiGraph.
     * @param edgesAttachment number of edges should have
     * @param maxVertices max number of vertices of graph
     */
    public BarabasiGraph(int edgesAttachment, int maxVertices) {
        this.isDirected = false;
        Factory<String> vertexFactory = new Factory<String>() {
                int count=1;
                @Override
                public String create() {
                    return Integer.toString(count++);
            }};
        Factory<Integer> edgeFactory = new Factory<Integer>() {
                int count=0;
                @Override
                public Integer create() {
                    return count++;
            }};
        
        UndirectedSparseGraph<String,Integer> ug = new UndirectedSparseGraph<>();
        Set<String> initVertex = new HashSet<String>();
        BarabasiAlbertGenerator<String, Integer> bag = new BarabasiAlbertGenerator(UndirectedSparseGraph.getFactory(), 
                vertexFactory, edgeFactory,edgesAttachment+1,edgesAttachment,initVertex);
        bag.evolveGraph(maxVertices-(edgesAttachment+1));
        g = bag.create();
        
        nVertex = g.getVertexCount();
        nEdges = g.getEdgeCount();
        isDirected = false;
    }
}
