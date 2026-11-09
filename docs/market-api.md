# Market query proof of concept

Tabla exposes its ingested market directory through MCP and REST. Both interfaces
use the same synchronous queries and return the same result fields. This is a local
proof of concept; authentication is not configured.

## Running

Start the saved `TablaApplication` configuration in IntelliJ IDEA, or run
`./gradlew bootRun`. The application requires `EDGAR_USER_AGENT` to identify the SEC
data client (for example, `Tabla Your Name contact@example.com`). The saved IDE
configuration already supplies it. Startup ingests the source data; allow ingestion
to finish before trying the examples.

The default server address is `http://localhost:8080`.

## MCP

Connect an MCP client supporting **MCP 2026-07-28** to `http://localhost:8080/mcp`.
Tabla uses Spring AI's `MODERN` protocol: stateless HTTP POST requests, with
`server/discover` instead of initialization and sessions. The client sends the
protocol version and capabilities in each request's metadata.

Only the modern protocol is supported. Tool definitions include generated output
schemas, and calls return structured content. Resources, prompts, completions,
and subscriptions are disabled because this proof of concept implements only tools.
See the [Spring AI modern-server documentation](https://docs.spring.io/spring-ai/reference/2.1/api/mcp/mcp-modern-server-boot-starter-docs.html).

Tool arguments are:

| Tool | Optional arguments | Searches |
| --- | --- | --- |
| `search_listings` | `query`, `limit`, `etf` | Nasdaq directory symbol, Nasdaq symbol, or security name |
| `search_fund_share_classes` | `query`, `limit` | SEC fund ticker, series ID, or class ID |

Example tool calls:

```text
search_listings({"query": "aapl", "limit": 5, "etf": false})
search_listings({"query": "vanguard", "limit": 10, "etf": true})
search_fund_share_classes({"query": "vfiax", "limit": 5})
```

Successful MCP calls expose the result array in `result.structuredContent`.
`tools/list` exposes each tool's `outputSchema`, including field types and nullability.

For a direct HTTP smoke test, modern requests require routing headers that agree
with the JSON body. This example calls the tool without creating a session:

```sh
curl 'http://localhost:8080/mcp' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H 'Mcp-Method: tools/call' \
  -H 'Mcp-Name: search_listings' \
  -H 'MCP-Protocol-Version: 2026-07-28' \
  --data '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "tools/call",
    "params": {
      "name": "search_listings",
      "arguments": {"query": "aapl", "limit": 5, "etf": false},
      "_meta": {
        "io.modelcontextprotocol/protocolVersion": "2026-07-28",
        "io.modelcontextprotocol/clientCapabilities": {}
      }
    }
  }'
```

## REST

These endpoints return JSON arrays. They are convenient for agents with an HTTP
tool and for testing with `curl`; an MCP client is not required.

```sh
curl --get 'http://localhost:8080/api/v1/market/listings' \
  --data-urlencode 'query=aapl' --data-urlencode 'limit=5' --data-urlencode 'etf=false'

curl --get 'http://localhost:8080/api/v1/market/listings' \
  --data-urlencode 'query=vanguard' --data-urlencode 'limit=10' --data-urlencode 'etf=true'

curl --get 'http://localhost:8080/api/v1/market/fund-share-classes' \
  --data-urlencode 'query=vfiax' --data-urlencode 'limit=5'
```

Listings contain `nasdaqSymbol`, `symbol`, `name`, `exchange`, and `etf`.
Fund share classes contain `classId`, `seriesId`, `cik`, and nullable `symbol`.

## Search behavior and coverage

- `query` is a case-insensitive literal substring, with surrounding whitespace
  trimmed. Omitted or blank text browses the directory. Maximum length: 200 characters.
- `limit` defaults to 20 and accepts 1–100. Results are ordered by Nasdaq symbol
  or fund class ID, respectively. There is no pagination or total count.
- `etf=true` selects ETFs; `etf=false` selects non-ETF listings, including stocks
  and other securities. Omitting it returns both.
- SEC fund share classes include mutual funds and ETFs. This source supplies no
  fund names and does not distinguish those fund types.
- Searches with no matches return an empty array. Invalid limits or oversized queries
  produce an MCP tool error or HTTP 400 with a problem detail response.
- Data reflects the latest successful ingestion. These APIs supply directory
  metadata, not prices. Regex search is deferred.

## Structure and versions

`market.internal.MarketQueries` owns the Jimmer queries and small result models.
`market.internal.mcp.MarketTools` and `market.internal.web.MarketController` adapt
them to MCP and HTTP. All three remain internal to the existing `market` module;
no new application module or cross-module API is needed for this proof of concept.
The existing Modulith verification test checks those boundaries.

The implementation was verified with the project's existing Spring Boot
`4.2.0-RC2`, Spring AI `2.1.0-RC2`, and Spring Modulith `2.2.0-RC1` dependencies.
No dependency upgrade or database migration is required.
