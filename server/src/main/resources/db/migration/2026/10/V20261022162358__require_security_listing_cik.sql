alter table security_listing
    alter column cik set not null,
    add constraint security_listing_cik_positive check (cik > 0);
