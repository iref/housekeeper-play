# --- Households schema

# --- !Ups
CREATE TABLE "households" (
  "id" SERIAL PRIMARY KEY,
  "name" VARCHAR(50) NOT NULL,
  "description" TEXT,
  "logo" VARCHAR(100),
  "owner_id" INT NOT NULL,
  CONSTRAINT "household_owner_fk" FOREIGN KEY ("owner_id") REFERENCES "users" ("id")
);


# --- !Downs

DROP TABLE IF EXISTS households;