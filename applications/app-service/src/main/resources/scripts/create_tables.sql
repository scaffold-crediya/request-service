-- Database creation (if it doesn't exist)
CREATE DATABASE crediya_requests; -- Keeping the database name as is, assuming it's a brand name

-- Connect to the newly created database
use crediya_requests;

-- -----------------------------------------------------
-- Table `loan_type`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS loan_type (
  id_loan_type UUID NOT NULL PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE,
  minimum_amount NUMERIC(15, 2) NOT NULL,
  maximum_amount NUMERIC(15, 2) NOT NULL,
  interest_rate NUMERIC(5, 2) NOT NULL,
  automatic_validation BOOLEAN NOT NULL DEFAULT FALSE
);

-- -----------------------------------------------------
-- Table `statuses`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS statuses (
  id_status UUID NOT NULL PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE,
  description VARCHAR(255)
);

-- -----------------------------------------------------
-- Table `application`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS application (
  id_application UUID NOT NULL PRIMARY KEY,
  amount NUMERIC(15, 2) NOT NULL,
  term INT NOT NULL,
  email VARCHAR(255) NOT NULL,
  creation_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  id_status UUID NOT NULL,
  id_loan_type UUID NOT NULL,
  identity_document varchar(20) NOT NULL,
  CONSTRAINT fk_application_statuses
    FOREIGN KEY (id_status)
    REFERENCES statuses (id_status)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_application_loan_type
    FOREIGN KEY (id_loan_type)
    REFERENCES loan_type (id_loan_type)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);
CREATE UNIQUE INDEX application_identity_document_idx ON public.application USING btree (identity_document, id_loan_type);
CREATE UNIQUE INDEX application_email_idx ON public.application USING btree (email, id_loan_type);
