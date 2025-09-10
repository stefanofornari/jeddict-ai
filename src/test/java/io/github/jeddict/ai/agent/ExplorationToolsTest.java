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
package io.github.jeddict.ai.agent;

import io.github.jeddict.ai.components.AssistantChat;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;

public class ExplorationToolsTest extends ToolsTest {

    private ExplorationTools explorationTools;

    @BeforeEach
    public void setup() {
        project = new DummyProject();
        handler = new DummyHandler(new AssistantChat("Dummy", "Dummy", project));
        explorationTools = new ExplorationTools(project, handler);
    }

    @Test
    public void testListClassesInFile_withNonExistingFile_shouldReturnNoClassesFound() {
        String result = explorationTools.listClassesInFile("non/existing/File.java");
        assertNotNull(result);
        assertTrue(result.contains("No classes found") || result.trim().isEmpty(), "Expected 'No classes found' or empty result");
    }

    @Test
    public void testListMethodsInFile_withNonExistingFile_shouldReturnNoMethodsFound() {
        String result = explorationTools.listMethodsInFile("non/existing/File.java");
        assertNotNull(result);
        assertTrue(result.contains("No methods found") || result.trim().isEmpty(), "Expected 'No methods found' or empty result");
    }

    @Test
    public void testFindUsages_withInvalidPath_shouldReturnNoUsagesFound() {
        String result = explorationTools.findUsages("invalid/path/File.java", "symbolName");
        assertNotNull(result);
        assertTrue(result.contains("No usages found") || result.contains("Find usages failed"));
    }
}
