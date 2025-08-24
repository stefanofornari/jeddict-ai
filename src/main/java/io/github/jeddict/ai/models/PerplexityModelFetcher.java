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
package io.github.jeddict.ai.models;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class PerplexityModelFetcher {

    public final String API_URL = "https://api.perplexity.ai";

    public String getAPIUrl() {
        return API_URL;
    }

    /**
     * Perplexity does not support fetching the models for now, but we keep same
     * pattern as per the other providers.
     */
    public List<String> getModels() {
        return Arrays.asList("sonar", "sonar-pro", "sonar-reasoning", "sonar-reasoning-pro", "sonar-deep-research");
    }
}
