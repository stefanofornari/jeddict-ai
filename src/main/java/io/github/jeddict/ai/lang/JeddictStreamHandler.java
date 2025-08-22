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

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import io.github.jeddict.ai.components.AssistantChat;
import io.github.jeddict.ai.response.TokenHandler;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;

/**
 *
 * @author Shiwani Gupta
 */
public abstract class JeddictStreamHandler implements StreamingChatResponseHandler {

    private final AssistantChat topComponent;
    private boolean init = true;
    private JTextArea textArea;
    private ProgressHandle handle;
    private boolean complete;
    private static final Logger LOGGER = Logger.getLogger(JeddictStreamHandler.class.getName());


    public JeddictStreamHandler(AssistantChat topComponent) {
        this.topComponent = topComponent;
    }

    public ProgressHandle getProgressHandle() {
        return handle;
    }

    public void setHandle(ProgressHandle handle) {
        this.handle = handle;
    }

    @Override
    public void onPartialResponse(String partialResponse) {
        LOGGER.finest(() -> "partial response received: " + partialResponse);
        if (init) {
            topComponent.clear();
            textArea = topComponent.createTextAreaPane();
            textArea.setText(partialResponse);
            init = false;
        } else {
            textArea.append(partialResponse);
        }
    }

    public boolean isComplete() {
        return complete;
    }

    @Override
    public void onError(Throwable throwable) {
        LOGGER.finest(() -> "error received: " + throwable);
        complete = true;
        // Log the error with timestamp and thread info
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String threadName = Thread.currentThread().getName();
        LOGGER.log(Level.SEVERE, "Error occurred at {0} on thread [{1}]", new Object[] { timestamp, threadName });
        LOGGER.log(Level.SEVERE, "Exception in JeddictStreamHandler", throwable);

        //
        // Build the error message
        //
        final StringWriter w = new StringWriter();
            w.append("An error occurred: " + throwable.getMessage());
            w.append("\n\nMore details:\n\n");
            throwable.printStackTrace(new PrintWriter(w));
        final String error = w.toString();

        // Update UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            if (textArea != null) {
                textArea.append("\n\n" + error);
            } else {
                // If textArea not yet initialized, clear and create one to show error
                topComponent.clear();
                JTextArea errorArea = topComponent.createTextAreaPane();
                errorArea.setText(error);
                textArea = errorArea;
            }

            onCompleteResponse(
                ChatResponse.builder().aiMessage(new AiMessage(error)).build()
            );
        });
    }

    @Override
    public void onCompleteResponse(ChatResponse completeResponse) {
        LOGGER.finest(() -> "complete response received: " + completeResponse);
        complete = true;
        SwingUtilities.invokeLater(() -> {
            String response = completeResponse.aiMessage().text();
            if (response != null && !response.isEmpty()) {
                CompletableFuture.runAsync(() -> TokenHandler.saveOutputToken(response));
                handle.finish();
            }
        });
    }

}
