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
import org.netbeans.api.java.source.JavaSource;

public class FormatFileTool extends BaseCodeTool {

    public FormatFileTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "FormatFileTool_formatFile",
        value = "Format a Java file by path using NetBeans code formatter"
    )
    public String formatFile(String path) throws Exception {
        progress("Formatting " + path);
        return withJavaSource(path, javaSource -> {
            javaSource.runModificationTask(cc -> {
                cc.toPhase(JavaSource.Phase.UP_TO_DATE);
                // NetBeans formatter applies automatically on save
            }).commit();
            return "File formatted successfully";
        }, true);
    }
}
