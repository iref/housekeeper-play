# --- Shopping list schema

# --- !Ups
CREATE TABLE "shopping_lists" (
  "id" SERIAL PRIMARY KEY,
  "title" VARCHAR(100) NOT NULL,
  "description" TEXT
);

# --- !Downs
DROP TABLE IF EXISTS "shopping_lists";