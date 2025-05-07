package io.github.jeddict.ai.components.mermaid;

import static io.github.jeddict.ai.util.EditorUtil.getBackgroundColorFromMimeType;
import static io.github.jeddict.ai.util.EditorUtil.getFontFromMimeType;
import static io.github.jeddict.ai.util.EditorUtil.getTextColorFromMimeType;
import static io.github.jeddict.ai.util.MimeUtil.MIME_PLAIN_TEXT;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.Border;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.*;

public class MermaidERDViewer extends MermaidViewer {

    private static AnchorShape getAnchorShape(String cardinality) {
        return switch (cardinality.trim()) {
            case "||" ->
                exactlyOneShape;       // exactly one
            case "|o", "o|" ->
                zeroOrOneShape;  // zero or one
            case "o{" ->
                zeroOrMoreShape;       // zero or more
            case "|{" ->
                oneOrMoreShape;        // one or more
            case "}o" ->
                zeroOrOneShape;        // many to zero or one (inverted)
            case "}|" ->
                oneOrMoreShape;        // many to one (inverted)
            case "}{" ->
                zeroOrMoreShape;       // many to many
            default ->
                AnchorShape.NONE;
        };
    }

    public static JComponent createMermaidERDView(String mermaidText) {
        Scene scene = new Scene();
        LayerWidget nodeLayer = new LayerWidget(scene);
        LayerWidget connectionLayer = new LayerWidget(scene);
        scene.addChild(nodeLayer);
        scene.addChild(connectionLayer);
        scene.getActions().addAction(ActionFactory.createZoomAction());
        scene.getActions().addAction(ActionFactory.createPanAction());

        Map<String, Widget> entityWidgets = new HashMap<>();
        Map<String, List<String>> entityAttributes = new HashMap<>();
        Map<String, List<String>> entityRelations = new HashMap<>();
        List<Relationship> relationships = new ArrayList<>();

        Pattern relationPattern = Pattern.compile("(\\w+)\\s+([|}o]{1,2})--([|{o]{1,2})\\s+(\\w+)\\s*:\\s*(\\w+)");
        Pattern entityStartPattern = Pattern.compile("^(\\w+)\\s*\\{");
        Pattern attributePattern = Pattern.compile("^\\s*(\\w+)\\s+(\\w+)");

        String[] lines = mermaidText.split("\\n");
        String currentEntity = null;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("erDiagram")) {
                continue;
            }

            Matcher relMatch = relationPattern.matcher(line);
            if (relMatch.matches()) {
                Relationship rel = new Relationship(
                        relMatch.group(1),
                        relMatch.group(4),
                        relMatch.group(2) + "--" + relMatch.group(3),
                        relMatch.group(5)
                );
                relationships.add(rel);
                entityRelations.computeIfAbsent(rel.from, k -> new ArrayList<>())
                        .add("→ " + rel.to + " : " + rel.label);
            } else {
                Matcher entityStart = entityStartPattern.matcher(line);
                if (entityStart.matches()) {
                    currentEntity = entityStart.group(1);
                    entityAttributes.putIfAbsent(currentEntity, new ArrayList<>());
                } else if (line.startsWith("}")) {
                    currentEntity = null;
                } else if (currentEntity != null) {
                    Matcher attrMatch = attributePattern.matcher(line);
                    if (attrMatch.matches()) {
                        entityAttributes.get(currentEntity).add(attrMatch.group(1) + " " + attrMatch.group(2));
                    }
                }
            }
        }

        // Create entity widgets with relations
        for (String entity : entityAttributes.keySet()) {
            List<String> rels = entityRelations.getOrDefault(entity, Collections.emptyList());
            Widget widget = createEntityWidget(scene, entity, entityAttributes.get(entity), rels);
            nodeLayer.addChild(widget);
            entityWidgets.put(entity, widget);
        }

        applyGridLayout(entityWidgets, 3, 300, 220);

        Color textColor = getTextColorFromMimeType(MIME_PLAIN_TEXT);
        Color backgroundColor = getBackgroundColorFromMimeType(MIME_PLAIN_TEXT);

        for (Relationship rel : relationships) {
            Widget from = entityWidgets.get(rel.from);
            Widget to = entityWidgets.get(rel.to);
            if (from == null || to == null) {
                System.err.printf("Skipping relationship '%s' -> '%s': Widget not found.%n", rel.from, rel.to);
                continue;
            }
            ConnectionWidget conn = new ConnectionWidget(scene);
            conn.setRouter(RouterFactory.createOrthogonalSearchRouter(connectionLayer));
            conn.setSourceAnchor(AnchorFactory.createRectangularAnchor(from));
            conn.setTargetAnchor(AnchorFactory.createRectangularAnchor(to));
            conn.setSourceAnchorShape(getAnchorShape(rel.sourceCardinality));
            conn.setTargetAnchorShape(getAnchorShape(rel.targetCardinality));
            conn.setForeground(textColor);

            LabelWidget label = new LabelWidget(scene, rel.label);
            label.setOpaque(false);
            label.setForeground(textColor);
            label.setBackground(backgroundColor);
            label.setAlignment(LabelWidget.Alignment.BASELINE);
            conn.addChild(label);
            conn.setConstraint(label, LayoutFactory.ConnectionWidgetLayoutAlignment.CENTER, 0.5f);

            connectionLayer.addChild(conn);
        }

        scene.setBackground(getBackgroundColorFromMimeType(MIME_PLAIN_TEXT));
        JComponent pane = scene.createView();
        pane.setBackground(getBackgroundColorFromMimeType(MIME_PLAIN_TEXT));
        return pane;
    }

    private static Widget createEntityWidget(Scene scene, String title, List<String> attributes, List<String> relations) {
        Widget box = new Widget(scene);
        box.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 4));

        Font font = getFontFromMimeType(MIME_PLAIN_TEXT);
        Color textColor = getTextColorFromMimeType(MIME_PLAIN_TEXT);
        Color backgroundColor = getBackgroundColorFromMimeType(MIME_PLAIN_TEXT);
        boolean isDark = isDarkColor(backgroundColor);

        Color headerColor = isDark ? backgroundColor.brighter() : backgroundColor.darker();

        // Rounded border
        Border roundedBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isDark ? textColor.brighter() : textColor.darker()),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        );
        box.setBorder(roundedBorder);
        box.setBackground(backgroundColor);
        box.setOpaque(true);

        // Header panel with full width
        Widget header = new Widget(scene) {
            @Override
            protected void paintWidget() {
                Graphics2D g = (Graphics2D) this.getGraphics();
                if (g != null) {
                    g.setColor(headerColor);
                    g.fillRoundRect(0, 0, getBounds().width, getBounds().height, 4, 4);
                }
                super.paintWidget();
            }
        };
        header.setLayout(LayoutFactory.createOverlayLayout());
        header.setOpaque(false);
        header.setPreferredBounds(new Rectangle(0, 0, 200, 30)); // Ensure full width

// Full-width title label inside header
        LabelWidget titleLabel = new LabelWidget(scene, title);
        titleLabel.setFont(font.deriveFont(Font.BOLD));
//        titleLabel.setForeground(textColor);
        titleLabel.setForeground(new Color(0x007bff));
        titleLabel.setAlignment(LabelWidget.Alignment.CENTER);

        header.addChild(titleLabel);
        box.addChild(header);

        // Shadow effect via padding layer (optional)
        box.setPreferredLocation(new Point(5, 5));

        // Attributes
        for (String attr : attributes) {
            String[] parts = attr.split(" ", 2);
            String type = parts.length > 0 ? parts[0] : "";
            String name = parts.length > 1 ? parts[1] : "";

            Widget attrBox = new Widget(scene);
            attrBox.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.LEFT_TOP, 4));

            LabelWidget typeLabel = new LabelWidget(scene, type);
            typeLabel.setFont(font);
            typeLabel.setForeground(new Color(0xFF6600));
            attrBox.addChild(typeLabel);

            LabelWidget nameLabel = new LabelWidget(scene, name);
            nameLabel.setFont(font);
            nameLabel.setForeground(textColor);
            attrBox.addChild(nameLabel);

            box.addChild(attrBox);
        }

        // Relations Section
        if (!relations.isEmpty()) {
            LabelWidget separator = new LabelWidget(scene, "── Relations ──");
            separator.setFont(font.deriveFont(Font.BOLD));
            separator.setForeground(textColor.darker());
            box.addChild(separator);

            for (String rel : relations) {
                LabelWidget relLabel = new LabelWidget(scene, rel);
                relLabel.setFont(font);
                relLabel.setForeground(new Color(0x1DA1F2));
                box.addChild(relLabel);
            }
        }

        box.getActions().addAction(ActionFactory.createMoveAction());

        return box;
    }

    private static class Relationship {

        String from, to, label;
        String sourceCardinality, targetCardinality;

        Relationship(String from, String to, String cardinality, String label) {
            this.from = from;
            this.to = to;
            this.label = label;
            if (cardinality.length() > 4) {
                this.sourceCardinality = cardinality.substring(0, 2).trim();
                this.targetCardinality = cardinality.substring(4).trim();
            }
        }

    }

    public static void main(String[] args) {
        String mermaid = """
            erDiagram
                CUSTOMER {
                    string customerID
                    string name
                    string email
                }
                ORDER {
                    string orderID
                    date orderDate
                    float totalAmount
                }
                PRODUCT {
                    string productID
                    string productName
                    float price
                }

             
            """;

        JFrame frame = new JFrame("Mermaid ERD Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.add(createMermaidERDView(mermaid));
        frame.setVisible(true);
    }

    private static Color textColor = getTextColorFromMimeType(MIME_PLAIN_TEXT);
    private static AnchorShape zeroOrOneShape = new AnchorShape() {
        @Override
        public void paint(Graphics2D g, boolean source) {
            // Draw a small circle
            g.setColor(textColor);
            g.drawOval(-6, -6, 6, 6);

            // Draw a small vertical line beside the circle
            g.drawLine(0, -6, 0, 6);
        }

        @Override
        public boolean isLineOriented() {
            return false;
        }

        @Override
        public int getRadius() {
            return 6;
        }

        @Override
        public double getCutDistance() {
            return getRadius();
        }
    };

    private static AnchorShape exactlyOneShape = new AnchorShape() {
        @Override
        public void paint(Graphics2D g, boolean source) {
            g.setColor(textColor);
            // Two vertical lines spaced slightly apart
            g.drawLine(-3, -6, -3, 6);
            g.drawLine(3, -6, 3, 6);
        }

        @Override
        public boolean isLineOriented() {
            return false;
        }

        @Override
        public int getRadius() {
            return 6;
        }

        @Override
        public double getCutDistance() {
            return getRadius();
        }
    };

    private static AnchorShape zeroOrMoreShape = new AnchorShape() {
        @Override
        public void paint(Graphics2D g, boolean source) {
            g.setColor(textColor);
            // Circle for zero
            g.drawOval(-10, -4, 6, 6);

            // Fork/star for many
            g.drawLine(0, 0, 6, 0);      // horizontal
            g.drawLine(0, 0, 5, -5);     // upper diagonal
            g.drawLine(0, 0, 5, 5);      // lower diagonal
        }

        @Override
        public boolean isLineOriented() {
            return false;
        }

        @Override
        public int getRadius() {
            return 10;
        }

        @Override
        public double getCutDistance() {
            return getRadius();
        }
    };

    private static final AnchorShape oneOrMoreShape = new AnchorShape() {
        @Override
        public void paint(Graphics2D g, boolean source) {
            g.setColor(textColor);

            // Vertical line for 1
            g.drawLine(-6, -6, -6, 6);

            // Fork/star for many
            g.drawLine(0, 0, 6, 0);      // horizontal
            g.drawLine(0, 0, 5, -5);     // upper diagonal
            g.drawLine(0, 0, 5, 5);      // lower diagonal
        }

        @Override
        public boolean isLineOriented() {
            return false;
        }

        @Override
        public int getRadius() {
            return 10;
        }

        @Override
        public double getCutDistance() {
            return getRadius();
        }
    };

}
