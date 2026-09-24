DELIMITER //
CREATE PROCEDURE IF NOT EXISTS add_movie(
                                         IN movieTitle varchar(100),
                                         IN movieYear int,
                                         IN movieDirector varchar(100),
                                         IN movieStar varchar(100),
                                         IN movieGenre varchar(32),
                                         OUT message TEXT
)
BEGIN
    DECLARE count int;
    DECLARE movieId varchar(10);
    DECLARE starId varchar(10);
    DECLARE genreId int;

    SET count = (SELECT count(*) FROM movies WHERE title = movieTitle AND year = movieYear AND director = movieDirector);
    IF count > 0 THEN
        SET message = CONCAT("Movie already exists, No changes ", "\n");
    ELSE
        SELECT CONCAT('tt', COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)), 0) + 1) INTO movieId FROM movies;
        INSERT INTO movies VALUES (movieId, movieTitle, movieYear, movieDirector);
        SET message = CONCAT("Creating new movie with id ", movieId, "\n");

        SET count = (SELECT count(*) FROM stars WHERE name = movieStar);
        IF count = 0 THEN
            SELECT CONCAT('nm', COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)), 0) + 1) INTO starId FROM stars;
            INSERT INTO stars VALUES (starId, movieStar, NULL);
            SET message = CONCAT(message, "Created new star with id ", starId, "\n");
        ELSE
            SELECT id INTO starId FROM stars WHERE name = movieStar LIMIT 1;
            SET message = CONCAT(message, "Using existing star with id ", starId, "\n");
        END IF;
        INSERT INTO stars_in_movies VALUES (starId, movieId);
        SET message = CONCAT(message, "Created new link between ", starId, " and ", movieId, "\n");

        SET count = (SELECT count(*) FROM genres WHERE name = movieGenre);
        IF count = 0 THEN
            INSERT INTO genres(name) VALUES (movieGenre);
            SELECT LAST_INSERT_ID() INTO genreId;
            SET message = CONCAT(message, "Created new genre with id ", genreId, "\n");

        ELSE
            SELECT id INTO genreId FROM genres WHERE name = movieGenre LIMIT 1;
            SET message = CONCAT(message, "Using existing genre with id ", genreId, "\n");
        END IF;
        INSERT INTO genres_in_movies(genreId, movieId) VALUES (genreId, movieId);
        INSERT INTO ratings VALUES (movieId, 0.0, 0);
        SET message = CONCAT(message, "Created new link between ", genreId, " and ", movieId, "\n");
    END IF;
END //
DELIMITER ;