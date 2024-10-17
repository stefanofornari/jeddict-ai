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

import static io.github.jeddict.ai.settings.GenAIProvider.GOOGLE;
import static io.github.jeddict.ai.settings.GenAIProvider.OLLAMA;
import static io.github.jeddict.ai.settings.GenAIProvider.OPEN_AI;
import java.util.HashMap;
import java.util.Map;

/**
 * Class representing GPT models used in AI analysis.
 *
 * Author: Shiwani Gupta, Gaurav Gupta
 */
public class GenAIModel {

    public static String DEFAULT_MODEL = "gpt-4o-mini";
    public static Map<String, GenAIModel> models = new HashMap<>();

    static {
        models.put("gemini-1.5-flash", new GenAIModel(GOOGLE, "gemini-1.5-flash", "A fast and cost-effective model for rapid assessments. Highly recommended."));

        models.put("llama3.1:latest", new GenAIModel(OLLAMA, "llama3.1:latest", "Optimized for lightweight tasks that require quick responses."));

        models.put("llama3.2:latest", new GenAIModel(OLLAMA, "llama3.2:latest", "Efficient for tasks needing fast turnaround without heavy resources."));

        models.put("gemma2:latest", new GenAIModel(OLLAMA, "gemma2:latest", "Designed for speed and quality in lightweight applications."));

        models.put("qwen2.5:latest", new GenAIModel(OLLAMA, "qwen2.5:latest", "Compact and efficient, ideal for responsive tasks."));

        models.put("deepseek-coder-v2:latest", new GenAIModel(OLLAMA, "deepseek-coder-v2:latest",
                "Specialized for coding tasks, offering quick assistance."));

        models.put("gpt-4o-mini", new GenAIModel(OPEN_AI, "gpt-4o-mini", "Highly recommended for its excellent balance of performance and cost."));

        models.put("gpt-4-turbo", new GenAIModel(OPEN_AI, "gpt-4-turbo", "An advanced model providing great speed and reliability for diverse tasks."));

        models.put("gpt-4o", new GenAIModel(OPEN_AI, "gpt-4o", "The premium choice for complex tasks requiring deep analysis and understanding."));
    }

    private final GenAIProvider provider;
    private final String name;
    private final String description;

    public GenAIModel(GenAIProvider provider, String name, String description) {
        this.provider = provider;
        this.name = name;
        this.description = description;
    }

    public GenAIProvider getProvider() {
        return provider;
    }

    public String getName() {
        return name;
    }

    public static GenAIModel findByName(String name) {
        return models.get(name);
    }

    public String getDescription() {
        return description;
    }

    public String getFormattedInfo() {
        return String.format("%s: %s", name, description);
    }

    @Override
    public String toString() {
        return name;
    }
}
