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
import java.util.Set;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.util.Lookup;

public class SearchSymbolTool extends BaseCodeTool {

    private final Lookup lookup;

    public SearchSymbolTool(final String basedir, final Lookup lookup) {
        super(basedir);
        this.lookup = lookup;
    }

    @Tool(
        name = "SearchSymbolTool_searchSymbol",
        value = "Search for a symbol (class, method, or field) in the whole project"
    )
    public String searchSymbol(String symbolName)
    throws Exception {
        progress("Searching symbol " + symbolName);

        StringBuilder result = new StringBuilder();
        Sources sources = lookup.lookup(Sources.class);
        if (sources == null) {
            return "No sources found in project.";
        }

        for (SourceGroup sg : sources.getSourceGroups(Sources.TYPE_GENERIC)) {
            JavaSource javaSource = JavaSource.forFileObject(sg.getRootFolder());
            if (javaSource == null) {
                continue;
            }

            javaSource.runUserActionTask(cc -> {
                cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ClassIndex idx = cc.getClasspathInfo().getClassIndex();
                Set<ElementHandle<TypeElement>> handles
                        = idx.getDeclaredTypes(symbolName, ClassIndex.NameKind.SIMPLE_NAME,
                                Set.of(ClassIndex.SearchScope.SOURCE));

                for (ElementHandle<TypeElement> h : handles) {
                    result.append("Found: ").append(h.getQualifiedName()).append("\n");
                }
            }, true);
        }

        return result.length() == 0 ? "No matches found." : result.toString();
    }
}
