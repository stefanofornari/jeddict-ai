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
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.source.JavaSource;

public class ListMethodsInFileTool extends BaseCodeTool {

    public ListMethodsInFileTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "ListMethodsInFileTool_listMethodsInFile",
        value = "List all methods of a class in a given Java file by path"
    )
    public String listMethodsInFile(String path) throws Exception {
        progress("Listing methods in " + path);
        return withJavaSource(path, javaSource -> {
            final StringBuilder result = new StringBuilder();
            javaSource.runUserActionTask(cc -> {
                cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                for (Element element : cc.getTopLevelElements()) {
                    List<? extends Element> enclosed = element.getEnclosedElements();
                    enclosed.stream()
                            .filter(e -> e.getKind() == ElementKind.METHOD || e.getKind() == ElementKind.CONSTRUCTOR)
                            .forEach(m -> result.append("Method: ").append(m.toString()).append("\n"));
                }
            }, true);
            return result.toString();
        }, false).toString();
    }
}
