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
package io.github.jeddict.ai.util;

import java.util.HashMap;
import java.util.Map;

public class HtmlEscapeUtil {

    // Create a mapping of HTML escape codes to their corresponding characters
    private static final Map<String, String> escapeMap = new HashMap<>();

    static {
        escapeMap.put("&amp;", "&");
        escapeMap.put("&lt;", "<");
        escapeMap.put("&gt;", ">");
        escapeMap.put("&quot;", "\"");
        escapeMap.put("&apos;", "'");
        escapeMap.put("&nbsp;", " ");
        escapeMap.put("&copy;", "©");
        escapeMap.put("&reg;", "®");
        escapeMap.put("&euro;", "€");
        escapeMap.put("&pound;", "£");
        // Add more escape codes as needed
    }

    // Function to replace escape codes with their corresponding characters
    public static String decodeHtml(String input) {
        if (input == null) {
            return null; // Handle null input
        }

        String result = input;
        for (Map.Entry<String, String> entry : escapeMap.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

}
