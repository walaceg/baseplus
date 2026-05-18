update branding_settings
set logo_url = case
    when logo_url like '/branding/logo/%' then '/uploads' || logo_url
    when logo_url = '/branding/logo' then null
    when logo_url = '/branding/logo/' then null
    else logo_url
end,
    favicon_url = case
        when favicon_url like '/branding/favicon/%' then '/uploads' || favicon_url
        when favicon_url = '/branding/favicon' then null
        when favicon_url = '/branding/favicon/' then null
        else favicon_url
    end,
    login_background_url = case
        when login_background_url like '/branding/login-background/%' then '/uploads' || login_background_url
        when login_background_url = '/branding/login-background' then null
        when login_background_url = '/branding/login-background/' then null
        else login_background_url
    end;
