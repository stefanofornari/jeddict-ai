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
package io.github.jeddict.ai.settings;

/**
 *
 * @author Gaurav Gupta, Shiwani Gupta
 */
import static io.github.jeddict.ai.settings.GenAIModel.DEFAULT_MODEL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.openide.util.NbPreferences;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;

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
        preferences.remove(getProvider().name()+ API_KEY_PREFERENCES);
    }

    public void setApiKey(String key) {
        preferences.put(getProvider().name()+API_KEY_PREFERENCES, key);
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
            apiKey = preferences.get(getProvider().name() +API_KEY_PREFERENCES, null);
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

    public String getTestCasePrompt() {
        return preferences.get("testCasePrompt", "Generate JUnit Test");
    }

    public void setTestCasePrompt(String prompt) {
        if (prompt != null && !prompt.isEmpty()) {
            preferences.put("testCasePrompt", prompt);
        }
    }

}
