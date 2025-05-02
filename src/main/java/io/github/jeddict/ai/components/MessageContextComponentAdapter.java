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
package io.github.jeddict.ai.components;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Gaurav Gupta
 */
public class MessageContextComponentAdapter extends ComponentAdapter {

    private final JPanel filePanel;
    private boolean shortLabel = false;

    public MessageContextComponentAdapter(JPanel filePanel) {
        this.filePanel = filePanel;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        int panelWidth = filePanel.getWidth();
        int maxAllowedWidth = (int) (panelWidth * 0.8);

        int totalTabWidth = 0;
        List<FileTab> tabs = new ArrayList<>();

        for (Component comp : filePanel.getComponents()) {
            if (comp instanceof FileTab fileTab) {
                tabs.add(fileTab);
                totalTabWidth += fileTab.getPreferredSize().width;
            }
        }
        if (shortLabel) {
            totalTabWidth = (int) (totalTabWidth * 1.5);
        }

        if (totalTabWidth > maxAllowedWidth) {
            shortLabel = true;
            for (FileTab tab : tabs) {
                JLabel label = getLabelFromFileTab(tab);
                if (label != null) {
                    String fullName = tab.getFile().getNameExt();
                    FontMetrics fm = label.getFontMetrics(label.getFont());
                    int individualMaxWidth = tab.getPreferredSize().width - 40; // allow for close button, padding, etc.
                    String shortened = shortenFileName(fullName, individualMaxWidth, fm);
                    label.setText(shortened);
                }
            }
        } else {
            shortLabel = false;
            for (FileTab tab : tabs) {
                JLabel label = getLabelFromFileTab(tab);
                if (label != null) {
                    label.setText(tab.getFile().getNameExt());
                }
            }
        }
    }

    private JLabel getLabelFromFileTab(FileTab fileTab) {
        for (Component child : fileTab.getComponents()) {
            if (child instanceof JPanel roundedPanel) {
                for (Component sub : roundedPanel.getComponents()) {
                    if (sub instanceof JLabel label) {
                        return label;
                    }
                }
            }
        }
        return null;
    }

    private String shortenFileName(String fullName, int maxWidth, FontMetrics fm) {
        int dotIndex = fullName.lastIndexOf('.');
        String name = (dotIndex > 0) ? fullName.substring(0, dotIndex) : fullName;

        String suffix = "..";

        for (int len = Math.min(5, name.length()); len > 0; len--) {
            String shortened = name.substring(0, len) + suffix;
            if (fm.stringWidth(shortened) <= maxWidth) {
                return shortened;
            }
        }

        return suffix;
    }

}
