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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Class for parsing documents.
 *
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class StopwordParser extends BasicParser {

    /* Minimum size of a String to consider it a term */
    private static final int MIN_TERM_SIZE = 3;

    /* Stopword array */
    private static final String[] STOP_WORDS = {
        "a", "an", "and", "are", "as",
        "at", "be", "but", "by", "for",
        "if", "in", "into", "is", "it",
        "no", "not", "of", "on", "or",
        "such", "that", "the", "their",
        "then", "there", "these", "they",
        "this", "to", "was", "will", "with"};

    /* HashSet so search is O(1) */
    private final HashSet<String> stopwordmap = new HashSet<>(Arrays.asList(STOP_WORDS));

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
            if (s.length()<MIN_TERM_SIZE) continue;
            if (stopwordmap.contains(s)) continue;
            parsed += s + " ";
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
        List<String> filtered = new ArrayList<>();
        String[] terms = super.parse(text, splitter);
        for (String s : terms){
            if (s.length()<MIN_TERM_SIZE) continue;
            if (stopwordmap.contains(s)) continue;
            filtered.add(s);
        }
        return filtered.toArray(new String[0]);
    }

}
