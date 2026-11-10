alter table security_listing
drop constraint security_listing_pkey,
    drop constraint security_listing_id_uuidv7,
    drop column id;

alter table security_listing
    add constraint security_listing_pkey primary key (nasdaq_symbol);
