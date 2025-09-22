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
package io.github.jeddict.ai.project;

import io.github.jeddict.ai.settings.PreferencesManager;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;

/**
 *
 * @author Gaurav Gupta
 */
public class JeddictAIPropertiesPanel extends JPanel {

    private final Project project;
    private final PreferencesManager pm = PreferencesManager.getInstance();

    public JeddictAIPropertiesPanel(ModelHandle2 handle, Project project) {
        this.project = project;
        initComponents();
        projectRulesTextArea.setText(pm.getProjectRules(project));
        buildCommandTextArea.setText(pm.getBuildCommand(project));
        testCommandTextArea.setText(pm.getTestCommand(project));
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectRulesLayeredPane = new javax.swing.JLayeredPane();
        projectRulesLabel = new javax.swing.JLabel();
        projectRulesScrollPane = new javax.swing.JScrollPane();
        projectRulesTextArea = new javax.swing.JTextArea();
        commandsPane = new javax.swing.JLayeredPane();
        buildCommandPane = new javax.swing.JLayeredPane();
        buildCommandLabel = new javax.swing.JLabel();
        buildCommandScrollPane = new javax.swing.JScrollPane();
        buildCommandTextArea = new javax.swing.JTextArea();
        testCommandPane = new javax.swing.JLayeredPane();
        testCommandLabel = new javax.swing.JLabel();
        testCommandScrollPane = new javax.swing.JScrollPane();
        testCommandTextArea = new javax.swing.JTextArea();

        setLayout(new java.awt.GridLayout(0, 1));

        org.openide.awt.Mnemonics.setLocalizedText(projectRulesLabel, org.openide.util.NbBundle.getMessage(JeddictAIPropertiesPanel.class, "JeddictAIPropertiesPanel.projectRulesLabel.text")); // NOI18N

        projectRulesTextArea.setColumns(20);
        projectRulesTextArea.setRows(5);
        projectRulesScrollPane.setViewportView(projectRulesTextArea);

        projectRulesLayeredPane.setLayer(projectRulesLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        projectRulesLayeredPane.setLayer(projectRulesScrollPane, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout projectRulesLayeredPaneLayout = new javax.swing.GroupLayout(projectRulesLayeredPane);
        projectRulesLayeredPane.setLayout(projectRulesLayeredPaneLayout);
        projectRulesLayeredPaneLayout.setHorizontalGroup(
            projectRulesLayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(projectRulesLayeredPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(projectRulesLayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(projectRulesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE)
                    .addGroup(projectRulesLayeredPaneLayout.createSequentialGroup()
                        .addComponent(projectRulesLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        projectRulesLayeredPaneLayout.setVerticalGroup(
            projectRulesLayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(projectRulesLayeredPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(projectRulesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(projectRulesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                .addContainerGap())
        );

        add(projectRulesLayeredPane);

        commandsPane.setLayout(new java.awt.GridLayout(0, 1));

        org.openide.awt.Mnemonics.setLocalizedText(buildCommandLabel, org.openide.util.NbBundle.getMessage(JeddictAIPropertiesPanel.class, "JeddictAIPropertiesPanel.buildCommandLabel.text")); // NOI18N

        buildCommandTextArea.setColumns(20);
        buildCommandTextArea.setRows(5);
        buildCommandScrollPane.setViewportView(buildCommandTextArea);

        buildCommandPane.setLayer(buildCommandLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        buildCommandPane.setLayer(buildCommandScrollPane, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout buildCommandPaneLayout = new javax.swing.GroupLayout(buildCommandPane);
        buildCommandPane.setLayout(buildCommandPaneLayout);
        buildCommandPaneLayout.setHorizontalGroup(
            buildCommandPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buildCommandPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(buildCommandPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buildCommandScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE)
                    .addGroup(buildCommandPaneLayout.createSequentialGroup()
                        .addComponent(buildCommandLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        buildCommandPaneLayout.setVerticalGroup(
            buildCommandPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buildCommandPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(buildCommandLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buildCommandScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
                .addContainerGap())
        );

        commandsPane.add(buildCommandPane);

        org.openide.awt.Mnemonics.setLocalizedText(testCommandLabel, org.openide.util.NbBundle.getMessage(JeddictAIPropertiesPanel.class, "JeddictAIPropertiesPanel.testCommandLabel.text")); // NOI18N

        testCommandTextArea.setColumns(20);
        testCommandTextArea.setRows(5);
        testCommandScrollPane.setViewportView(testCommandTextArea);

        testCommandPane.setLayer(testCommandLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        testCommandPane.setLayer(testCommandScrollPane, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout testCommandPaneLayout = new javax.swing.GroupLayout(testCommandPane);
        testCommandPane.setLayout(testCommandPaneLayout);
        testCommandPaneLayout.setHorizontalGroup(
            testCommandPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(testCommandPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(testCommandPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(testCommandScrollPane)
                    .addGroup(testCommandPaneLayout.createSequentialGroup()
                        .addComponent(testCommandLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        testCommandPaneLayout.setVerticalGroup(
            testCommandPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(testCommandPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(testCommandLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(testCommandScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
                .addContainerGap())
        );

        commandsPane.add(testCommandPane);

        add(commandsPane);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel buildCommandLabel;
    private javax.swing.JLayeredPane buildCommandPane;
    private javax.swing.JScrollPane buildCommandScrollPane;
    private javax.swing.JTextArea buildCommandTextArea;
    private javax.swing.JLayeredPane commandsPane;
    private javax.swing.JLabel projectRulesLabel;
    private javax.swing.JLayeredPane projectRulesLayeredPane;
    private javax.swing.JScrollPane projectRulesScrollPane;
    private javax.swing.JTextArea projectRulesTextArea;
    private javax.swing.JLabel testCommandLabel;
    private javax.swing.JLayeredPane testCommandPane;
    private javax.swing.JScrollPane testCommandScrollPane;
    private javax.swing.JTextArea testCommandTextArea;
    // End of variables declaration//GEN-END:variables

    public void applyChanges() {
        pm.setProjectRules(project, projectRulesTextArea.getText());
        pm.setBuildCommand(project, buildCommandTextArea.getText());
        pm.setTestCommand(project, testCommandTextArea.getText());
    }

}
