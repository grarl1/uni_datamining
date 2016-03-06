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

import java.util.regex.Pattern;
import org.jsoup.Jsoup;

/**
 * Class for parsing documents. Removes HTML tags and filters characters keeping
 * only a-z (lowering case if A-Z is found).
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class BasicParser implements TextParser {

    private static final Pattern FILTER_NON_LETTER_PATTERN = Pattern.compile("[^A-Za-z]+");
    
    /**
     * Processes the input text removing the HTML tags and every non letter character.
     *
     * @param text Text to be processed.
     * @return the processed input text without HTML tags.
     */
    @Override
    public String parse(String text) {
        text = Jsoup.parse(text).text().toLowerCase();
        return FILTER_NON_LETTER_PATTERN.matcher(text).replaceAll(" ");
    }
    
    /**
     * Processes the input text removing the HTML tags and every non letter character.
     * Splits text after parsing into tokens.
     *
     * @param text Text to be processed.
     * @param splitter String to split text with
     * @return the processed input text without HTML tags.
     */
    @Override
    public String[] parse(String text, String splitter) {
        text = Jsoup.parse(text).text().toLowerCase();
        return FILTER_NON_LETTER_PATTERN.matcher(text).replaceAll(" ").split(splitter);
    }

}
