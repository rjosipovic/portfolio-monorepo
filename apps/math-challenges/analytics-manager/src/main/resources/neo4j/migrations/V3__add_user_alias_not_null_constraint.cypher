CREATE CONSTRAINT user_alias_not_null IF NOT EXISTS
FOR (u:User) REQUIRE u.alias IS NOT NULL;
