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
package es.uam.eps.bmi.search.ranking.graph;

import com.google.common.collect.HashBasedTable;
import es.uam.eps.bmi.util.MinHeap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.DoubleStream;

/**
 * Class implementing Page Rank.
 *
 * @author Guillermo Ruiz Álvarez
 * @author Enrique Cabrerizo Fernández
 */
public class PageRank {

    /* Default file to save PageRank scores */
    private final static String OUTPUT_FILE_DEFAULT = "score";
    /* Default file suffix to save map between file names and ids */
    private final static String HASH_FILE_ID_SUFFIX = "_files-id";
    /* Default file suffix to save map between file ids and names */
    private final static String HASH_ID_FILE_SUFFIX = "_id-file";
    /* Number of attributes in each file line, separated by spaces, before
        the actual list of links */
    private final static Integer N_ATTRIBUTES = 2;
    /* Default d value for PageRank: P[i] = P[i]+(1-d)P[i]/out[i] */
    private final static Double DEF_D = 0.15;
    /* PageRank will stop when it has done DEF_ITER iterations */
    private final static Integer DEF_ITER = 50;
    /* PageRank will stop when no value in rankVector has cahnged more than this value */
    private final static Double DEF_TOL = 0.001;

    /* Path to file with file links */
    private String linkFile;
    /* File to save PageRank scores */
    private String scoreFile;
    /* File to save map between file names and ids */
    private String NameToIdFile;
    /* File to save map between file ids and names */
    private String IdToNameFile;

    /* Table with probabilities to go from page with Row id to Column id 
        only probabilities > 0 are saved in this table*/
    protected HashBasedTable<Integer, Integer, Integer> rankMatrix;

    /* Incremental value to assign a unique doc ID to each document */
    protected Integer currentDocId = 0;
    /* Total number of documents to rank */
    protected Integer totalDocNo = 0;

    /* Bidirectional HashMap to store document names and numeric id assigned */
    protected HashMap<Integer, String> idToNameMap;
    /* Bidirectional HashMap to store document names and numeric id assigned */
    protected HashMap<String, Integer> NameToIdMap;

    /* Array of scores for each document (addressed by its numeric id) */
    protected double[] rankVector;

    /* Array of Sink Node indexes */
    protected Integer[] sinkNodeIndexes;

    /* Number of sink nodes */
    protected int nSinkNodes;

    /**
     * Default constructor for <code>PageRank</code> class.
     *
     * @param linkFile file containing links for every document.
     */
    public PageRank(String linkFile) {
        this(linkFile, OUTPUT_FILE_DEFAULT);
    }

    /**
     * Constructor for <code>IndexWriter</code> class.
     *
     * @param linkFile file containing links for every document. Can be null if
     * outFile
     * <br> exists form a previous execution and the user just wants to use
     * loadRanking() method
     * @param outFile file to write ranking to.
     */
    public PageRank(String linkFile, String outFile) {
        this.rankVector = null;
        this.sinkNodeIndexes = null;
        this.nSinkNodes = 0;
        this.linkFile = linkFile;
        this.scoreFile = outFile;
        this.NameToIdFile = outFile + HASH_FILE_ID_SUFFIX;
        this.IdToNameFile = outFile + HASH_ID_FILE_SUFFIX;
    }

    /**
     * Gets PageRank score of given document
     *
     * @param documentid document name to retrieve score.
     * @return score of the document.
     */
    public double getScoreOf(String documentid) {
        if (rankVector == null) {
            return -1;
        }
        Integer index = NameToIdMap.get(documentid);
        if (index == null) {
            return -1;
        }
        return rankVector[index];
    }

    /**
     * Returns top n documents sorted by PageRank score.
     *
     * @param n number of documents to retrieve.
     * @return
     */
    public List<RankedDocument> getTopNIds(int n) {
        if ((n <= 0) || (rankVector == null)) {
            return null;
        }
        MinHeap<RankedDocument> minHeap = new MinHeap<>(n);
        for (int i = 0; i < rankVector.length; i++) {
            String docName = idToNameMap.get(i);
            RankedDocument doc = new RankedDocument(i, rankVector[i], docName);
            minHeap.add(doc);
        }

        return minHeap.asList();
    }

    /**
     * Initializes Ranking from files passed when the class instance was
     * created.
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    public void loadRanking() throws IOException, ClassNotFoundException {

        NameToIdMap = (HashMap<String, Integer>) readObjectFromFile(new File(NameToIdFile));
        idToNameMap = (HashMap<Integer, String>) readObjectFromFile(new File(IdToNameFile));
        rankVector = (double[]) readObjectFromFile(new File(scoreFile));

    }

    /**
     * Performs PageRank algorithm over linkFile and saves results to scoreFile.
     *
     * @return true if everything went OK. false on error.
     */
    public boolean rank() {
        return rank(DEF_D, DEF_ITER, DEF_TOL);
    }

    /**
     * Performs PageRank algorithm over linkFile and saves results to scoreFile
     * with the stop conditions passed as argument. Non verbose mode by default.
     *
     * @param d bias value for the algorithm.
     * @param iter max number of iterations.
     * @param tol threshold, once rank values don't change more than this value
     * <br> from one iteration to another, the algorithm will stop.
     * @return true if everything went OK. false on error.
     */
    public boolean rank(Double d, Integer iter, Double tol) {
        return rank(d,iter,tol,false);
    }
    
    /**
     * Performs PageRank algorithm over linkFile and saves results to scoreFile
     * with the stop conditions passed as argument.
     *
     * @param d bias value for the algorithm.
     * @param iter max number of iterations.
     * @param tol threshold, once rank values don't change more than this value
     * <br> from one iteration to another, the algorithm will stop.
     * @param verbose prints the value of rank vector on each iteration
     * @return true if everything went OK. false on error.
     */
    public boolean rank(Double d, Integer iter, Double tol, boolean verbose) {
        double[] currentRank = null;
        double max_change = -1;

        if ((iter <= 0) || (tol < 0) || (d <= 0)) {
            return false;
        }

        if (!readLinksFile()) {
            return false;
        }
        rankVector = new double[totalDocNo];
        //PageRank main loop
        Arrays.fill(rankVector, (1.0 / totalDocNo));
        do {
            double sinkProb = 0;
            max_change = -1.0;
            currentRank = new double[totalDocNo];
            if (sinkNodeIndexes != null) {
                for (int i = 0; i < nSinkNodes; i++) {
                    sinkProb += rankVector[sinkNodeIndexes[i]];
                }
                sinkProb *=(1.0 / totalDocNo);
            }
            for (int i = 0; i < totalDocNo; i++) {
                for (Map.Entry<Integer, Integer> entry : rankMatrix.column(i).entrySet()) {
                    currentRank[i] += rankVector[entry.getKey()] / entry.getValue();
                }
                currentRank[i] *= (1 - d);
                currentRank[i] += (d / totalDocNo) + (1 - d) * sinkProb;
                double i_change = Math.abs(currentRank[i]-rankVector[i]);
                if (max_change < i_change)
                    max_change = i_change;
            }
            rankVector = currentRank;
            if (verbose) {
                for (int i=0; i<totalDocNo; i++){
                    System.out.print(rankVector[i] + " ");
                }
                System.out.println();
            }
            iter--;
        } while ((iter > 0) && (max_change >= tol));

        writeObjToFile(new File(NameToIdFile), NameToIdMap);
        writeObjToFile(new File(IdToNameFile), idToNameMap);
        writeObjToFile(new File(scoreFile), rankVector);

        rankMatrix = null; //there is no use for this object anymore.
        return true;
    }

    /**
     * Reads links file creating tables and structures needed for PageRank.
     *
     * @return true if everything went OK. false on error.
     */
    private boolean readLinksFile() {
        ArrayList<Integer> sinkNodes = new ArrayList<>();
        Integer linkFromId, linkToId;
        NameToIdMap = new HashMap<>();
        idToNameMap = new HashMap<>();
        rankMatrix = HashBasedTable.create();
        /* Open and start reading linkFile line by line*/
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(linkFile));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split("\\s+");
                Integer nLinks = split.length - N_ATTRIBUTES;
                String linkFromFile = split[0];
                if (NameToIdMap.containsKey(linkFromFile)) {
                    linkFromId = NameToIdMap.get(linkFromFile);
                } else {
                    NameToIdMap.put(linkFromFile, currentDocId);
                    idToNameMap.put(currentDocId, linkFromFile);
                    linkFromId = currentDocId;
                    currentDocId++;
                }
                if ((nLinks == 1)  && (split[0].compareTo(split[N_ATTRIBUTES]) == 0)) //just one link, to himself, sink node
                    nLinks = 0;
                for (int i = 0; i < nLinks; i++) {
                    if (NameToIdMap.containsKey(split[i + N_ATTRIBUTES])) {
                        linkToId = NameToIdMap.get(split[i + N_ATTRIBUTES]);
                    } else {
                        NameToIdMap.put(split[i + N_ATTRIBUTES], currentDocId);
                        idToNameMap.put(currentDocId, split[i + N_ATTRIBUTES]);
                        linkToId = currentDocId;
                        currentDocId++;
                    }
                    //add value nLinks to position (linkFromId,linkToId) on matrix RankMatrix.
                    rankMatrix.put(linkFromId, linkToId, nLinks);
                }
                if (nLinks == 0) { //sink node
                    sinkNodes.add(linkFromId);
                }
            }
            totalDocNo = currentDocId;
            if (sinkNodes.size() > 0) {
                sinkNodeIndexes = sinkNodes.toArray(new Integer[0]);
                nSinkNodes = sinkNodeIndexes.length;
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
     * Writes an Object to a file checking first if parent directories exist and
     * creating them if not.
     *
     * @param f file to write to.
     * @param o object to write to file.
     * @return true on success. false otherwise.
     */
    private boolean writeObjToFile(File f, Object o) {
        ObjectOutputStream oos = null;
        try {
            File parent = f.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(o);
            oos.flush();
            oos.close();
        } catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Reads an Object from a file.
     *
     * @param f file to write from.
     * @return Object read on success. null otherwise.
     */
    private Object readObjectFromFile(File f) {
        Object read;
        try {
            read = (new ObjectInputStream(new FileInputStream(f))).readObject();
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
            return null;
        }
        return read;
    }

}
