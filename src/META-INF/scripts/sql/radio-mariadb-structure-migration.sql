-- MariaDB data migration script for the radio database
-- run this after structure definition
-- best import using MariaDB client command "source <path to this file>"

SET CHARACTER SET utf8;
USE radio;
-- ALTER TABLE
-- if not exist

ALTER TABLE Person ADD COLUMN IF NOT EXISTS lastTransmissionTimestamp BIGINT NULL;
ALTER TABLE Person ADD COLUMN IF NOT EXISTS lastTransmissionAddress VARCHAR(63) NULL;
ALTER TABLE Person ADD COLUMN IF NOT EXISTS lastTransmissionOffer VARCHAR(4096) NULL;
ALTER TABLE Person ADD COLUMN IF NOT EXISTS lastTransmissionAnswer VARCHAR(4096) NULL;

