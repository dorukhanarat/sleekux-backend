package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class SortLinesTransformation extends AbstractTransformation {

    public SortLinesTransformation() {
        super(
                "sort_lines",
                "Sort Lines",
                List.of(
                        new ParameterDefinition(
                                "order",
                                "Order",
                                ParameterType.TEXT,
                                false,
                                "asc",
                                "asc (A to Z) or desc (Z to A)"
                        ),
                        new ParameterDefinition(
                                "caseSensitive",
                                "Case Sensitive",
                                ParameterType.BOOLEAN,
                                false,
                                true,
                                "Uppercase letters sort before lowercase"
                        ),
                        new ParameterDefinition(
                                "numeric",
                                "Numeric Sort",
                                ParameterType.BOOLEAN,
                                false,
                                false,
                                "Sort lines by their numeric value"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }

        boolean descending = getStringParam(parameters, "order", "asc").toLowerCase(Locale.ROOT).equals("desc");
        boolean caseSensitive = getBooleanParam(parameters, "caseSensitive", true);
        boolean numeric = getBooleanParam(parameters, "numeric", false);

        Comparator<String> textOrder = caseSensitive ? Comparator.naturalOrder() : String.CASE_INSENSITIVE_ORDER;
        if(descending) {
            textOrder = textOrder.reversed();
        }

        Comparator<String> comparator = numeric ? numericOrder(textOrder, descending) : textOrder;

        return Arrays.stream(input.split("\\R", -1))
                .sorted(comparator)
                .collect(Collectors.joining("\n"));
    }

    // Numbers are compared by value. Lines that are not numbers always come after the numbers, sorted as text
    private static Comparator<String> numericOrder(Comparator<String> textOrder, boolean descending) {
        Comparator<BigDecimal> numberOrder = descending ? Comparator.reverseOrder() : Comparator.naturalOrder();
        return (a, b) -> {
            BigDecimal x = parseNumber(a);
            BigDecimal y = parseNumber(b);
            if(x != null && y != null) {
                return numberOrder.compare(x, y);
            }
            if(x != null) {
                return -1;
            }
            if(y != null) {
                return 1;
            }
            return textOrder.compare(a, b);
        };
    }

    private static BigDecimal parseNumber(String line) {
        try {
            return new BigDecimal(line.strip());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
