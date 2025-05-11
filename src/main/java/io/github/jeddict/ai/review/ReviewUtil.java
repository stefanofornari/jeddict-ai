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

import io.github.jeddict.ai.util.ColorUtil;
import static io.github.jeddict.ai.util.EditorUtil.getBackgroundColorFromMimeType;
import static io.github.jeddict.ai.util.MimeUtil.MIME_PLAIN_TEXT;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 *
 * @author Gaurav Gupta
 */
public class ReviewUtil {

    public static String convertReviewsToHtml(List<Review> reviews) {
        Color backgroundColor = getBackgroundColorFromMimeType(MIME_PLAIN_TEXT);
        boolean isDark = ColorUtil.isDarkColor(backgroundColor);
        StringBuilder html = new StringBuilder();
        html.append("<h2>Review Summary</h2>");
        html.append("<table>");
        html.append("<tr><th>File</th><th>Type</th><th>Title</th></tr>");

        for (Review review : reviews) {
            String fileDisplay;
            if (review.filePath != null && review.filePath.endsWith(".java")) {
                String pathWithoutExtension = review.filePath.substring(0, review.filePath.length() - 5);
                String converted = pathWithoutExtension
                        .replace('\\', '/') // normalize windows paths
                        .replace("src/main/java/", "") // remove known prefix
                        .replace("/", ".");
                fileDisplay = converted;
            } else {
                fileDisplay = review.filePath;
            }

            // Determine title color based on type
            String titleColor;
            switch (review.type.toLowerCase()) {
                case "warning":
                    titleColor = isDark ? "yellow": "orange";
                    break;
                case "info":
                    titleColor = "#007bff";
                    break;
                case "suggestion":
                    titleColor = "green";
                    break;
                default:
                    titleColor = "grey"; // default color
            }

            html.append("<tr>");
            html.append("<td><a href=\"#").append(fileDisplay)
                    .append("@").append(review.startLine).append("\">")
                    .append(fileDisplay)
                    .append("<a/></td>");
            html.append("<td class='type-").append(review.type).append("'>").append(review.type).append("</td>");
            html.append("<td style='color:").append(titleColor).append(";'>").append(review.title).append("</td>");
            html.append("</tr>");
        }

        html.append("</table>");
        return html.toString();
    }

    public static List<Review> parseReviewsFromYaml(String yamlResponse) {
        List<Review> reviews = new ArrayList<>();
        LoaderOptions options = new LoaderOptions();
        Yaml yaml = new Yaml(new SafeConstructor(options));

        try {
            Iterable<Object> yamlObjects = yaml.loadAll(yamlResponse);

            for (Object obj : yamlObjects) {
                if (obj instanceof List<?> reviewList) {
                    for (Object item : reviewList) {
                        if (item instanceof Map<?, ?> map) {

                            Object fileObj = map.get("file");
                            String file = fileObj != null ? fileObj.toString() : "unknown";

                            Object hunkObj = map.get("hunk");
                            String hunk = hunkObj != null ? hunkObj.toString() : "";

                            Object typeObj = map.get("type");
                            String type = typeObj != null ? typeObj.toString() : "info";

                            Object titleObj = map.get("title");
                            String title = titleObj != null ? titleObj.toString() : "";

                            Object descObj = map.get("description");
                            String description = descObj != null ? descObj.toString() : "";

                            try {
                                reviews.add(new Review(file, hunk, type, title, description));
                            } catch (Exception e) {
                                e.printStackTrace(); // Handle malformed YAML or unexpected structure
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Handle malformed YAML or unexpected structure
        }

        return reviews;
    }

}
