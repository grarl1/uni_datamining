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

import org.jsoup.Jsoup;

/**
 * Class for parsing HTML document.
 *
 * @author Enrique Cabrerizo Fernández
 * @author Guillermo Ruiz Álvarez
 */
public class HTMLSimpleParser implements TextParser {

    /**
     * Processes the input text removing the HTML tags.
     *
     * @param text Text to be processed.
     * @return the processed input text without HTML tags.
     */
    @Override
    public String parse(String text) {
        return Jsoup.parse(text).text();
    }

    /**
     * Processes the input text removing the HTML tags. Splits text after
     * parsing into tokens.
     *
     * @param text Text to be processed.
     * @return the processed input text without HTML tags.
     */
    @Override
    public String[] parse(String text, String splitter) {
        return Jsoup.parse(text).text().split(splitter);
    }
}
