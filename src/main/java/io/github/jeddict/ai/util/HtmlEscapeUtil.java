/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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