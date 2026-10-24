# SleekUx Backend

A REST API for transforming text. You send a piece of text and a list of steps (for example *trim → replace → uppercase*), and the API runs them in order and returns the final result together with the output of every step.

Built with **Java 21**, **Spring Boot**, and **Maven**.

## Prerequisites

- **Java**: `JDK 21` or newer
- **Maven**: not required. The included Maven wrapper (`mvnw` / `mvnw.cmd`) downloads the right version automatically.

## Setup & Running Locally

From the `sleekux-backend` folder:

```
# Windows
.\mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

- API URL: http://localhost:8080

### Configuration

| Setting | Default | Where to change it |
|---|---|---|
| Server port | `8080` | Add `server.port=<port>` to `src/main/resources/application.properties` |
| Allowed CORS origins | `http://localhost:5173`, `http://localhost:3000` | `corsConfigurer()` in `web/config/CoreConfig.java` |

If your frontend runs on a different port, add its origin to the CORS list. Otherwise the browser will block every request.

## API

All endpoints are under `/api`.

### `GET /api/transformations`

Lists every available transformation and its parameters. A frontend can use this to build its forms.

```json
[
  {
    "key": "replace",
    "displayName": "Replace Text",
    "parameters": [
      {
        "name": "oldValue",
        "label": "Target String",
        "type": "text",
        "required": true,
        "defaultValue": "",
        "placeholder": "Exact text to find, e.g. world"
      },
      {
        "name": "newValue",
        "label": "Replacement String",
        "type": "text",
        "required": false,
        "defaultValue": "",
        "placeholder": "Text to put in its place, leave empty to delete"
      }
    ]
  },
  "..."
]
```

Parameter `type` is one of `text`, `number`, `boolean` or `regex`.

### `POST /api/transform`

Runs a list of steps on a text. Each step receives the output of the previous one.

Request:

```json
{
  "text": "  hello world  ",
  "steps": [
    { "transformationKey": "trim" },
    { "transformationKey": "replace", "parameters": { "oldValue": "world", "newValue": "there" } },
    { "transformationKey": "uppercase" }
  ]
}
```

- `text`: optional. A missing value is treated as `""`.
- `steps`: optional. Without steps the text is returned unchanged.
- `transformationKey`: required for each step. Matching ignores upper/lower case.
- `parameters`: optional. Missing parameters use their default value.

Response (`200 OK`):

```json
{
  "originalText": "  hello world  ",
  "finalResult": "HELLO THERE",
  "history": [
    {
      "stepIndex": 1,
      "transformationKey": "trim",
      "transformationName": "Trim Whitespace",
      "parameters": {},
      "inputSnapshot": "  hello world  ",
      "outputSnapshot": "hello world",
      "executionTime": 0
    },
    {
      "stepIndex": 2,
      "transformationKey": "replace",
      "transformationName": "Replace Text",
      "parameters": { "oldValue": "world", "newValue": "there" },
      "inputSnapshot": "hello world",
      "outputSnapshot": "hello there",
      "executionTime": 0
    },
    {
      "stepIndex": 3,
      "transformationKey": "uppercase",
      "transformationName": "To Uppercase",
      "parameters": {},
      "inputSnapshot": "hello there",
      "outputSnapshot": "HELLO THERE",
      "executionTime": 0
    }
  ]
}
```

`executionTime` is in milliseconds.

### Errors

Errors return a JSON body in this format:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Unknown transformation key: reverse",
  "path": "/api/transform",
  "timestamp": "2026-10-24T13:05:52.782683100Z"
}
```

| Status | When |
|---|---|
| `400 Bad Request` | Unknown or blank `transformationKey`, a `null` step, an invalid regex pattern, or a body that is missing or not valid JSON |
| `404 Not Found` | The URL does not exist |
| `405 Method Not Allowed` | Wrong HTTP method, e.g. `GET /api/transform` |
| `415 Unsupported Media Type` | The body is not sent as `application/json` |
| `500 Internal Server Error` | Unexpected server error. The message is always generic, so no internal details are exposed |

## Transformations

| Key | Name | Parameters | Notes |
|---|---|---|---|
| `replace` | Replace Text | `oldValue` (text, required), `newValue` (text, default `""`) | Replaces every occurrence. Case-sensitive, and special characters are matched literally (not as regex). An empty `newValue` removes the matches. An empty `oldValue` leaves the text unchanged. |
| `append` | Append Text | `suffix` (text, required) | Adds the suffix to the end. |
| `prepend` | Prepend Text | `prefix` (text, required) | Adds the prefix to the start. |
| `substring` | Substring | `startIndex` (number, default `0`), `endIndex` (number, default `-1`) | Start is inclusive, end is exclusive, and indexes start at 0. `-1` means the end of the text. Out-of-range indexes are clamped to the text length. Returns `""` if start is not before end. |
| `regex_match` | Regex Match | `pattern` (regex, required), `matchAll` (boolean, default `true`), `delimiter` (text, default `", "`) | Returns the matches joined by the delimiter, or only the first match if `matchAll` is `false`. Returns `""` if nothing matches. An invalid pattern returns `400`. |
| `uppercase` | To Uppercase | – | |
| `lowercase` | To Lowercase | – | |
| `trim` | Trim Whitespace | `mode` (text, default `both`) | `start`, `end` or `both` (ignores upper/lower case). Unknown values trim both ends. |
| `regex_replace` | Regex Replace | `pattern` (regex, required), `replacement` (text, default `""`), `caseInsensitive` (boolean, default `false`) | Replaces every match. Use `$1`, `$2`, … in the replacement to insert groups. An empty `pattern` leaves the text unchanged. An invalid pattern or a reference to a missing group returns `400`. |
| `case_convert` | Change Case | `style` (text, default `title`) | `title`, `sentence`, `camel`, `pascal`, `snake`, `kebab` or `constant`. Words are split on spaces, `_`, `-` and camelCase humps (`parseHTMLText` → `parse_html_text`). `title` and `sentence` keep the original spacing. Unknown values use `title`. |
| `reverse` | Reverse Text | `mode` (text, default `chars`) | `chars`, `words` or `lines`. Emoji stay intact. `words` joins the words with single spaces. Unknown values reverse characters. |
| `pad` | Pad Text | `length` (number, required), `padChar` (text, default `" "`), `side` (text, default `start`) | Pads to at least `length` characters (max 10000). Only the first character of `padChar` is used. `side` is `start`, `end` or `both`; with `both` the extra character goes to the end. Longer text is left unchanged. |
| `repeat` | Repeat Text | `count` (number, default `2`), `separator` (text, default `""`) | `count` is limited to 0–1000. A count of `0` returns `""`. |
| `normalize_whitespace` | Normalize Whitespace | `mode` (text, default `collapse`) | `collapse` turns whitespace runs into one space and trims the ends, `all` removes every whitespace character, `blank_lines` removes empty lines. Unknown values collapse. |
| `sort_lines` | Sort Lines | `order` (text, default `asc`), `caseSensitive` (boolean, default `true`), `numeric` (boolean, default `false`) | `order` is `asc` or `desc`. With `numeric`, lines are compared by value and lines that are not numbers come after the numbers. |
| `dedupe_lines` | Remove Duplicate Lines | `caseSensitive` (boolean, default `true`), `trimLines` (boolean, default `false`) | Keeps the first occurrence of each line, in its original form and order. `trimLines` ignores leading/trailing whitespace when comparing. |
| `filter_lines` | Filter Lines | `pattern` (regex, required), `invert` (boolean, default `false`) | Keeps lines that contain a match, or removes them when `invert` is `true`. An empty `pattern` leaves the text unchanged. An invalid pattern returns `400`. |
| `number_lines` | Number Lines | `start` (number, default `1`), `separator` (text, default `". "`) | Every line is numbered, including empty ones. |
| `split_join` | Split & Join | `splitBy` (text, default `","`), `joinWith` (text, default newline), `trimItems` (boolean, default `true`) | Splits on `splitBy` (matched literally) and joins the items with `joinWith`, e.g. turns `a, b, c` into one item per line. Empty items are kept. An empty `splitBy` leaves the text unchanged. |
| `wrap_lines` | Wrap Each Line | `prefix` (text, default `""`), `suffix` (text, default `""`) | Adds the prefix and suffix to every line, e.g. `'` and `',` to quote values. |

Line-based transformations (`reverse` with `lines`, `normalize_whitespace` with `blank_lines`, `sort_lines`, `dedupe_lines`, `filter_lines`, `number_lines`, `wrap_lines`) accept `\n`, `\r\n` and `\r` line endings and always return `\n`.

Parameter values can be sent as JSON strings, numbers or booleans. For example, `"startIndex": 2` and `"startIndex": "2"` both work. A value that cannot be read (such as `"abc"` for a number) falls back to its default.

## Project Structure

```
src/main/java/sleekux_backend/
├── core/                         Transformation logic (no web dependencies)
│   ├── model/                    Parameter definitions, pipeline steps and results
│   └── transformation/
│       ├── impl/                 The individual transformations
│       ├── pipeline/             Runs a list of steps in order
│       ├── AbstractTransformation.java
│       └── TransformationRegistry.java
└── web/                          REST layer
    ├── config/                   Spring beans, CORS and error handling
    ├── controller/               REST endpoints
    └── dto/                      Request and response objects
```

To add a new transformation, create a class in `core/transformation/impl/` that extends `AbstractTransformation`, then add it to the default list in `TransformationRegistry`.

## Running Tests

Tests use **JUnit 5** and Spring's **MockMvc**. Run them from the `sleekux-backend` folder.
On macOS/Linux, use `./mvnw` instead of `.\mvnw.cmd`.

Run all tests:

```
.\mvnw.cmd test
```

Run all tests after deleting old build output (recommended after moving or renaming classes):

```
.\mvnw.cmd clean test
```

Run a single test class:

```
.\mvnw.cmd test "-Dtest=TransformationPipelineTest"
```

Run a single test method:

```
.\mvnw.cmd test "-Dtest=ReplaceTransformationTest#replacesAllOccurrences"
```

Run every test class matching a pattern:

```
.\mvnw.cmd test "-Dtest=*TransformationTest"
```

Test reports are written to `target/surefire-reports/`.
