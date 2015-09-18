CREATE TABLE person
(
  id         INT IDENTITY,
  first_name VARCHAR(255),
  last_name  VARCHAR(255),
  age        INT
);

INSERT INTO person
    VALUES (1, 'Jon', 'Doe', 20)
