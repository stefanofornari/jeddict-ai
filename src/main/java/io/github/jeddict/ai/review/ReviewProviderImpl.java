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
package io.github.jeddict.ai.review;

import io.github.jeddict.ai.components.AssistantChat;
import static io.github.jeddict.ai.review.Review.getFilePath;
import java.awt.Color;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

@ServiceProvider(service = ReviewProvider.class)
public class ReviewProviderImpl implements ReviewProvider {

    public static List<Review> getAllOpenAssistantChatReviews() {
        List<Review> allReviews = new ArrayList<>();

        for (TopComponent tc : TopComponent.getRegistry().getOpened()) {
            if (tc instanceof AssistantChat) {
                AssistantChat assistantChat = (AssistantChat) tc;
                List<Review> reviews = assistantChat.getReviews();
                if (reviews != null) {
                    allReviews.addAll(reviews);
                }
            }
        }

        return allReviews;
    }

    private static final String PROVIDER_ID = "ReviewCodesProvider";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayName() {
        return "Review Provider";
    }

    @Override
    public String getDescription() {
        return "Highlights lines with issues in the Review sidebar";
    }

    /**
     * Enable only if the document corresponds to one of the issue file paths
     */
    @Override
    public boolean isProviderEnabled(Document document) {
        String path = getFilePath(document);
        if (path == null) {
            return false;
        }
        boolean matchFound = false;
        List<Review> reviews = getAllOpenAssistantChatReviews();
        for (Review review : reviews) {

            Path absolutePath = Paths.get(path).normalize();
            Path issuePath = Paths.get(review.filePath).normalize();

            if (absolutePath.endsWith(issuePath)) {
                matchFound = true;
                break;
            }
        }
        return matchFound;
    }

    @Override
    public List<ReviewValue> getValues(Document document, String lineText, int lineNumber, Map<String, List<ReviewValue>> variableColorValues) {
        if (!(document instanceof BaseDocument)) {
            return Collections.emptyList();
        }
        String path = getFilePath(document);
        if (path == null) {
            return Collections.emptyList();
        }
        List<ReviewValue> result = new ArrayList<>();
        List<Review> reviews = getAllOpenAssistantChatReviews();
        for (Review review : reviews) {
            Path absolutePath = Paths.get(path).normalize();
            Path issuePath = Paths.get(review.filePath).normalize();
            if (!absolutePath.endsWith(issuePath)) {
                continue;
            }
            if (lineNumber >= review.startLine && lineNumber <= review.endLine) {
                int startOffset = 0;
                int endOffset = lineText.length();
                if (endOffset <= 0) {
                    continue;
                }
                Color color;
                color = switch (review.type.toLowerCase()) {
                    case "security" ->
                        new Color(255, 0, 0, 128);
                    case "warning" ->
                        new Color(255, 255, 0, 128);
                    case "info" ->
                        new Color(0, 123, 255, 128);
                    case "suggestion" ->
                        new Color(0, 255, 0, 128);
                    default ->
                        new Color(128, 128, 128, 128);
                };

                ReviewValue cv = new ReviewValue() {
                    @Override
                    public Color getColor() {
                        return color;
                    }

                    @Override
                    public int getStartOffset() {
                        return startOffset;
                    }

                    @Override
                    public int getEndOffset() {
                        return endOffset;
                    }

                    @Override
                    public int getLine() {
                        return lineNumber;
                    }

                    @Override
                    public String getValue() {
                        return review.type.toUpperCase();
                    }

                    @Override
                    public String getTitle() {
                        return review.title;
                    }

                    @Override
                    public String getDescription() {
                        return review.description;
                    }

                    @Override
                    public boolean isEditable() {
                        return false;
                    }
                };
                result.add(cv);
            }
        }
        return result;
    }

    @Override
    public int getStartIndex(Document document, int currentIndex) {
        return currentIndex;
    }

}
