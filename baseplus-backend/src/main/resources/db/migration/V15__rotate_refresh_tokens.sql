alter table refresh_tokens
    add column consumed_at timestamp with time zone;

alter table refresh_tokens
    drop constraint refresh_tokens_session_id_key;

create unique index ux_refresh_tokens_active_session
    on refresh_tokens (session_id)
    where consumed_at is null;

create index ix_refresh_tokens_expires_at
    on refresh_tokens (expires_at);
