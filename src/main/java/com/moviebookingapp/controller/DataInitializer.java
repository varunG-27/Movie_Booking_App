package com.moviebookingapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.moviebookingapp.entity.Movie;
import com.moviebookingapp.repository.MovieRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final MovieRepository movieRepository;

    @Autowired
    public DataInitializer(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if the database is empty
        if (movieRepository.count() == 0) {
            // If the database is empty, initialize dummy movie data
            Movie movie1 = new Movie("Movie 1", "Theatre A", 100);
            Movie movie2 = new Movie("Movie 2", "Theatre B", 150);
            Movie movie3 = new Movie("Movie 3", "Theatre A", 100);
            Movie movie4 = new Movie("Movie 15", "Theatre A", 100);
            Movie movie5 = new Movie("Movie 431", "Theatre A", 100);
            Movie movie6 = new Movie("Movie 134", "Theatre A", 100);
            Movie movie7 = new Movie("Movie 31", "Theatre A", 0);

            // Save the movies to the database
            movieRepository.save(movie1);
            movieRepository.save(movie2);
            movieRepository.save(movie3);
            movieRepository.save(movie4);
            movieRepository.save(movie5);
            movieRepository.save(movie6);
            movieRepository.save(movie7);
        }
    }
}
