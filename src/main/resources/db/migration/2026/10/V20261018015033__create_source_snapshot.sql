create table source_snapshot
(
    id           uuid primary key,
    source_uri   text                     not null,
    retrieved_at timestamp with time zone not null,
    content      bytea                    not null,

    constraint source_snapshot_id_uuidv7
        check (uuid_extract_version(id) is not distinct from 7),

    constraint source_snapshot_source_uri_not_blank
        check (btrim(source_uri) <> ''),

    constraint source_snapshot_content_not_empty
        check (octet_length(content) > 0)
);
