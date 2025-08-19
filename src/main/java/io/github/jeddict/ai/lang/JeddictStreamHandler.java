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
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.output.Response;
import io.github.jeddict.ai.components.AssistantChat;
import io.github.jeddict.ai.response.TokenHandler;
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
public abstract class JeddictStreamHandler implements StreamingResponseHandler<AiMessage> {

    private final AssistantChat topComponent;
    private boolean init = true;
    private JTextArea textArea;
    private ProgressHandle handle;
    private boolean complete;
//    protected MarkdownStreamParser parser;
    private static final Logger LOGGER = Logger.getLogger(JeddictStreamHandler.class.getName());


    public JeddictStreamHandler(AssistantChat topComponent) {
        this.topComponent = topComponent;
//        parser = new MarkdownStreamParser(block -> {}, topComponent);
    }

    public ProgressHandle getProgressHandle() {
        return handle;
    }

    public void setHandle(ProgressHandle handle) {
        this.handle = handle;
    }

    @Override
    public void onNext(String token) {
        if (init) {
            topComponent.clear();
            textArea = topComponent.createTextAreaPane();
            textArea.setText(token);
            init = false;
        } else {
            textArea.append(token);
        }
//        parser.processToken(token);
    }

    public boolean isComplete() {
        return complete;
    }

    @Override
    public void onError(Throwable throwable) {
        complete = true;
        // Log the error with timestamp and thread info
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String threadName = Thread.currentThread().getName();
        LOGGER.log(Level.SEVERE, "Error occurred at {0} on thread [{1}]", new Object[] { timestamp, threadName });
        LOGGER.log(Level.SEVERE, "Exception in JeddictStreamHandler", throwable);
        // Update UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            final String error =  "[Error] An error occurred: " + throwable.getMessage();
            if (textArea != null) {
                textArea.append("\n\n" + error);
            } else {
                // If textArea not yet initialized, clear and create one to show error
                topComponent.clear();
                JTextArea errorArea = topComponent.createTextAreaPane();
                errorArea.setText(error);
                textArea = errorArea;
            }
            onComplete(error);
            if (handle != null) {
                handle.finish();
            }
        });
    }

    @Override
    public void onComplete(Response<AiMessage> out) {
        complete = true;
        SwingUtilities.invokeLater(() -> {
            String response = out.content().text();
            if (response != null && !response.isEmpty()) {
                CompletableFuture.runAsync(() -> TokenHandler.saveOutputToken(response));
                onComplete(response);
                handle.finish();
            }
        });
    }

    public abstract void onComplete(String response);


}
