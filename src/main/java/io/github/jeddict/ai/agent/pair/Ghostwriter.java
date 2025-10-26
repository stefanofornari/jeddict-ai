package io.github.jeddict.ai.agent.pair;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.github.jeddict.ai.lang.Snippet;
import static io.github.jeddict.ai.util.StringUtil.removeCodeBlockMarkers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public interface Ghostwriter extends PairProgrammer {

    static final String SYSTEM_MESSAGE = """
You are an expert programmer that can suggest code based on the context of the
program and best practices to write good quality code. Generate context-aware,
and syntactically valid Java code suggestions that naturally fit into the
code surrounding the placeholder ${SUGGESTION} to improve code clarity or functionality.
Keep same formatting, structure and convensions. Add the code at the placeholder
location ${SUGGESTION}
{{format}}
""";

    static final String USER_MESSAGE = """
{{message}}
Current code: {{code}}
Current line: {{line}}
The project classes: {{classes}}
Project info: {{project}}
Hint: {{hint}}
""";

    static final String USER_MESSAGE_DEFAULT = """
Suggest additional classes, interfaces, enums, or other top-level constructs.
Ensure that the suggestions fit the context of the entire file.
""";

    static final String USER_MESSAGE_COMPILATION_UNIT =
        "Suggest package declarations, import statements, comments, or annotations for public class.";

    static final String USER_MESSAGE_CLASS_MODIFIERS =
        "Suggest either a class-level modifier such as 'public', 'protected', 'private', 'abstract', 'final', or a relevant class-level annotation.";

    static final String USER_MESSAGE_METHOD_MODIFIERS =
        "Suggest either a class-level modifier such as 'public', 'protected', 'private', 'abstract', 'final', or a relevant class-level annotation." +
        "Ensure that the suggestions are appropriate for the class context provided.";

    static final String USER_MESSAGE_INNER_CLASS =
        "Suggest either relevant inner class modifiers such as 'public', 'private', 'protected', 'static', 'abstract', 'final', or a full inner class definition." +
        "Additionally, you may suggest class-level annotations for the inner class." +
        "Ensure that the suggestions are contextually appropriate for an inner class.";

    static final String USER_MESSAGE_CLASS_NAME =
        "Suggest either relevant class level members, attributes, constants, methods or blocks." +
        "Ensure that the suggestions are contextually appropriate for a class.";

    static final String USER_MESSAGE_IF_CONDITION =
        "Suggest additional conditional checks or actions within the if-statement." +
        "Ensure that the suggestions are contextually appropriate for the condition.";

    static final String USER_MESSAGE_LINES =
        "Suggest a relevant single line of code or a multi-line code block as appropriate for the context represented by the placeholder ${SUGGESTION} in the Java class." +
        "Ensure that the suggestions are relevant to the context.";

    static final String USER_MESSAGE_LINES2 =
        "Suggest a relevant single line of code or a multi-line code block as appropriate for provided context." +
        "Ensure that the suggestions are relevant to the context.";

    static final String OUTPUT_JSON_OBJECT = """
Return a JSON object with a single best suggestion without any additional text or explanation. The object should contain two fields: 'imports' and 'snippet'.
'imports' should be an array of required Java import statements (if no imports are required, return an empty array).
'snippet' should contain the suggested code as a text block, which may include multiple lines formatted as a single string using \\n for line breaks.
Make sure to escape any double quotes within the snippet using a backslash (\\) so that the JSON remains valid.
""";

    static final String OUTPUT_JSON_ARRAY = """
Return a JSON array with a few best suggestions without any additional text or explanation. Each element should be an object containing two fields: 'imports' and 'snippet'.
'imports' should be an array of required Java import statements (if no imports are required, return an empty array).
'snippet' should contain the suggested code as a text block, which may include multiple lines formatted as a single string using \\n for line breaks.
Make sure to escape any double quotes within the snippet using a backslash (\\) so that the JSON remains valid.
""";

    static final String OUTPUT_JSON_ARRAY_WITH_DESCRIPTION = """
Return a JSON array with a few best suggestions without any additional text or explanation. Each element should be an object containing three fields: 'imports', 'snippet', and 'description'.
'imports' should be an array of required Java import statements (if no imports are required, return an empty array).
'snippet' should contain the suggested code as a text block, which may include multiple lines formatted as a single string using \\n for line breaks.
'description' should be a very short explanation of what the snippet does and why it might be appropriate in this context, formatted with <b>, <br> and optionally, if required, include any important link with <a href=''> tags.
Make sure to escape any double quotes within the snippet and description using a backslash (\\) so that the JSON remains valid.
""";

    @SystemMessage(SYSTEM_MESSAGE)
    @UserMessage(USER_MESSAGE)
    @Agent("Suggest code for the given content and cotext")
    String suggest(
            @V("message") final String message, // additional user request
            @V("classes") final String classes, // the relevant classes and methods signature
            @V("code") final String code, // the current code (e.g. class)
            @V("line") final String line, // the current line
            @V("hint") final String hint, // the hint context
            @V("project") final String project, // some project info
            @V("format") final String format // the output format
    );

    default List<Snippet> suggestNextLineCode(
            final String classes,
            final String code,
            final String line,
            final String project,
            final String hint,
            final TreePath tree,
            boolean description
    ) {
        log(classes, code, line, project, hint, description);

        if (hint != null) {
            return suggestNextLineCodeWithHint(classes, code, line, project, hint);
        }

        final String output = (description)
                ? OUTPUT_JSON_ARRAY_WITH_DESCRIPTION
                : OUTPUT_JSON_ARRAY;

        return jsonToSnippets(suggest(userMessage(tree), classes, code, line, "", project, output));
    }

    default List<Snippet> suggestNextLineCodeWithHint(
            final String classes,
            final String code,
            final String line,
            final String project,
            final String hint
    ) {
        log(classes, code, line, project, hint, false);

        return jsonToSnippets(suggest("", classes, code, line, hint, project, OUTPUT_JSON_OBJECT));
    }

    default String userMessage(final TreePath tree) {
        //
        // Undefined code parsing
        //
        if (tree == null) {
            return USER_MESSAGE_DEFAULT;
        }

        //
        // Top level compilation unit
        //
        if (tree.getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
            return USER_MESSAGE_COMPILATION_UNIT;
        }

        //
        // Class modifier
        //
        if (tree.getLeaf().getKind() == Tree.Kind.MODIFIERS
                && tree.getParentPath() != null
                && tree.getParentPath().getLeaf().getKind() == Tree.Kind.CLASS) {
            return USER_MESSAGE_CLASS_MODIFIERS;
        }

        //
        // Method modifier
        //
        if (tree.getLeaf().getKind() == Tree.Kind.MODIFIERS
                && tree.getParentPath() != null
                && tree.getParentPath().getLeaf().getKind() == Tree.Kind.METHOD) {
            return USER_MESSAGE_METHOD_MODIFIERS;
        }

        //
        // Inner class
        //
        if (tree.getLeaf().getKind() == Tree.Kind.CLASS
                && tree.getParentPath() != null
                && tree.getParentPath().getLeaf().getKind() == Tree.Kind.CLASS) {
            return USER_MESSAGE_INNER_CLASS;
        }

        //
        // Class name
        //
        if (tree.getLeaf().getKind() == Tree.Kind.CLASS
                && tree.getParentPath() != null
                && tree.getParentPath().getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
            return USER_MESSAGE_CLASS_NAME;
        }

        if (tree.getLeaf().getKind() == Tree.Kind.PARENTHESIZED
                && tree.getParentPath() != null
                && tree.getParentPath().getLeaf().getKind() == Tree.Kind.IF) {
            return USER_MESSAGE_IF_CONDITION;
        }

        return USER_MESSAGE_LINES;
    }

    default List<Snippet> jsonToSnippets(String jsonResponse) {
        if (jsonResponse == null) {
            return Collections.EMPTY_LIST;
        }
        List<Snippet> snippets = new ArrayList<>();

        JSONArray jsonArray;

        if (jsonResponse.contains("```json")) {
            int index = jsonResponse.indexOf("```json") + 7;
            jsonResponse = jsonResponse.substring(index, jsonResponse.indexOf("```", index)).trim();
        } else {
            jsonResponse = removeCodeBlockMarkers(jsonResponse);
        }
        try {
            // Parse the JSON response
            jsonArray = new JSONArray(jsonResponse);
        } catch (org.json.JSONException jsone) {
            JSONObject jsonObject = new JSONObject(jsonResponse);
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

    /**
     * Logs the provided information at the FINEST level. Note that it returns
     * Void because void methods are not supported in agents by lanchain4j.
     *
     * @param line The line information to be logged.
     * @param code The code information to be logged.
     * @param classes The classes information to be logged.
     *
     * @return Void always returns null.
     */
    default Void log(
            final String classes,
            final String code,
            final String line,
            final String project,
            final String hint,
            final boolean description
    ) {
        LOG.finest(() -> "\n"
                + "classes: " + StringUtils.abbreviate(classes, 80) + "\n"
                + "line: " + StringUtils.abbreviate(line, 80) + "\n"
                + "code: " + StringUtils.abbreviate(code, 80) + "\n"
                + "project: " + StringUtils.abbreviate(project, 80) + "\n"
                + "hint: " + StringUtils.abbreviate(hint, 80) + "\n"
        );

        return null;
    }

}
