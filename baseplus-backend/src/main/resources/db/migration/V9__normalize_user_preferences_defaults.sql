update user_preferences
set tema = case upper(trim(tema))
    when 'LIGHT' then 'LIGHT'
    when 'DARK' then 'DARK'
    when 'APP_DEFAULT' then 'APP_DEFAULT'
    else 'APP_DEFAULT'
end;

update user_preferences
set preferencia_visual = case upper(trim(preferencia_visual))
    when 'REGULAR' then 'REGULAR'
    when 'COMPACT' then 'COMPACT'
    when 'APP_DEFAULT' then 'APP_DEFAULT'
    else 'APP_DEFAULT'
end;
