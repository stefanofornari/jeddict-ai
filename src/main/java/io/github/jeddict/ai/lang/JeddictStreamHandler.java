/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.github.jeddict.ai.lang;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.output.Response;
import io.github.jeddict.ai.components.AssistantTopComponent;
import static io.github.jeddict.ai.util.StringUtil.removeCodeBlockMarkers;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author Shiwani Gupta
 */
public abstract class JeddictStreamHandler implements StreamingResponseHandler<AiMessage> {

    private final AssistantTopComponent topComponent;
    private boolean init = true;
    private JEditorPane editorPane;

    public JeddictStreamHandler(AssistantTopComponent topComponent) {
        this.topComponent = topComponent;
    }

    @Override
    public void onNext(String string) {
        if (init) {
            topComponent.clear();
            editorPane = topComponent.createPane();
            editorPane.setText(string);
            init = false;
        } else {
            String partial = editorPane.getText() + string;
            editorPane.setText(partial);
        }
    }

    @Override
    public void onError(Throwable thrwbl) {
    }

    @Override
    public void onComplete(Response<AiMessage> out) {
        SwingUtilities.invokeLater(() -> {
            String response = out.content().text();
            if (response != null && !response.isEmpty()) {
                response = removeCodeBlockMarkers(response);
                onComplete(response);
            }
        });
    }

    public abstract void onComplete(String response);
}
