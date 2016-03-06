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
package es.uam.eps.bmi.search.indexing;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * IndexBuilder class
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class IndexBuilder {
    
    private final static String XML_INPUT = "index-settings.xml";
    private final static String COLLECTION_TAG_NAME = "collection-folder";
    private final static String OUTPATH_TAG_NAME = "index-folder";
    private final static String BASIC_I_APPEND = "basic/";
    private final static String STOP_I_APPEND = "stopword/";
    private final static String STEM_I_APPEND = "stem/";
    
    /**
     * Main method for IndexBuilder.
     * Builds a BasicIndex, StopwordIndex and StemIndex reading
     * collection path and output from XML_INPUT file.
     * @param args 
     */
    public static void main(String[] args) {
        String collectionPath, outPath;
        File inputFile = new File("input.txt");
        
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(XML_INPUT);
            doc.getDocumentElement().normalize();
            collectionPath = doc.getElementsByTagName(COLLECTION_TAG_NAME).item(0).getTextContent();
            outPath = doc.getElementsByTagName(OUTPATH_TAG_NAME).item(0).getTextContent();
        } catch (ParserConfigurationException | SAXException ex) {
            System.err.println("Exception caught while configurating XML parser: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
            return;
        } catch (IOException ex) {
            System.err.println("Exception caught while performing IO operation: " + ex.getClass().getSimpleName());
            System.err.println(ex.getMessage());
            return;
        }
        if (!outPath.endsWith("/"))
        {
            outPath+="/";
        }
        
        //Build basic index
        System.out.println("Creating Basic Index");
        BasicIndex.main(new String[]{collectionPath, outPath + BASIC_I_APPEND});
        System.out.println();
        
        //Build stopword index
        System.out.println("Creating Stopword Index");
        StopwordIndex.main(new String[]{collectionPath, outPath + STOP_I_APPEND});
        System.out.println();
        
        //Build stem index
        System.out.println("Creating Stem Index");
        StemIndex.main(new String[]{collectionPath, outPath + STEM_I_APPEND});
        System.out.println();
        
    }
    
}
