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

import javax.swing.JComponent;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.SideBarFactory;
import org.netbeans.spi.editor.mimelookup.MimeLocation;

@MimeRegistration(position = 0, mimeType = "", service = SideBarFactory.class)
@MimeLocation(subfolderName = "SideBars")
public class ReviewSideBarFactory implements SideBarFactory {

    @Override
    public JComponent createSideBar(JTextComponent editor) {
        Document doc = editor.getDocument();
        ReviewProviderImpl provider = new ReviewProviderImpl();
        if (!provider.isProviderEnabled(doc)) {
            return null;
        }
        return new ReviewSideBarPanel(editor);
    }
}
