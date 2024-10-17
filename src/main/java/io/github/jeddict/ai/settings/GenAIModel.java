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

import static io.github.jeddict.ai.settings.GenAIProvider.ANTHROPIC;
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
    public static Map<String, GenAIModel> MODELS = new HashMap<>();

    static {
        MODELS.put("gemini-1.5-flash", new GenAIModel(GOOGLE, "gemini-1.5-flash", "A fast and cost-effective model for rapid assessments. Highly recommended.",
                0.075, 0.30));
        MODELS.put("gemini-1.5-pro", new GenAIModel(GOOGLE, "gemini-1.5-pro", "A professional version of the Gemini model with enhanced capabilities.",
                1.25, 5.00));

        MODELS.put("gpt-4o-mini", new GenAIModel(OPEN_AI, "gpt-4o-mini", "Highly recommended for its excellent balance of performance and cost.",
                0.150, 0.600));
        MODELS.put("o1-mini", new GenAIModel(OPEN_AI, "o1-mini", "A compact model for diverse tasks.",
                3.00, 12.00));
        MODELS.put("chatgpt-4o-latest", new GenAIModel(OPEN_AI, "chatgpt-4o-latest", "Latest ChatGPT model offering powerful capabilities.",
                5.00, 15.00));
        MODELS.put("gpt-4o", new GenAIModel(OPEN_AI, "gpt-4o", "The premium choice for complex tasks requiring deep analysis and understanding.",
                2.50, 10.00));

        MODELS.put("claude-3-5-sonnet-20240620", new GenAIModel(ANTHROPIC, "claude-3-5-sonnet-20240620", "A sonnet model offering refined conversational capabilities.", 3.00, 15.00));
        MODELS.put("claude-3-haiku-20240307", new GenAIModel(ANTHROPIC, "claude-3-haiku-20240307", "A haiku model designed for concise and creative expression.", 0.25, 1.25));
    
        MODELS.put("llama3.1:latest", new GenAIModel(OLLAMA, "llama3.1:latest", "Optimized for lightweight tasks that require quick responses.", 0.0, 0.0));
        MODELS.put("llama3.2:latest", new GenAIModel(OLLAMA, "llama3.2:latest", "Efficient for tasks needing fast turnaround without heavy resources.", 0.0, 0.0));
        MODELS.put("gemma2:latest", new GenAIModel(OLLAMA, "gemma2:latest", "Designed for speed and quality in lightweight applications.", 0.0, 0.0));
        MODELS.put("qwen2.5:latest", new GenAIModel(OLLAMA, "qwen2.5:latest", "Compact and efficient, ideal for responsive tasks.", 0.0, 0.0));
        MODELS.put("deepseek-coder-v2:latest", new GenAIModel(OLLAMA, "deepseek-coder-v2:latest", "Specialized for coding tasks, offering quick assistance.", 0.0, 0.0));
}

    private final GenAIProvider provider;
    private final String name;
    private final String description;
    private final double inputPrice, outputPrice;

    public GenAIModel(GenAIProvider provider, String name, String description, double inputPrice, double outputPrice) {
        this.provider = provider;
        this.name = name;
        this.description = description;
        this.inputPrice = inputPrice;
        this.outputPrice = outputPrice;
    }

    public GenAIProvider getProvider() {
        return provider;
    }

    public String getName() {
        return name;
    }

    public static GenAIModel findByName(String name) {
        return MODELS.get(name);
    }

    public String getDescription() {
        return description;
    }

    public double getInputPrice() {
        return inputPrice;
    }

    public double getOutputPrice() {
        return outputPrice;
    }

    public String getFormattedInfo() {
        return String.format("%s: %s", name, description);
    }

    @Override
    public String toString() {
        return name;
    }
}
