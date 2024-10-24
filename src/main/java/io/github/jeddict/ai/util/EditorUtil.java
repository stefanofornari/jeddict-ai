/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.jeddict.ai.util;

import io.github.jeddict.ai.components.AssistantTopComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 *
 * @author Shiwani Gupta
 */
public class EditorUtil {

    public static JEditorPane updateEditors(AssistantTopComponent topComponent, String text) {
        JEditorPane editorPane = null;

        topComponent.clear();
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String[] parts = text.split("```");
        String regex = "(\\w+)\\n([\\s\\S]+)";
        Pattern pattern = Pattern.compile(regex);

        for (int i = 0; i < parts.length; i++) {
            if (i % 2 == 0) {
                // Text part
                System.out.println("Text Part " + (i / 2 + 1) + ":");
                System.out.println();

                String html = renderer.render(parser.parse(parts[i].trim()));
                editorPane = topComponent.createHtmlPane(html);
            } else {
                Matcher matcher = pattern.matcher(parts[i]);
                if (matcher.matches()) {
                    String codeType = matcher.group(1);
                    String codeContent = matcher.group(2);
                    System.out.println("Code Type: " + codeType);
                    System.out.println("Code Content:\n" + codeContent);
                    editorPane = topComponent.createCodePane(getMimeType(codeType), codeContent);
                } else {
                    String html = renderer.render(parser.parse(parts[i].trim()));
                    editorPane = topComponent.createHtmlPane(html);
                }
            }
        }

        if (editorPane != null) {
            editorPane.setCaretPosition(0);
        }
        return editorPane;
    }

    private static Map<String, String> OPENAI_NETBEANS_EDITOR_MAP = new HashMap<>();

    static {
        // OpenAI Code Block and NetBeans Editor MIME type mappings
        OPENAI_NETBEANS_EDITOR_MAP.put("java", "text/x-java");
        OPENAI_NETBEANS_EDITOR_MAP.put("xml", "text/xml");
        OPENAI_NETBEANS_EDITOR_MAP.put("python", "text/x-python");
        OPENAI_NETBEANS_EDITOR_MAP.put("javascript", "text/javascript");
        OPENAI_NETBEANS_EDITOR_MAP.put("html", "text/html");
        OPENAI_NETBEANS_EDITOR_MAP.put("css", "text/css");
        OPENAI_NETBEANS_EDITOR_MAP.put("sql", "text/x-sql");
        OPENAI_NETBEANS_EDITOR_MAP.put("csharp", "text/x-csharp");
        OPENAI_NETBEANS_EDITOR_MAP.put("cpp", "text/x-c++src");
        OPENAI_NETBEANS_EDITOR_MAP.put("bash", "text/x-shellscript");
        OPENAI_NETBEANS_EDITOR_MAP.put("ruby", "text/x-ruby");
        OPENAI_NETBEANS_EDITOR_MAP.put("go", "text/x-go");
        OPENAI_NETBEANS_EDITOR_MAP.put("kotlin", "text/x-kotlin");
        OPENAI_NETBEANS_EDITOR_MAP.put("php", "text/x-php");
        OPENAI_NETBEANS_EDITOR_MAP.put("r", "text/x-r");
        OPENAI_NETBEANS_EDITOR_MAP.put("swift", "text/x-swift");
        OPENAI_NETBEANS_EDITOR_MAP.put("typescript", "text/typescript");
        OPENAI_NETBEANS_EDITOR_MAP.put("scala", "text/x-scala");
        OPENAI_NETBEANS_EDITOR_MAP.put("dart", "text/x-dart"); // Consider "application/dart"
        OPENAI_NETBEANS_EDITOR_MAP.put("perl", "text/x-perl");
        OPENAI_NETBEANS_EDITOR_MAP.put("yaml", "text/x-yaml");
        OPENAI_NETBEANS_EDITOR_MAP.put("json", "text/x-json");
        OPENAI_NETBEANS_EDITOR_MAP.put("asm", "text/x-asm");
        OPENAI_NETBEANS_EDITOR_MAP.put("haskell", "text/x-haskell");
        OPENAI_NETBEANS_EDITOR_MAP.put("markdown", "text/x-markdown");
        OPENAI_NETBEANS_EDITOR_MAP.put("latex", "text/x-tex");
        OPENAI_NETBEANS_EDITOR_MAP.put("groovy", "text/x-groovy");
        OPENAI_NETBEANS_EDITOR_MAP.put("powershell", "text/x-powershell");
        OPENAI_NETBEANS_EDITOR_MAP.put("vb", "text/x-vb");
        OPENAI_NETBEANS_EDITOR_MAP.put("scheme", "text/x-scheme");
        OPENAI_NETBEANS_EDITOR_MAP.put("rust", "text/x-rust");
        OPENAI_NETBEANS_EDITOR_MAP.put("objectivec", "text/x-objectivec");
        OPENAI_NETBEANS_EDITOR_MAP.put("elixir", "text/x-elixir");
        OPENAI_NETBEANS_EDITOR_MAP.put("lua", "text/x-lua");
        OPENAI_NETBEANS_EDITOR_MAP.put("cobol", "text/x-cobol");
        OPENAI_NETBEANS_EDITOR_MAP.put("fsharp", "text/x-fsharp");
        OPENAI_NETBEANS_EDITOR_MAP.put("crystal", "text/x-crystal");
        OPENAI_NETBEANS_EDITOR_MAP.put("actionscript", "text/x-actionscript");
        OPENAI_NETBEANS_EDITOR_MAP.put("nim", "text/x-nim");
        OPENAI_NETBEANS_EDITOR_MAP.put("vhdl", "text/x-vhdl");
        OPENAI_NETBEANS_EDITOR_MAP.put("sas", "text/x-sas");
        OPENAI_NETBEANS_EDITOR_MAP.put("cakephp", "text/x-cakephp");
        OPENAI_NETBEANS_EDITOR_MAP.put("laravel", "text/x-laravel");
        OPENAI_NETBEANS_EDITOR_MAP.put("cmake", "text/x-cmake");
        OPENAI_NETBEANS_EDITOR_MAP.put("embeddedc", "text/x-embeddedc");
        OPENAI_NETBEANS_EDITOR_MAP.put("sass", "text/x-sass"); // Separate from SCSS
        OPENAI_NETBEANS_EDITOR_MAP.put("scss", "text/x-scss");
        OPENAI_NETBEANS_EDITOR_MAP.put("less", "text/x-less");
        OPENAI_NETBEANS_EDITOR_MAP.put("pug", "text/x-pug");
        OPENAI_NETBEANS_EDITOR_MAP.put("coffeescript", "text/x-coffeescript");
        OPENAI_NETBEANS_EDITOR_MAP.put("vue", "text/x-vue");
        OPENAI_NETBEANS_EDITOR_MAP.put("jsx", "text/jsx");
        OPENAI_NETBEANS_EDITOR_MAP.put("c", "text/x-c"); // C language
        OPENAI_NETBEANS_EDITOR_MAP.put("clojure", "text/x-clojure"); // Clojure
        OPENAI_NETBEANS_EDITOR_MAP.put("forth", "text/x-forth"); // Forth
        OPENAI_NETBEANS_EDITOR_MAP.put("smalltalk", "text/x-smalltalk"); // Smalltalk
        OPENAI_NETBEANS_EDITOR_MAP.put("sml", "text/x-sml"); // Standard ML
        OPENAI_NETBEANS_EDITOR_MAP.put("ada", "text/x-ada"); // Ada
        OPENAI_NETBEANS_EDITOR_MAP.put("scratch", "text/x-scratch"); // Scratch

// Missing types added
        OPENAI_NETBEANS_EDITOR_MAP.put("properties", "text/x-properties"); // Properties files
        OPENAI_NETBEANS_EDITOR_MAP.put("dockerfile", "text/x-dockerfile"); // Dockerfiles
        OPENAI_NETBEANS_EDITOR_MAP.put("csv", "text/csv"); // CSV files
        OPENAI_NETBEANS_EDITOR_MAP.put("graphql", "application/graphql"); // GraphQL files
        OPENAI_NETBEANS_EDITOR_MAP.put("json5", "text/x-json5"); // JSON5 files
        OPENAI_NETBEANS_EDITOR_MAP.put("yml", "text/x-yaml"); // YAML files (alternative extension)
        OPENAI_NETBEANS_EDITOR_MAP.put("ini", "text/x-ini"); // INI files
        OPENAI_NETBEANS_EDITOR_MAP.put("html5", "text/html"); // HTML5 files

// OpenAI Code Block and NetBeans Editor MIME type mappings for Jakarta EE
        OPENAI_NETBEANS_EDITOR_MAP.put("jakarta", "text/x-java"); // General Jakarta EE Java files
        OPENAI_NETBEANS_EDITOR_MAP.put("jsp", "text/x-jsp"); // JavaServer Pages
        OPENAI_NETBEANS_EDITOR_MAP.put("faces", "text/x-jsf"); // JavaServer Faces
        OPENAI_NETBEANS_EDITOR_MAP.put("webxml", "text/xml"); // web.xml deployment descriptor
        OPENAI_NETBEANS_EDITOR_MAP.put("persistence", "text/x-java"); // JPA persistence.xml files
        OPENAI_NETBEANS_EDITOR_MAP.put("beans", "text/x-java"); // CDI beans.xml files
        OPENAI_NETBEANS_EDITOR_MAP.put("context", "text/x-java"); // CDI context.xml files
        OPENAI_NETBEANS_EDITOR_MAP.put("config", "text/x-properties"); // Configuration properties files
        OPENAI_NETBEANS_EDITOR_MAP.put("css", "text/css"); // CSS files for styling
        OPENAI_NETBEANS_EDITOR_MAP.put("js", "text/javascript"); // JavaScript files for web applications
        
    }

    // Method to get the NetBeans MIME type for a given ChatGPT code block type
    public static String getMimeType(String chatGptType) {
        return OPENAI_NETBEANS_EDITOR_MAP.getOrDefault(chatGptType, "application/octet-stream"); // Default to binary if not found
    }
}
