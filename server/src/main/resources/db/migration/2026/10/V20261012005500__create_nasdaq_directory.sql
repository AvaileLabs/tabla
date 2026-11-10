create table nasdaq_directory
(
    id              integer primary key,
    source_uri      text                     not null,
    retrieved_at    timestamp with time zone not null,
    file_created_at timestamp                not null,

    constraint nasdaq_directory_singleton
        check (id = 1),

    constraint nasdaq_directory_source_uri_not_blank
        check (btrim(source_uri) <> '')
);

create table nasdaq_directory_entry
(
    id               uuid primary key,
    directory_id     integer not null references nasdaq_directory (id) on delete cascade,
    nasdaq_traded     text    not null,
    symbol           text    not null,
    security_name    text    not null,
    listing_exchange char(1) not null,
    market_category  char(1),
    etf              char(1) not null,
    round_lot_size   integer not null,
    test_issue       char(1) not null,
    financial_status char(1),
    cqs_symbol       text,
    nasdaq_symbol    text    not null,
    next_shares      char(1) not null,

    constraint nasdaq_directory_entry_symbol_unique
        unique (directory_id, symbol),

    constraint nasdaq_directory_entry_id_uuidv7
        check (uuid_extract_version(id) is not distinct from 7),

    constraint nasdaq_directory_entry_symbol_not_blank
        check (btrim(symbol) <> ''),

    constraint nasdaq_directory_entry_nasdaq_symbol_not_blank
        check (btrim(nasdaq_symbol) <> ''),

    constraint nasdaq_directory_entry_cqs_symbol_not_blank
        check (cqs_symbol is null or btrim(cqs_symbol) <> '')
);
