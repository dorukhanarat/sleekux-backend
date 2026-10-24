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
        "placeholder": "Text to replace"
      },
      {
        "name": "newValue",
        "label": "Replacement String",
        "type": "text",
        "required": false,
        "defaultValue": "",
        "placeholder": "New Text"
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
