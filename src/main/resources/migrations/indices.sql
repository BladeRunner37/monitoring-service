create index m_user_date_idx on measurement (user_login, date_saved);
create index c_mid_idx on consumption (measurement_id);