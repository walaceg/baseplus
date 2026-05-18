alter table branding_settings
    add column if not exists login_logo_url varchar(255);

alter table branding_settings
    add column if not exists white_label_enabled boolean not null default false;

alter table branding_settings
    add column if not exists white_label_name varchar(120);

alter table branding_settings
    add column if not exists white_label_subtitle varchar(255);

update branding_settings
set login_logo_url = case
    when login_logo_url like '/branding/login-logo/%' then '/uploads' || login_logo_url
    when login_logo_url = '/branding/login-logo' then null
    when login_logo_url = '/branding/login-logo/' then null
    else login_logo_url
end;
