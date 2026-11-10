drop table nasdaq_traded_security_directory_entry;
drop table nasdaq_traded_security_directory;

create table security_listing
(
    id            uuid primary key,
    cik           integer,
    symbol        text not null,
    nasdaq_symbol text not null,
    cqs_symbol    text,
    exchange      text not null,

    constraint security_listing_id_uuidv7
        check (uuid_extract_version(id) is not distinct from 7)
);