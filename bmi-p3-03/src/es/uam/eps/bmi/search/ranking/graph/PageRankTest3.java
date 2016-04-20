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

import es.uam.eps.bmi.search.indexing.BasicIndex;
import es.uam.eps.bmi.search.indexing.IndexWriter;
import es.uam.eps.bmi.search.parsing.BasicParser;
import es.uam.eps.bmi.search.parsing.TextParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Third test for PageRank, receives as a first argument the number of the link file<br>
 * and as second argument, the output file name
 *
 * @author Guillermo Ruiz Álvarez
 * @author Enrique Cabrerizo Fernández
 */
public class PageRankTest3 {
    
    /**
     * Main method.
     * @param args 
     */
    public static void main (String args[]) throws IOException, ClassNotFoundException {
        final int MAX_CONTENT=300;
        
        // Input control
        if (args.length != 3) {
            System.err.printf("Usage: %s linkFile outFile zipFile\n"
                    + "\tlinkFile: Graph to rank.\n"
                    + "\toutFile: File to write ranking vector to.\n"
                    + "\tzipFile: Zip file containing documents. Needed to show a sample for the top 10 files\n",
                    BasicIndex.class.getSimpleName());
            return;
        }
        String linkFile = args[0];
        String outFile = args[1];
        String zipFile = args[2];
        
        PageRank pr = new PageRank(linkFile, outFile);
        BasicParser parser = new BasicParser();
        System.out.println("Top Documents:");
        if (pr.rank(0.15, 50, 0.0)) { //0.0 for tol ensures the execution of max iterations
            List<RankedDocument> lr = pr.getTopNIds(10);
            Collections.sort(lr);
            Collections.reverse(lr);
            for (RankedDocument rd : lr) {
                System.out.println(rd.getDocName() + " score: " + Double.toString(rd.getScore()));
            }
            //seek content of each file in zip file
            String[] contents =readContents(lr, zipFile, parser, MAX_CONTENT);
            for (int i = 0; i < lr.size(); i++) {
                System.out.print("Content of " + lr.get(i).getDocName() + ":\n" + contents[i]+"\n");
            }
        }
        
        return;
    }

    /**
     * Reads zip file looking for documents given in the list, returns a string
     * with some bytes for each file.
     */
    private static String[] readContents(List<RankedDocument> docs, String zipPath, TextParser textParser, int maxRead) {
        ZipEntry ze;
        int list_size = docs.size();
        ArrayList<RankedDocument> docsCopy = new ArrayList(docs);
        String[] contents = new String[list_size];
        List<Integer> indexes = new ArrayList<>();
        
        File zipFile = new File(zipPath);
        File parent = zipFile.getParentFile();
        if (parent == null || !parent.exists()) {
            return null;
        }
        
        for (int i=0; i<list_size; i++) {
            indexes.add(i);
        }
        
        try (FileInputStream fis = new FileInputStream(zipFile);
                ZipInputStream zis = new ZipInputStream(fis)) {
            //Iterate over every file inside zip.
            while ((ze = zis.getNextEntry()) != null) {
                int i;
                for (i=0; i<docsCopy.size(); i++){
                    if (docsCopy.get(i).getDocName().concat(".html").equals(ze.getName())) {
                        // read file
                        byte[] byteContent = new byte[2048];
                        int justRead;
                        String s = "";
                        while ((justRead = zis.read(byteContent)) > 0) {
                            s += new String(byteContent, 0, justRead);
                        }
                        contents[indexes.get(i)] = textParser.parse(s).substring(0, maxRead);
                        break;
                    }
                }
                if (i < list_size) {
                    docsCopy.remove(i);
                    indexes.remove(i);
                    list_size--;
                }
            }
        } catch (IOException ex) {
            System.err.println("Exception caught while performing an I/O operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
        }
        return contents;
    }
}
