CREATE CONSTRAINT challenge_game_not_null IF NOT EXISTS
FOR (c:Challenge) REQUIRE c.game IS NOT NULL;

CREATE CONSTRAINT challenge_difficulty_not_null IF NOT EXISTS
FOR (c:Challenge) REQUIRE c.difficulty IS NOT NULL;

CREATE CONSTRAINT challenge_firstNumber_not_null IF NOT EXISTS
FOR (c:Challenge) REQUIRE c.firstNumber IS NOT NULL;

CREATE CONSTRAINT challenge_secondNumber_not_null IF NOT EXISTS
FOR (c:Challenge) REQUIRE c.secondNumber IS NOT NULL;
