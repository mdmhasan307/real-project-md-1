--NOOP
--tacticaldb uses a single stratis PDB called STRATISPDB:
-- CREATE DATABASE LINK dbl_stratesd CONNECT TO STRATIS IDENTIFIED BY webacct#X2013X# USING 'STRATISPDB';

--enterprisedb uses 6 stratis pdbs, for the 6 stratis sites.
-- CREATE DATABASE LINK dbl_stratesd CONNECT TO stratis IDENTIFIED BY "webacct#X2013X#" USING 'STRATESD';
-- CREATE DATABASE LINK dbl_stratcpen CONNECT TO stratis IDENTIFIED BY "webacct#X2013X#" USING 'STRATCPEN';
-- CREATE DATABASE LINK dbl_stratclnc CONNECT TO stratis IDENTIFIED BY "webacct#X2013X#" USING 'STRATCLNC';
-- CREATE DATABASE LINK dbl_stratbic CONNECT TO stratis IDENTIFIED BY "webacct#X2013X#" USING 'STRATBIC';
-- CREATE DATABASE LINK dbl_stratmcbh CONNECT TO stratis IDENTIFIED BY "webacct#X2013X#" USING 'STRATMCBH';
-- CREATE DATABASE LINK dbl_stratoki CONNECT TO stratis IDENTIFIED BY "webacct#X2013X#" USING 'STRATOKI';

--If this fails, the following grant will need added to docker setup scripts for the database:
-- grant create database link to system;
--FUTURE: This is a hard coded DB Link for the TACTICAL design of stratis, based off of the .210 edition of db/pdb's.