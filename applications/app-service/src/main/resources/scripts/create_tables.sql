
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
    REFERENCES status (id_status)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_application_loan_type
    FOREIGN KEY (id_loan_type)
    REFERENCES loan_type (id_loan_type)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);
CREATE UNIQUE INDEX application_email_idx ON application (email, id_loan_type);

