/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
