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
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.openide.util.lookup.Lookups;

public class FindUsagesTool extends BaseCodeTool {

    public FindUsagesTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "FindUsagesTool_findUsages",
        value = "Find all usages of a class, method, or field"
    )
    public String findUsages(String path, String symbolName)
    throws Exception {
        String jr = withJavaSource(path, javaSource -> {
            final StringBuilder result = new StringBuilder();
            javaSource.runUserActionTask(cc -> {
                cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                for (TypeElement type : ElementFilter.typesIn(cc.getTopLevelElements())) {
                    for (Element member : type.getEnclosedElements()) {
                        if (member.getSimpleName().toString().equals(symbolName)
                                || type.getSimpleName().toString().equals(symbolName)) {

                            ElementHandle<Element> handle = ElementHandle.create(member);
                            org.netbeans.modules.refactoring.api.WhereUsedQuery query
                                    = new org.netbeans.modules.refactoring.api.WhereUsedQuery(Lookups.singleton(handle));

                            RefactoringSession session = RefactoringSession.create("Find Usages");
                            query.prepare(session);
                            session.doRefactoring(true);

                            session.getRefactoringElements().forEach(elem
                                    -> result.append("Usage: ").append(elem.getDisplayText()).append("\n")
                            );
                        }
                    }
                }
            }, true);
            return result.length() == 0 ? "No usages found." : result.toString();
        }, false).toString();
        return jr;
    }
}
