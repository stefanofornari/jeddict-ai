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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MavenMavenDependencyTool extends BaseBuildTool {

    public MavenMavenDependencyTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "MavenMavenDependencyTool_removeDependency",
        value = "Remove a dependency from the pom.xml file"
    )
    public String removeDependency(String groupId, String artifactId)
    throws Exception {
        progress("Removing dependency: " + groupId + ":" + artifactId);
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
            for (int i = 0; i < dependencyList.getLength(); i++) {
                Element dep = (Element) dependencyList.item(i);
                String g = dep.getElementsByTagName("groupId").item(0).getTextContent();
                String a = dep.getElementsByTagName("artifactId").item(0).getTextContent();
                if (g.equals(groupId) && a.equals(artifactId)) {
                    dependenciesElement.removeChild(dep);

                    // Write changes back to pom.xml
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    DOMSource source = new DOMSource(doc);
                    StreamResult result = new StreamResult(pomFile.getOutputStream());
                    transformer.transform(source, result);

                    progress("Dependency removed successfully: " + groupId + ":" + artifactId);
                    return "Dependency removed successfully from pom.xml";
                }
            }

            progress("Dependency not found: " + groupId + ":" + artifactId);
            return "Dependency not found in pom.xml";
        } catch (Exception e) {
            progress("Failed to remove dependency: " + e.getMessage());

            throw e;
        }
    }
}
