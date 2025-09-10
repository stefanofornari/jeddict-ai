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
package io.github.jeddict.ai.agent;

import dev.langchain4j.agent.tool.Tool;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import io.github.jeddict.ai.lang.JeddictStreamHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Tool to manage dependencies in the pom.xml file.
 * Allows adding, removing, updating, and listing dependencies in the project's pom.xml.
 *
 * Author: Assistant
 */
public class MavenTools {

    private final Project project;
    private final JeddictStreamHandler handler;
    private final static Logger logger = Logger.getLogger(MavenTools.class.getName());

    public MavenTools(Project project, JeddictStreamHandler handler) {
        this.project = project;
        this.handler = handler;
    }

    @Tool("Add a dependency to the pom.xml file")
    public String addDependency(String groupId, String artifactId, String version) {
        log("Adding dependency", groupId + ":" + artifactId + ":" + version);
        return addDependency(groupId, artifactId, version, null);
    }

    @Tool("Add a dependency with scope to the pom.xml file")
    public String addDependencyWithScope(String groupId, String artifactId, String version, String scope) {
        log("Adding dependency with scope", groupId + ":" + artifactId + ":" + version + ":" + scope);
        return addDependency(groupId, artifactId, version, scope);
    }

    private String addDependency(String groupId, String artifactId, String version, String scope) {
        try {
            FileObject projectDir = project.getProjectDirectory();
            FileObject pomFile = projectDir.getFileObject("pom.xml");
            if (pomFile == null || !pomFile.isValid()) {
                log("pom.xml not found in project directory", null);
                return "pom.xml not found in project directory";
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pomFile.getInputStream());
            doc.getDocumentElement().normalize();

            NodeList dependenciesNodes = doc.getElementsByTagName("dependencies");
            Element dependenciesElement;
            if (dependenciesNodes.getLength() == 0) {
                dependenciesElement = doc.createElement("dependencies");
                doc.getDocumentElement().appendChild(dependenciesElement);
            } else {
                dependenciesElement = (Element) dependenciesNodes.item(0);
            }

            // Check if dependency already exists
            NodeList dependencyList = dependenciesElement.getElementsByTagName("dependency");
            for (int i = 0; i < dependencyList.getLength(); i++) {
                Element dep = (Element) dependencyList.item(i);
                String g = dep.getElementsByTagName("groupId").item(0).getTextContent();
                String a = dep.getElementsByTagName("artifactId").item(0).getTextContent();
                if (g.equals(groupId) && a.equals(artifactId)) {
                    log("Dependency already exists", groupId + ":" + artifactId);
                    return "Dependency already exists in pom.xml";
                }
            }

            Element dependency = doc.createElement("dependency");

            Element gId = doc.createElement("groupId");
            gId.setTextContent(groupId);
            Element aId = doc.createElement("artifactId");
            aId.setTextContent(artifactId);
            Element ver = doc.createElement("version");
            ver.setTextContent(version);

            dependency.appendChild(gId);
            dependency.appendChild(aId);
            dependency.appendChild(ver);

            if (scope != null && !scope.isEmpty()) {
                Element scopeElement = doc.createElement("scope");
                scopeElement.setTextContent(scope);
                dependency.appendChild(scopeElement);
            }

            dependenciesElement.appendChild(dependency);

            // Write changes back to pom.xml
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(pomFile.getOutputStream());
            transformer.transform(source, result);

            log("Dependency added successfully", groupId + ":" + artifactId + ":" + version);
            return "Dependency added successfully to pom.xml";
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error adding dependency to pom.xml", ex);
            log("Failed to add dependency", ex.getMessage());
            return "Failed to add dependency: " + ex.getMessage();
        }
    }

    @Tool("Remove a dependency from the pom.xml file")
    public String removeDependency(String groupId, String artifactId) {
        log("Removing dependency", groupId + ":" + artifactId);
        try {
            FileObject projectDir = project.getProjectDirectory();
            FileObject pomFile = projectDir.getFileObject("pom.xml");
            if (pomFile == null || !pomFile.isValid()) {
                log("pom.xml not found in project directory", null);
                return "pom.xml not found in project directory";
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pomFile.getInputStream());
            doc.getDocumentElement().normalize();

            NodeList dependenciesNodes = doc.getElementsByTagName("dependencies");
            if (dependenciesNodes.getLength() == 0) {
                log("No dependencies section found in pom.xml", null);
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

                    log("Dependency removed successfully", groupId + ":" + artifactId);
                    return "Dependency removed successfully from pom.xml";
                }
            }

            log("Dependency not found", groupId + ":" + artifactId);
            return "Dependency not found in pom.xml";
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error removing dependency from pom.xml", ex);
            log("Failed to remove dependency", ex.getMessage());
            return "Failed to remove dependency: " + ex.getMessage();
        }
    }

    @Tool("List all dependencies in the pom.xml file")
    public String listDependencies() {
        log("Listing dependencies", null);
        try {
            FileObject projectDir = project.getProjectDirectory();
            FileObject pomFile = projectDir.getFileObject("pom.xml");
            if (pomFile == null || !pomFile.isValid()) {
                log("pom.xml not found in project directory", null);
                return "pom.xml not found in project directory";
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pomFile.getInputStream());
            doc.getDocumentElement().normalize();

            NodeList dependenciesNodes = doc.getElementsByTagName("dependencies");
            if (dependenciesNodes.getLength() == 0) {
                log("No dependencies section found in pom.xml", null);
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

            log("Dependencies listed", null);
            if (dependencies.isEmpty()) {
                return "No dependencies found in pom.xml";
            }
            return String.join("\n", dependencies);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error listing dependencies from pom.xml", ex);
            log("Failed to list dependencies", ex.getMessage());
            return "Failed to list dependencies: " + ex.getMessage();
        }
    }

    @Tool("Update the version of an existing dependency in the pom.xml file")
    public String updateDependencyVersion(String groupId, String artifactId, String newVersion) {
        log("Updating dependency version", groupId + ":" + artifactId + ":" + newVersion);
        try {
            FileObject projectDir = project.getProjectDirectory();
            FileObject pomFile = projectDir.getFileObject("pom.xml");
            if (pomFile == null || !pomFile.isValid()) {
                log("pom.xml not found in project directory", null);
                return "pom.xml not found in project directory";
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pomFile.getInputStream());
            doc.getDocumentElement().normalize();

            NodeList dependenciesNodes = doc.getElementsByTagName("dependencies");
            if (dependenciesNodes.getLength() == 0) {
                log("No dependencies section found in pom.xml", null);
                return "No dependencies section found in pom.xml";
            }

            Element dependenciesElement = (Element) dependenciesNodes.item(0);
            NodeList dependencyList = dependenciesElement.getElementsByTagName("dependency");
            for (int i = 0; i < dependencyList.getLength(); i++) {
                Element dep = (Element) dependencyList.item(i);
                String g = dep.getElementsByTagName("groupId").item(0).getTextContent();
                String a = dep.getElementsByTagName("artifactId").item(0).getTextContent();
                if (g.equals(groupId) && a.equals(artifactId)) {
                    dep.getElementsByTagName("version").item(0).setTextContent(newVersion);

                    // Write changes back to pom.xml
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    DOMSource source = new DOMSource(doc);
                    StreamResult result = new StreamResult(pomFile.getOutputStream());
                    transformer.transform(source, result);

                    log("Dependency version updated successfully", groupId + ":" + artifactId + ":" + newVersion);
                    return "Dependency version updated successfully in pom.xml";
                }
            }

            log("Dependency not found", groupId + ":" + artifactId);
            return "Dependency not found in pom.xml";
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error updating dependency version in pom.xml", ex);
            log("Failed to update dependency", ex.getMessage());
            return "Failed to update dependency: " + ex.getMessage();
        }
    }

    @Tool("Check if a dependency exists in the pom.xml file")
    public boolean dependencyExists(String groupId, String artifactId) {
        log("Checking dependency existence", groupId + ":" + artifactId);
        try {
            FileObject projectDir = project.getProjectDirectory();
            FileObject pomFile = projectDir.getFileObject("pom.xml");
            if (pomFile == null || !pomFile.isValid()) {
                log("pom.xml not found in project directory", null);
                return false;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pomFile.getInputStream());
            doc.getDocumentElement().normalize();

            NodeList dependenciesNodes = doc.getElementsByTagName("dependencies");
            if (dependenciesNodes.getLength() == 0) {
                log("No dependencies section found in pom.xml", null);
                return false;
            }

            Element dependenciesElement = (Element) dependenciesNodes.item(0);
            NodeList dependencyList = dependenciesElement.getElementsByTagName("dependency");
            for (int i = 0; i < dependencyList.getLength(); i++) {
                Element dep = (Element) dependencyList.item(i);
                String g = dep.getElementsByTagName("groupId").item(0).getTextContent();
                String a = dep.getElementsByTagName("artifactId").item(0).getTextContent();
                if (g.equals(groupId) && a.equals(artifactId)) {
                    log("Dependency exists", groupId + ":" + artifactId);
                    return true;
                }
            }

            log("Dependency does not exist", groupId + ":" + artifactId);
            return false;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error checking dependency existence in pom.xml", ex);
            log("Failed to check dependency existence", ex.getMessage());
            return false;
        }
    }

    private void log(String action, String detail) {
        if (handler != null) {
            String message = action + (detail != null ? (": " + detail) : "") + "\n";
            handler.onToolingResponse(message);
        }
    }

}
