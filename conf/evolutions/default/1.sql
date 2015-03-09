# --- User schema

# --- !Ups

CREATE TABLE "users" (
  "id" serial PRIMARY KEY,
  "name" VARCHAR(50) NOT NULL,
  "email" VARCHAR(80) NOT NULL UNIQUE,
  "google_id" VARCHAR(50) NOT NULL UNIQUE,
  "avatar" VARCHAR(100)
);

# --- !Downs
DROP TABLE IF EXISTS "users";