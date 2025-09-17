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
package io.github.jeddict.ai.agent.tools.build;

import io.github.jeddict.ai.agent.tools.AbstractTool;
import io.github.jeddict.ai.agent.LogPrinter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class BaseBuildTool extends AbstractTool {

    public final String command;

    private LogPrinter log;

    public BaseBuildTool(final String basedir, final String command) {
        super(basedir);

        if ((command == null) || command.isBlank()) {
            throw new IllegalArgumentException("command can not be null or blank");
        }
        this.command = command;
    }

    protected String runCommand(String goals, String actionLabel)
    throws Exception {
        progress(actionLabel + " " + basedir);
        StringBuilder fullLog = new StringBuilder();
         if(this.log == null) {
                this.log = new LogPrinter(basepath.getFileName().toString());
        }
        this.log.show();

        try {
            ProcessBuilder pb;

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                pb = new ProcessBuilder("cmd", "/c", goals)
                        .directory(basepath.toFile())
                        .redirectErrorStream(true);
            } else {
                pb = new ProcessBuilder("sh", "-c", goals)
                        .directory(basepath.toFile())
                        .redirectErrorStream(true);
            }

            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    fullLog.append(line).append("\n");
                    this.log.print(line);
                }
            }

            int exitCode = process.waitFor();
            String result = (exitCode == 0 ? actionLabel + " successful"
                    : actionLabel + " failed with exit code " + exitCode);
            progress(actionLabel + " finished: " + result);
            fullLog.append(result).append("\n");

            return fullLog.toString();

        } catch (Exception e) {
            String error = actionLabel + " failed: " + e.getMessage();
            progress(actionLabel + " error: " + error);

            throw e;
        }
    }
}
