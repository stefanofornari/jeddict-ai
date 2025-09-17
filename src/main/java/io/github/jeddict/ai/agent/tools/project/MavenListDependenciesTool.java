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

//
// TODO: review
//

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
