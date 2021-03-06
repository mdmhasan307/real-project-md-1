-- As of 04/29/2021 the ACTIVEMQ persistence functionality has been migrated to the MIPS suite of applications.
-- The ACTIVEMQ user is no longer required directly in the client applications.

-- NOTE: This file does not actually create the STRATIS Common ACTIVEMQ user, but instead checks for the existence of the user.
--       Filename preserved for compatibility with servers this has already executed on!
--
-- -- Ensure that the ACTIVEMQ Oracle user/schemas exists in the STRATIS Common database:
-- declare
--     userexists integer;
-- begin
--     select count(*) into userexists from dba_users where username = 'ACTIVEMQ';
--     if userexists = 0 then
--         raise_application_error(-20001,
--             'User ACTIVEMQ does not exist and must be manually created using the /manual/users/ACTIVEMQ_USER.sql script');
--     end if;
-- exception
--     when others
--         then raise;
-- end;
--
-- /
