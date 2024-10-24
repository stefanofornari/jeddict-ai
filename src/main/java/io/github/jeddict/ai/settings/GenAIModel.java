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
import static io.github.jeddict.ai.settings.GenAIProvider.DEEPINFRA;
import static io.github.jeddict.ai.settings.GenAIProvider.DEEPSEEK;
import static io.github.jeddict.ai.settings.GenAIProvider.GOOGLE;
import static io.github.jeddict.ai.settings.GenAIProvider.MISTRAL;
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
        MODELS.put("o1-preview", new GenAIModel(OPEN_AI, "o1-preview", "A preview version of the O1 model, designed for early testing and feedback.",
                0.300, 1.200));

        MODELS.put("chatgpt-4o-latest", new GenAIModel(OPEN_AI, "chatgpt-4o-latest", "Latest ChatGPT model offering powerful capabilities.",
                5.00, 15.00));
        MODELS.put("gpt-4o", new GenAIModel(OPEN_AI, "gpt-4o", "The premium choice for complex tasks requiring deep analysis and understanding.",
                2.50, 10.00));

        MODELS.put("claude-3-5-sonnet-20240620", new GenAIModel(ANTHROPIC, "claude-3-5-sonnet-20240620", "A sonnet model offering refined conversational capabilities.", 3.00, 15.00));
        MODELS.put("claude-3-haiku-20240307", new GenAIModel(ANTHROPIC, "claude-3-haiku-20240307", "A haiku model designed for concise and creative expression.", 0.25, 1.25));

        MODELS.put("open-codestral-mamba", new GenAIModel(MISTRAL, "open-codestral-mamba", "The first Mamba 2 open-source model, ideal for diverse tasks.", 0.0, 0.0));
        MODELS.put("pixtral-12b", new GenAIModel(MISTRAL, "pixtral-12b", "Version-capable small model.", 0.15, 0.15));
        MODELS.put("mistral-nemo", new GenAIModel(MISTRAL, "mistral-nemo", "State-of-the-art Mistral model trained specifically for code tasks.", 0.15, 0.15));
        MODELS.put("pixtral-12b-2409", new GenAIModel(MISTRAL, "pixtral-12b-2409", "A 12B model with image understanding capabilities in addition to text.", 0.0, 0.0));
        MODELS.put("open-mistral-nemo", new GenAIModel(MISTRAL, "open-mistral-nemo", "A multilingual open-source model released in July 2024.", 0.0, 0.0));

        MODELS.put("mistral-large-latest", new GenAIModel(MISTRAL, "mistral-large-latest", "Top-tier reasoning for high-complexity tasks, for your most sophisticated needs.", 2.00, 6.00));
        MODELS.put("mistral-small-latest", new GenAIModel(MISTRAL, "mistral-small-latest", "Cost-efficient, fast, and reliable option for translation, summarization, and sentiment analysis.", 0.20, 0.60));
        MODELS.put("codestral-latest", new GenAIModel(MISTRAL, "codestral-latest", "State-of-the-art Mistral model trained specifically for code tasks.", 0.20, 0.60));
        MODELS.put("mistral-embed", new GenAIModel(MISTRAL, "mistral-embed", "State-of-the-art semantic model for extracting text representations.", 0.10, 0.00)); // No output price provided
        MODELS.put("ministral-3b-latest", new GenAIModel(MISTRAL, "ministral-3b-latest", "Most efficient edge model.", 0.04, 0.04));
        MODELS.put("ministral-8b-latest", new GenAIModel(MISTRAL, "ministral-8b-latest", "Powerful model for on-device use cases.", 0.10, 0.10));

        MODELS.put("meta-llama/Llama-3.2-3B-Instruct", new GenAIModel(DEEPINFRA, "meta-llama/Llama-3.2-3B-Instruct", "A 3B instruct model by Meta for instructional tasks.", 0.15, 0.45));
        MODELS.put("Qwen/Qwen2.5-72B-Instruct", new GenAIModel(DEEPINFRA, "Qwen/Qwen2.5-72B-Instruct", "A large instruct model for various applications.", 0.20, 0.50));
        MODELS.put("google/gemma-2-9b-it", new GenAIModel(DEEPINFRA, "google/gemma-2-9b-it", "Gemini model specialized for IT tasks, with a focus on performance.", 0.10, 0.30));
        MODELS.put("microsoft/WizardLM-2-8x22B", new GenAIModel(DEEPINFRA, "microsoft/WizardLM-2-8x22B", "An 8x22B model designed for advanced conversational applications.", 0.25, 0.75));
        MODELS.put("mistralai/Mistral-7B-Instruct-v0.3", new GenAIModel(DEEPINFRA, "mistralai/Mistral-7B-Instruct-v0.3", "A 7B instruct model optimized for general tasks.", 0.15, 0.45));

        MODELS.put("deepseek-chat", new GenAIModel(DEEPSEEK, "deepseek-chat", "", 0.014, 0.28));
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
