/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.jeddict.ai.agent;

/**
 *
 * @author Gaurav Gupta
 */
public class FileAction {
    private String path;
    private String action;
    private String content;

    public FileAction() {}

    public FileAction(String path, String action, String content) {
        this.path = path;
        this.action = action;
        this.content = content;
    }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    @Override
    public String toString() {
        return "FileAction{" +
                "path='" + path + '\'' +
                ", action='" + action + '\'' +
                ", content='" + (content == null ? "null" : "[content]") + '\'' +
                '}';
    }
}