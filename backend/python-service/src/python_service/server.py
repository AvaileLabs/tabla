from typing import Annotated

from edgar import Company
from fastapi import FastAPI, HTTPException, Query

app = FastAPI()

Ticker = Annotated[
    str,
    Query(
        min_length=1,
        max_length=14,
        pattern=r"^[A-Za-z][A-Za-z0-9-]*$",
    ),
]


@app.get("/api/edgartools/quarterly-financials")
def get_quarterly_financials(ticker: Ticker):
    ticker = ticker.upper()
    company = Company(ticker)

    filing = company.get_filings(form="10-Q").latest()

    if filing is None:
        raise HTTPException(
            status_code=404, detail=f"No 10-Q filing found for {ticker}"
        )

    raise NotImplementedError("TODO")
