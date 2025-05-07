/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.jeddict.ai.components.mermaid;

import java.awt.Color;
import java.awt.Point;
import java.util.Map;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Gaurav Gupta
 */
public class MermaidViewer {
    
    protected static boolean isDarkColor(Color color) {
        double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return luminance < 0.5;
    }

    protected static void applyGridLayout(Map<String, Widget> widgets, int cols, int spacingX, int spacingY) {
        int i = 0;
        for (Widget widget : widgets.values()) {
            int x = (i % cols) * spacingX + 50;
            int y = (i / cols) * spacingY + 50;
            widget.setPreferredLocation(new Point(x, y));
            i++;
        }
    }

}
