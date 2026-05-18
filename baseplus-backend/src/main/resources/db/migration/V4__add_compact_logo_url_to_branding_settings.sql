alter table branding_settings
    add column if not exists compact_logo_url varchar(255);
