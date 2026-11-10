drop table security_listing;

alter table nasdaq_directory_entry
    drop column test_issue;

-- rename tables

alter table nasdaq_directory
    rename to nasdaq_traded_security_directory;

alter table nasdaq_directory_entry
    rename to nasdaq_traded_security_directory_entry;

-- rename constraints to match the new table names

alter table nasdaq_traded_security_directory
    rename constraint nasdaq_directory_pkey
        to nasdaq_traded_security_directory_pkey;

alter table nasdaq_traded_security_directory
    rename constraint nasdaq_directory_singleton
        to nasdaq_traded_security_directory_singleton;

alter table nasdaq_traded_security_directory
    rename constraint nasdaq_directory_source_uri_not_blank
        to nasdaq_traded_security_directory_source_uri_not_blank;

alter table nasdaq_traded_security_directory_entry
    rename constraint nasdaq_directory_entry_pkey
        to nasdaq_traded_security_directory_entry_pkey;

alter table nasdaq_traded_security_directory_entry
    rename constraint nasdaq_directory_entry_directory_id_fkey
        to nasdaq_traded_security_directory_entry_directory_id_fkey;

alter table nasdaq_traded_security_directory_entry
    rename constraint nasdaq_directory_entry_symbol_unique
        to nasdaq_traded_security_directory_entry_symbol_unique;

alter table nasdaq_traded_security_directory_entry
    rename constraint nasdaq_directory_entry_id_uuidv7
        to nasdaq_traded_security_directory_entry_id_uuidv7;

alter table nasdaq_traded_security_directory_entry
    rename constraint nasdaq_directory_entry_symbol_not_blank
        to nasdaq_traded_security_directory_entry_symbol_not_blank;

alter table nasdaq_traded_security_directory_entry
    rename constraint nasdaq_directory_entry_nasdaq_symbol_not_blank
        to nasdaq_traded_security_directory_entry_nasdaq_symbol_not_blank;

alter table nasdaq_traded_security_directory_entry
    rename constraint nasdaq_directory_entry_cqs_symbol_not_blank
        to nasdaq_traded_security_directory_entry_cqs_symbol_not_blank;
