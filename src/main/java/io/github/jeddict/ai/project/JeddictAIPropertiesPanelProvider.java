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

import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;

/**
 *
 * @author Gaurav Gupta
 */
public class JeddictAIPropertiesPanelProvider implements ProjectCustomizer.CompositeCategoryProvider {

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-maven",
            position = 305
    )
    public static JeddictAIPropertiesPanelProvider createRun() {
        return new JeddictAIPropertiesPanelProvider();
    }
    
    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create("JeddictAI", "Jeddict AI", null); // NOI18N
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        ModelHandle2 handle = context.lookup(ModelHandle2.class);
        final Project project = context.lookup(Project.class);
        JeddictAIPropertiesPanel propertiesPanel = new JeddictAIPropertiesPanel(handle, project);
        category.setOkButtonListener(event -> propertiesPanel.applyChanges());
        category.setStoreListener(event -> propertiesPanel.applyChanges());
        return propertiesPanel;
    }

}
