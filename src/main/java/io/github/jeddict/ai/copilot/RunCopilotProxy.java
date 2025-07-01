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
package io.github.jeddict.ai.copilot;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.WindowManager;

/**
 *
 * @author arsi arsi(at)arsi.sk
 */
/**
 * Service provider implementation for managing the lifecycle of a Copilot Proxy
 * process.
 * <p>
 * This class is responsible for starting, monitoring, and terminating a
 * background process that runs the Copilot API via NPX. It provides methods to
 * start the process, handle process output, and gracefully shut down the
 * process and its descendants. The proxy is intended to be integrated with user
 * interface components, supporting event-driven notifications when the proxy is
 * ready to accept connections.
 * </p>
 * <p>
 * The class is annotated with {@code @ServiceProvider}, making it discoverable
 * via the NetBeans Lookup API as an implementation of {@link RunCopilotProxy}.
 * </p>
 * <p>
 * Designed for use with Java 17 and NetBeans platform services.
 * </p>
 */
/**
 * The {@code RunCopilotProxy} class provides functionality to start, monitor,
 * and manage a subprocess that runs the Copilot proxy via the
 * {@code npx copilot-api@latest start} command. It handles process lifecycle,
 * streams process output and errors to the NetBeans I/O window, and notifies
 * listeners when the proxy is ready. This class is intended for integration
 * within an environment that requires programmatic control of the GitHub
 * Copilot proxy process.
 * <p>
 * It is registered as a service provider for {@code RunCopilotProxy}.
 * </p>
 * <p>
 * Java Version: 17
 * </p>
 */
@ServiceProvider(service = RunCopilotProxy.class)
public class RunCopilotProxy {

    private static volatile Process process;
    private final SingletonProxyManager proxyManager = new SingletonProxyManager();

    /**
     * Checks whether the process is currently running.
     *
     * @return {@code true} if the process exists and is alive; {@code false}
     * otherwise.
     */
    public boolean isRunning() {
        return process != null && process.isAlive();
    }

    private boolean isNpxInstalled() {
        String npxCmd = System.getProperty("os.name").toLowerCase().contains("win") ? "npx.cmd" : "npx";
        ProcessBuilder pb = new ProcessBuilder(npxCmd, "--version");
        try {
            Process proc = pb.start();
            int exitCode = proc.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException ex) {
            return false;
        }
    }

    /**
     * Closes the proxy by terminating the associated process and all its
     * descendants.
     * <p>
     * If the process is alive, this method first destroys all descendant
     * processes, then destroys the main process itself, and waits for its
     * termination. Any {@link InterruptedException} thrown while waiting for
     * the process to finish is ignored.
     */
    public void closeProxy() {
        if (process != null && process.isAlive()) {
            process.descendants().forEach((t) -> {
                t.destroy();
            });
            process.destroy();
            try {
                process.waitFor();
            } catch (InterruptedException ex) {
            }
        }
    }

    /**
     * Executes the process using default parameters.
     * <p>
     * This method is a convenience overload that calls
     * {@link #runProcess(Object)} with {@code null} as the argument.
     * </p>
     *
     * @see #runProcess(Object)
     */
    public void runProcess() {
        runProcess(null);
    }

    /**
     * Runs the "copilot-api@latest start" command using NPX in a separate
     * process, streaming its output and errors to the NetBeans Output window.
     * Detects when the process is "listening" and notifies the provided
     * {@link ActionListener} with an {@link ActionEvent} labeled "LISTENING".
     * Handles both standard and error output streams in separate threads, and
     * prints process exit status upon completion.
     *
     * @param listener an {@link ActionListener} that will be notified via
     * {@link ActionEvent} when the output contains "listening on:"; may be
     * {@code null}.
     */
    public void runProcess(ActionListener listener) {
        if (!isNpxInstalled()) {
            NotifyDescriptor nd = new NotifyDescriptor.Message("\"npx\" was not found. Please check your Node.js installation.", NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }
        String npxCmd = System.getProperty("os.name").toLowerCase().contains("win") ? "npx.cmd" : "npx";
        ProcessBuilder pb = new ProcessBuilder(npxCmd, "copilot-api@latest", "start");
        try {
            process = pb.start();
            InputOutput io = IOProvider.getDefault().getIO("Copilot Proxy", false);
            new Thread(() -> {
                boolean listeningSent = false;
                try ( BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        io.getOut().println(line);
                        if (!listeningSent && line.toLowerCase().contains("listening on:")) {
                            listeningSent = true;
                            if (listener != null) {
                                listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "LISTENING"));
                            }
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }).start();
            new Thread(() -> {
                try ( BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        io.getErr().println(line);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }).start();
            process.waitFor();
            io.getOut().println("Process finished with exit code: " + process.exitValue());
        } catch (IOException | InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Starts the proxy with default settings.
     * <p>
     * This method is a convenience overload that calls
     * {@link #startProxy(Object)} with {@code null} as the argument,
     * initializing the proxy with default parameters.
     */
    public void startProxy() {
        WindowManager.getDefault().invokeWhenUIReady(() -> {
            proxyManager.waitForLockAndRun(() -> {
                // Tu spusti runProcess(listener)
                new Thread(() -> runProcess(null)).start();
            });
        });
    }

    /**
     * Starts a proxy process in a new thread once the UI is ready.
     * <p>
     * This method schedules the proxy process to start after the user interface
     * is fully initialized by utilizing the
     * {@code WindowManager.getDefault().invokeWhenUIReady} mechanism. The
     * process is executed in a background thread to avoid blocking the UI. The
     * provided {@link ActionListener} is passed to the process and can be used
     * to handle process-related events or notifications.
     *
     * @param listener the {@link ActionListener} to be notified of process
     * events
     */
    public void startProxy(ActionListener listener) {
        WindowManager.getDefault().invokeWhenUIReady(() -> {
            proxyManager.waitForLockAndRun(() -> {
                // Tu spusti runProcess(listener)
                new Thread(() -> runProcess(listener)).start();
            });
        });
    }
}
