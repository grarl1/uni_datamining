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
package es.uam.eps.bmi.search.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.SnowballStemmer;

/**
 * Class for parsing documents.
 *
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class StemParser extends StopwordParser {

    /**
     * Processes the input text removing the HTML tags and every non letter
     * character.
     *
     * @param text Text to be processed.
     * @return the processed input text without HTML tags.
     */
    @Override
    public String parse(String text) {
        String parsed = "";
        String[] terms = super.parse(text, "\\s+");
        for (String s : terms){
            //TODO
        }
        return parsed;
    }
    
    /**
     * Processes the input text removing the HTML tags and every non letter
     * character.
     * Splits text after parsing into tokens.
     *
     * @param text Text to be processed.
     * @param splitter string to split text with
     * @return the processed input text without HTML tags.
     */
    @Override
    public String[] parse(String text, String splitter) {
        Class stemClass;
        try {
            stemClass = Class.forName("org.tartarus.snowball.ext.englishStemmer");
            SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(StemParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(StemParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(StemParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        List<String> filtered = new ArrayList<>();
        String[] terms = super.parse(text, splitter);
        for (String s : terms){
            
            //TODO SOMETHING
        }
        return filtered.toArray(new String[0]);
    }

}

