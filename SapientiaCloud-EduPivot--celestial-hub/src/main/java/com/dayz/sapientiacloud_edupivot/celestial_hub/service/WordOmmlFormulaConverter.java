package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.openxmlformats.schemas.officeDocument.x2006.math.CTF;
import org.openxmlformats.schemas.officeDocument.x2006.math.CTOMath;
import org.openxmlformats.schemas.officeDocument.x2006.math.CTOMathArg;
import org.openxmlformats.schemas.officeDocument.x2006.math.CTOMathJc;
import org.openxmlformats.schemas.officeDocument.x2006.math.CTOMathPara;
import org.openxmlformats.schemas.officeDocument.x2006.math.CTOMathParaPr;
import org.openxmlformats.schemas.officeDocument.x2006.math.CTOnOff;
import org.openxmlformats.schemas.officeDocument.x2006.math.CTR;
import org.openxmlformats.schemas.officeDocument.x2006.math.CTRad;
import org.openxmlformats.schemas.officeDocument.x2006.math.CTSSub;
import org.openxmlformats.schemas.officeDocument.x2006.math.CTSSubSup;
import org.openxmlformats.schemas.officeDocument.x2006.math.CTSSup;
import org.openxmlformats.schemas.officeDocument.x2006.math.CTText;
import org.openxmlformats.schemas.officeDocument.x2006.math.STJc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
final class WordOmmlFormulaConverter {

    private static final Map<String, String> COMMAND_SYMBOLS = createCommandSymbols();
    private static final Map<String, String> DELIMITER_SYMBOLS = createDelimiterSymbols();
    private static final Set<String> RAW_TEXT_COMMANDS = Set.of(
            "text", "textrm", "textbf", "textit", "mathrm", "mathbf",
            "mathit", "mathsf", "mathtt", "operatorname", "mbox"
    );
    private static final Set<String> SPACE_COMMANDS = Set.of(
            ",", ";", ":", "quad", "qquad", "enspace", "thinspace", "medspace"
    );
    private static final Set<String> NOOP_COMMANDS = Set.of(
            "displaystyle", "textstyle", "scriptstyle", "scriptscriptstyle",
            "limits", "nolimits", "!", "left.", "right."
    );

    private WordOmmlFormulaConverter() {
    }

    static boolean appendFormula(XWPFParagraph paragraph, String latex, boolean display) {
        if (paragraph == null || latex == null || latex.isBlank()) {
            return false;
        }

        try {
            Node root = new Parser(latex).parse();
            if (root instanceof EmptyNode) {
                return false;
            }

            if (display) {
                CTOMathPara mathPara = paragraph.getCTP().addNewOMathPara();
                CTOMathParaPr mathParaPr = mathPara.addNewOMathParaPr();
                CTOMathJc mathJc = mathParaPr.addNewJc();
                mathJc.setVal(STJc.CENTER_GROUP);
                appendNode(asContainer(mathPara.addNewOMath()), root);
                return true;
            }

            appendNode(asContainer(paragraph.getCTP().addNewOMath()), root);
            return true;
        } catch (Exception e) {
            log.warn("Failed to convert latex to Word OMML. latex={}", latex, e);
            return false;
        }
    }

    private static void appendNode(MathContainer container, Node node) {
        if (node == null || node instanceof EmptyNode) {
            return;
        }

        if (node instanceof SequenceNode sequenceNode) {
            for (Node child : sequenceNode.children()) {
                appendNode(container, child);
            }
            return;
        }

        if (node instanceof TextNode textNode) {
            appendText(container, textNode.text());
            return;
        }

        if (node instanceof DelimitedNode delimitedNode) {
            appendText(container, delimitedNode.leftDelimiter());
            appendNode(container, delimitedNode.content());
            appendText(container, delimitedNode.rightDelimiter());
            return;
        }

        if (node instanceof FractionNode fractionNode) {
            CTF fraction = container.addFraction();
            appendNode(asContainer(fraction.addNewNum()), fractionNode.numerator());
            appendNode(asContainer(fraction.addNewDen()), fractionNode.denominator());
            return;
        }

        if (node instanceof RadicalNode radicalNode) {
            CTRad radical = container.addRadical();
            if (radicalNode.degree() == null || radicalNode.degree() instanceof EmptyNode) {
                CTOnOff degHide = radical.addNewRadPr().addNewDegHide();
                degHide.setVal("1");
                radical.addNewDeg();
            } else {
                appendNode(asContainer(radical.addNewDeg()), radicalNode.degree());
            }
            appendNode(asContainer(radical.addNewE()), radicalNode.radicand());
            return;
        }

        if (node instanceof ScriptNode scriptNode) {
            if (scriptNode.subscript() != null && !(scriptNode.subscript() instanceof EmptyNode)
                    && scriptNode.superscript() != null && !(scriptNode.superscript() instanceof EmptyNode)) {
                CTSSubSup subSup = container.addSubSuperscript();
                appendNode(asContainer(subSup.addNewE()), scriptNode.base());
                appendNode(asContainer(subSup.addNewSub()), scriptNode.subscript());
                appendNode(asContainer(subSup.addNewSup()), scriptNode.superscript());
                return;
            }

            if (scriptNode.subscript() != null && !(scriptNode.subscript() instanceof EmptyNode)) {
                CTSSub sub = container.addSubscript();
                appendNode(asContainer(sub.addNewE()), scriptNode.base());
                appendNode(asContainer(sub.addNewSub()), scriptNode.subscript());
                return;
            }

            if (scriptNode.superscript() != null && !(scriptNode.superscript() instanceof EmptyNode)) {
                CTSSup sup = container.addSuperscript();
                appendNode(asContainer(sup.addNewE()), scriptNode.base());
                appendNode(asContainer(sup.addNewSup()), scriptNode.superscript());
                return;
            }

            appendNode(container, scriptNode.base());
        }
    }

    private static void appendText(MathContainer container, String text) {
        if (text == null || text.isEmpty()) {
            return;
        }

        CTR run = container.addRun();
        CTText value = run.addNewT2();
        value.setStringValue(text);
        if (hasPreservedSpaces(text)) {
            value.setSpace(org.apache.xmlbeans.impl.xb.xmlschema.SpaceAttribute.Space.PRESERVE);
        }
    }

    private static boolean hasPreservedSpaces(String text) {
        return !text.isEmpty() && (Character.isWhitespace(text.charAt(0))
                || Character.isWhitespace(text.charAt(text.length() - 1))
                || text.contains("  "));
    }

    private static MathContainer asContainer(CTOMath math) {
        return new MathContainer() {
            @Override
            public CTR addRun() {
                return math.addNewR();
            }

            @Override
            public CTF addFraction() {
                return math.addNewF();
            }

            @Override
            public CTRad addRadical() {
                return math.addNewRad();
            }

            @Override
            public CTSSub addSubscript() {
                return math.addNewSSub();
            }

            @Override
            public CTSSup addSuperscript() {
                return math.addNewSSup();
            }

            @Override
            public CTSSubSup addSubSuperscript() {
                return math.addNewSSubSup();
            }
        };
    }

    private static MathContainer asContainer(CTOMathArg mathArg) {
        return new MathContainer() {
            @Override
            public CTR addRun() {
                return mathArg.addNewR();
            }

            @Override
            public CTF addFraction() {
                return mathArg.addNewF();
            }

            @Override
            public CTRad addRadical() {
                return mathArg.addNewRad();
            }

            @Override
            public CTSSub addSubscript() {
                return mathArg.addNewSSub();
            }

            @Override
            public CTSSup addSuperscript() {
                return mathArg.addNewSSup();
            }

            @Override
            public CTSSubSup addSubSuperscript() {
                return mathArg.addNewSSubSup();
            }
        };
    }

    private interface MathContainer {
        CTR addRun();

        CTF addFraction();

        CTRad addRadical();

        CTSSub addSubscript();

        CTSSup addSuperscript();

        CTSSubSup addSubSuperscript();
    }

    private sealed interface Node permits EmptyNode, SequenceNode, TextNode, FractionNode, RadicalNode, ScriptNode, DelimitedNode {
    }

    private record EmptyNode() implements Node {
    }

    private record SequenceNode(List<Node> children) implements Node {
    }

    private record TextNode(String text) implements Node {
    }

    private record FractionNode(Node numerator, Node denominator) implements Node {
    }

    private record RadicalNode(Node degree, Node radicand) implements Node {
    }

    private record ScriptNode(Node base, Node subscript, Node superscript) implements Node {
    }

    private record DelimitedNode(String leftDelimiter, Node content, String rightDelimiter) implements Node {
    }

    private static final class Parser {

        private final String input;
        private int index;

        private Parser(String input) {
            this.input = input == null ? "" : input;
        }

        private Node parse() {
            return simplify(parseNodes(null, false));
        }

        private List<Node> parseNodes(Character stopChar, boolean stopAtRight) {
            List<Node> nodes = new ArrayList<>();
            while (!isEnd()) {
                if (stopChar != null && peek() == stopChar) {
                    break;
                }
                if (stopAtRight && isRightCommandAhead()) {
                    break;
                }

                Node node = parseAtomWithScripts();
                if (!(node instanceof EmptyNode)) {
                    nodes.add(node);
                } else if (!isEnd() && stopChar == null && !stopAtRight) {
                    break;
                }
            }
            return nodes;
        }

        private Node parseAtomWithScripts() {
            skipWhitespace();
            if (isEnd()) {
                return new EmptyNode();
            }

            Node base = parsePrimary();
            if (base == null || base instanceof EmptyNode) {
                return new EmptyNode();
            }

            Node subscript = null;
            Node superscript = null;
            while (!isEnd()) {
                skipWhitespace();
                if (isEnd()) {
                    break;
                }

                char current = peek();
                if (current == '_') {
                    index++;
                    subscript = parseRequiredArgument();
                    continue;
                }
                if (current == '^') {
                    index++;
                    superscript = parseRequiredArgument();
                    continue;
                }
                break;
            }

            if (subscript == null && superscript == null) {
                return base;
            }
            return new ScriptNode(base, subscript, superscript);
        }

        private Node parsePrimary() {
            skipWhitespace();
            if (isEnd()) {
                return new EmptyNode();
            }

            char current = peek();
            if (current == '{') {
                index++;
                Node group = simplify(parseNodes('}', false));
                consumeIf('}');
                return group;
            }
            if (current == '(') {
                return parseDelimitedGroup('(', ')', "(", ")");
            }
            if (current == '[') {
                return parseDelimitedGroup('[', ']', "[", "]");
            }
            if (current == '\\') {
                return parseCommand();
            }
            if (current == '}' || current == ')' || current == ']') {
                return new EmptyNode();
            }
            return new TextNode(readPlainText());
        }

        private Node parseDelimitedGroup(char open, char close, String leftDelimiter, String rightDelimiter) {
            index++;
            Node content = simplify(parseNodes(close, false));
            consumeIf(close);
            return new DelimitedNode(leftDelimiter, content, rightDelimiter);
        }

        private Node parseCommand() {
            index++;
            if (isEnd()) {
                return new TextNode("\\");
            }

            if (!Character.isLetter(peek())) {
                char escaped = input.charAt(index++);
                if (SPACE_COMMANDS.contains(String.valueOf(escaped))) {
                    return new TextNode(" ");
                }
                return new TextNode(DELIMITER_SYMBOLS.getOrDefault(String.valueOf(escaped), String.valueOf(escaped)));
            }

            String command = readCommandName();
            if (command.isEmpty()) {
                return new EmptyNode();
            }
            if (NOOP_COMMANDS.contains(command)) {
                return new EmptyNode();
            }
            if (SPACE_COMMANDS.contains(command)) {
                return new TextNode(" ");
            }
            if (RAW_TEXT_COMMANDS.contains(command)) {
                return new TextNode(parseRawTextArgument());
            }

            return switch (command) {
                case "frac", "dfrac", "tfrac" -> new FractionNode(parseRequiredArgument(), parseRequiredArgument());
                case "sqrt" -> parseRoot();
                case "left" -> parseLeftRightGroup();
                case "right" -> new EmptyNode();
                case "bar" -> applyCombiningAccent(parseRequiredArgument(), "\u0304");
                case "overline" -> applyCombiningAccent(parseRequiredArgument(), "\u0305");
                case "hat" -> applyCombiningAccent(parseRequiredArgument(), "\u0302");
                case "vec", "overrightarrow" -> applyCombiningAccent(parseRequiredArgument(), "\u20d7");
                case "underline" -> applyCombiningAccent(parseRequiredArgument(), "\u0332");
                case "dots", "cdots", "ldots" -> new TextNode("\u2026");
                default -> {
                    String mapped = COMMAND_SYMBOLS.get(command);
                    if (mapped != null) {
                        yield new TextNode(mapped);
                    }
                    yield new TextNode("\\" + command);
                }
            };
        }

        private Node parseRoot() {
            skipWhitespace();
            Node degree = null;
            if (!isEnd() && peek() == '[') {
                index++;
                degree = simplify(parseNodes(']', false));
                consumeIf(']');
            }
            return new RadicalNode(degree, parseRequiredArgument());
        }

        private Node parseLeftRightGroup() {
            String leftDelimiter = parseDelimiterToken();
            Node content = simplify(parseNodes(null, true));
            String rightDelimiter = "";
            if (isRightCommandAhead()) {
                index += "\\right".length();
                rightDelimiter = parseDelimiterToken();
            }
            return new DelimitedNode(leftDelimiter, content, rightDelimiter);
        }

        private String parseDelimiterToken() {
            skipWhitespace();
            if (isEnd()) {
                return "";
            }

            if (peek() == '.') {
                index++;
                return "";
            }

            if (peek() == '\\') {
                index++;
                if (isEnd()) {
                    return "";
                }
                if (!Character.isLetter(peek())) {
                    char escaped = input.charAt(index++);
                    return DELIMITER_SYMBOLS.getOrDefault(String.valueOf(escaped), String.valueOf(escaped));
                }
                String command = readCommandName();
                return DELIMITER_SYMBOLS.getOrDefault(command, COMMAND_SYMBOLS.getOrDefault(command, "\\" + command));
            }

            return String.valueOf(input.charAt(index++));
        }

        private Node parseRequiredArgument() {
            skipWhitespace();
            if (isEnd()) {
                return new EmptyNode();
            }

            if (peek() == '{') {
                index++;
                Node group = simplify(parseNodes('}', false));
                consumeIf('}');
                return group;
            }
            if (peek() == '(') {
                return parseDelimitedGroup('(', ')', "(", ")");
            }
            if (peek() == '[') {
                return parseDelimitedGroup('[', ']', "[", "]");
            }
            return parseAtomWithScripts();
        }

        private String parseRawTextArgument() {
            skipWhitespace();
            if (isEnd()) {
                return "";
            }

            if (peek() != '{') {
                if (peek() == '\\') {
                    Node node = parseCommand();
                    return flattenPlainText(node);
                }
                return String.valueOf(input.charAt(index++));
            }

            index++;
            int depth = 1;
            StringBuilder builder = new StringBuilder();
            while (!isEnd() && depth > 0) {
                char current = input.charAt(index++);
                if (current == '{') {
                    depth++;
                    if (depth > 1) {
                        builder.append(current);
                    }
                    continue;
                }
                if (current == '}') {
                    depth--;
                    if (depth > 0) {
                        builder.append(current);
                    }
                    continue;
                }
                if (current == '\\' && !isEnd()) {
                    char escaped = input.charAt(index);
                    if (!Character.isLetter(escaped)) {
                        builder.append(DELIMITER_SYMBOLS.getOrDefault(String.valueOf(escaped), String.valueOf(escaped)));
                        index++;
                        continue;
                    }
                }
                builder.append(current);
            }
            return builder.toString();
        }

        private String readCommandName() {
            int start = index;
            while (!isEnd() && Character.isLetter(peek())) {
                index++;
            }
            return input.substring(start, index);
        }

        private String readPlainText() {
            StringBuilder builder = new StringBuilder();
            while (!isEnd()) {
                char current = peek();
                if (Character.isWhitespace(current) || current == '\\' || current == '{' || current == '}'
                        || current == '^' || current == '_' || current == '(' || current == ')' || current == '['
                        || current == ']') {
                    break;
                }
                builder.append(current == '~' ? ' ' : current);
                index++;
            }
            return builder.toString();
        }

        private boolean isRightCommandAhead() {
            return input.startsWith("\\right", index)
                    && (index + 6 >= input.length() || !Character.isLetter(input.charAt(index + 6)));
        }

        private boolean consumeIf(char expected) {
            if (!isEnd() && peek() == expected) {
                index++;
                return true;
            }
            return false;
        }

        private void skipWhitespace() {
            while (!isEnd() && Character.isWhitespace(peek())) {
                index++;
            }
        }

        private boolean isEnd() {
            return index >= input.length();
        }

        private char peek() {
            return input.charAt(index);
        }
    }

    private static Node simplify(List<Node> nodes) {
        List<Node> normalized = new ArrayList<>();
        for (Node node : nodes) {
            if (node instanceof EmptyNode) {
                continue;
            }
            if (node instanceof SequenceNode sequenceNode) {
                normalized.addAll(sequenceNode.children());
                continue;
            }
            normalized.add(node);
        }

        if (normalized.isEmpty()) {
            return new EmptyNode();
        }
        if (normalized.size() == 1) {
            return normalized.get(0);
        }
        return new SequenceNode(List.copyOf(normalized));
    }

    private static Node applyCombiningAccent(Node node, String accent) {
        String plainText = flattenPlainText(node);
        if (plainText.isEmpty()) {
            return node;
        }
        return new TextNode(plainText + accent);
    }

    private static String flattenPlainText(Node node) {
        if (node == null || node instanceof EmptyNode) {
            return "";
        }
        if (node instanceof TextNode textNode) {
            return textNode.text();
        }
        if (node instanceof DelimitedNode delimitedNode) {
            return delimitedNode.leftDelimiter() + flattenPlainText(delimitedNode.content()) + delimitedNode.rightDelimiter();
        }
        if (node instanceof SequenceNode sequenceNode) {
            StringBuilder builder = new StringBuilder();
            for (Node child : sequenceNode.children()) {
                String childText = flattenPlainText(child);
                if (childText == null) {
                    return "";
                }
                builder.append(childText);
            }
            return builder.toString();
        }
        return "";
    }

    private static Map<String, String> createCommandSymbols() {
        Map<String, String> symbols = new HashMap<>();
        symbols.put("alpha", "α");
        symbols.put("beta", "β");
        symbols.put("gamma", "γ");
        symbols.put("delta", "δ");
        symbols.put("epsilon", "ε");
        symbols.put("varepsilon", "ε");
        symbols.put("zeta", "ζ");
        symbols.put("eta", "η");
        symbols.put("theta", "θ");
        symbols.put("vartheta", "ϑ");
        symbols.put("iota", "ι");
        symbols.put("kappa", "κ");
        symbols.put("lambda", "λ");
        symbols.put("mu", "μ");
        symbols.put("nu", "ν");
        symbols.put("xi", "ξ");
        symbols.put("pi", "π");
        symbols.put("varpi", "ϖ");
        symbols.put("rho", "ρ");
        symbols.put("varrho", "ϱ");
        symbols.put("sigma", "σ");
        symbols.put("varsigma", "ς");
        symbols.put("tau", "τ");
        symbols.put("upsilon", "υ");
        symbols.put("phi", "φ");
        symbols.put("varphi", "ϕ");
        symbols.put("chi", "χ");
        symbols.put("psi", "ψ");
        symbols.put("omega", "ω");
        symbols.put("Gamma", "Γ");
        symbols.put("Delta", "Δ");
        symbols.put("Theta", "Θ");
        symbols.put("Lambda", "Λ");
        symbols.put("Xi", "Ξ");
        symbols.put("Pi", "Π");
        symbols.put("Sigma", "Σ");
        symbols.put("Upsilon", "Υ");
        symbols.put("Phi", "Φ");
        symbols.put("Psi", "Ψ");
        symbols.put("Omega", "Ω");
        symbols.put("pm", "±");
        symbols.put("mp", "∓");
        symbols.put("times", "×");
        symbols.put("div", "÷");
        symbols.put("cdot", "·");
        symbols.put("ast", "∗");
        symbols.put("star", "⋆");
        symbols.put("circ", "∘");
        symbols.put("bullet", "∙");
        symbols.put("leq", "≤");
        symbols.put("le", "≤");
        symbols.put("geq", "≥");
        symbols.put("ge", "≥");
        symbols.put("neq", "≠");
        symbols.put("ne", "≠");
        symbols.put("approx", "≈");
        symbols.put("equiv", "≡");
        symbols.put("sim", "∼");
        symbols.put("simeq", "≃");
        symbols.put("propto", "∝");
        symbols.put("infty", "∞");
        symbols.put("partial", "∂");
        symbols.put("nabla", "∇");
        symbols.put("forall", "∀");
        symbols.put("exists", "∃");
        symbols.put("in", "∈");
        symbols.put("notin", "∉");
        symbols.put("subset", "⊂");
        symbols.put("subseteq", "⊆");
        symbols.put("supset", "⊃");
        symbols.put("supseteq", "⊇");
        symbols.put("cup", "∪");
        symbols.put("cap", "∩");
        symbols.put("setminus", "∖");
        symbols.put("emptyset", "∅");
        symbols.put("to", "→");
        symbols.put("rightarrow", "→");
        symbols.put("leftarrow", "←");
        symbols.put("leftrightarrow", "↔");
        symbols.put("Rightarrow", "⇒");
        symbols.put("Leftarrow", "⇐");
        symbols.put("Leftrightarrow", "⇔");
        symbols.put("mapsto", "↦");
        symbols.put("longrightarrow", "⟶");
        symbols.put("Longrightarrow", "⟹");
        symbols.put("sum", "∑");
        symbols.put("prod", "∏");
        symbols.put("coprod", "∐");
        symbols.put("int", "∫");
        symbols.put("iint", "∬");
        symbols.put("iiint", "∭");
        symbols.put("oint", "∮");
        symbols.put("sin", "sin");
        symbols.put("cos", "cos");
        symbols.put("tan", "tan");
        symbols.put("cot", "cot");
        symbols.put("sec", "sec");
        symbols.put("csc", "csc");
        symbols.put("arcsin", "arcsin");
        symbols.put("arccos", "arccos");
        symbols.put("arctan", "arctan");
        symbols.put("ln", "ln");
        symbols.put("log", "log");
        symbols.put("lg", "lg");
        symbols.put("exp", "exp");
        symbols.put("lim", "lim");
        symbols.put("max", "max");
        symbols.put("min", "min");
        symbols.put("sup", "sup");
        symbols.put("inf", "inf");
        symbols.put("det", "det");
        symbols.put("Pr", "Pr");
        symbols.put("because", "∵");
        symbols.put("therefore", "∴");
        symbols.put("angle", "∠");
        symbols.put("triangle", "△");
        symbols.put("perp", "⊥");
        symbols.put("parallel", "∥");
        symbols.put("degree", "°");
        symbols.put("prime", "′");
        symbols.put("ldotp", ".");
        return symbols;
    }

    private static Map<String, String> createDelimiterSymbols() {
        Map<String, String> delimiters = new HashMap<>();
        delimiters.put("(", "(");
        delimiters.put(")", ")");
        delimiters.put("[", "[");
        delimiters.put("]", "]");
        delimiters.put("{", "{");
        delimiters.put("}", "}");
        delimiters.put("|", "|");
        delimiters.put("lbrace", "{");
        delimiters.put("rbrace", "}");
        delimiters.put("langle", "⟨");
        delimiters.put("rangle", "⟩");
        delimiters.put("lvert", "|");
        delimiters.put("rvert", "|");
        delimiters.put("lVert", "‖");
        delimiters.put("rVert", "‖");
        delimiters.put("lfloor", "⌊");
        delimiters.put("rfloor", "⌋");
        delimiters.put("lceil", "⌈");
        delimiters.put("rceil", "⌉");
        return delimiters;
    }
}
