-- Align first/last name length with the registration form (2..50 characters).
alter table custom_user alter column first_name type varchar(50);
alter table custom_user alter column last_name  type varchar(50);
