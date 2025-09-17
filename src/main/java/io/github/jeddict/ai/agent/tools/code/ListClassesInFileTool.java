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
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.JavaSource;

public class ListClassesInFileTool extends BaseCodeTool {

    public ListClassesInFileTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "ListClassesInFileTool_listClassesInFile",
        value = "List all classes declared in a given Java file by path"
    )
    public String listClassesInFile(String path) throws Exception {
        progress("Listing classes in " + path);
        return withJavaSource(path, javaSource -> {
            final StringBuilder result = new StringBuilder();
            javaSource.runUserActionTask(cc -> {
                cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                List<TypeElement> classes = ElementFilter.typesIn(cc.getTopLevelElements());
                for (TypeElement clazz : classes) {
                    result.append("Class: ").append(clazz.getQualifiedName()).append("\n");
                }
            }, true);
            return result.toString();
        }, false).toString();

//.replace("File not found", "No classes found").replace("Not a Java source file", "No classes found");
    }
}
