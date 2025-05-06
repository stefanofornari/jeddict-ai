package io.github.jeddict.ai.components.mermaid;

import static io.github.jeddict.ai.util.EditorUtil.getBackgroundColorFromMimeType;
import static io.github.jeddict.ai.util.EditorUtil.getFontFromMimeType;
import static io.github.jeddict.ai.util.EditorUtil.getTextColorFromMimeType;
import static io.github.jeddict.ai.util.MimeUtil.MIME_PLAIN_TEXT;

import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.widget.*;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.router.RouterFactory;

public class MermaidClassDiagramViewer extends MermaidViewer {

    public static JComponent createMermaidClassDiagramView(String mermaidText) {
        Scene scene = new Scene();
        LayerWidget nodeLayer = new LayerWidget(scene);
        LayerWidget connectionLayer = new LayerWidget(scene);
        scene.addChild(nodeLayer);
        scene.addChild(connectionLayer);
        scene.getActions().addAction(ActionFactory.createZoomAction());
        scene.getActions().addAction(ActionFactory.createPanAction());

        Map<String, Widget> classWidgets = new HashMap<>();
        Map<String, ClassInfo> classes = new HashMap<>();
        List<Inheritance> inheritances = new ArrayList<>();
        List<Association> associations = new ArrayList<>();

        Color textColor = getTextColorFromMimeType(MIME_PLAIN_TEXT);
        Color backgroundColor = getBackgroundColorFromMimeType(MIME_PLAIN_TEXT);
        Font font = getFontFromMimeType(MIME_PLAIN_TEXT);

        Pattern classPattern = Pattern.compile("class\\s+(\\w+)\\s*\\{?");
        Pattern methodPattern = Pattern.compile("([+\\-#])\\s*(?:(\\w+)\\s+)?(\\w+)\\s*\\(\\)");
        Pattern attributePattern1 = Pattern.compile("([+\\-#])\\s*(\\w+)\\s+(\\w+)");  // +String name
        Pattern attributePattern2 = Pattern.compile("([+\\-#])\\s*(\\w+)\\s*:\\s*(\\w+)"); // +address: String
        Pattern inheritancePattern = Pattern.compile("(\\w+)\\s+<\\|--\\s+(\\w+)");
        Pattern associationPattern = Pattern.compile("(\\w+)\\s+\"([^\"]+)\"\\s+--\\s+\"([^\"]+)\"\\s+(\\w+)\\s*:\\s*(\\w+)");

        String[] lines = mermaidText.split("\\n");
        String currentClass = null;

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("classDiagram")) {
                continue;
            }

            Matcher inheritance = inheritancePattern.matcher(line);
            if (inheritance.matches()) {
                inheritances.add(new Inheritance(inheritance.group(2), inheritance.group(1)));
                continue;
            }

            // Inside the for-loop that processes lines
            Matcher assocMatch = associationPattern.matcher(line);
            if (assocMatch.matches()) {
                associations.add(new Association(
                        assocMatch.group(1), // source
                        assocMatch.group(2), // "1"
                        assocMatch.group(3), // "1..*"
                        assocMatch.group(4), // target
                        assocMatch.group(5) // label
                ));
                continue;
            }

            Matcher classDecl = classPattern.matcher(line);
            if (classDecl.matches()) {
                currentClass = classDecl.group(1);
                classes.putIfAbsent(currentClass, new ClassInfo(currentClass));
                continue;
            }

            if (line.equals("}")) {
                currentClass = null;
                continue;
            }

            // NEW: Implicit class usage via 'ClassName : ...'
            if (line.contains(":") && !line.trim().startsWith("+")) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    String implicitClass = parts[0].trim();
                    String member = parts[1].trim();
                    classes.putIfAbsent(implicitClass, new ClassInfo(implicitClass));
                    currentClass = implicitClass;

                    Matcher methodMatch = methodPattern.matcher(member);

                    if (methodMatch.matches()) {
                        String returnType = methodMatch.group(2); // may be null
                        String methodName = methodMatch.group(3);
                        String methodSignature = methodName + "()";
                        if (returnType != null) {
                            methodSignature += ": " + returnType;
                        }
                        classes.get(currentClass).methods.add(methodSignature);
                    }

                    Matcher attrMatch1 = attributePattern1.matcher(line);
                    Matcher attrMatch2 = attributePattern2.matcher(line);

                    if (attrMatch1.matches()) {
                        // attrMatch1: visibility, type, name
                        String visibility = attrMatch1.group(1);
                        String type = attrMatch1.group(2);
                        String name = attrMatch1.group(3);
                        classes.get(currentClass).attributes.add(name + ": " + type);
                    } else if (attrMatch2.matches()) {
                        // attrMatch2: visibility, name, type
                        String visibility = attrMatch2.group(1);
                        String name = attrMatch2.group(2);
                        String type = attrMatch2.group(3);
                        classes.get(currentClass).attributes.add(name + ": " + type);
                    }
                }
                continue;
            }

            if (currentClass != null) {
                Matcher methodMatch = methodPattern.matcher(line);

                if (methodMatch.matches()) {
                    String returnType = methodMatch.group(2); // may be null
                    String methodName = methodMatch.group(3);
                    String methodSignature = methodName + "()";
                    if (returnType != null) {
                        methodSignature += ": " + returnType;
                    }
                    classes.get(currentClass).methods.add(methodSignature);
                }

                Matcher attrMatch1 = attributePattern1.matcher(line);
                Matcher attrMatch2 = attributePattern2.matcher(line);

                if (attrMatch1.matches()) {
                    // attrMatch1: visibility, type, name
                    String visibility = attrMatch1.group(1);
                    String type = attrMatch1.group(2);
                    String name = attrMatch1.group(3);
                    classes.get(currentClass).attributes.add(name + ": " + type);
                } else if (attrMatch2.matches()) {
                    // attrMatch2: visibility, name, type
                    String visibility = attrMatch2.group(1);
                    String name = attrMatch2.group(2);
                    String type = attrMatch2.group(3);
                    classes.get(currentClass).attributes.add(name + ": " + type);
                }

            }
        }

        for (ClassInfo cls : classes.values()) {
            Widget widget = createClassWidget(scene, cls);
            classWidgets.put(cls.name, widget);
            nodeLayer.addChild(widget);
        }

        applyGridLayout(classWidgets, 3, 280, 220);

        for (Inheritance inh : inheritances) {
            Widget sub = classWidgets.get(inh.subclass);
            Widget sup = classWidgets.get(inh.superclass);
            if (sub == null || sup == null) {
                continue;
            }

            ConnectionWidget conn = new ConnectionWidget(scene);
            conn.setSourceAnchor(AnchorFactory.createRectangularAnchor(sub));
            conn.setTargetAnchor(AnchorFactory.createRectangularAnchor(sup));
            conn.setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);
            conn.setRouter(RouterFactory.createOrthogonalSearchRouter(connectionLayer));
            conn.setForeground(textColor);
            connectionLayer.addChild(conn);
        }

        for (Association assoc : associations) {
            Widget source = classWidgets.get(assoc.sourceClass);
            Widget target = classWidgets.get(assoc.targetClass);
            if (source == null || target == null) {
                continue;
            }

            ConnectionWidget conn = new ConnectionWidget(scene);
            conn.setSourceAnchor(AnchorFactory.createRectangularAnchor(source));
            conn.setTargetAnchor(AnchorFactory.createRectangularAnchor(target));
            conn.setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);
            conn.setRouter(RouterFactory.createOrthogonalSearchRouter(connectionLayer));
            conn.setForeground(textColor);

//            if (assoc.label != null && !assoc.label.isEmpty()) {
//                LabelWidget label = new LabelWidget(scene, assoc.label);
//                label.setFont(font);
//                label.setForeground(textColor);
//                conn.addChild(label);
////        conn.setConstraint(label, new Point(0, 0)); // optional, adjust position
//            }

//            // Label for source cardinality
//            if (assoc.sourceCardinality != null && !assoc.sourceCardinality.isEmpty()) {
//                LabelWidget sourceLabel = new LabelWidget(scene, assoc.sourceCardinality);
//                sourceLabel.setFont(font);
//                sourceLabel.setForeground(textColor);
//                conn.addChild(sourceLabel);
            ////        conn.setConstraint(sourceLabel, ConnectionWidget.LayoutAlignment.LEFT_SOURCE, 0.1f);
//            }
//
//            // Label for target cardinality
//            if (assoc.targetCardinality != null && !assoc.targetCardinality.isEmpty()) {
//                LabelWidget targetLabel = new LabelWidget(scene, assoc.targetCardinality);
//                targetLabel.setFont(font);
//                targetLabel.setForeground(textColor);
//                conn.addChild(targetLabel, ConnectionWidget.RoutingPolicy.TARGET);
////        conn.setConstraint(targetLabel, ConnectionWidget.RoutingPolicy.RIGHT_TARGET, 0.9f);
//
//            }
            connectionLayer.addChild(conn);
        }

        scene.setBackground(getBackgroundColorFromMimeType(MIME_PLAIN_TEXT));
        return scene.createView();
    }

    private static Widget createClassWidget(Scene scene, ClassInfo cls) {
        Widget box = new Widget(scene);
        box.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 4));

        Font font = getFontFromMimeType(MIME_PLAIN_TEXT);
        Color textColor = getTextColorFromMimeType(MIME_PLAIN_TEXT);
        Color backgroundColor = getBackgroundColorFromMimeType(MIME_PLAIN_TEXT);

        Border border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(textColor),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        );
        box.setBorder(border);
        box.setBackground(backgroundColor);
        box.setOpaque(true);
        box.setPreferredLocation(new Point(5, 5));

        LabelWidget titleLabel = new LabelWidget(scene, cls.name);
        titleLabel.setFont(font.deriveFont(Font.BOLD));
        titleLabel.setForeground(new Color(0x007bff));
        titleLabel.setAlignment(LabelWidget.Alignment.CENTER);
        box.addChild(titleLabel);

        if (!cls.attributes.isEmpty()) {
            LabelWidget sep = new LabelWidget(scene, "-- Attributes --");
            sep.setFont(font.deriveFont(Font.BOLD));
            sep.setForeground(textColor);
            box.addChild(sep);
            for (String attr : cls.attributes) {
                LabelWidget lbl = new LabelWidget(scene, attr);
                lbl.setFont(font);
                lbl.setForeground(new Color(0xFF6600));
                box.addChild(lbl);
            }
        }

        if (!cls.methods.isEmpty()) {
            LabelWidget sep = new LabelWidget(scene, "-- Methods --");
            sep.setFont(font.deriveFont(Font.BOLD));
            sep.setForeground(textColor);
            box.addChild(sep);
            for (String method : cls.methods) {
                LabelWidget lbl = new LabelWidget(scene, method);
                lbl.setFont(font);
                lbl.setForeground(new Color(0x1DA1F2));
                box.addChild(lbl);
            }
        }

        box.getActions().addAction(ActionFactory.createMoveAction());
        return box;
    }

    private static class ClassInfo {

        String name;
        List<String> attributes = new ArrayList<>();
        List<String> methods = new ArrayList<>();

        ClassInfo(String name) {
            this.name = name;
        }
    }

    private static class Inheritance {

        String subclass, superclass;

        Inheritance(String subclass, String superclass) {
            this.subclass = subclass;
            this.superclass = superclass;
        }
    }

    private static class Association {

        String sourceClass, targetClass;
        String sourceCardinality, targetCardinality, label;

        Association(String sourceClass, String sourceCardinality, String targetCardinality, String targetClass, String label) {
            this.sourceClass = sourceClass;
            this.targetClass = targetClass;
            this.sourceCardinality = sourceCardinality;
            this.targetCardinality = targetCardinality;
            this.label = label;
        }
    }

}
