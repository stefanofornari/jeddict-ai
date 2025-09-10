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
package io.github.jeddict.ai.util;

import javax.swing.text.Document;

/**
 * Functional interface representing an action to be applied to a
 * {@link Document}. This allows lambda expressions that can throw
 * checked exceptions.
 *  
 * @author Gaurav Gupta
 */
@FunctionalInterface
public interface DocAction {

    /**
     * Apply the action to the given document.
     *
     * @param doc the Swing document to operate on
     * @return a status or result string
     * @throws Exception if the document operation fails
     */
    String apply(Document doc) throws Exception;
}
