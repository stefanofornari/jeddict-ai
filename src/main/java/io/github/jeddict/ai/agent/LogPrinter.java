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

/**
 *
 * @author Gaurav Gupta
 */
import java.io.IOException;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

public class LogPrinter {

    private final InputOutput io;

    public LogPrinter(String tabName) {
        io = IOProvider.getDefault().getIO(tabName, false);
    }

    public void show() {
        io.select();
    }

    public void print(String message) {
        try (OutputWriter writer = io.getOut()) {
            writer.println(message);
        }
    }

    public void printError(String message) {
        try (OutputWriter writer = io.getErr()) {
            writer.println(message);
        }
    }

    public void clear() throws IOException {
        io.getOut().reset();
    }
}
