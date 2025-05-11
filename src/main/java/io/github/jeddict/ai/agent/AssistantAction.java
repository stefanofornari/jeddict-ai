/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.jeddict.ai.agent;

/**
 *
 * @author Gaurav Gupta
 */
public enum AssistantAction {
    ASK("Ask"),
    BUILD("Build");

    private final String displayName;

    AssistantAction(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}