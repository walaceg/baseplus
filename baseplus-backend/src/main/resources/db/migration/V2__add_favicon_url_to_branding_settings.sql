alter table branding_settings
    add column if not exists favicon_url varchar(255);
