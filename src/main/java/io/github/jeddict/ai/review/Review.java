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

import java.io.File;
import java.util.Objects;
import javax.swing.text.Document;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Gaurav Gupta
 */
public class Review {

    public final String filePath;
    public int startLine;
    public int endLine;
    public final String type; 
    public final String title;
    public final String description;
    public final String hunk;

    public Review(String filePath, String hunk, String type, String title, String description) {
        this.filePath = filePath;
        HunkInfo hunkInfo =  parseHunkHeader(hunk);
        this.startLine = hunkInfo.newStart;
        this.endLine = hunkInfo.newStart + hunkInfo.newCount;
        this.type = type;
        this.title = title;
        this.description = description;
        this.hunk = hunk;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Review)) {
            return false;
        }
        Review other = (Review) obj;
        return filePath.equals(other.filePath) && startLine == other.startLine && endLine == other.endLine;
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath, startLine, endLine);
    }

    public static String getFilePath(Document document) {
        DataObject dob = (DataObject) document.getProperty(Document.StreamDescriptionProperty);
        if (dob != null) {
            FileObject fo = dob.getPrimaryFile();
            File file = FileUtil.toFile(fo);
            if (file != null) {
                String absolutePath = file.getAbsolutePath();
                return absolutePath;
            }
        }
        return null;
    }

    public static class HunkInfo {

        public final int oldStart;
        public final int oldCount;
        public final int newStart;
        public final int newCount;

        public HunkInfo(int oldStart, int oldCount, int newStart, int newCount) {
            this.oldStart = oldStart;
            this.oldCount = oldCount;
            this.newStart = newStart;
            this.newCount = newCount;
        }

        @Override
        public String toString() {
            return "HunkInfo{"
                    + "oldStart=" + oldStart
                    + ", oldCount=" + oldCount
                    + ", newStart=" + newStart
                    + ", newCount=" + newCount
                    + '}';
        }
    }

 public static HunkInfo parseHunkHeader(String hunkHeader) {
    if (hunkHeader == null) {
        throw new IllegalArgumentException("Hunk header cannot be null");
    }

    // Regex to match optional counts after starts
    // Examples matched:
    // @@ -17,3 +19,4 @@
    // @@ -17 +19 @@
    String pattern = "@@\\s+-?(\\d+)(?:,(\\d+))?\\s+\\+?(\\d+)(?:,(\\d+))?\\s+@@";

    java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(hunkHeader);

    if (matcher.find()) {
        int oldStart = Integer.parseInt(matcher.group(1));
        int oldCount = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 1;
        int newStart = Integer.parseInt(matcher.group(3));
        int newCount = matcher.group(4) != null ? Integer.parseInt(matcher.group(4)) : 1;
        return new HunkInfo(oldStart, oldCount, newStart, newCount);
    } else {
        throw new IllegalArgumentException("Invalid hunk header format: " + hunkHeader);
    }
}

}
