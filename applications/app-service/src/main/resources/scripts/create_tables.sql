CREATE TABLE statuses (
	id serial4 NOT NULL,
	"name" varchar(255) NOT NULL,
	description varchar(255) NULL,
	CONSTRAINT status_name_key UNIQUE (name),
	CONSTRAINT status_pkey PRIMARY KEY (id_status)
);

CREATE TABLE loan_type (
	id serial4 NOT NULL,
	"name" varchar(255) NOT NULL,
	minimum_amount numeric(15, 2) NOT NULL,
	maximum_amount numeric(15, 2) NOT NULL,
	interest_rate numeric(5, 2) NOT NULL,
	automatic_validation bool DEFAULT false NOT NULL,
	CONSTRAINT loan_type_name_key UNIQUE (name),
	CONSTRAINT loan_type_pkey PRIMARY KEY (id_loan_type)
);

CREATE TABLE IF NOT EXISTS application (
  id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
  amount NUMERIC(15, 2) NOT NULL,
  term INT NOT NULL,
  email VARCHAR(255) NOT NULL,
  creation_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  id_status INT NOT NULL,
  id_loan_type INT NOT NULL,
  identity_document VARCHAR(20) ,
  CONSTRAINT fk_application_status
    FOREIGN KEY (id_status)
    REFERENCES statuses (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_application_loan_type
    FOREIGN KEY (id_loan_type)
    REFERENCES loan_type (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);
CREATE UNIQUE INDEX application_email_idx ON application (email, id_loan_type);

