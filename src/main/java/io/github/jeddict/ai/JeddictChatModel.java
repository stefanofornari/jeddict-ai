/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.github.jeddict.ai;

/**
 *
 * @author Shiwani Gupta
 */
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.localai.LocalAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import static io.github.jeddict.ai.settings.GenAIProvider.ANTHROPIC;
import static io.github.jeddict.ai.settings.GenAIProvider.OLLAMA;
import io.github.jeddict.ai.models.LMStudioChatModel;
import static io.github.jeddict.ai.settings.GenAIProvider.DEEPINFRA;
import static io.github.jeddict.ai.settings.GenAIProvider.LM_STUDIO;
import static io.github.jeddict.ai.settings.GenAIProvider.OPEN_AI;
import io.github.jeddict.ai.settings.PreferencesManager;
import static io.github.jeddict.ai.util.MimeUtil.MIME_TYPE_DESCRIPTIONS;
import static io.github.jeddict.ai.util.StringUtil.removeCodeBlockMarkers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.json.JSONArray;
import org.json.JSONObject;

public class JeddictChatModel {

    private ChatLanguageModel model;
    private int cachedClassDatasLength = -1; // Cache the length of classDatas
    PreferencesManager preferencesManager = PreferencesManager.getInstance();

    public JeddictChatModel() {
        if (null != preferencesManager.getModel()) {
            switch (preferencesManager.getProvider()) {
                case GOOGLE ->
                    model = GoogleAiGeminiChatModel.builder()
                            .apiKey(preferencesManager.getApiKey())
                            .modelName(preferencesManager.getModelName())
                            .build();
                case OPEN_AI ->
                    model = OpenAiChatModel.builder()
                            .apiKey(preferencesManager.getApiKey())
                            .modelName(preferencesManager.getModelName())
                            .build();
                case DEEPINFRA, DEEPSEEK, GROQ, CUSTOM_OPEN_AI ->
                    model = OpenAiChatModel.builder()
                            .baseUrl(preferencesManager.getProviderLocation())
                            .apiKey(preferencesManager.getApiKey())
                            .modelName(preferencesManager.getModelName())
                            .build();
                case MISTRAL ->
                    model = MistralAiChatModel.builder()
                            .apiKey(preferencesManager.getApiKey())
                            .modelName(preferencesManager.getModelName())
                            .build();
                case ANTHROPIC ->
                    model = AnthropicChatModel.builder()
                            .apiKey(preferencesManager.getApiKey())
                            .modelName(preferencesManager.getModelName())
                            .build();
                case OLLAMA ->
                    model = OllamaChatModel.builder()
                            .baseUrl(preferencesManager.getProviderLocation())
                            .modelName(preferencesManager.getModelName())
                            .build();
                case LM_STUDIO ->
                    model = LMStudioChatModel.builder()
                            .baseUrl(preferencesManager.getProviderLocation())
                            .modelName(preferencesManager.getModelName())
                            .build();
                case GPT4ALL ->
                    model = LocalAiChatModel.builder()
                            .baseUrl(preferencesManager.getProviderLocation())
                            .modelName(preferencesManager.getModelName())
                            .build();
            }
        }
    }

    private String generate(String prompt) {
        if (model == null) {
            JOptionPane.showMessageDialog(null,
                    "AI assistance model not intitalized.",
                    "Error in AI Assistance",
                    JOptionPane.ERROR_MESSAGE);
        }
        try {
            return model.generate(prompt);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (e.getCause() != null
                    && e.getCause() instanceof dev.ai4j.openai4j.OpenAiHttpException) {
                JSONObject jsonObject = new JSONObject(e.getCause().getMessage());
                if (jsonObject.has("error") && jsonObject.getJSONObject("error").has("message")) {
                    errorMessage = jsonObject.getJSONObject("error").getString("message");
                }
            }
            if (errorMessage != null
                    && errorMessage.toLowerCase().contains("incorrect api key")) {
                JTextField apiKeyField = new JTextField(20);
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Set layout to BoxLayout

                panel.add(new JLabel("Incorrect API key. Please enter a new key:"));
                panel.add(Box.createVerticalStrut(10)); // Add space between label and text field
                panel.add(apiKeyField);

                int option = JOptionPane.showConfirmDialog(null, panel,
                        "API Key Required", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    preferencesManager.setApiKey(apiKeyField.getText().trim());
                }
            } else {
                JOptionPane.showMessageDialog(null,
                        "AI assistance failed to generate the requested response: " + errorMessage,
                        "Error in AI Assistance",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    public String generateJavadocForClass(String classContent) {
        String prompt
                = "You are an API server that responds only with Javadoc comments for class not the member of class. "
                + "Generate only the Javadoc wrapped with in /** ${javadoc} **/ for the following Java class not the member of class. Do not include any additional text or explanation.\n\n"
                + classContent;
        String answer = generate(prompt);
        System.out.println(answer);
        return answer;
    }

    public String generateJavadocForMethod(String methodContent) {
        String prompt
                = "You are an API server that responds only with Javadoc comments for method. "
                + "Generate only the Javadoc wrapped with in /** ${javadoc} **/ for the following Java method. Do not include any additional text or explanation.\n\n"
                + methodContent;
        String answer = generate(prompt);
        System.out.println(answer);
        return answer;
    }

    public String generateJavadocForField(String fieldContent) {
        String prompt
                = "You are an API server that responds only with Javadoc comments for field. "
                + "Generate only the Javadoc wrapped with in /** ${javadoc} **/ for the following Java variable. Do not include any additional text or explanation.\n\n"
                + fieldContent;
        String answer = generate(prompt);
        System.out.println(answer);
        return answer;
    }

    public String enhanceJavadocForClass(String existingJavadoc, String classContent) {
        String prompt
                = "You are an API server that enhances existing Javadoc comments for a class. "
                + "Given the existing Javadoc comment and the following Java class, enhance the Javadoc comment by adding more details if necessary. "
                + "Do not include any additional text or explanation, just the enhanced Javadoc wrapped with /** ${javadoc} **/.\n\n"
                + "Existing Javadoc:\n" + existingJavadoc + "\n\n"
                + "Java Class Content:\n" + classContent;
        String answer = generate(prompt);
        System.out.println(answer);
        return answer;
    }

    public String enhanceJavadocForMethod(String existingJavadoc, String methodContent) {
        String prompt
                = "You are an API server that enhances existing Javadoc comments for a method. "
                + "Given the existing Javadoc comment and the following Java method, enhance the Javadoc comment by adding more details if necessary. "
                + "Do not include any additional text or explanation, just the enhanced Javadoc wrapped with /** ${javadoc} **/.\n\n"
                + "Existing Javadoc:\n" + existingJavadoc + "\n\n"
                + "Java Method Content:\n" + methodContent;
        String answer = generate(prompt);
        System.out.println(answer);
        return answer;
    }

    public String enhanceJavadocForField(String existingJavadoc, String fieldContent) {
        String prompt
                = "You are an API server that enhances existing Javadoc comments for a field. "
                + "Given the existing Javadoc comment and the following Java field, enhance the Javadoc comment by adding more details if necessary. "
                + "Do not include any additional text or explanation, just the enhanced Javadoc wrapped with /** ${javadoc} **/.\n\n"
                + "Existing Javadoc:\n" + existingJavadoc + "\n\n"
                + "Java Field Content:\n" + fieldContent;
        String answer = generate(prompt);
        System.out.println(answer);
        return answer;
    }

    public String generateRestEndpointForClass(String classContent) {
        // Define a prompt to generate unique JAX-RS resource methods with necessary imports
        String prompt = "You are an API server that generates JAX-RS REST endpoints based on the provided Java class definition. "
                + "Analyze the context and functionality of the class to create meaningful and relevant REST endpoints. "
                + "Ensure that you create new methods for various HTTP operations (GET, POST, PUT, DELETE) and include all necessary imports for JAX-RS annotations and responses. "
                + "The generated methods should have some basic implementation and should not be empty. Avoid duplicating existing methods from the class content. "
                + "Format the output as a JSON object with two fields: 'imports' as array (list of necessary imports) and 'methodContent' as text. "
                + "Include all required imports such as Response, GET, POST, PUT, DELETE, Path, etc. "
                + "Example output:\n"
                + "{\n"
                + "  \"imports\": [\n"
                + "    \"jakarta.ws.rs.GET\",\n"
                + "    \"jakarta.ws.rs.POST\",\n"
                + "    \"jakarta.ws.rs.PUT\",\n"
                + "    \"jakarta.ws.rs.DELETE\",\n"
                + "    \"jakarta.ws.rs.core.Response\"\n"
                + "  ],\n"
                + "  \"methodContent\": \"@GET public Response getPing() { // implementation }@POST public Response createPing() { // implementation for createPing }@PUT public Response updatePing() { // implementation }@DELETE public Response deletePing() { // implementation for deletePing }\"\n"
                + "}\n\n"
                + "Only return methods with annotations, implementation details, and necessary imports for the given class. "
                + "Do not include class declarations, constructors, or unnecessary boilerplate code. Ensure the generated methods are unique and not duplicates of existing methods in the class content.\n\n"
                + classContent;

        // Generate the unique JAX-RS methods with imports
        String answer = generate(prompt);

        // Print and return the generated JAX-RS methods with imports
        System.out.println(answer);
        return answer;
    }

    public String updateMethodFromDevQuery(String javaClassContent, String methodContent, String developerRequest) {
        String prompt
                = "You are an API server that enhances Java methods based on user requests. "
                + "Given the following Java method and the developer's request, modify and enhance the method accordingly. "
                + "Incorporate any specific details or requirements mentioned by the developer. Do not include any additional text or explanation, just return the enhanced Java method source code.\n\n"
                + "Include all necessary imports relevant to the enhanced or newly created method. "
                + "Return only the Java method and its necessary imports, without including any class declarations, constructors, or other boilerplate code. "
                + "Do not include full java class, any additional text or explanation, just the imports and the method source code.\n\n"
                + "Format the output as a JSON object with two fields: 'imports' (list of necessary imports) and 'methodContent'. "
                + "Developer Request:\n" + developerRequest + "\n\n"
                + "Java Class Content:\n" + javaClassContent + "\n\n"
                + "Java Method Content:\n" + methodContent;

        // Generate the enhanced Java method
        String answer = generate(prompt);
        System.out.println(answer);
        return answer;
    }

    public String enhanceMethodFromMethodContent(String javaClassContent, String methodContent) {
        String prompt
                = "You are an API server that enhances or creates Java methods based on the method name, comments, and its content. "
                + "Given the following Java class content and Java method content, modify and enhance the method accordingly. "
                + "Include all necessary imports relevant to the enhanced or newly created method. "
                + "Return only the Java method and its necessary imports, without including any class declarations, constructors, or other boilerplate code. "
                + "Do not include full java class, any additional text or explanation, just the imports and the method source code.\n\n"
                + "Format the output as a JSON object with two fields: 'imports' (list of necessary imports) and 'methodContent'. "
                + "Java Class Content:\n" + javaClassContent + "\n\n"
                + "Java Method Content:\n" + methodContent;

        // Generate the enhanced or newly created Java method with necessary imports
        String answer = generate(prompt);
        System.out.println(answer);
        return answer;
    }

    public String fixMethodCompilationError(String javaClassContent, String methodContent, String errorMessage) {
        String prompt
                = "You are an API server that fixes compilation errors in Java methods based on the provided error messages. "
                + "Given the following Java method content, class content, and the error message, correct the method accordingly. "
                + "Ensure that all compilation errors indicated by the error message are resolved. "
                + "Include any necessary imports relevant to the fixed method. "
                + "Return only the corrected Java method and its necessary imports, without including any class declarations, constructors, or other boilerplate code. "
                + "Do not include full Java class, any additional text, or explanation—just the imports and the corrected method source code.\n\n"
                + "Format the output as a JSON object with two fields: 'imports' (list of necessary imports) and 'methodContent'. "
                + "Error Message:\n" + errorMessage + "\n\n"
                + "Java Class Content:\n" + javaClassContent + "\n\n"
                + "Java Method Content:\n" + methodContent;

        // Generate the fixed Java method
        String answer = generate(prompt);
        System.out.println(answer);
        return answer;
    }

    public String fixVariableError(String javaClassContent, String errorMessage) {
        String prompt
                = "You are an API server that fixes variable-related compilation errors in Java classes based on the provided error messages. "
                + "Given the following Java class content and the error message, correct given variable-related issues based on error message at class level. "
                + "Ensure that all compilation errors indicated by the error message, such as undeclared variables, incorrect variable types, or misuse of variables, are resolved. "
                + "Include any necessary imports relevant to the fixed method or class. "
                + "Return only the corrected variable content and its necessary imports, without including any unnecessary boilerplate code. "
                + "Do not include any additional text or explanation—just the imports and the corrected variable source code.\n\n"
                + "Format the output as a JSON object with two fields: 'imports' (list of necessary imports) and 'variableContent' (corrected variable line or content). "
                + "Error Message:\n" + errorMessage + "\n\n"
                + "Java Class Content:\n" + javaClassContent;

        // Generate the fixed Java class or method
        String answer = generate(prompt);
        System.out.println(answer);
        return answer;
    }

    public String enhanceVariableName(String variableContext, String methodContent, String classContent) {
        String prompt
                = "You are an API server that suggests a more meaningful and descriptive name for a specific variable in a given Java class. "
                + "Based on the provided Java class content and the variable context, suggest an improved name for the variable. "
                + "Return only the new variable name. Do not include any additional text or explanation.\n\n"
                + "Variable Context:\n" + variableContext + "\n\n"
                + (methodContent != null ? ("Java Method Content:\n" + methodContent + "\n\n") : "")
                + (classContent != null ? ("Java Class Content:\n" + classContent) : "");

        // Generate the new variable name
        String answer = generate(prompt);
        System.out.println(answer);
        return answer;
    }

    public List<String> suggestVariableNames(String classDatas, String variablePrefix, String classContent, String variableExpression) {
        String prompt;

//    if(variablePrefix == null || variablePrefix.isEmpty()) {
        prompt = "You are an API server that suggests a list of meaningful and descriptive names for a specific variable in a given Java class. "
                + "Based on the provided Java class content, variable prefix, and variable expression, generate a list of improved names for the variable. "
                + "Return only the list of suggested names, one per line, without any additional text or explanation.\n\n"
                + "Variable Prefix: " + variablePrefix + "\n\n"
                + "Variable Expression Line:\n" + variableExpression + "\n\n"
                //            + "Parent Content:\n" + parentContent + "\n\n"
                + "Java Class Content:\n" + classContent + "\n\n"
                + "Here is the context of all classes in the project, including variable names and method signatures (method bodies are excluded to avoid sending unnecessary code):\n" + classDatas;
//    }

        // Generate the list of suggested variable names
        String answer = generate(prompt);
        System.out.println(answer);

        // Split the response into a list and return
        return Arrays.asList(answer.split("\n"));
    }

    private String loadClassData(String prompt, String classDatas) {
        // Check if the length of classDatas has changed
        if (classDatas == null) {
            return prompt;
        }
        if (classDatas.length() != cachedClassDatasLength) {
            // Add classDatas to the prompt if the length has changed
            prompt += "\n\n" + "Here is the context of all classes in the project, including variable names and method signatures (method bodies are excluded to avoid sending unnecessary code):\n"
                    + classDatas;
            // Update cached length
            cachedClassDatasLength = classDatas.length();
        } else {
            // Don't include classDatas if it's unchanged
            prompt += "The class context has not changed.";
        }
        return prompt;
    }

    public List<String> suggestVariableNames(String classDatas, String classContent, String lineText) {
        String prompt = "You are an API server that suggests multiple meaningful and descriptive names for a specific variable in a given Java class. "
                + "Based on the provided Java class content and the line of code: \"" + lineText + "\", suggest a list of improved names for the variable represented by the placeholder ${SUGGEST_VAR_NAMES_LIST} in Java Class. "
                + "Do not include additional text; return only the suggestions as a JSON array.\n\n"
                + "Java Class Content:\n" + classContent;

        prompt = loadClassData(prompt, classDatas);

        // Generate the list of new variable names
        String jsonResponse = generate(prompt);

        // Parse the JSON response into a List
        List<String> variableNames = parseJsonToList(jsonResponse);

        return variableNames;
    }

    public List<String> suggestMethodNames(String classDatas, String classContent, String lineText) {
        String prompt = "You are an API server that suggests multiple meaningful and descriptive names for a specific method in a given Java class. "
                + "Based on the provided Java class content and the line of code: \"" + lineText + "\", suggest a list of improved names for the method represented by the placeholder ${SUGGEST_METHOD_NAMES_LIST} in Java Class. "
                + "Do not include additional text; return only the suggestions as a JSON array.\n\n"
                + "Java Class Content:\n" + classContent;

        prompt = loadClassData(prompt, classDatas);
        // Generate the list of new method names
        String jsonResponse = generate(prompt);

        // Parse the JSON response into a List
        List<String> methodNames = parseJsonToList(jsonResponse);

        return methodNames;
    }

    public List<String> suggestStringLiterals(String classDatas, String classContent, String lineText) {
        String prompt = "You are an API server that suggests multiple meaningful and descriptive string literals for a specific context in a given Java class. "
                + "Based on the provided Java class content and the line of code: \"" + lineText + "\", suggest a list of improved string literals represented by the placeholder ${SUGGEST_STRING_LITERAL_LIST} in Java Class. "
                + "Do not include additional text; return only the suggestions as a JSON array.\n\n"
                + "Java Class Content:\n" + classContent;

        prompt = loadClassData(prompt, classDatas);
        // Generate the list of new string literals
        String jsonResponse = generate(prompt);

        // Parse the JSON response into a List
        List<String> stringLiterals = parseJsonToListWithSplit(jsonResponse);

        return stringLiterals;
    }

    public List<String> suggestMethodInvocations(String classDatas, String classContent, String lineText) {
        String prompt = "You are an API server that suggests multiple meaningful and appropriate method invocations for a specific context in a given Java class. "
                + "Based on the provided Java class content and the line of code: \"" + lineText + "\", suggest a list of improved method invocations represented by the placeholder ${SUGGEST_METHOD_INVOCATION} in Java Class. "
                + "Do not include additional text; return only the suggestions as a JSON array.\n\n"
                + "Java Class Content:\n" + classContent;

        prompt = loadClassData(prompt, classDatas);

        // Generate the list of new method invocations
        String jsonResponse = generate(prompt);

        // Parse the JSON response into a List
        List<String> methodInvocations = parseJsonToList(jsonResponse);

        return methodInvocations;
    }

    public List<Snippet> suggestNextLineCode(String classDatas, String classContent, String lineText, TreePath path) {
        String prompt;

        if (path == null) {
            prompt = "You are an API server that suggests Java code for the outermost context of a Java source file, outside of any existing class. "
                    + "Based on the provided Java source file content, suggest relevant code to be added at the placeholder location ${SUGGEST_CODE_LIST}. "
                    + "Suggest additional classes, interfaces, enums, or other top-level constructs. "
                    + "Ensure that the suggestions fit the context of the entire file. "
                    + (preferencesManager.isDescriptionEnabled() ? jsonRequestWithDescription : jsonRequest)
                    + "Java Source File Content:\n" + classContent;
        } else if (path.getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
            prompt = "You are an API server that suggests Java code for the outermost context of a Java source file, outside of any existing class. "
                    + "Based on the provided Java source file content, suggest relevant code to be added at the placeholder location ${SUGGEST_CODE_LIST}. "
                    + "Suggest package declarations, import statements, comments, or annotations for public class. "
                    + "Ensure that the suggestions fit the context of the entire file. "
                    + (preferencesManager.isDescriptionEnabled() ? jsonRequestWithDescription : jsonRequest)
                    + "Java Source File Content:\n" + classContent;
        } else if (path.getLeaf().getKind() == Tree.Kind.MODIFIERS
                && path.getParentPath() != null
                && path.getParentPath().getLeaf().getKind() == Tree.Kind.CLASS) {
            prompt = "You are an API server that suggests Java code modifications for a class. "
                    + "At the placeholder location ${SUGGEST_CODE_LIST}, suggest either a class-level modifier such as 'public', 'protected', 'private', 'abstract', 'final', or a relevant class-level annotation. "
                    + "Ensure that the suggestions are appropriate for the class context provided. "
                    + (preferencesManager.isDescriptionEnabled() ? jsonRequestWithDescription : jsonRequest)
                    + "Java Class Content:\n" + classContent;
        } else if (path.getLeaf().getKind() == Tree.Kind.MODIFIERS
                && path.getParentPath() != null
                && path.getParentPath().getLeaf().getKind() == Tree.Kind.METHOD) {
            prompt = "You are an API server that suggests Java code modifications for a method. "
                    + "At the placeholder location ${SUGGEST_CODE_LIST}, suggest method-level modifiers such as 'public', 'protected', 'private', 'abstract', 'static', 'final', 'synchronized', or relevant method-level annotations. "
                    + "Additionally, you may suggest method-specific annotations like '@Override', '@Deprecated', '@Transactional', etc. "
                    + "Ensure that the suggestions are appropriate for the method context provided. "
                    + (preferencesManager.isDescriptionEnabled() ? jsonRequestWithDescription : jsonRequest)
                    + "Java Method Content:\n" + classContent;
        } else if (path.getLeaf().getKind() == Tree.Kind.CLASS
                && path.getParentPath() != null
                && path.getParentPath().getLeaf().getKind() == Tree.Kind.CLASS) {
            prompt = "You are an API server that suggests Java code for an inner class at the placeholder location ${SUGGEST_CODE_LIST}. "
                    + "Based on the provided Java class content, suggest either relevant inner class modifiers such as 'public', 'private', 'protected', 'static', 'abstract', 'final', or a full inner class definition. "
                    + "Additionally, you may suggest class-level annotations for the inner class. Ensure that the suggestions are contextually appropriate for an inner class. "
                    + (preferencesManager.isDescriptionEnabled() ? jsonRequestWithDescription : jsonRequest)
                    + "Java Class Content:\n" + classContent;
        } else if (path.getLeaf().getKind() == Tree.Kind.CLASS
                && path.getParentPath() != null
                && path.getParentPath().getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
            prompt = "You are an API server that suggests Java code for an class at the placeholder location ${SUGGEST_CODE_LIST}. "
                    + "Based on the provided Java class content, suggest either relevant class level members, attributes, constants, methods or blocks. "
                    + "Ensure that the suggestions are contextually appropriate for an class. "
                    + (preferencesManager.isDescriptionEnabled() ? jsonRequestWithDescription : jsonRequest)
                    + "Java Class Content:\n" + classContent;
        } else if (path.getLeaf().getKind() == Tree.Kind.PARENTHESIZED
                && path.getParentPath() != null
                && path.getParentPath().getLeaf().getKind() == Tree.Kind.IF) {
            prompt = "You are an API server that suggests Java code to enhance an if-statement. "
                    + "At the placeholder location ${SUGGEST_IF_CONDITIONS}, suggest additional conditional checks or actions within the if-statement. "
                    + "Ensure that the suggestions are contextually appropriate for the condition. "
                    + (preferencesManager.isDescriptionEnabled() ? jsonRequestWithDescription : jsonRequest)
                    + "Java If Statement Content:\n" + classContent;
        } else {
            prompt = "You are an API server that suggests Java code for a specific context in a given Java class at the placeholder location ${SUGGEST_CODE_LIST}. "
                    + "Based on the provided Java class content and the line of code: \"" + lineText + "\", suggest a relevant single line of code or a multi-line code block as appropriate for the context represented by the placeholder ${SUGGEST_CODE_LIST} in the Java class. "
                    + "Ensure that the suggestions are relevant to the context. "
                    + (preferencesManager.isDescriptionEnabled() ? jsonRequestWithDescription : jsonRequest)
                    + "Java Class Content:\n" + classContent;
        }

        prompt = loadClassData(prompt, classDatas);

        // Generate the list of suggested next lines of code
        String jsonResponse = generate(prompt);
        System.out.println("jsonResponse " + jsonResponse);
        // Parse the JSON response into a List
        List<Snippet> nextLines = parseJsonToSnippets(jsonResponse);
        return nextLines;
    }

    public List<String> suggestJavaComment(String classDatas, String classContent, String lineText) {
        String prompt = "You are an API server that suggests appropriate Java comments for a specific context in a given Java class at the placeholder location ${SUGGEST_JAVA_COMMENT}. "
                + "Based on the provided Java class content and the line of comment: \"" + lineText + " ${SUGGEST_JAVA_COMMENT} \", suggest relevant Java comment as appropriate for the context represented by the placeholder ${SUGGEST_JAVA_COMMENT} in the Java Class. "
                + "Return a JSON array where each element must be single line comment. \n\n"
                + "Java Class Content:\n" + classContent;
        // Generate the list of suggested Javadoc or comments
        String jsonResponse = generate(prompt);
        System.out.println("jsonResponse " + jsonResponse);
        // Parse the JSON response into a List
        List<String> comments = parseJsonToList(jsonResponse);

        return comments;
    }

    public List<String> suggestJavadocOrComment(String classDatas, String classContent, String lineText) {
        String prompt = "You are an API server that suggests appropriate Javadoc or comments for a specific context in a given Java class at the placeholder location ${SUGGEST_JAVADOC}. "
                + "Based on the provided Java class content and the line of code: \"" + lineText + "\", suggest relevant Javadoc or a comment block as appropriate for the context represented by the placeholder ${SUGGEST_JAVADOC} in the Java Class. "
                + "Return a JSON array where each element can either be a single-line comment, a multi-line comment block, or a Javadoc comment formatted as a single string using \\n for line breaks. "
                + " Do not split multi line javadoc comments to array, must be at same index in json array. \n\n"
                //            + "Ensure that the suggestions are relevant to the context of com.sun.source.tree.Tree.Kind." + type + ". "
                + "Java Class Content:\n" + classContent;
        // Generate the list of suggested Javadoc or comments
        String jsonResponse = generate(prompt);
        System.out.println("jsonResponse " + jsonResponse);
        // Parse the JSON response into a List
        List<String> comments = parseJsonToList(jsonResponse);
        return comments;
    }

    String jsonRequest = "Return a JSON array with a few best suggestions without any additional text or explanation. Each element should be an object containing two fields: 'imports' and 'snippet'. "
            + "'imports' should be an array of required Java import statements (if no imports are required, return an empty array). "
            + "'snippet' should contain the suggested code as a text block, which may include multiple lines formatted as a single string using \\n for line breaks. "
            + "Make sure to escape any double quotes within the snippet using a backslash (\\) so that the JSON remains valid. \n\n";

    String jsonRequestWithDescription = "Return a JSON array with a few best suggestions without any additional text or explanation. Each element should be an object containing three fields: 'imports', 'snippet', and 'description'. "
            + "'imports' should be an array of required Java import statements (if no imports are required, return an empty array). "
            + "'snippet' should contain the suggested code as a text block, which may include multiple lines formatted as a single string using \\n for line breaks. "
            + "'description' should be a very short explanation of what the snippet does and why it might be appropriate in this context, formatted with <b>, <\br> and optionally if required then include any imporant link with <a href=''> tags. "
            + "Make sure to escape any double quotes within the snippet and description using a backslash (\\) so that the JSON remains valid. \n\n";

    public List<Snippet> suggestAnnotations(String classDatas, String classContent, String lineText) {
        String prompt = "You are an API server that suggests Java annotations for a specific context in a given Java class at the placeholder location ${SUGGEST_ANNOTATION_LIST}. "
                + "Based on the provided Java class content and the line of code: \"" + lineText + "\", suggest relevant annotations that can be applied at the placeholder location represented by ${SUGGEST_ANNOTATION_LIST} in the Java Class. "
                + (preferencesManager.isDescriptionEnabled() ? jsonRequestWithDescription : jsonRequest)
                + "Ensure that the suggestions are appropriate for the given Java Class Content:\n\n" + classContent;

        // Generate the list of suggested annotations
        String jsonResponse = generate(prompt);
        System.out.println("jsonResponse " + jsonResponse);

        // Parse the JSON response into a List
        List<Snippet> annotations = parseJsonToSnippets(jsonResponse);
        return annotations;
    }

    public List<Snippet> parseJsonToSnippets(String jsonResponse) {
        if (jsonResponse == null) {
            return Collections.EMPTY_LIST;
        }
        List<Snippet> snippets = new ArrayList<>();

        JSONArray jsonArray;
        try {
            // Parse the JSON response
            jsonArray = new JSONArray(removeCodeBlockMarkers(jsonResponse));
        } catch (org.json.JSONException jsone) {
            JSONObject jsonObject = new JSONObject(removeCodeBlockMarkers(jsonResponse));
            jsonArray = new JSONArray();
            jsonArray.put(jsonObject);
        }

        // Loop through each element in the JSON array
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            List<String> importsList = new ArrayList<>();
            if (jsonObject.has("imports")) {
                // Extract the "imports" array
                JSONArray importsJsonArray = jsonObject.getJSONArray("imports");
                for (int j = 0; j < importsJsonArray.length(); j++) {
                    importsList.add(importsJsonArray.getString(j));
                }
            }

            // Extract the "snippet" field
            String snippet = jsonObject.getString("snippet");
            if (jsonObject.has("description")) {
                String descripion = jsonObject.getString("description");
                Snippet snippetObj = new Snippet(snippet, descripion, importsList);
                snippets.add(snippetObj);
            } else {
                Snippet snippetObj = new Snippet(snippet, importsList);
                snippets.add(snippetObj);
            }
        }

        return snippets;
    }

    private List<String> parseJsonToList(String json) {
        List<String> variableNames = new ArrayList<>();
        try {
            // Use JSONArray to parse the JSON array string
            JSONArray jsonArray = new JSONArray(removeCodeBlockMarkers(json));
            boolean split = false;
            int docCount = 0;
            for (int i = 0; i < jsonArray.length(); i++) {
                variableNames.add(jsonArray.getString(i));
                String line = jsonArray.getString(i).trim();
                if (line.startsWith("}")) {
                    split = true;
                }
                if (line.trim().startsWith("*")) {
                    docCount++;
                }
            }
            if (split || jsonArray.length() - 1 == docCount) {
                return Collections.singletonList(String.join("\n", variableNames));
            }
        } catch (Exception e) {
            return parseJsonToListWithSplit(removeCodeBlockMarkers(json));
        }
        return variableNames;
    }

    private List<String> parseJsonToListWithSplit(String json) {
        List<String> variableNames = new ArrayList<>();
        if (json == null || json.isEmpty()) {
            return variableNames;
        }

        json = removeCodeBlockMarkers(json).trim();
        String newjson = json;
        if (json.startsWith("[") && json.endsWith("]")) {
            newjson = json.substring(1, json.length() - 1).trim();
        }
        // Remove square brackets and split by new lines
        String[] lines = newjson.split("\\n");

        if (lines.length > 1) {
            for (String line : lines) {
                // Trim each line and add to the list if it's not empty
                line = line.trim();
                if (line.startsWith("\"")) {
                    if (line.endsWith("\"")) {
                        line = line.substring(1, line.length() - 1).trim();
                    } else if (line.endsWith("\",")) {
                        line = line.substring(1, line.length() - 2).trim();
                    }
                }
                if (!line.isEmpty()) {
                    variableNames.add(line);
                }
            }
        } else {
            variableNames = parseJsonToList(json);
        }

        return variableNames;
    }

    public String fixGrammar(String text, String classContent) {
        String prompt
                = "You are an AI model designed to correct grammar mistakes. "
                + "Given the following text and the context of the Java class, correct any grammar issues in the text. "
                + "Return only the fixed text. Do not include any additional details or explanations.\n\n"
                + "Java Class Content:\n" + classContent + "\n\n"
                + "Text to Fix:\n" + text;

        // Generate the grammar-fixed text
        String answer = generate(prompt);
        System.out.println(answer);
        return answer;
    }

    public String enhanceText(String text, String classContent) {
        String prompt = "You are an AI model designed to improve text. "
                + "Given the following text and the context of the Java class, enhance the text to be more engaging, clear, and polished. "
                + "Ensure the text is well-structured and free of any grammatical errors or awkward phrasing. "
                + "Return only the enhanced text. Do not include any additional details or explanations.\n\n"
                + "Java Class Content:\n" + classContent + "\n\n"
                + "Text to Enhance:\n" + text;

        // Generate the enhanced text
        String enhancedText = generate(prompt);
        System.out.println(enhancedText);
        return enhancedText;
    }

    public String enhanceExpressionStatement(String classContent, String parentContent, String expressionStatementContent) {
        // Construct the prompt for enhancing the expression statement
        String prompt = "You are an API server that enhances Java code snippets. "
                + "Given the following Java class content, the parent content of the EXPRESSION_STATEMENT, "
                + "and the content of the EXPRESSION_STATEMENT itself, enhance the EXPRESSION_STATEMENT to be more efficient, "
                + "clear, or follow best practices. Do not include any additional text or explanation, just return the enhanced code snippet.\n\n"
                + "Java Class Content:\n" + classContent + "\n\n"
                + "Parent Content of EXPRESSION_STATEMENT:\n" + parentContent + "\n\n"
                + "EXPRESSION_STATEMENT Content:\n" + expressionStatementContent;

        String enhanced = generate(prompt);
        System.out.println(enhanced);
        return enhanced;
    }

public String generateCommitMessageSuggestions(String gitDiffOutput, String referenceCommitMessage) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("You are an API server that generates commit message suggestions based on the provided 'git diff' and 'git status' output in HTML format. ")
            .append("Your goal is to create commit messages that reflect business or domain features rather than technical details like dependency updates or refactoring. Please provide various types of commit messages based on the changes: \n")
            .append("- Very Short\n")
            .append("- Short\n")
            .append("- Medium\n")
            .append("- Long\n")
            .append("- Descriptive\n\n")
            .append("Here is the 'git diff' and 'git status' output:\n")
            .append(gitDiffOutput.replace("\n", "<br>")) // Use <br> for HTML rendering
            .append("\n");

    // Add reference commit message to the prompt if it is not empty or null
    if (referenceCommitMessage != null && !referenceCommitMessage.isEmpty()) {
        prompt.append("Reference Commit Message:\n").append(referenceCommitMessage).append("<br><br>")
              .append("Ensure that all the following commit message suggestions are aligned with this reference message. "
                      + "The suggestions should reflect the intent and context of the reference commit message, focusing on the business or domain features, adapting it as necessary to fit the changes in the 'git diff' output. "
                      + "The goal is to keep all suggestions consistent with the meaning of the reference commit message.<br>");
    } else {
        prompt.append("No reference commit message provided.<br><br>")
              .append("Please generate commit message suggestions based on the 'git diff' output and the context of the changes, emphasizing business or domain features.");
    }

    prompt.append("Please respond with the commit messages strictly in HTML format only. "
            + "Do not use any Markdown or code formatting (like backticks or triple backticks). "
            + "Ensure the HTML content is well-structured, styled using Bootstrap CSS, "
            + "and split any long lines (over 100 characters) with <br> tags for multiline display. "
            + "Your response should look like this: <br>"
            + "<h3>Very Short:</h3> Commit message here<br>"
            + "<h3>Short:</h3> Commit message here<br>"
            + "<h3>Medium:</h3> Commit message here<br>"
            + "<h3>Long:</h3> Commit message here<br>"
            + "<h3>Descriptive:</h3> Commit message here<br>"
            + "Do not include any other text or formatting outside of HTML.");

    // Generate the commit message suggestions
    String answer = generate(prompt.toString());
    System.out.println(answer);
    // Wrap long lines with <br> tags
    answer = wrapLongLinesWithBr(removeCodeBlockMarkers(answer), 100);
    System.out.println("===========================================");
    System.out.println(answer);
    return answer;
}

    private String wrapLongLinesWithBr(String input, int maxLineLength) {
        StringBuilder wrapped = new StringBuilder();
        String[] lines = input.split("<br>"); // Split by existing line breaks

        for (String line : lines) {
            String[] words = line.split(" "); // Split line into words
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                // Check if adding the next word exceeds the maximum line length
                if (currentLine.length() + word.length() + 1 > maxLineLength) {
                    // If current line is not empty, append it to the result
                    if (currentLine.length() > 0) {
                        wrapped.append(currentLine.toString().trim()).append("<br>");
                        currentLine.setLength(0); // Reset current line
                    }
                }
                currentLine.append(word).append(" "); // Append the word with a space
            }

            // Append any remaining words in the current line
            if (currentLine.length() > 0) {
                wrapped.append(currentLine.toString().trim()).append("<br>");
            }
        }

        return wrapped.toString();
    }
    
public String generateHtmlResponseFromDbMetadata(String dbMetadata, String developerQuestion) {
    StringBuilder dbPrompt = new StringBuilder("You are an API server that provides assistance with SQL queries and database-related questions. ");

    dbPrompt.append("Given the following database schema metadata:\n")
            .append(dbMetadata)
            .append("\nRespond to the developer's question: \n")
            .append(developerQuestion)
            .append("\n")
            .append("""
              Analyze the metadata and provide a relevant SQL query with a description. Offer guidance 
              or explanations to address the developer's question, error, or inquiry related to the database. 
              Ensure the SQL queries match the database structure, constraints, and relationships. 
              Wrap only the full SQL query in <code type="full" class="sql"> tags without adding any HTML tags inside this block, 
              and do not wrap individual SQL keywords or table/column names in <code> tags
              and do not wrap any partial sql query segment  in <code> tags. 
              Always include a detailed explanation of the query, including its purpose and how it relates to the developer's question. 
              Respond with the answer directly in HTML format (not in Markdown syntax), including both the SQL query and the explanation, 
              and do not include any text outside the HTML response.
              """);

    String htmlResponse = generate(dbPrompt.toString());
    System.out.println(htmlResponse);
    return htmlResponse;
}


    public String generateHtmlDescriptionForProject(String projectContent, String query) {
        String prompt = "You are an API server that provides answer to query of following project in HTML. "
                + "Do not include additional text or explanations outside of the HTML content.\n\n"
                + "Do not include text in <code> block.\n\n"
                + "If Full Java Class is in response then wrap it in <code type=\"full\" class=\"java\">. "
                + "If partial snippet of Java Class are in response then wrap it in <code type=\"snippet\" class=\"java\">. "
                + "Projects Content:\n" + projectContent + "\n\n"
                + "Query:\n" + query;

        // Generate the HTML description
        String answer = generate(prompt);
        System.out.println(answer);
        return answer;
    }

    public String generateHtmlDescriptionForClass(String classContent) {
        String prompt = "You are an API server that provides description of following class in HTML. "
                + "Do not include additional text or explanations outside of the HTML content.\n\n"
                + "If Full Java Class is in response then wrap it in <code type=\"full\" class=\"java\">. "
                + "If partial snippet of Java Class are in response then wrap it in <code type=\"snippet\" class=\"java\">. "
                + "Java Class Content:\n" + classContent;

        // Generate the HTML description
        String answer = generate(prompt);
        System.out.println(answer);
        return answer;
    }

    public String generateHtmlDescriptionForMethod(String methodContent) {
        String prompt = "You are an API server that provides description of following Method in HTML. "
                + "Do not include additional text or explanations outside of the HTML content.\n\n"
                + "If Full Java Class is in response then wrap it in <code type=\"full\" class=\"java\">. "
                + "If partial snippet of Java Class are in response then wrap it in <code type=\"snippet\" class=\"java\">. "
                + "Java Method Content:\n" + methodContent;

        // Generate the HTML description
        String answer = generate(prompt);
        System.out.println(answer);
        return answer;
    }

    public String generateHtmlDescription(
            String projectContent, String classContent, String methodContent,
            String previousChatResponse, String userQuery) {
        String prompt;
        String promptExtend;
        if (methodContent != null) {
            promptExtend = "Method Content:\n" + methodContent + "\n\n"
                    + "Do not return complete Java Class, return only Method and wrap it in <code type=\"full\" class=\"java\">. \n";
        } else if (projectContent != null) {
            promptExtend = "Project Full Content:\n" + projectContent + "\n\n"
                    + "If Full Java Class is in response then wrap it in <code type=\"full\" class=\"java\">. "
                    + "If partial snippet of Java Class are in response then wrap it in <code type=\"snippet\" class=\"java\">. ";
        } else {
            promptExtend = "Orignal Java Class Content:\n" + classContent + "\n\n"
                    + "If Full Java Class is in response then wrap it in <code type=\"full\" class=\"java\">. "
                    + "If partial snippet of Java Class are in response then wrap it in <code type=\"snippet\" class=\"java\">. ";
        }
        if (previousChatResponse == null) {
            prompt = "You are an API server that provides an interactive HTML-formatted answer to a user's query based on Orignal Java class content. "
                    + "Given the following Java class content, and the user's query, generate an HTML document that directly addresses the specific query. "
                    + "Ensure the HTML content is well-structured, clearly answers the query. "
                    + "Use Bootstrap CSS for overall styling and highlight.js for code examples in the response. "
                    + "Do not include additional text or explanations outside of the HTML content.\n\n"
                    + promptExtend
                    + "\n User Query:\n" + userQuery;
        } else {
            prompt = "You are an API server that provides an interactive HTML-formatted answer to a user's query based on Orignal Java class content and Previous Chat Content. "
                    + "Given the following Java class content, the previous chat response, and the user's query, generate an HTML document that directly addresses the specific query. "
                    + "Ensure the HTML content is well-structured, clearly answers the query, and reflects any modifications or updates suggested in the previous chat response. "
                    + "Use Bootstrap CSS for overall styling and highlight.js for code examples in the response. "
                    + "Do not include additional text or explanations outside of the HTML content.\n\n"
                    + "Previous Chat Response:\n" + previousChatResponse + "\n\n"
                    + promptExtend
                    + "User Query:\n" + userQuery;
        }

        // Generate the HTML description
        String answer = generate(prompt);
        System.out.println(answer);
        return answer;
    }

    public String generateTestCase(
            String projectContent, String classContent, String methodContent,
            String previousChatResponse, String userQuery) {

        String prompt;
        String promptExtend;
        Set<String> testCaseTypes = new HashSet<>(); // Using a Set to avoid duplicates

        // Determine the test case type based on the user query
        if (userQuery != null) {
            userQuery = userQuery +" ,\n "+ PreferencesManager.getInstance().getTestCasePrompt();
        } else {
            userQuery = PreferencesManager.getInstance().getTestCasePrompt();
        }

        if (userQuery.toLowerCase().contains("junit5")) {
            testCaseTypes.add("JUnit5");
        } else if (userQuery.toLowerCase().contains("junit")) {
            testCaseTypes.add("JUnit");
        }

        if (userQuery.toLowerCase().contains("testng")) {
            testCaseTypes.add("TestNG");
        }

        if (userQuery.toLowerCase().contains("mockito")) {
            testCaseTypes.add("Mockito");
        }

        if (userQuery.toLowerCase().contains("spock")) {
            testCaseTypes.add("Spock");
        }

        if (userQuery.toLowerCase().contains("assertj")) {
            testCaseTypes.add("AssertJ");
        }

        if (userQuery.toLowerCase().contains("hamcrest")) {
            testCaseTypes.add("Hamcrest");
        }

        if (userQuery.toLowerCase().contains("powermock")) {
            testCaseTypes.add("PowerMock");
        }

        if (userQuery.toLowerCase().contains("cucumber")) {
            testCaseTypes.add("Cucumber");
        }

        if (userQuery.toLowerCase().contains("spring test")) {
            testCaseTypes.add("Spring Test");
        }
        
        if (userQuery.toLowerCase().contains("arquillian")) {
            testCaseTypes.add("Arquillian Test");
        }
        userQuery = "User Query: " + userQuery;
        String testCaseType = String.join(", ", testCaseTypes);
        if (methodContent != null) {
            promptExtend = "Method Content:\n" + methodContent + "\n\n"
                    + "Generate " + testCaseType + " test cases for this method. Include assertions and necessary mock setups. "
                    + "Wrap the " + testCaseType + " test cases in <code type=\"full\" class=\"java\">. ";
        } else if (projectContent != null) {
            promptExtend = "Project Full Content:\n" + projectContent + "\n\n"
                    + "Generate " + testCaseType + " test cases for all classes. Include assertions and necessary mock setups. "
                    + "If Full Java Class is in response, wrap the generated " + testCaseType + " test class in <code type=\"full\" class=\"java\">. "
                    + "If partial snippets are in response, wrap them in <code type=\"snippet\" class=\"java\">. ";
        } else {
            promptExtend = "Original Java Class Content:\n" + classContent + "\n\n"
                    + "Generate " + testCaseType + " test cases for the main methods in this class. Include assertions and necessary mock setups. "
                    + "If Full Java Class is in response, wrap the generated " + testCaseType + " test class in <code type=\"full\" class=\"java\">. "
                    + "If partial snippets are in response, wrap them in <code type=\"snippet\" class=\"java\">. ";
        }

        if (previousChatResponse == null) {
            prompt = "You are an API server that provides " + testCaseType + " test cases in Java for a given class or method based on the original Java class content. "
                    + "Given the following Java class or method content and the user's query, generate " + testCaseType + " test cases that are well-structured and functional. "
                    + "Provide an interactive HTML-formatted descriptive answer with test case. "
                    + "Ensure the HTML content is well-structured, clearly answers the query. "
                    + "Use Bootstrap CSS for overall styling for code examples in the response. "
                    + "Do not include additional text or explanations outside of the HTML content.\n\n"
                    + promptExtend
                    + userQuery;
        } else {
            prompt = "You are an API server that provides " + testCaseType + " test cases in Java for a given class or method based on the original Java class content and previous chat content. "
                    + "Given the following Java class content, previous chat response, and the user's query, generate " + testCaseType + " test cases that directly address the user's query. "
                    + "Ensure the " + testCaseType + " test cases are well-structured and reflect any modifications or updates suggested in the previous chat response. "
                    + "Provide an interactive HTML-formatted descriptive answer with test case. "
                    + "Ensure the HTML content is well-structured, clearly answers the query. "
                    + "Use Bootstrap CSS for overall styling for code examples in the response. "
                    + "Do not include additional text or explanations outside of the HTML content.\n\n"
                    + "Previous Chat Response:\n" + previousChatResponse + "\n\n"
                    + promptExtend
                    + userQuery;
        }

        // Generate the test cases
        String answer = generate(prompt);
        System.out.println(answer);
        return answer;
    }

    public List<Snippet> suggestNextLineCode(String fileContent, String currentLine, String mimeType) {
        StringBuilder description = new StringBuilder(MIME_TYPE_DESCRIPTIONS.getOrDefault(mimeType, "code snippets"));
        StringBuilder prompt = new StringBuilder("You are an API server that provides ").append(description).append(" suggestions based on the file content. ");
        if (currentLine == null || currentLine.isEmpty()) {
            prompt.append("Analyze the content and recommend appropriate additions at the placeholder ${SUGGEST_CODE_LIST}. ");
        } else {
            prompt.append("Analyze the content and the current line: \n")
                    .append(currentLine)
                    .append("\nRecommend appropriate additions at the placeholder ${SUGGEST_CODE_LIST}. ");
        }
        prompt.append("""
                  Ensure the suggestions align with the file's context and structure. 
                  Respond with a JSON array containing a few of the best options. 
                  Each entry should have one field, 'snippet', holding the recommended code block. 
                  The code block can contain multiple lines, formatted as a single string using \\n for line breaks.
                  
                  File Content:
                  """).append(fileContent);
        String jsonResponse = generate(prompt.toString());
        List<Snippet> nextLines = parseJsonToSnippets(jsonResponse);
        return nextLines;
    }

    public List<Snippet> suggestSQLQuery(String dbMetadata, String editorContent) {
        StringBuilder prompt = new StringBuilder("You are an API server that provides SQL query suggestions based on the provided database schema metadata. ");

        if (editorContent == null || editorContent.isEmpty()) {
            prompt.append("Analyze the metadata and recommend appropriate SQL queries at the placeholder ${SUGGEST_SQL_QUERY_LIST}. ");
        } else {
            prompt.append("Based on the following content in the editor: \n")
                    .append(editorContent)
                    .append("\nAnalyze the metadata and recommend SQL queries at the placeholder ${SUGGEST_SQL_QUERY_LIST}. ");
        }

        prompt.append("""
          Ensure the SQL queries match the database structure, constraints, and relationships. 
          Respond with a JSON array containing the best SQL query options. 
          Each entry should have one field, 'snippet', holding the recommended SQL query block, which may include multiple lines formatted as a single string using \\n for line breaks.
          """);

        // Include description if enabled
        if (preferencesManager.isDescriptionEnabled()) {
            prompt.append("""
          Additionally, each entry should contain a 'description' field providing a very short explanation of what the query does and why it might be appropriate in this context, 
          formatted with <b>, <br> tags, and optionally, if required, include any important link with <a href=''> tags.
          """);
        }

        prompt.append("Database Metadata:\n").append(dbMetadata);

        String jsonResponse = generate(prompt.toString());
        List<Snippet> sqlQueries = parseJsonToSnippets(jsonResponse);
        return sqlQueries;
    }

}
