package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class StripHtmlTransformation extends AbstractTransformation {

    // Scripts, styles and the page head are not shown by a browser, so they are removed together with their contents
    private static final Pattern HIDDEN_ELEMENTS = Pattern.compile("<(script|style|head|title)\\b[^>]*>.*?</\\1\\s*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern COMMENT = Pattern.compile("<!--.*?-->", Pattern.DOTALL);
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    // Paragraph-like blocks are separated by a blank line, list items and line breaks by a single line break
    private static final Pattern PARAGRAPH_END = Pattern.compile("</(p|h[1-6]|blockquote|pre|table|ul|ol)\\s*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern LINE_END = Pattern.compile("<br\\s*/?>|</(li|tr|div)\\s*>", Pattern.CASE_INSENSITIVE);
    // Only real tags are removed, so text like "5 < 6 > 3" is left alone
    private static final Pattern TAG = Pattern.compile("</?[a-zA-Z][^>]*>|<![a-zA-Z][^>]*>");
    private static final Pattern REPEATED_SPACES = Pattern.compile(" {2,}");
    private static final Pattern SPACES_AROUND_LINE_BREAK = Pattern.compile(" *\\n *");
    private static final Pattern EXTRA_BLANK_LINES = Pattern.compile("\\n{3,}");

    public StripHtmlTransformation() {
        super(
                "strip_html",
                "Strip HTML",
                List.of(
                        new ParameterDefinition(
                                "decodeEntities",
                                "Decode Entities",
                                ParameterType.BOOLEAN,
                                false,
                                true,
                                "Turn &amp; and &lt; back into & and <"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }

        boolean decodeEntities = getBooleanParam(parameters, "decodeEntities", true);

        String text = HIDDEN_ELEMENTS.matcher(input).replaceAll("");
        text = COMMENT.matcher(text).replaceAll("");
        // Like a browser, line breaks and indentation in the HTML source count as a single space
        text = WHITESPACE.matcher(text).replaceAll(" ");
        text = PARAGRAPH_END.matcher(text).replaceAll("\n\n");
        text = LINE_END.matcher(text).replaceAll("\n");
        text = TAG.matcher(text).replaceAll("");
        text = REPEATED_SPACES.matcher(text).replaceAll(" ");

        if(decodeEntities) {
            text = HtmlEscapeTransformation.unescape(text);
        }

        text = SPACES_AROUND_LINE_BREAK.matcher(text).replaceAll("\n");
        text = EXTRA_BLANK_LINES.matcher(text).replaceAll("\n\n");
        return text.strip();
    }
}
