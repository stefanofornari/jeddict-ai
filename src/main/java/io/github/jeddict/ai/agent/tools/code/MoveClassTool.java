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
import org.openide.util.lookup.Lookups;

public class MoveClassTool extends BaseCodeTool {

    public MoveClassTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "MoveClassTool_moveClass",
        value = "Move a class to another package"
    )
    public String moveClass(String path, String className, String newPackage)
    throws Exception {
        return withJavaSource(path, javaSource -> {
            final StringBuilder result = new StringBuilder();
            javaSource.runModificationTask(cc -> {
                cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                for (TypeElement type : ElementFilter.typesIn(cc.getTopLevelElements())) {
                    if (type.getSimpleName().toString().equals(className)) {
                        ElementHandle<TypeElement> handle = ElementHandle.create(type);

                        org.netbeans.modules.refactoring.api.MoveRefactoring ref
                                = new org.netbeans.modules.refactoring.api.MoveRefactoring(Lookups.singleton(handle));
                        ref.setTarget(Lookups.singleton(newPackage));

                        RefactoringSession session = RefactoringSession.create("Move Class");
                        ref.prepare(session);
                        session.doRefactoring(true);

                        result.append("Moved class ").append(className)
                                .append(" to package ").append(newPackage);
                    }
                }
            }).commit();
            return result.length() == 0 ? "No class " + className + " found." : result.toString();
        }, true);
    }
}
