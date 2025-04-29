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
package io.github.jeddict.ai.util;

import io.github.jeddict.ai.components.AssistantTopComponent;
import static io.github.jeddict.ai.util.MimeUtil.JAVA_MIME;
import static io.github.jeddict.ai.util.MimeUtil.MIME_MARKDOWN;
import static io.github.jeddict.ai.util.MimeUtil.MIME_PLAIN_TEXT;
import static io.github.jeddict.ai.util.MimeUtil.MIME_PUML;
import java.awt.Font;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import java.awt.FontMetrics;
import java.util.List;
import javax.swing.text.AttributeSet;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Shiwani Gupta
 */
public class EditorUtil {

    public static String updateEditors(AssistantTopComponent topComponent, String text, List<FileObject> fileObjects) {
        StringBuilder code = new StringBuilder();
        
        topComponent.clear();
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        
        for (MarkdownParser.MarkdownBlock block : MarkdownParser.parseMarkdown(text)) {
            if (block.type.equals("text")) {
                String html = renderer.render(parser.parse(block.content));
                topComponent.createHtmlPane(html);
            } else {
                code.append('\n').append(block.content).append('\n');
                String mimeType = getMimeType(block.type);
                if (MIME_PUML.equals(mimeType)) {
                    topComponent.createSVGPane(block.content);
                } else if (MIME_MARKDOWN.equals(mimeType)) {
                    topComponent.createMarkdownPane(block.content);
                } else {
                    topComponent.createCodePane(mimeType, block.content);
                }
            }
        }
        
        topComponent.getParseCodeEditor(fileObjects);
        return code.toString();
    }
    

    private static int getWordBreakLimit(AssistantTopComponent topComponent) {
        int width;
        try {
            Font font = getFontFromMimeType(MIME_PLAIN_TEXT);
            FontMetrics metrics = topComponent.getFontMetrics(font);
            width = (int) (topComponent.getWidth() / metrics.charWidth('O'));
        } catch (Exception ex) {
            width = 100;
        }
        return width;
    }

    public static String addLineBreaksToMarkdown(String markdown, int maxLineLength) {
        String[] lines = markdown.split("\n");
        StringBuilder formattedMarkdown = new StringBuilder();

        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                formattedMarkdown.append(breakLongLine(line, maxLineLength));
            }
            formattedMarkdown.append('\n');
        }

        return formattedMarkdown.toString();
    }

    private static String breakLongLine(String line, int maxLineLength) {
        StringBuilder formattedLine = new StringBuilder();
        int lastBreakIndex = 0;

        for (int i = 0; i < line.length(); i++) {
            if (i - lastBreakIndex >= maxLineLength && (line.charAt(i) == '.' || line.charAt(i) == ',')) {
                formattedLine.append(line, lastBreakIndex, i + 1).append("<br>");
                lastBreakIndex = i + 1;
            }
        }

        if (lastBreakIndex < line.length()) {
            formattedLine.append(line.substring(lastBreakIndex));
        }

        return formattedLine.toString();
    }

    private static final Map<String, String> OPENAI_NETBEANS_EDITOR_MAP = new HashMap<>();
    private static final Map<String, String> REVERSE_OPENAI_NETBEANS_EDITOR_MAP = new HashMap<>();

    static {
        // OpenAI Code Block and NetBeans Editor MIME type mappings
        OPENAI_NETBEANS_EDITOR_MAP.put("java", JAVA_MIME);
        OPENAI_NETBEANS_EDITOR_MAP.put("puml", "text/x-puml");
        OPENAI_NETBEANS_EDITOR_MAP.put("plantuml", "text/x-puml");
        OPENAI_NETBEANS_EDITOR_MAP.put("md", "text/x-markdown");
        OPENAI_NETBEANS_EDITOR_MAP.put("markdown", "text/x-markdown");
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
        OPENAI_NETBEANS_EDITOR_MAP.put("jakarta", JAVA_MIME); // General Jakarta EE Java files
        OPENAI_NETBEANS_EDITOR_MAP.put("jsp", "text/x-jsp"); // JavaServer Pages
        OPENAI_NETBEANS_EDITOR_MAP.put("faces", "text/x-jsf"); // JavaServer Faces
        OPENAI_NETBEANS_EDITOR_MAP.put("webxml", "text/xml"); // web.xml deployment descriptor
        OPENAI_NETBEANS_EDITOR_MAP.put("persistence", JAVA_MIME); // JPA persistence.xml files
        OPENAI_NETBEANS_EDITOR_MAP.put("beans", JAVA_MIME); // CDI beans.xml files
        OPENAI_NETBEANS_EDITOR_MAP.put("context", JAVA_MIME); // CDI context.xml files
        OPENAI_NETBEANS_EDITOR_MAP.put("config", "text/x-properties"); // Configuration properties files
        OPENAI_NETBEANS_EDITOR_MAP.put("js", "text/javascript"); // JavaScript files for web applications

        for (Map.Entry<String, String> entry : OPENAI_NETBEANS_EDITOR_MAP.entrySet()) {
            REVERSE_OPENAI_NETBEANS_EDITOR_MAP.put(entry.getValue(), entry.getKey());

            REVERSE_OPENAI_NETBEANS_EDITOR_MAP.put(JAVA_MIME, "java");
            REVERSE_OPENAI_NETBEANS_EDITOR_MAP.put("text/xml", "xml");
            REVERSE_OPENAI_NETBEANS_EDITOR_MAP.put("text/javascript", "js");
            REVERSE_OPENAI_NETBEANS_EDITOR_MAP.put("text/x-yaml", "yaml");
            REVERSE_OPENAI_NETBEANS_EDITOR_MAP.put("text/html", "html");
        }

    }

    // Method to get the NetBeans MIME type for a given ChatGPT code block type
    public static String getMimeType(String chatGptType) {
         if (chatGptType == null) {
            return MIME_PLAIN_TEXT;
        }
        return OPENAI_NETBEANS_EDITOR_MAP.getOrDefault(chatGptType, MIME_PLAIN_TEXT); // Default to binary if not found
    }

    public static String getExtension(String mimeType) {
        if (mimeType == null) {
            return "java";
        }
        return REVERSE_OPENAI_NETBEANS_EDITOR_MAP.getOrDefault(mimeType, null); // Returns null if not found
    }

    public static boolean isSuitableForWebAppDirectory(String mimeType) {
        // Define the allowed MIME types for src/main/webapp
        Set<String> allowedMimeTypes = new HashSet<>(Arrays.asList(
                "text/html", // HTML files
                "text/x-jsp", // JSP files
                "text/css", // CSS files
                "text/x-scss", // SCSS files
                "text/x-less", // LESS files
                "text/javascript", // JavaScript files
                "text/x-vue", // Vue.js files
                "text/x-pug", // Pug template engine files
                "text/xml", // XML files (for web-related configs like web.xml)
                "text/x-ts", // TypeScript files
                "text/x-jsx" // JSX files (React.js)
        ));

        // Check if the MIME type is allowed for web applications
        return allowedMimeTypes.contains(mimeType);
    }
    
  

     public static String getHTMLContent(int wrapWidth, String bodyContent) {
        Font newFont = getFontFromMimeType("text/html");
        java.awt.Color textColor = getTextColorFromMimeType("text/html"); // Get text color
        java.awt.Color backgroundColor = getBackgroundColorFromMimeType("text/html"); // Get background color

        String newContent = """
    <html>
      <head>
        <style>
          html, body { margin: 0; padding: 10px; width:NB_WRAP_WIDTHpx; font-family: 'NB_FONT_NAME'; font-size: NB_FONT_SIZEpx; line-height: 1.5; color: NB_FONT_COLOR; background-color: NB_BACKGROUND_COLOR; word-wrap: break-word; overflow-wrap: break-word; white-space: normal; }
          h1, h2, h3, h4, h5, h6 { margin: 0 0 10px 0; font-weight: bold; font-size: inherit; }
          p { margin: 0 0 10px 0; }
          a { color: #007bff; text-decoration: none; }
          a:hover { text-decoration: underline; }
          strong { font-weight: bold; }
        </style>
      </head>
      <body>
        """ + bodyContent + """
      </body>
    </html>
    """;
        newContent = newContent.replace("NB_FONT_SIZE", String.valueOf(newFont.getSize()));
        newContent = newContent.replace("NB_FONT_NAME", newFont.getName());
        newContent = newContent.replace("NB_WRAP_WIDTH", String.valueOf(wrapWidth));

        if (textColor != null) {
            newContent = newContent.replace("NB_FONT_COLOR", "#" + Integer.toHexString(textColor.getRGB()).substring(2).toUpperCase());
        }
        if (backgroundColor != null) {
            newContent = newContent.replace("NB_BACKGROUND_COLOR", "#" + Integer.toHexString(backgroundColor.getRGB()).substring(2).toUpperCase());
        }
        return newContent;
    }

    public static Font getFontFromMimeType(String mimeType) {
        FontColorSettings fcs = MimeLookup.getLookup(MimePath.parse(mimeType))
                .lookup(FontColorSettings.class);

        if (fcs != null) {
            AttributeSet defaultSet = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
            if (defaultSet != null) {
                String fontFamily = (String) defaultSet.getAttribute(javax.swing.text.StyleConstants.FontFamily);
                Integer fontSize = (Integer) defaultSet.getAttribute(javax.swing.text.StyleConstants.FontSize);
                if (fontFamily != null && fontSize != null) {
                    return new Font(fontFamily, Font.PLAIN, fontSize);
                }
            }
        }
        return null;
    }

    public static java.awt.Color getTextColorFromMimeType(String mimeType) {
        FontColorSettings fcs = MimeLookup.getLookup(MimePath.parse(mimeType))
                .lookup(FontColorSettings.class);

        if (fcs != null) {
            AttributeSet defaultSet = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
            if (defaultSet != null) {
                return (java.awt.Color) defaultSet.getAttribute(javax.swing.text.StyleConstants.Foreground);
            }
        }
        return null; // Default color if not found
    }

    public static java.awt.Color getBackgroundColorFromMimeType(String mimeType) {
        FontColorSettings fcs = MimeLookup.getLookup(MimePath.parse(mimeType))
                .lookup(FontColorSettings.class);

        if (fcs != null) {
            AttributeSet defaultSet = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
            if (defaultSet != null) {
                return (java.awt.Color) defaultSet.getAttribute(javax.swing.text.StyleConstants.Background);
            }
        }
        return null; // Default background color if not found
    }
}
