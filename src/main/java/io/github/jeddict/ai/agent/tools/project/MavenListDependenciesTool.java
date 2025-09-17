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
package io.github.jeddict.ai.agent.tools.project;

import dev.langchain4j.agent.tool.Tool;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MavenListDependenciesTool extends BaseBuildTool {

    public MavenListDependenciesTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "MavenListDependenciesTool_listDependencies",
        value = "List all dependencies in the pom.xml file"
    )
    public String listDependencies() throws Exception {
        progress("Listing dependencies");
        try {
            FileObject projectDir = FileUtil.toFileObject(Paths.get(basedir));
            FileObject pomFile = projectDir.getFileObject("pom.xml");
            if (pomFile == null || !pomFile.isValid()) {
                progress("pom.xml not found in project directory");
                return "pom.xml not found in project directory";
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pomFile.getInputStream());
            doc.getDocumentElement().normalize();

            NodeList dependenciesNodes = doc.getElementsByTagName("dependencies");
            if (dependenciesNodes.getLength() == 0) {
                progress("No dependencies section found in pom.xml");
                return "No dependencies section found in pom.xml";
            }

            Element dependenciesElement = (Element) dependenciesNodes.item(0);
            NodeList dependencyList = dependenciesElement.getElementsByTagName("dependency");

            List<String> dependencies = new ArrayList<>();
            for (int i = 0; i < dependencyList.getLength(); i++) {
                Element dep = (Element) dependencyList.item(i);
                String g = dep.getElementsByTagName("groupId").item(0).getTextContent();
                String a = dep.getElementsByTagName("artifactId").item(0).getTextContent();
                String v = dep.getElementsByTagName("version").item(0).getTextContent();
                String scope = "";
                NodeList scopeNodes = dep.getElementsByTagName("scope");
                if (scopeNodes.getLength() > 0) {
                    scope = scopeNodes.item(0).getTextContent();
                }
                dependencies.add(String.format("%s:%s:%s%s", g, a, v, scope.isEmpty() ? "" : ":" + scope));
            }

            progress("Dependencies listed");
            if (dependencies.isEmpty()) {
                return "No dependencies found in pom.xml";
            }
            return String.join("\n", dependencies);
        } catch (Exception e) {
            progress("Failed to list dependencies: " + e.getMessage());

            throw e;
        }
    }
}
