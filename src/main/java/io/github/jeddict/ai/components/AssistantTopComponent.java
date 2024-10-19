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
package io.github.jeddict.ai.components;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.prefs.Preferences;
import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.openide.windows.TopComponent;

/**
 *
 * @author Shiwani Gupta
 */
public class AssistantTopComponent extends TopComponent {

    public static final String PREFERENCE_KEY = "AssistantTopComponentOpen";
    private final JPanel parentPanel;
    private HTMLEditorKit editorKit;

    public AssistantTopComponent(String name) {
        setName(name);
        setLayout(new BorderLayout());

        parentPanel = new JPanel();
        parentPanel.setLayout(new BoxLayout(parentPanel, BoxLayout.Y_AXIS));

        add(parentPanel, BorderLayout.CENTER);
    }

    public void clear() {
        parentPanel.removeAll();
    }

    public JEditorPane createHtmlPane(String content) {
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditorKit(getHTMLEditorKit());
        editorPane.addHyperlinkListener(e -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        editorPane.setEditable(false);
        editorPane.setText(content);
        parentPanel.add(editorPane);
        return editorPane;
    }

    public JEditorPane createCodePane(String content) {
        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditorKit(createEditorKit("text/x-java"));
        editorPane.setEditable(false);
        editorPane.setText(content);
        parentPanel.add(editorPane);
        return editorPane;
    }

    public static EditorKit createEditorKit(String mimeType) {
        return MimeLookup.getLookup(MimePath.parse(mimeType)).lookup(EditorKit.class);
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        boolean shouldOpen = prefs.getBoolean(PREFERENCE_KEY, true);
        if (!shouldOpen) {
            this.close();
        }
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        prefs.putBoolean(PREFERENCE_KEY, false);
    }

    public JPanel getParentPanel() {
        return parentPanel;
    }

    private HTMLEditorKit getHTMLEditorKit() {
        if (editorKit != null) {
            return editorKit;
        }
        editorKit = new HTMLEditorKit();
        StyleSheet styleSheet = editorKit.getStyleSheet();
        styleSheet.addRule("html { font-family: sans-serif; line-height: 1.15; -webkit-text-size-adjust: 100%; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); }");
        styleSheet.addRule("article, aside, figcaption, figure, footer, header, hgroup, main, nav, section { display: block; }");
        styleSheet.addRule("body { margin: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Noto Sans', sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji'; font-size: 1rem; font-weight: 400; line-height: 1.5; color: #212529; text-align: left; background-color: #fff; }");
        styleSheet.addRule("hr { box-sizing: content-box; height: 0; overflow: visible; }");
        styleSheet.addRule("h1, h2, h3, h4, h5, h6 { margin-top: 0; margin-bottom: 0.5rem; }");
        styleSheet.addRule("p { margin-top: 0; margin-bottom: 1rem; }");
        styleSheet.addRule("abbr[title], abbr[data-original-title] { text-decoration: underline; -webkit-text-decoration: underline dotted; text-decoration: underline dotted; cursor: help; border-bottom: 0; -webkit-text-decoration-skip-ink: none; text-decoration-skip-ink: none; }");
        styleSheet.addRule("address { margin-bottom: 1rem; font-style: normal; line-height: inherit; }");
        styleSheet.addRule("ol, ul, dl { margin-top: 0; margin-bottom: 1rem; }");
        styleSheet.addRule("ol ol, ul ul, ol ul, ul ol { margin-bottom: 0; }");
        styleSheet.addRule("dt { font-weight: 700; }");
        styleSheet.addRule("dd { margin-bottom: .5rem; margin-left: 0; }");
        styleSheet.addRule("blockquote { margin: 0 0 1rem; }");
        styleSheet.addRule("b, strong { font-weight: bolder; }");
        styleSheet.addRule("small { font-size: 80%; }");
        styleSheet.addRule("sub, sup { position: relative; font-size: 75%; line-height: 0; vertical-align: baseline; }");
        styleSheet.addRule("sub { bottom: -.25em; }");
        styleSheet.addRule("sup { top: -.5em; }");
        styleSheet.addRule("a { color: #007bff; text-decoration: none; background-color: transparent; }");
        styleSheet.addRule("a:hover { color: #0056b3; text-decoration: underline; }");
        styleSheet.addRule("a:not([href]) { color: inherit; text-decoration: none; }");
        styleSheet.addRule("a:not([href]):hover { color: inherit; text-decoration: none; }");
        styleSheet.addRule("pre, code, kbd, samp { font-family: SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace; font-size: 1em; }");
        styleSheet.addRule("pre { margin-top: 0; margin-bottom: 1rem; overflow: auto; }");
        styleSheet.addRule("figure { margin: 0 0 1rem; }");
        styleSheet.addRule("img { vertical-align: middle; border-style: none; }");
        styleSheet.addRule("svg { overflow: hidden; vertical-align: middle; }");
        styleSheet.addRule("table { border-collapse: collapse; }");
        styleSheet.addRule("caption { padding-top: 0.75rem; padding-bottom: 0.75rem; color: #6c757d; text-align: left; caption-side: bottom; }");
        styleSheet.addRule("th { text-align: inherit; }");
        styleSheet.addRule("label { display: inline-block; margin-bottom: 0.5rem; }");
        styleSheet.addRule("button { border-radius: 0; }");
        styleSheet.addRule("button:focus { outline: 1px dotted; outline: 5px auto -webkit-focus-ring-color; }");
        styleSheet.addRule("input, button, select, optgroup, textarea { margin: 0; font-family: inherit; font-size: inherit; line-height: inherit; }");
        styleSheet.addRule("button, input { overflow: visible; }");
        styleSheet.addRule("button, select { text-transform: none; }");
        styleSheet.addRule("select { word-wrap: normal; }");
        styleSheet.addRule("button, [type='button'], [type='reset'], [type='submit'] { -webkit-appearance: button; }");
        styleSheet.addRule("button:not(:disabled), [type='button']:not(:disabled), [type='reset']:not(:disabled), [type='submit']:not(:disabled) { cursor: pointer; }");
        styleSheet.addRule("button::-moz-focus-inner, [type='button']::-moz-focus-inner, [type='reset']::-moz-focus-inner, [type='submit']::-moz-focus-inner { padding: 0; border-style: none; }");
        styleSheet.addRule("input[type='radio'], input[type='checkbox'] { box-sizing: border-box; padding: 0; }");
        styleSheet.addRule("input[type='date'], input[type='time'], input[type='datetime-local'], input[type='month'] { -webkit-appearance: listbox; }");
        styleSheet.addRule("textarea { overflow: auto; resize: vertical; }");
        styleSheet.addRule("fieldset { min-width: 0; padding: 0; margin: 0; border: 0; }");
        styleSheet.addRule("legend { display: block; width: 100%; max-width: 100%; padding: 0; margin-bottom: .5rem; font-size: 1.5rem; line-height: inherit; color: inherit; white-space: normal; }");
        styleSheet.addRule("progress { vertical-align: baseline; }");
        styleSheet.addRule("[type='number']::-webkit-inner-spin-button, [type='number']::-webkit-outer-spin-button { height: auto; }");
        styleSheet.addRule("[type='search'] { outline-offset: -2px; -webkit-appearance: none; }");
        styleSheet.addRule("[type='search']::-webkit-search-decoration { -webkit-appearance: none; }");
        styleSheet.addRule("::-webkit-file-upload-button { font: inherit; -webkit-appearance: button; }");
        styleSheet.addRule("output { display: inline-block; }");
        styleSheet.addRule("summary { display: list-item; cursor: pointer; }");
        styleSheet.addRule("template { display: none; }");
        styleSheet.addRule("[hidden] { display: none !important; }");
        styleSheet.addRule("h1, h2, h3, h4, h5, h6, .h1, .h2, .h3, .h4, .h5, .h6 { margin-bottom: 0.5rem; font-weight: 500; line-height: 1.2; }");
        styleSheet.addRule("h1, .h1 { font-size: 2.5rem; }");
        styleSheet.addRule("h2, .h2 { font-size: 2rem; }");
        styleSheet.addRule("h3, .h3 { font-size: 1.75rem; }");
        styleSheet.addRule("h4, .h4 { font-size: 1.5rem; }");
        styleSheet.addRule("h5, .h5 { font-size: 1.25rem; }");
        styleSheet.addRule("h6, .h6 { font-size: 1rem; }");
        styleSheet.addRule(".lead { font-size: 1.25rem; font-weight: 300; }");
        styleSheet.addRule(".display-1 { font-size: 6rem; font-weight: 300; line-height: 1.2; }");
        styleSheet.addRule(".display-2 { font-size: 5.5rem; font-weight: 300; line-height: 1.2; }");
        styleSheet.addRule(".display-3 { font-size: 4.5rem; font-weight: 300; line-height: 1.2; }");
        styleSheet.addRule(".display-4 { font-size: 3.5rem; font-weight: 300; line-height: 1.2; }");
        styleSheet.addRule("hr { margin-top: 1rem; margin-bottom: 1rem; border: 0; border-top: 1px solid rgba(0, 0, 0, 0.1); }");
        styleSheet.addRule("pre, code, kbd, samp { font-family: SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace; font-size: 1em; }");
        styleSheet.addRule("pre { margin-top: 0; margin-bottom: 1rem; overflow: auto; }");
        styleSheet.addRule("code { font-size: 87.5%; color: #e83e8c; word-wrap: break-word; }");
        styleSheet.addRule("pre { display: block; font-size: 87.5%; color: #212529; }");
        styleSheet.addRule("pre code { font-size: inherit; color: inherit; word-break: normal; }");
        return editorKit;
    }

}
