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
package io.github.jeddict.ai.agent.tools.code;

import dev.langchain4j.agent.tool.Tool;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class RenameClassTool extends BaseCodeTool {

    public RenameClassTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "RenameClassTool_renameClass",
        value = "Rename a class in a Java file"
    )
    public String renameClass(String path, String oldName, String newName)
    throws Exception {
        progress("Renaming class " + oldName + " -> " + newName);

        return withJavaSource(path, javaSource -> {
            final StringBuilder result = new StringBuilder();

            javaSource.runModificationTask(cc -> {
                cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                for (TypeElement type : ElementFilter.typesIn(cc.getTopLevelElements())) {
                    if (type.getSimpleName().toString().equals(oldName)) {
                        ElementHandle<TypeElement> handle = ElementHandle.create(type);

                        // Create refactoring
                        Lookup lookup = Lookups.singleton(handle);
                        RenameRefactoring refactor = new RenameRefactoring(lookup);
                        refactor.setNewName(newName);

                        // Run refactoring in session
                        RefactoringSession session = RefactoringSession.create("Rename Class");
                        refactor.prepare(session);
                        session.doRefactoring(true);

                        result.append("Class renamed from ")
                                .append(oldName)
                                .append(" to ")
                                .append(newName);
                    }
                }
            }).commit();

            return result.length() == 0
                    ? "No class named " + oldName + " found."
                    : result.toString();
        }, true);
    }
}
