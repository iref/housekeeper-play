# --- Shopping list items schema

# --- !Ups
CREATE TABLE "shopping_list_items" (
  "id" SERIAL PRIMARY KEY,
  "name" VARCHAR(100) NOT NULL,
  "quantity" INT NOT NULL DEFAULT 1,
  "price_for_one" NUMERIC (10, 2),
  "shopping_list_id" INT NOT NULL,
  CONSTRAINT "shopping_item_list_fk"
    FOREIGN KEY ("shopping_list_id") REFERENCES "shopping_lists" ("id")
    ON DELETE CASCADE ON UPDATE NO ACTION
);

# --- !Downs
DROP TABLE IF EXISTS "shopping_list_items";