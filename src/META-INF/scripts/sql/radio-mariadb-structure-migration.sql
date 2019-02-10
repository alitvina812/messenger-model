-- MariaDB data migration script for the radio database
-- run this after structure definition
-- best import using MariaDB client command "source <path to this file>"

SET CHARACTER SET utf8;
USE radio;
-- ALTER TABLE
-- if not exist
-- TODO rename last transmission to negotiation
alter table Person drop column lastTransmissionAddress;
alter table Person drop column lastTransmissionOffer;
alter table Person drop column lastTransmissionAnswer;
alter table Person drop column lastTransmissionTimestamp;
ALTER TABLE Person ADD COLUMN IF NOT EXISTS negotiationTimestamp BIGINT NULL;
ALTER TABLE Person ADD COLUMN IF NOT EXISTS negotiationOffer VARCHAR(2046) NULL;
ALTER TABLE Person ADD COLUMN IF NOT EXISTS negotiationAnswer VARCHAR(2046) NULL;

