# --- User schema

# --- !Ups
CREATE TABLE "users" (
  "id" SERIAL PRIMARY KEY,
  "name" VARCHAR(30) NOT NULL,
  "email" VARCHAR(50) NOT NULL UNIQUE,
  "password" VARCHAR NOT NULL UNIQUE
);

# --- !Downs
DROP TABLE IF EXISTS "users";