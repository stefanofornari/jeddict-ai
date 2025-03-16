/**
 * Copyright 2025 the original author or authors from the Jeddict project (https://jeddict.github.io/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.jeddict.ai.completion;

/**
 *
 * @author Gaurav Gupta
 */
public class LineScanResult {
    private final String fullText; // Full text from '/' to cursor
    private final String firstWord; // First word after '/'
    private final String secondWord; // Second word after '/'
    private final int slashPosition; // Position of '/'

    public LineScanResult(String fullText, String firstWord, String secondWord, int slashPosition) {
        this.fullText = fullText;
        this.firstWord = firstWord;
        this.secondWord = secondWord;
        this.slashPosition = slashPosition;
    }

    public String getFullText() {
        return fullText;
    }

    public String getFirstWord() {
        return firstWord;
    }

    public String getSecondWord() {
        return secondWord;
    }

    public int getSlashPosition() {
        return slashPosition;
    }

    @Override
    public String toString() {
        return "LineScanResult{" +
               "fullText='" + fullText + '\'' +
               ", firstWord='" + firstWord + '\'' +
               ", secondWord='" + secondWord + '\'' +
               ", slashPosition=" + slashPosition +
               '}';
    }
}
