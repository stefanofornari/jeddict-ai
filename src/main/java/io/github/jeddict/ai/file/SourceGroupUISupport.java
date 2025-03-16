/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.jeddict.ai.file;

/**
 *
 * @author Gaurav Gupta
 */

import java.awt.Component;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import org.netbeans.api.project.SourceGroup;

public class SourceGroupUISupport {
    
    private SourceGroupUISupport() {
    }
    
    public static void connect(JComboBox comboBox, SourceGroup[] sourceGroups) {
        comboBox.setModel(new DefaultComboBoxModel(sourceGroups));
        comboBox.setRenderer(new SourceGroupRenderer());
    }
    
    private static final class SourceGroupRenderer extends DefaultListCellRenderer {
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Object displayName = null;
            
            if (value instanceof SourceGroup) {
                displayName = ((SourceGroup)value).getDisplayName();
            } else {
                displayName = value;
            }
            
            return super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
        }
    }
}