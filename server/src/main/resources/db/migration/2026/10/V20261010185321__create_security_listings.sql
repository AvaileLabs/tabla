create table security_listing
(
    id            uuid primary key,
    symbol        text not null,
    nasdaq_symbol text not null,
    cqs_symbol    text,

    constraint security_listing_id_uuidv7
        check (uuid_extract_version(id) is not distinct from 7),

    constraint security_listing_symbol_not_blank
        check (btrim(symbol) <> ''),

    constraint security_listing_nasdaq_symbol_not_blank
        check (btrim(nasdaq_symbol) <> ''),

    constraint security_listing_cqs_symbol_not_blank
        check (cqs_symbol is null or btrim(cqs_symbol) <> '')
);
