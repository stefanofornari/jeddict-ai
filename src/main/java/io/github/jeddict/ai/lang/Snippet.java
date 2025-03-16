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
package io.github.jeddict.ai.lang;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gaurav Gupta
 */
public class Snippet {

    private List<String> imports = new ArrayList<>();

    private final String snippet;

    private String description;

    public Snippet(String snippet, String description, List<String> imports) {
        this.snippet = snippet;
        this.description = description;
        this.imports = imports;
    }

    public Snippet(String snippet, List<String> imports) {
        this.snippet = snippet;
        this.imports = imports;
    }

    public Snippet(String snippet) {
        this.snippet = snippet;
    }

    public List<String> getImports() {
        return imports;
    }

    public String getSnippet() {
        return snippet;
    }

    public String getDescription() {
        return description;
    }

}
