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
package es.uam.eps.bmi.social;

import edu.uci.ics.jung.graph.util.Pair;
import es.uam.eps.bmi.search.ranking.graph.RankedDocument;
import es.uam.eps.bmi.social.graph.BarabasiGraph;
import es.uam.eps.bmi.social.graph.ErdosGraph;
import es.uam.eps.bmi.social.graph.SocialGraph;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Loads a graph and prints some output stats.
 *
 * @author Guillermo Ruiz Álvarez
 * @author Enrique Cabrerizo Fernández
 */
public class GraphTest {

    /*output directory*/
    public static final String OUTDIR = "./out/";
    /*suffix for distribution file*/
    public static final String DIST_SUFFIX = "_distribution.csv";
    /*suffix for scatterplots data file*/
    public static final String SCAT_SUFFIX = "_scatter.csv";
    
    /*Default number of edges for each new node in barabasi graph*/
    private static final Integer BARABASI_EDGES = 2;
    /*Number of nodes for random barabasi graph*/
    private static final Integer BARABASI_NODES = 2000;
    /*Number of nodes for random erdos graph*/
    private static final Integer ERDOS_NODES = 1000;
    /*Default probability in Erdos graph*/
    private static final Double ERDOS_P = 0.2;
    
    private static SocialGraph g = null;
    
    /**
     * Writes scatterplot data to a file
     * @param fileName name of file to write data
     * @param normalized if data output is normalized for betweenness
     * @throws IOException 
     */
    private static void writeScatter(String fileName, boolean normalized) throws IOException {
        
        Double norm_factor = 1.0;
        
        if (normalized) {
            Integer n = g.getnVertex();
            norm_factor = 1 / ((n-1.0)*(n-2.0));
            if (!g.isDirected()) {
                norm_factor*=2;
            }
        }
        
        Iterator<Integer> degree = g.getInDegreesList().iterator();
        Iterator<Double> pageRank = g.getPagerankList().iterator();
        Iterator<Double> betweenness = g.getBetweennessList().iterator();
        
        File f = new File(fileName);
        File parent = f.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(f), "utf-8"))) {
            while (degree.hasNext() && pageRank.hasNext() && betweenness.hasNext()) {
                writer.write(degree.next() + "," + pageRank.next() + "," + betweenness.next()*norm_factor + "\n");
            }
            
        }
    }
    
    /**
     * Writes distribution data to a file
     * @param fileName name of file to write data
     * @param normalized if distribution is normalized (sum of every value = 1)
     * @throws IOException 
     */
    private static void writeDistribution(String fileName, boolean normalized) throws IOException {
        
        Iterator<Double> dist = g.degreeDistribution(normalized).iterator();
        
        File f = new File(fileName);
        File parent = f.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(f), "utf-8"))) {
            int i = 0;
            while (dist.hasNext()){
                writer.write(i + "," + dist.next() + "\n");
                i++;
            }
            
        }
    }
    
    /**
     * Main method.
     *
     * @param args
     */
    public static void main(String args[]) throws IOException {
        String graphName = null;
        // Input control
        if (args.length < 3) {
            System.err.printf("Usage: %s csvFile directed graphName\n"
                    + "\tcsvFile: File containing vertices and edges.\n"
                    + "\tdirected: true if graph is directed, false otherwise.\n"
                    + "\tgraphName: Name of the graph, to print before each line and to write output files.\n" 
                    + "Output files generated in %s directory\n",
                    GraphTest.class.getSimpleName(), OUTDIR);
            return;
        }
        String csvFile = args[0];
        String directed = args[1];
        graphName = args[2] + "\t";
        if (args[0].compareToIgnoreCase("barabasi") == 0) { //barabasi random graph
            g = new BarabasiGraph(BARABASI_EDGES, BARABASI_NODES);
        }
        else if (args[0].compareToIgnoreCase("erdos") == 0) {
            g = new ErdosGraph(ERDOS_P, ERDOS_NODES);
        }
        else {
            g = new SocialGraph(csvFile, Boolean.valueOf(directed));
        }
        
        //get PageRank values
        List<RankedDocument> lr = g.pageRank(10, 50);
        Collections.sort(lr);
        Collections.reverse(lr);
        System.out.println("Page Rank Top:");
        for (RankedDocument rd : lr) {
            System.out.println(graphName + rd.getDocName() + "\t" + Double.toString(rd.getScore()));
        }
        
        //clustering coeficients
        lr = g.clustering(10);
        Collections.sort(lr);
        Collections.reverse(lr);
        System.out.println("\nClustering Top:");
        for (RankedDocument rd : lr) {
            System.out.println(graphName + rd.getDocName() + "\t" + Double.toString(rd.getScore()));
        }
        
        //embeddedness
        lr = g.embeddedness(10);
        Collections.sort(lr);
        Collections.reverse(lr);
        System.out.println("\nEmbeddedness Top:");
        for (RankedDocument rd : lr) {
            Pair<String> verticesName = g.getEdgeVertices(rd.getDocID());
            System.out.println(graphName + "\t" + verticesName.getFirst() + "\t" + 
                    verticesName.getSecond() + "\t" + Double.toString(rd.getScore()));
        }
        
        //Number of Vertices
        System.out.println("\nNumber of Vertices in graph: " + g.getnVertex());
        //Number of Edges
        System.out.println("Number of Edges in graph: " + g.getnEdges());
        //Clustering Average
        System.out.println("Clustering average: " + g.clusteringAvg());
        //Friendship Paradox
        System.out.println("Friendship paradox fulfill rate: " + g.friendshipParadox());
        //Local Bridges
        System.out.println("Local bridges: " + g.getLocalBridges());
        //Grade assortativity
        System.out.println("Grade Assortativity: " + g.assortativity());
        
        try{ //write output files
            System.out.println("Writting Distribution file");
            writeDistribution(OUTDIR + args[2] + DIST_SUFFIX, true);
            System.out.println("Writting Pagerank and Betweenness file");
            writeScatter(OUTDIR + args[2] + SCAT_SUFFIX, true);
        }
        catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
        }
        System.out.println("Done.\n");
        return;
    }
}
