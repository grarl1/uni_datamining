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

import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import es.uam.eps.bmi.search.ranking.graph.RankedDocument;
import es.uam.eps.bmi.util.MinHeap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

/**
 * Loads a graph and prints some output stats.
 *
 * @author Guillermo Ruiz Álvarez
 * @author Enrique Cabrerizo Fernández
 */
public class SocialGraph {

    /*teleport value for pagerank*/
    private static double PAGERANK_R = 0.15;
    /*Default number of iterations*/
    private static int PAGERANK_ITER = 50;

    /*graph loaded*/
    protected Graph<String, Integer> g;
    /*if graph is directed*/
    protected boolean isDirected = false;
    /*path to graph file*/
    private String filePath = null;
    /*number of edges in graph*/
    protected int nEdges = 0;
    /*number of vertex in graph*/
    protected int nVertex = 0;
    /*number of local bridges in graph, related to embeddedness of edges*/
    protected int nEmbedLocalBridges = 0;
    /*pageRank*/
    private PageRank<String, Integer> pr = null;
    /*clustering coefficients*/
    private Map<String, Double> clustering = null;
    /*embeddeddness values for each edge*/
    private Map<Integer, Double> embeddedness = null;
    /*betweenness of the graph*/
    private BetweennessCentrality<String, Integer> betweenness = null;

    /*clustering avg*/
    private Double cavg = 0.0;
    /*assortativity*/
    private Double assortativity = 0.0;

    /**
     * Empty constructor for inheritance.
     */
    protected SocialGraph() {
    }

    /**
     * Constructor, currently only supports loading graph from file.
     *
     * @param filePath path to csv file with edges and vertices
     * @param isDirected true if graph is directed.
     * @throws IOException
     */
    public SocialGraph(String filePath, boolean isDirected) throws IOException {
        this.isDirected = isDirected;
        this.filePath = filePath;
        if (!load()) {
            throw new IOException("Error loading graph from file.");
        }
    }

    /**
     * getter for isDirected
     *
     * @return true if graph is directed, false otherwise.
     */
    public boolean isDirected() {
        return isDirected;
    }

    /**
     * getter for nEdges
     *
     * @return number of edges in graph
     */
    public int getnEdges() {
        return nEdges;
    }

    /**
     * getter for nVertex
     *
     * @return number of vertices
     */
    public int getnVertex() {
        return nVertex;
    }

    /**
     * loads a graph from a csv file.
     *
     * @param filePath path to file to load graph from.
     * @param directed if the graph is directed.
     */
    private boolean load() {
        EdgeType e;
        if (isDirected) {
            g = new DirectedSparseGraph();
            e = EdgeType.DIRECTED;
        } else {
            g = new UndirectedSparseGraph();
            e = EdgeType.UNDIRECTED;
        }
        /* Open and start reading linkFile line by line*/
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                String vFrom = split[0];
                if (!g.containsVertex(vFrom)) {
                    g.addVertex(vFrom);
                    nVertex++;
                }
                for (int i = 1; i < split.length; i++) {
                    if (!g.containsVertex(split[i])) {
                        g.addVertex(split[i]);
                        nVertex++;
                    }
                    g.addEdge(++nEdges, vFrom, split[i], e);
                }
            }
            br.close();
        } catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
            return false;
        }
        return true;

    }

    /**
     * Retrieves a list of in Degrees for each node in the graph. The order of
     * the nodes is the order given by Graph.getVertices
     *
     * @return A list of node input degrees.
     */
    public List<Integer> getInDegreesList() {
        ArrayList<Integer> degrees = new ArrayList();
        for (String node : g.getVertices()) {
            degrees.add(g.inDegree(node));
        }
        return degrees;
    }

    /**
     * Returns topN of vertex sorted by page rank value. If topN = 0, returns
     * all vertices, sorted by pageRank value.
     *
     * @param topN number of top vertices to retrieve.
     * @param maxIter max amount of iterations for pageRank.
     * @return list of top N vertices with score
     */
    public List<RankedDocument> pageRank(int topN, int maxIter) {
        int nRank = topN;
        if (pr == null) { //if not already computed
            computePageRank(maxIter);
        }

        if ((topN == 0) || topN > g.getVertexCount()) {
            nRank = g.getVertexCount();
        }
        MinHeap<RankedDocument> minHeap = new MinHeap<>(nRank);
        int i = 0; //random numeric id
        for (String s : g.getVertices()) {
            RankedDocument doc = new RankedDocument(i, pr.getVertexScore(s), s);
            minHeap.add(doc);
            i++;
        }
        return minHeap.asList();
    }

    /**
     * Retrieves pagerank score for each node in graph, the order of the nodes
     * in list is the same order given by getInDegreesList.
     *
     * @return
     */
    public List<Double> getPagerankList() {
        ArrayList<Double> betList = new ArrayList();
        if (pr == null) { //if not already computed.
            computePageRank(PAGERANK_ITER);
        }
        for (String node : g.getVertices()) {
            betList.add(pr.getVertexScore(node));
        }
        return betList;
    }

    /**
     * computes PageRank for current graph
     *
     * @param maxIter max number of iterations to perform.
     */
    private void computePageRank(int maxIter) {
        pr = new PageRank(g, PAGERANK_R);
        pr.setMaxIterations(maxIter);
        pr.evaluate();
    }

    /**
     * Returns clustering coefficient for the topN nodes of the graph. If topN =
     * 0, returns all vertices, sorted by pageRank value.
     */
    public List<RankedDocument> clustering(int topN) {
        int nCoef = topN;
        if (clustering == null) {//if not already computed
            computeClustering();
        }
        if ((topN == 0) || topN > g.getVertexCount()) {
            nCoef = g.getVertexCount();
        }
        MinHeap<RankedDocument> minHeap = new MinHeap<>(nCoef);
        int i = 0; //random numeric id
        for (Entry<String, Double> e : clustering.entrySet()) {
            RankedDocument doc = new RankedDocument(i, e.getValue(), e.getKey());
            minHeap.add(doc);
            i++;
        }
        return minHeap.asList();
    }

    /**
     * Returns clustering average, calculating clustering for each node if not
     * already calculated. The average is just calculated as the mean of each
     * clustering value for each node.
     *
     * @return graph clustering value.
     */
    public Double clusteringAvg() {
        if (clustering == null) {//if not already computed
            computeClustering();
        }
        return cavg;
    }

    /**
     * Computes clustering coefficient calculations
     */
    private void computeClustering() {
        Double clustering_num = 0.0;
        Double clustering_denom = 0.0;
        Double cavg_num = 0.0;
        Double cavg_denom = 0.0;
        Double factor = 1.0;
        if (!isDirected) {
            factor = 0.5;
        }
        //TODO remove 2 lines below
        //clustering = Metrics.clusteringCoefficients(g);
        //return;
        clustering = new HashMap<>();
        for (String vertex : g.getVertices()) {
            clustering_denom = 0.0;
            clustering_num = 0.0;
            TreeSet<String> vertexNeighbors = null;
            if (isDirected) {
                vertexNeighbors = new TreeSet<>(g.getSuccessors(vertex));
            } else {
                vertexNeighbors = new TreeSet<>(g.getNeighbors(vertex));
            }
            for (String neighbor : vertexNeighbors) {
                TreeSet<String> v2Neighbors = null;
                if (isDirected) {
                    v2Neighbors = new TreeSet<>(g.getSuccessors(neighbor));
                } else {
                    v2Neighbors = new TreeSet<>(g.getNeighbors(neighbor));
                }
                v2Neighbors.retainAll(vertexNeighbors); //keep only common neighbors
                v2Neighbors.retainAll(vertexNeighbors);
                clustering_num += v2Neighbors.size() * factor;
            }
            clustering_denom = vertexNeighbors.size() * (vertexNeighbors.size() - 1) * factor;
            cavg_num += clustering_num;
            cavg_denom += clustering_denom;
            Double clustCoef = 0.0;
            if (clustering_denom == 0.0) {
                clustCoef = 0.0;
            } else {
                clustCoef = clustering_num / clustering_denom;
            }
            clustering.put(vertex, clustCoef);
        }
        if (cavg_denom == 0) {
            cavg = 0.0;
        } else {
            cavg = cavg_num / cavg_denom;
        }
    }

    /**
     * Returns topN of edges sorted by embeddedness value. If topN = 0, returns
     * all edges, sorted by embeddedness value.
     *
     * @param topN number of top edges to retrieve.
     * @return list of top N edges with score
     */
    public List<RankedDocument> embeddedness(int topN) {
        int nRank = topN;
        if (embeddedness == null) { //if not already computed
            computeEmbededdness();
        }

        if ((topN == 0) || (topN > g.getEdgeCount())) {
            nRank = g.getEdgeCount();
        }
        MinHeap<RankedDocument> minHeap = new MinHeap<>(nRank);
        for (Integer edge : g.getEdges()) {
            RankedDocument doc = new RankedDocument(edge, embeddedness.get(edge), Integer.toString(edge));
            minHeap.add(doc);
        }
        return minHeap.asList();
    }

    /**
     * Computes Embeddeddness for each edge in graph.
     */
    private void computeEmbededdness() {
        embeddedness = new HashMap();
        nEmbedLocalBridges = 0; //edges with embeddedness 0 are local bridges
        for (Integer i : g.getEdges()) {
            Pair<String> vertexNames = g.getEndpoints(i);
            TreeSet<String> v1 = new TreeSet(g.getNeighbors(vertexNames.getFirst()));
            TreeSet<String> v2 = new TreeSet(g.getNeighbors(vertexNames.getSecond()));
            v1.remove(vertexNames.getSecond()); //remove v2 as neighbour of v1
            v2.remove(vertexNames.getFirst()); //remove v1 as neighbour of v2
            Double v1AmountNeighbours = (double) v1.size();
            Double v2AmountNeighbours = (double) v2.size();
            v1.retainAll(v2);
            Double v1v2Intersect = (double) v1.size();
            Double jaccard = v1v2Intersect / (v1AmountNeighbours + v2AmountNeighbours - v1v2Intersect);
            if (Double.isNaN(jaccard)) { //if denominator of above is 0, then only neighbor of v2 is v1 and viceversa.
                jaccard = 1.0;
            }
            embeddedness.put(i, jaccard);
            if (jaccard == 0) {
                nEmbedLocalBridges++;
            }
        }
    }

    /**
     * Returns the number of local bridges in graph, considering a local bridge
     * every edge with embededdness 0.
     *
     * @return number of local bridges in map.
     */
    public int getLocalBridges() {
        if (embeddedness == null) {
            computeEmbededdness();
        }
        return nEmbedLocalBridges;
    }

    /**
     * Given an edge id, returns the name of the 2 vertices it is connected to.
     *
     * @param edgeID number of edge
     * @return pair of vertices connected to edge given
     */
    public Pair<String> getEdgeVertices(Integer edgeID) {
        if (g == null) {
            return null;
        }
        return g.getEndpoints(edgeID);
    }

    /**
     * Retrieves betweenness for each node in graph, the order of the nodes in
     * list is the same order given by getInDegreesList.
     *
     * @return
     */
    public List<Double> getBetweennessList() {
        ArrayList<Double> betList = new ArrayList();
        if (betweenness == null) { //if not already computed.
            computeBetweenness();
        }
        for (String node : g.getVertices()) {
            betList.add(betweenness.getVertexRankScore(node));
        }
        return betList;
    }

    /**
     * Computes betweenness for the graph
     */
    private void computeBetweenness() {
        betweenness = new BetweennessCentrality(g, true, false);
        betweenness.setRemoveRankScoresOnFinalize(false);
        betweenness.evaluate();
    }

    /**
     * returns a list of doubles where the element in position i represents the
     * number of nodes with degree i. If normalize is true, each value is
     * divided by the amount of nodes in the graph.
     *
     * @param normalize normalizes the distribution or returns raw values.
     * @return distribution as a list.
     */
    public List<Double> degreeDistribution(boolean normalize) {
        int maxDegree = 0;
        Collection<String> vertices = g.getVertices();
        Double[] distribution = new Double[vertices.size() + 1];
        for (int i = 0; i < distribution.length; i++) {
            distribution[i] = 0.0;
        }

        for (String s : vertices) {
            int deg = g.degree(s);
            distribution[deg]++;
            if (deg > maxDegree) {
                maxDegree = deg;
            }
        }
        if (normalize) {
            Double norm = (double) vertices.size();
            for (int i = 0; i < vertices.size(); i++) {
                distribution[i] /= norm;
            }
        }
        /*Remove every 0 entry from last non 0 entry.*/
        return (Arrays.asList(distribution)).subList(0, maxDegree + 1);
    }

    /**
     * Returns the proportion of nodes that fulfill the friendship paradox, thus
     * meaning that the average number of his friends is greater than his own
     * number of friends.
     *
     * @return
     */
    public double friendshipParadox() {
        int paradox_fulfilled = 0;
        //compute the number of friends for each 
        HashMap<String, Integer> friendMap = new HashMap();
        if (!isDirected) {
            for (String s : g.getVertices()) {
                friendMap.put(s, g.getNeighborCount(s));
            }
        }
        else {
            for (String s : g.getVertices()) {
                friendMap.put(s, g.getPredecessorCount(s));
            }
        }
        for (String me : g.getVertices()) {
            int nFriendFriends = 0;
            for (String friend : g.getNeighbors(me)) {
                nFriendFriends += friendMap.get(friend);
            }
            if (nFriendFriends / ((double) friendMap.get(me)) > (double) friendMap.get(me)) {
                paradox_fulfilled++;
            }
        }
        return paradox_fulfilled / ((double) nVertex);
    }
    
    /**
     * 
     */
    public Double assortativity() {
        HashMap<String, Integer> gradeMap = new HashMap();
        Double factor = 1.0;
        Integer m = g.getEdgeCount();
        if (!isDirected) { //not directed graph
            factor = 0.5;
        }
        for (String vertex : g.getVertices()) {
            if (!isDirected) { //not directed graph
                gradeMap.put(vertex, g.getNeighborCount(vertex));
            }
            else {
                gradeMap.put(vertex, g.getSuccessorCount(vertex));
            }
        }
        
        Double squaredGradeSum = 0.0;
        Double cubedGradeSum = 0.0;
        Double gradeProductSum = 0.0;
        for (String vertex : g.getVertices()) {
            
            squaredGradeSum += Math.pow(gradeMap.get(vertex), 2);
            cubedGradeSum += Math.pow(gradeMap.get(vertex), 3);
            
            Collection<String> neighbours;
            if (!isDirected) {
                neighbours = g.getNeighbors(vertex);
            }
            else {
                neighbours = g.getSuccessors(vertex);
            }
            for (String neighbour : neighbours) {
                gradeProductSum += factor * gradeMap.get(vertex) * gradeMap.get(neighbour);
            }
        }
        assortativity = (4*m*gradeProductSum - Math.pow(squaredGradeSum, 2)) / 
                        (2*m*cubedGradeSum - Math.pow(squaredGradeSum, 2));
        return assortativity;
    }
}
