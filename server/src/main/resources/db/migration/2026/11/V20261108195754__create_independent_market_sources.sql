create table nasdaq_listing
(
    nasdaq_symbol     text primary key,
    symbol            text                     not null,
    cqs_symbol        text,
    name              text                     not null,
    exchange          text                     not null,
    nasdaq_traded     boolean                  not null,
    etf               boolean                  not null,
    round_lot_size    integer                  not null check (round_lot_size > 0),
    market_category   char(1),
    financial_status  char(1),
    next_shares       boolean                  not null,
    source_uri        text                     not null,
    retrieved_at      timestamp with time zone not null,
    source_created_at timestamp without time zone
);

create table company_ticker
(
    cik          integer                  not null check (cik > 0),
    symbol       text                     not null,
    name         text                     not null,
    exchange     text,
    source_uri   text                     not null,
    retrieved_at timestamp with time zone not null,
    primary key (cik, symbol)
);

create table fund_share_class
(
    class_id     text primary key,
    cik          integer                  not null check (cik > 0),
    series_id    text                     not null,
    symbol       text,
    source_uri   text                     not null,
    retrieved_at timestamp with time zone not null
);
