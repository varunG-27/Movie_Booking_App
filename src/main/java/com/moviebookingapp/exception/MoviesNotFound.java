package com.moviebookingapp.exception;

public class MoviesNotFound extends RuntimeException {
	public MoviesNotFound(String noMoviesAreAvailable) {
		super(noMoviesAreAvailable);
	}

}
