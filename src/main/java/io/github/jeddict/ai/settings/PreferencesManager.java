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
package io.github.jeddict.ai.settings;

/**
 *
 * @author Gaurav Gupta, Shiwani Gupta
 */
import static io.github.jeddict.ai.settings.GenAIModel.DEFAULT_MODEL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.NbPreferences;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import org.netbeans.api.editor.mimelookup.MimeLookup;

public class PreferencesManager {

    private final Preferences preferences;
    private static final String API_KEY_ENV_VAR = "OPENAI_API_KEY";
    private static final String API_KEY_SYS_PROP = "openai.api.key";
    private static final String MODEL_ENV_VAR = "OPENAI_MODEL";
    private static final String MODEL_SYS_PROP = "openai.model";
    private static final String API_KEY_PREFERENCES = "api_key";
    private static final String PROVIDER_LOCATION_PREFERENCES = "provider_location";
    private static final String PROVIDER_PREFERENCE = "provider";
    private static final String MODEL_PREFERENCE = "model";
    private static final String COMMON_PROMPT_RULES_PREFERENCE = "commonPromptRules";
    private static final String TEMPERATURE_PREFERENCE = "temperature";
    private static final String TOP_P_PREFERENCE = "topP";
    private static final String STREAM_PREFERENCE = "stream";
    private static final String TIMEOUT_PREFERENCE = "timeout";
    private static final String LOG_REQUESTS_PREFERENCE = "logRequests";
    private static final String LOG_RESPONSES_PREFERENCE = "logResponses";
    private static final String REPEAT_PENALTY_PREFERENCE = "repeatPenalty";
    private static final String ORGANIZATION_ID_PREFERENCE = "organizationId";
    private static final String TOP_K_PREFERENCE = "topK";
    private static final String MAX_TOKENS_PREFERENCE = "maxTokens";
    private static final String MAX_COMPLETION_TOKENS_PREFERENCE = "maxCompletionTokens";
    private static final String MAX_OUTPUT_TOKENS_PREFERENCE = "maxOutputTokens";
    private static final String PRESENCE_PENALTY_PREFERENCE = "presencePenalty";
    private static final String FREQUENCY_PENALTY_PREFERENCE = "frequencyPenalty";
    private static final String SEED_PREFERENCE = "seed";
    private static final String ALLOW_CODE_EXECUTION_PREFERENCE = "allowCodeExecution";
    private static final String INCLUDE_CODE_EXECUTION_OUTPUT_PREFERENCE = "includeCodeExecutionOutput";
    private static final String MAX_RETRIES_PREFERENCE = "maxRetries";

    private final List<String> EXCLUDE_DIR_DEFAULT = Arrays.asList(
            // Test Resources
            "src/test/java",
            "src/test/resources",
            "test",
            // Main Resources
            "src/main/resources",
            "src/main/webapp",
            // Build Directories
            "target",
            "build",
            "out", // Output directory for compiled files

            // IDE Specific Directories
            ".idea",
            ".vscode",
            ".settings",
            ".classpath", // Eclipse classpath file
            ".project", // Eclipse project file
            "nbproject",
            "nbactions.xml",
            "nb-configuration.xml",
            // Version Control Directories
            ".git",
            ".svn",
            // Temporary Directories
            "tmp",
            "temp",
            // Log Directories
            "logs",
            "log",
            "debug",
            "trace",
            "cache",
            "backup",
            // Other Configuration/Files
            ".env", // Environment variable definitions
            "docker-compose.yml", // Docker Compose config
            "Dockerfile", // Docker configuration

            // JavaScript Project Directories
            "node_modules",
            "dist",
            "public",
            "build",
            ".next",
            // Gradle
            "gradle",
            "gradlew",
            "gradlew.bat",
            // Github
            ".github",
            ".dependabot",
            ".gitignore",
            "CODE_OF_CONDUCT.md",
            "CONTRIBUTING.md",
            "LICENSE",
            // Security and Configuration Files
            "secrets", // Directory for secrets
            "credentials", // Directory for credentials
            "private",
            "confidential",
            "vault",
            // CI configuration
            ".gitlab-ci.yml",
            ".travis.yml",
            "azure-pipelines.yml",
            "Jenkinsfile"
    );

    private PreferencesManager() {
        preferences = NbPreferences.forModule(AIAssistancePanel.class);
    }

    private static PreferencesManager instance;

    public static PreferencesManager getInstance() {
        if (instance == null) {
            instance = new PreferencesManager();
        }
        return instance;
    }

    public void clearApiKey() {
        preferences.remove(getProvider().name() + API_KEY_PREFERENCES);
    }

    public void setApiKey(String key) {
        preferences.put(getProvider().name() + API_KEY_PREFERENCES, key);
    }

    public String getApiKey() {
        return getApiKey(false);
    }

    String getApiKey(GenAIProvider provider) {
        return preferences.get(provider.name() + API_KEY_PREFERENCES, null);
    }

    public String getApiKey(boolean headless) {
        // First, try to get the API key from the environment variable
        String apiKey = System.getenv(API_KEY_ENV_VAR);
        if (apiKey == null || apiKey.isEmpty()) {
            // If not found in environment variable, try system properties
            apiKey = System.getProperty(API_KEY_SYS_PROP);
        }
        if (apiKey == null || apiKey.isEmpty()) {
            // If not found in environment or system properties, check Preferences
            apiKey = preferences.get(getProvider().name() + API_KEY_PREFERENCES, null);
        }

        if (apiKey == null || apiKey.isEmpty()) {
            // If still not found, show input dialog to enter API key
            if (!headless) {
                apiKey = JOptionPane.showInputDialog(null,
                        getProvider().name() + ":" + getModelName() + " API key is not configured. Please enter it now.",
                        getProvider().name() + ":" + getModelName() + " API Key Required",
                        JOptionPane.WARNING_MESSAGE);
            }

            if (apiKey != null && !apiKey.isEmpty()) {
                // Save the entered API key in Preferences for future use
                preferences.put(getProvider().name() + API_KEY_PREFERENCES, apiKey);
            } else {
                if (!headless) {
                    // If user didn't provide a valid key, show error and throw exception
                    JOptionPane.showMessageDialog(null,
                            getProvider().name() + ":" + getModelName() + " API key setup is incomplete. Please provide a valid key.",
                            getProvider().name() + ":" + getModelName() + " API Key Not Configured",
                            JOptionPane.ERROR_MESSAGE);
                    throw new IllegalStateException("A valid OpenAI API key is necessary for this feature.");
                } else {
                    return null;
                }
            }
        }

        return apiKey;
    }

    public void setProviderLocation(String providerLocation) {
        preferences.put(getProvider().name() + PROVIDER_LOCATION_PREFERENCES, providerLocation);
    }

    public String getProviderLocation() {
        return preferences.get(getProvider().name() + PROVIDER_LOCATION_PREFERENCES, null);
    }

    public String getModelName() {
        // Try to get the model name from the environment variable
        String modelName = System.getenv(MODEL_ENV_VAR);
        if (modelName == null || modelName.isEmpty()) {
            // If not found in environment variable, try system properties
            modelName = System.getProperty(MODEL_SYS_PROP);
        }
        if (modelName == null || modelName.isEmpty()) {
            // Fallback to default model name
            modelName = getModel();
        }
        return modelName;
    }

    public boolean isAiAssistantActivated() {
        return preferences.getBoolean("aiAssistantActivated", true);
    }

    public void setAiAssistantActivated(boolean activated) {
        preferences.putBoolean("aiAssistantActivated", activated);
    }

    public AIClassContext getClassContextInlineHint() {
        String classContext = preferences.get("classContextInlineHint", null);
        if (classContext != null) {
            try {
                return AIClassContext.valueOf(classContext);
            } catch (IllegalArgumentException iae) {
                // .. skip
            }
        }
        return AIClassContext.REFERENCED_CLASSES;
    }

    public void setClassContextInlineHint(AIClassContext context) {
        preferences.put("classContextInlineHint", context != null ? context.name() : null);
    }

    public AIClassContext getClassContext() {
        String classContext = preferences.get("classContext", null);
        if (classContext != null) {
            try {
                return AIClassContext.valueOf(classContext);
            } catch (IllegalArgumentException iae) {
                // .. skip
            }
        }
        return AIClassContext.REFERENCED_CLASSES;
    }

    public void setClassContext(AIClassContext context) {
        preferences.put("classContext", context != null ? context.name() : null);
    }

    public AIClassContext getVarContext() {
        String classContext = preferences.get("varContext", null);
        if (classContext != null) {
            try {
                return AIClassContext.valueOf(classContext);
            } catch (IllegalArgumentException iae) {
                // .. skip
            }
        }
        return AIClassContext.CURRENT_CLASS;
    }

    public void setVarContext(AIClassContext context) {
        preferences.put("varContext", context != null ? context.name() : null);
    }

    public String getModel() {
        return preferences.get(MODEL_PREFERENCE, DEFAULT_MODEL);
    }

    public void setModel(String model) {
        preferences.put(MODEL_PREFERENCE, model);
    }

    public void setProvider(GenAIProvider provider) {
        if (provider != null) {
            preferences.put(PROVIDER_PREFERENCE, provider.name());
        }
    }

    public GenAIProvider getProvider() {
        String providerName = preferences.get(PROVIDER_PREFERENCE, null);
        if (providerName != null) {
            try {
                return GenAIProvider.valueOf(providerName);
            } catch (IllegalArgumentException e) {
                System.err.println("Unknown provider: " + providerName + ". Falling back to default.");
            }
        }
        return GenAIProvider.OPEN_AI;
    }

    public boolean isInlineHintEnabled() {
        return preferences.getBoolean("enableInlineHint", false);
    }

    public void setInlineHintEnabled(boolean enabled) {
        preferences.putBoolean("enableInlineHint", enabled);
        setInlineHintsEnabled(isInlineHintEnabled() || isInlinePromptHintEnabled());
    }
    
    public boolean isInlinePromptHintEnabled() {
        return preferences.getBoolean("enableInlinePromptHint", false);
    }

    public void setInlinePromptHintEnabled(boolean enabled) {
        preferences.putBoolean("enableInlinePromptHint", enabled);
        setInlineHintsEnabled(isInlineHintEnabled() || isInlinePromptHintEnabled());
    }

    private static final String JAVA_INLINE_HINTS_KEY = "enable.inline.hints";

    public static boolean isInlineHintsEnabled() {
        return MimeLookup.getLookup("").lookup(Preferences.class)
                .getBoolean(JAVA_INLINE_HINTS_KEY, false);
    }

    public static void setInlineHintsEnabled(boolean enabled) {
        MimeLookup.getLookup("").lookup(Preferences.class)
                .putBoolean(JAVA_INLINE_HINTS_KEY, enabled);
    }

    public boolean isHintsEnabled() {
        return preferences.getBoolean("enableHints", true);
    }

    public void setHintsEnabled(boolean enabled) {
        preferences.putBoolean("enableHints", enabled);
    }

    public boolean isSmartCodeEnabled() {
        return preferences.getBoolean("enableSmartCode", true);
    }

    public void setSmartCodeEnabled(boolean enabled) {
        preferences.putBoolean("enableSmartCode", enabled);
    }

    public boolean isCompletionAllQueryType() {
        return preferences.getBoolean("enableCompletionAllQueryType", true);
    }

    public void setCompletionAllQueryType(boolean enabled) {
        preferences.putBoolean("enableCompletionAllQueryType", enabled);
    }

    public boolean isDescriptionEnabled() {
        return preferences.getBoolean("showDecription", true);
    }

    public void setDescriptionEnabled(boolean enabled) {
        preferences.putBoolean("showDecription", enabled);
    }

    public boolean isExcludeJavadocEnabled() {
        return preferences.getBoolean("excludeJavadoc", true);
    }

    public void setExcludeJavadocEnabled(boolean enabled) {
        preferences.putBoolean("excludeJavadoc", enabled);
    }

    
    public String getChatPlacement() {
        return preferences.get("chatPlacement", "Right");
    }

    public void setChatPlacement(String placement) {
        preferences.put("chatPlacement", placement);
    }

    private final List<String> DEFAULT_ACCEPTED_EXTENSIONS = Arrays.asList(
            "java", "php", "jsf", "kt", "groovy", "scala", "xml", "json", "yaml", "yml",
            "properties", "txt", "md", "js", "ts", "css", "scss", "html", "xhtml", "sh",
            "bat", "sql", "jsp", "rb", "cs", "go", "swift", "rs", "c", "cpp", "h", "py"
    );
    private List<String> acceptedExtensions = Collections.EMPTY_LIST;

    public String getFileExtensionToInclude() {
        return preferences.get("fileExtensionToInclude", String.join(", ", DEFAULT_ACCEPTED_EXTENSIONS));
    }

    public void setFileExtensionToInclude(String exts) {
        if (exts != null && !exts.isEmpty()) {
            preferences.put("fileExtensionToInclude", exts);
            acceptedExtensions = Arrays.asList(getFileExtensionToInclude().split("\\s*,\\s*"));
        }
    }

    public List<String> getFileExtensionListToInclude() {
        if (acceptedExtensions.isEmpty()) {
            acceptedExtensions = Arrays.asList(getFileExtensionToInclude().split("\\s*,\\s*"));
        }
        return acceptedExtensions;
    }

    private List<String> excludeDir = Collections.EMPTY_LIST;

    public String getExcludeDirs() {
        return preferences.get("excludeDirs", String.join(", ", EXCLUDE_DIR_DEFAULT));
    }

    public void setExcludeDirs(String dirs) {
        if (dirs != null && !dirs.isEmpty()) {
            preferences.put("excludeDirs", dirs);
            excludeDir = Arrays.asList(getExcludeDirs().split("\\s*,\\s*", -1));
        }
    }

    public List<String> getExcludeDirList() {
        if (excludeDir.isEmpty()) {
            excludeDir = Arrays.asList(getExcludeDirs().split("\\s*,\\s*", -1));
        }
        return excludeDir;
    }

    private Map<String, String> headerKeyValueMap = new HashMap<>();

    public Map<String, String> getCustomHeaders() {
        if (headerKeyValueMap.isEmpty()) {
            String headers = preferences.get("customHeaders", "");
            if (!headers.isEmpty()) {
                headerKeyValueMap = Arrays.stream(headers.split("\\s*;\\s*"))
                        .map(entry -> entry.split("\\s*=\\s*", 2))
                        .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));
            }
        }
        return headerKeyValueMap;
    }

    public void setCustomHeaders(Map<String, String> map) {
        String headers = map.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("; "));
        preferences.put("customHeaders", headers);
        headerKeyValueMap = map;
    }

    private Map<String, String> promptMap = new HashMap<>();

    public Map<String, String> getPrompts() {
        if (promptMap.isEmpty()) {
            String prompts = preferences.get("prompts", "");
            if (!prompts.isEmpty()) {
                promptMap = Arrays.stream(prompts.split("\\s*;\\s*"))
                        .map(entry -> entry.split("=", 2))
                        .collect(Collectors.toMap(arr -> arr[0], arr -> arr.length > 1 ? arr[1] : ""));
            }
        }
        if (promptMap.isEmpty()) {
            promptMap.put("test", "Generate JUnit Test");
        }
        return promptMap;
    }

    public void setPrompts(Map<String, String> map) {
        String prompts = map.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("; "));
        preferences.put("prompts", prompts);
        promptMap = map;
    }

    public String getSystemMessage() {
        return preferences.get("systemMessage", null);
    }

    public void setSystemMessage(String message) {
        preferences.put("systemMessage", message);
    }

    public String getCommonPromptRules() {
        return preferences.get(COMMON_PROMPT_RULES_PREFERENCE, "");
    }

    public void setCommonPromptRules(String rules) {
        if (rules != null) {
            preferences.put(COMMON_PROMPT_RULES_PREFERENCE, rules.trim());
        }
    }

    public boolean isStreamEnabled() {
        return preferences.getBoolean(STREAM_PREFERENCE, true); // Default value
    }

    public void setStreamEnabled(boolean enabled) {
        preferences.putBoolean(STREAM_PREFERENCE, enabled);
    }

    public Double getTemperature() {
        return preferences.getDouble(TEMPERATURE_PREFERENCE, Double.MIN_VALUE);
    }

    public void setTemperature(Double temperature) {
        preferences.putDouble(TEMPERATURE_PREFERENCE, temperature);
    }

    public Double getTopP() {
        return preferences.getDouble(TOP_P_PREFERENCE, Double.MIN_VALUE);
    }

    public void setTopP(Double topP) {
        preferences.putDouble(TOP_P_PREFERENCE, topP);
    }

    public Integer getTimeout() {
        return preferences.getInt(TIMEOUT_PREFERENCE, Integer.MIN_VALUE);
    }

    public void setTimeout(Integer timeout) {
        preferences.putInt(TIMEOUT_PREFERENCE, timeout);
    }

    public boolean isLogRequestsEnabled() {
        return preferences.getBoolean(LOG_REQUESTS_PREFERENCE, false);
    }

    public void setLogRequestsEnabled(boolean enabled) {
        preferences.putBoolean(LOG_REQUESTS_PREFERENCE, enabled);
    }

    public boolean isLogResponsesEnabled() {
        return preferences.getBoolean(LOG_RESPONSES_PREFERENCE, false);
    }

    public void setLogResponsesEnabled(boolean enabled) {
        preferences.putBoolean(LOG_RESPONSES_PREFERENCE, enabled);
    }

    public Double getRepeatPenalty() {
        return preferences.getDouble(REPEAT_PENALTY_PREFERENCE, Double.MIN_VALUE);
    }

    public void setRepeatPenalty(Double repeatPenalty) {
        preferences.putDouble(REPEAT_PENALTY_PREFERENCE, repeatPenalty);
    }

    public String getOrganizationId() {
        return preferences.get(ORGANIZATION_ID_PREFERENCE, null);
    }

    public void setOrganizationId(String organizationId) {
        preferences.put(ORGANIZATION_ID_PREFERENCE, organizationId);
    }

    public Integer getTopK() {
        return preferences.getInt(TOP_K_PREFERENCE, Integer.MIN_VALUE);
    }

    public void setTopK(Integer topK) {
        preferences.putInt(TOP_K_PREFERENCE, topK);
    }

    public Integer getMaxTokens() {
        return preferences.getInt(MAX_TOKENS_PREFERENCE, Integer.MIN_VALUE);
    }

    public void setMaxTokens(Integer maxTokens) {
        preferences.putInt(MAX_TOKENS_PREFERENCE, maxTokens);
    }

    public Integer getMaxCompletionTokens() {
        return preferences.getInt(MAX_COMPLETION_TOKENS_PREFERENCE, Integer.MIN_VALUE);
    }

    public void setMaxCompletionTokens(Integer maxCompletionTokens) {
        preferences.putInt(MAX_COMPLETION_TOKENS_PREFERENCE, maxCompletionTokens);
    }

    public Integer getMaxOutputTokens() {
        return preferences.getInt(MAX_OUTPUT_TOKENS_PREFERENCE, Integer.MIN_VALUE);
    }

    public void setMaxOutputTokens(Integer maxOutputTokens) {
        preferences.putInt(MAX_OUTPUT_TOKENS_PREFERENCE, maxOutputTokens);
    }

    public Double getPresencePenalty() {
        return preferences.getDouble(PRESENCE_PENALTY_PREFERENCE, Double.MIN_VALUE);
    }

    public void setPresencePenalty(Double presencePenalty) {
        preferences.putDouble(PRESENCE_PENALTY_PREFERENCE, presencePenalty);
    }

    public Double getFrequencyPenalty() {
        return preferences.getDouble(FREQUENCY_PENALTY_PREFERENCE, Double.MIN_VALUE);
    }

    public void setFrequencyPenalty(Double frequencyPenalty) {
        preferences.putDouble(FREQUENCY_PENALTY_PREFERENCE, frequencyPenalty);
    }

    public Integer getSeed() {
        return preferences.getInt(SEED_PREFERENCE, Integer.MIN_VALUE);
    }

    public void setSeed(Integer seed) {
        preferences.putInt(SEED_PREFERENCE, seed);
    }

    public boolean isAllowCodeExecution() {
        return preferences.getBoolean(ALLOW_CODE_EXECUTION_PREFERENCE, false);
    }

    public void setAllowCodeExecution(boolean allowCodeExecution) {
        preferences.putBoolean(ALLOW_CODE_EXECUTION_PREFERENCE, allowCodeExecution);
    }

    public boolean isIncludeCodeExecutionOutput() {
        return preferences.getBoolean(INCLUDE_CODE_EXECUTION_OUTPUT_PREFERENCE, false);
    }

    public void setIncludeCodeExecutionOutput(boolean includeCodeExecutionOutput) {
        preferences.putBoolean(INCLUDE_CODE_EXECUTION_OUTPUT_PREFERENCE, includeCodeExecutionOutput);
    }

    public Integer getMaxRetries() {
        return preferences.getInt(MAX_RETRIES_PREFERENCE, Integer.MIN_VALUE);
    }

    public void setMaxRetries(Integer maxRetries) {
        preferences.putInt(MAX_RETRIES_PREFERENCE, maxRetries);
    }
}
