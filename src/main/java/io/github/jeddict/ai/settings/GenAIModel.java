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
import static io.github.jeddict.ai.settings.GenAIProvider.OPEN_AI;

/**
 * Enumeration for GPT models used in AI analysis.
 *
 * Author: Gaurav Gupta
 */
public enum GenAIModel {
    GEMINI_1_5_FLASH(GOOGLE, "gemini-1.5-flash", "A cost-effective and fast model, making it ideal for quick assessments. A better choice for those needing speed without sacrificing quality."),
    GPT_4O_MINI(OPEN_AI, "gpt-4o-mini", "A compact variant of the GPT-4 family, balancing cost and efficiency, suitable for lightweight tasks that require quick responses."),
    GPT_4_TURBO(OPEN_AI, "gpt-4-turbo", "An advanced model providing a balance between cost and performance, suitable for various applications that require a reliable response speed."),
    GPT_4O(OPEN_AI, "gpt-4o", "The flagship model for deep analysis, delivering high-quality insights at a premium cost, best suited for complex tasks that demand thorough understanding.");

    private final GenAIProvider provider;
    private final String displayName;
    private final String description;

    GenAIModel(GenAIProvider provider, String displayName, String description) {
        this.provider = provider;
        this.displayName = displayName;
        this.description = description;
    }

    public GenAIProvider getProvider() {
        return provider;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getFormattedInfo() {
        return String.format("%s: %s", displayName, description);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
