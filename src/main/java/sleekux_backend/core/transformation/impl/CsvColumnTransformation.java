package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CsvColumnTransformation extends AbstractTransformation {

    public CsvColumnTransformation() {
        super(
                "csv_column",
                "CSV Column",
                List.of(
                        new ParameterDefinition(
                                "column",
                                "Column Number",
                                ParameterType.NUMBER,
                                true,
                                1,
                                "1 is the first column, -1 is the last"
                        ),
                        new ParameterDefinition(
                                "delimiter",
                                "Delimiter",
                                ParameterType.TEXT,
                                false,
                                ",",
                                "Character between columns, e.g. ; or tab"
                        ),
                        new ParameterDefinition(
                                "skipHeader",
                                "Skip Header Row",
                                ParameterType.BOOLEAN,
                                false,
                                false,
                                "Leave out the first row"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null || input.isEmpty()) {
            return "";
        }

        int column = getIntParam(parameters, "column", 1);
        char delimiter = parseDelimiter(getStringParam(parameters, "delimiter", ","));
        boolean skipHeader = getBooleanParam(parameters, "skipHeader", false);

        List<List<String>> rows = parse(input, delimiter);
        if(skipHeader && !rows.isEmpty()) {
            rows = rows.subList(1, rows.size());
        }

        List<String> values = new ArrayList<>(rows.size());
        for(List<String> row : rows) {
            values.add(cell(row, column));
        }
        return String.join("\n", values);
    }

    // "tab" and "\t" both mean a tab, since a real tab is hard to type into a form field
    private static char parseDelimiter(String delimiter) {
        if(delimiter.isEmpty()) {
            return ',';
        }
        if(delimiter.equalsIgnoreCase("tab") || delimiter.equals("\\t")) {
            return '\t';
        }
        return delimiter.charAt(0);
    }

    // Column 0 is treated as 1. A column that a row does not have gives an empty value
    private static String cell(List<String> row, int column) {
        int index = column > 0 ? column - 1 : column == 0 ? 0 : row.size() + column;
        return index >= 0 && index < row.size() ? row.get(index) : "";
    }

    // Follows the usual CSV rules (RFC 4180): a field in double quotes may contain the delimiter,
    // line breaks and doubled quotes (""), which stand for one quote. Completely empty lines are skipped
    static List<List<String>> parse(String input, char delimiter) {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        boolean rowHasContent = false;

        int i = 0;
        while(i < input.length()) {
            char c = input.charAt(i);

            if(inQuotes) {
                if(c == '"' && i + 1 < input.length() && input.charAt(i + 1) == '"') {
                    field.append('"');
                    i += 2;
                    continue;
                }
                if(c == '"') {
                    inQuotes = false;
                } else {
                    field.append(c);
                }
                i++;
                continue;
            }

            if(c == '"' && field.isEmpty()) {
                inQuotes = true;
                rowHasContent = true;
            } else if(c == delimiter) {
                row.add(field.toString());
                field.setLength(0);
                rowHasContent = true;
            } else if(c == '\n' || c == '\r') {
                if(rowHasContent || !field.isEmpty()) {
                    row.add(field.toString());
                    rows.add(row);
                }
                row = new ArrayList<>();
                field.setLength(0);
                rowHasContent = false;
                if(c == '\r' && i + 1 < input.length() && input.charAt(i + 1) == '\n') {
                    i++;
                }
            } else {
                field.append(c);
            }
            i++;
        }

        if(rowHasContent || !field.isEmpty()) {
            row.add(field.toString());
            rows.add(row);
        }
        return rows;
    }
}
