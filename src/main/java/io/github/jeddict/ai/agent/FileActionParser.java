/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.jeddict.ai.agent;

/**
 *
 * @author Gaurav Gupta
 */
public class FileActionParser {
    public static FileAction parse(String action, String content) {
        FileAction fileAction = new FileAction();
        fileAction.setContent(content);
        String[] lines = action.split("\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("path=")) {
                fileAction.setPath(line.substring("path=".length()));
            } else if (line.startsWith("action=")) {
                fileAction.setAction(line.substring("action=".length()));
            }
        }
        
        return fileAction;
    }
}
