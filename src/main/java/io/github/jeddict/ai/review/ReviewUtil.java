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

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Gaurav Gupta
 */
public class ReviewUtil {

    public static String convertReviewsToHtml(List<Review> reviews) {
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
                    titleColor = "yellow";
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

    public static List<Review> parseReviewsFromJson(String jsonResponse) {
        List<Review> reviews = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String filePath = obj.optString("file", "unknown");
                String type = obj.optString("type", "info");
                String title = obj.optString("title", "");
                String description = obj.optString("description", "");
                String hunk = obj.optString("hunk", "");

                reviews.add(new Review(filePath, hunk, type, title, description));
            }
        } catch (Exception e) {
            e.printStackTrace(); // Handle malformed JSON or unexpected format
        }

        return reviews;
    }

}
