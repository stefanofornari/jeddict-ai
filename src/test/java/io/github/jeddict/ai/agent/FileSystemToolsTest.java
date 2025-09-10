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

import io.github.jeddict.ai.agent.ToolsTest.DummyHandler;
import io.github.jeddict.ai.agent.ToolsTest.DummyProject;
import static org.junit.jupiter.api.Assertions.*;
import io.github.jeddict.ai.components.AssistantChat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;

public class FileSystemToolsTest extends ToolsTest {

    private FileSystemTools fileSystemTools;

    @BeforeEach
    public void setup() {
        project = new DummyProject();
        handler = new DummyHandler(new AssistantChat("Dummy", "Dummy", project));
        fileSystemTools = new FileSystemTools(project, handler);
    }

    @Test
    public void testSearchInFile_WithMatches() throws IOException {
        String path = "src/test/resources/testfile.txt";
        String pattern = "test file";

        String result = fileSystemTools.searchInFile(path, pattern);
        String fileName = java.nio.file.Paths.get(path).getFileName().toString();
        assertTrue(handler.getResponses().contains("Looking for '" + pattern + "' inside "  + fileName));
        assertTrue(result.contains("Match at"));
        assertTrue(result.contains("test file"));
    }

    @Test
    public void testCreateFile_SuccessAndExists() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        String path = tmpDir + "/tempfile.txt";
        String content = "Sample content.";

        String createResult1 = fileSystemTools.createFile(path, content);
        assertEquals("File created", createResult1);

        String createResult2 = fileSystemTools.createFile(path, content);
        assertTrue(createResult2.startsWith("File already exists:"));

        fileSystemTools.deleteFile(path); // cleanup
    }

    @Test
    public void testDeleteFile_SuccessAndNotFound() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        String path = tmpDir + "/tempfile.txt";
        String content = "To be deleted.";
        fileSystemTools.createFile(path, content);

        String deleteResult1 = fileSystemTools.deleteFile(path);
        assertEquals("File deleted", deleteResult1);

        String deleteResult2 = fileSystemTools.deleteFile(path);
        assertTrue(deleteResult2.startsWith("File not found:"));
    }

    @Test
    public void testListFilesInDirectory_SuccessAndNotFound() {
        String existingDir = "src/test/resources/";
        String nonExistingDir = "nonexistentdir";

        String listResult1 = fileSystemTools.listFilesInDirectory(existingDir);
        assertTrue(listResult1.contains("testfile.txt") || listResult1.contains("tempfile.txt"));

        String listResult2 = fileSystemTools.listFilesInDirectory(nonExistingDir);
        assertTrue(listResult2.startsWith("Directory not found:"));
    }

    @Test
    public void testReadFile_Success() throws IOException {
        String path = "src/test/resources/testfile.txt";
        String expectedContent = "This is a test file content for real file testing.";
        String result = fileSystemTools.readFile (path);
        String fileName = java.nio.file.Paths.get(path).getFileName().toString();
        assertTrue(handler.getResponses().contains("Reading file " + fileName));
        assertEquals(expectedContent, result);
    }

    @Test
    public void testCreateDirectory_SuccessAndExists() {
        String path = "src/test/resources/newdir";

        String createResult1 = fileSystemTools.createDirectory(path);
        assertEquals("Directory created", createResult1);

        String createResult2 = fileSystemTools.createDirectory(path);
        assertTrue(createResult2.startsWith("Directory already exists:"));

        fileSystemTools.deleteDirectory(path); // cleanup
    }

    @Test
    public void testDeleteDirectory_SuccessAndNotFound() {
        String path = "src/test/resources/newdir";
        fileSystemTools.createDirectory(path);

        String deleteResult1 = fileSystemTools.deleteDirectory(path);
        assertEquals("Directory deleted", deleteResult1);

        String deleteResult2 = fileSystemTools.deleteDirectory(path);
        assertTrue(deleteResult2.startsWith("Directory not found:"));
    }

    @Test
    public void testReadFile_Exception() throws IOException {
        String path = "nonexistentfile.txt";

        String result = fileSystemTools.readFile (path);

        String fileName = java.nio.file.Paths.get(path).getFileName().toString();
        assertTrue(handler.getResponses().contains("Reading file " + fileName));
        assertTrue(result.startsWith("Could not read file:"));
    }

    @Test
    public void testSearchInFile_NoMatches() throws IOException {
        String path = "src/test/resources/testfile.txt";
        String pattern = "abc";

        String result = fileSystemTools.searchInFile(path, pattern);

        String fileName = java.nio.file.Paths.get(path).getFileName().toString();
        assertTrue(handler.getResponses().contains("Looking for '" + pattern + "' inside " + fileName));
        assertEquals("No matches found.", result);
    }

}
