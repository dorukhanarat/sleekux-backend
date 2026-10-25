# Thought Process

How the SleekUx backend was built, commit by commit, and the reasoning behind it.

## Core Idea: Transformations That Describe Themselves

Every transformation is a small class with a key, a display name, a list of parameter definitions (name, label, type, required, default, placeholder) and one `apply(input, parameters)` method. The registry, pipeline and API only use this interface, never a specific transformation. Growing from 8 to 41 transformations needed only new classes plus one line each in the registry. The controller, pipeline and error handling stayed unchanged.

The trade-off: a parameter can only be described as `text`, `number`, `boolean` or `regex`. A field like Trim's `mode` has three valid values, but the schema can't list them, so it is sent as free text, and unknown values quietly fall back to a default.

## Development Phases

**1. Setup** (`ce5209a`, `444e838`): Java 21 + Spring Boot + Maven from Spring Initializr, with only the web starter. The Maven wrapper is kept so building needs nothing but a JDK. A short README covered how to run it.

**2. Contract first** (`e9ef134`): the `Transformation` interface and `ParameterDefinition` were defined before any real transformation, with Replace as the first, simplest case to check the design. Parameters arrive from JSON as an untyped `Map<String, Object>`, so `AbstractTransformation` provides `getStringParam` / `getIntParam` / `getBooleanParam` helpers. These read numbers sent as `2` or `"2"` alike and fall back to the default for missing or invalid values, instead of each transformation repeating that logic.

**3. More transformations and a registry** (`b99e861`): Append, Prepend, Substring, Regex Match, Uppercase, Lowercase and Trim were added, plus a `TransformationRegistry` that looks transformations up by key, the same string a client sends. The implementations moved into `impl/`, away from the shared interface and registry. Writing their tests surfaced the first batch of bugs (see the table in phase 5).

**4. Pipeline with step history** (`baa924a`): `TransformationPipeline` runs steps in order, each step getting the previous output. It returns not only the final text but every step's input, output and duration, so a user can see which step in a chain misbehaved. `StepExecutionResult.equals` deliberately ignores the duration, which varies between runs and would make results impossible to compare.

**5. REST API, bug fixes and tests** (`0f23367`): `GET /api/transformations` returns the schema for the frontend, and `POST /api/transform` runs a pipeline. DTOs keep the JSON format separate from the core models, and `core/` has no dependency on Spring, so all logic is testable without a server. A global exception handler gives every error the same JSON shape. Testing this layer, and the earlier phases, found these bugs:

| Bug | Cause |
| --- | --- |
| Registry lookups failed for `"Replace"` | `register` lowercased keys, but `get` didn't |
| `"title"` became `"TİTLE"` on Turkish systems | `toUpperCase()` / `toLowerCase()` used the machine's language; now `Locale.ROOT` |
| Trim modes handled Unicode spaces differently | `both` used `trim()`, `start`/`end` used `strip…()` |
| `"matchAll": "yes"` returned only the first match | `Boolean.parseBoolean` turns anything unrecognized into `false` instead of the default |
| A pipeline result serialized as `{}` | `PipelineResult` had no getters |
| The frontend got an error listing transformations | Endpoint misspelled as `/api/tranformations` |
| Browsers blocked every request from the frontend | CORS mapping `*/api/**` matched no URL |
| Unknown URLs, malformed JSON and wrong methods all returned 500 | The catch-all `Exception` handler also caught Spring's own 404/400/405/415 errors |
| `"steps": [null]` returned 500 | `NullPointerException` in the controller |
| An invalid regex silently returned the input | `RegexMatchTransformation` caught `PatternSyntaxException` itself, so the user never saw why |

Controller tests use MockMvc with the real registry and pipeline, so they check the actual JSON, status codes and CORS headers. Every fix has a test that fails without it: putting the CORS bug back makes 3 controller tests fail.

**6. Documentation** (`4521571`): the README gained a description, configuration (port, CORS origins), both endpoints with request and response examples, the error format and status codes, a table of every transformation and its edge cases, the project structure and how to run tests. The examples were captured from real API responses, so they match what the server returns.

**7. Expanded transformation set** (`deb65a1`): twelve more transformations, including Regex Replace, Change Case, Sort Lines, Split & Join and Pad, each with tests. Two concerns shaped them:

1. Line-based transformations accept `\n`, `\r\n` and `\r`, so text pasted from any operating system works.
2. Pad (10,000 characters) and Repeat (1,000 copies) are capped, so one request can't exhaust server memory.

Placeholders were also rewritten as hints for end users with examples (e.g. "e.g. \d+ finds every number"), since the frontend shows them directly. This also fixed two typos visible in the UI.

**8. Alphabetical order** (`692f9a5`): with 20 transformations, the order they were written in was hard to scan, so the registry now lists them alphabetically.

**9. Encoding and cleanup tools** (not yet committed): eight transformations aimed at everyday developer and content tasks: Slugify, Remove Accents, Base64, URL Encode, Escape HTML, Format JSON, Truncate and Extract. Decisions behind them:

- **Remove Accents needs a lookup table.** Splitting a letter from its accent (Unicode NFD) handles `ç` or `é`, but not letters like Turkish `ı`, `ł`, `ø` or `ß`, which have no separate accent. Only marks from the Latin accent block are removed, so Korean or Hindi text is left unchanged. Slugify reuses this step, so `Çok Güzel` becomes `cok-guzel` instead of losing its letters.
- **Standards over library defaults.** Java's `URLEncoder` follows HTML form rules (space as `+`), so its output is adjusted to standard percent-encoding (`%20`). Base64 decoding ignores line breaks and missing `=` padding, because copied Base64 often has both.
- **Exact values in Format JSON.** Jackson is already part of the web starter, so no new dependency was needed. Numbers are read as `BigDecimal`, so `0.1` or `12345678901234567890` keep their exact value, and key order is preserved.
- **Readable errors.** Invalid input (bad JSON, Base64 or percent-encoding) returns `400` with an explanation, and the JSON error includes the line and column. While testing, Jackson's messages turned out to contain internal source details meaning nothing to a user, so they are removed.
- **Edge cases in Truncate and Extract.** Truncate counts emoji as one character so it never cuts one in half. Extract treats the dash in `2024-10-25` as a separator, not a minus sign, and keeps a closing `)` that belongs to a URL, such as a Wikipedia link, while dropping sentence punctuation after it.

Before writing test expectations, a small probe program ran each transformation on tricky input, so the tests document real behavior rather than assumptions.

**10. Line, text and data tools** (not yet committed): thirteen more transformations: Take Lines, Word Wrap, Count, Hash, Mask, Normalize Unicode, Swap Case, Shuffle Lines, Strip HTML, Remove Characters, Escape JSON String, Decode JWT and CSV Column. Three existing ones were also extended: Replace can ignore case, Substring accepts a negative start (`-3` keeps the last 3 characters), and Trim can trim chosen characters such as `/` instead of whitespace. All three extensions are optional parameters whose defaults keep the old behavior, so existing requests are unaffected.

- **Reuse instead of copying.** A shared `splitLines` helper handles all line endings and ignores a final line break. Strip HTML reuses the entity decoding from Escape HTML, and Decode JWT reuses the JSON formatting from Format JSON.
- **Strip HTML behaves like a browser.** Whitespace in the HTML source collapses to one space, paragraphs become a blank line, and `<br>` or list items a line break. Tests found two problems in the first version: text from `<head>` (such as the page title) leaked into the output, and removed tags left double spaces. Both are fixed.
- **Proper CSV parsing.** CSV Column follows the usual CSV rules: quoted fields may contain the delimiter, line breaks and `""` for a quote. A simple split on commas would break on these. Rows without the requested column give an empty line, so the output stays aligned with the input.
- **Repeatable randomness.** Shuffle Lines takes an optional seed, so the same seed always gives the same order and the transformation can be tested.
- **Safe masking.** Mask hides only letters and digits, so `**** **** **** 1111` keeps the shape of a card number.
- **Clear limits.** Decode JWT does not check the token's signature. The placeholder and README say so, so nobody treats a decoded token as verified.

With 41 transformations, controller tests that checked transformations by position (`$[17]`) broke on every addition. They now select by key, so adding a transformation no longer touches them. The suite grew to 612 tests, all passing.
