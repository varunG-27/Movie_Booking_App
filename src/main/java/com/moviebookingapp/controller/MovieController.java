package com.moviebookingapp.controller;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moviebookingapp.entity.LoginRequest;
import com.moviebookingapp.entity.Movie;
import com.moviebookingapp.entity.Ticket;
import com.moviebookingapp.entity.User;
import com.moviebookingapp.exception.MoviesNotFound;
import com.moviebookingapp.exception.SeatAlreadyBooked;
import com.moviebookingapp.repository.MovieRepository;
import com.moviebookingapp.repository.TicketRepository;
import com.moviebookingapp.repository.UserRepository;
import com.moviebookingapp.services.MovieService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;

@RestController
@CrossOrigin(origins = "http:localhost:4200")
@RequestMapping("/api/v1.0/moviebooking")
@OpenAPIDefinition(info = @Info(title = "Movie Application API", description = "This API provides endpoints for managing movies."))
@Slf4j
public class MovieController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	private MovieService movieService;

	@Autowired
	private MovieRepository movieRepository;

	@Autowired
	private com.moviebookingapp.kafka.kafkaProducerService kafkaProducerService;
	
	@Autowired
	private TicketRepository ticketRepository;

	@PutMapping("/{loginId}/forgot")
	@SecurityRequirement(name = "Bearer Authentication")
	@Operation(summary = "reser password")
//	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<String> changePassword(@RequestBody LoginRequest loginRequest, @PathVariable String loginId) {
		log.debug("forgot password endpoint accessed by " + loginRequest.getLoginId());
		Optional<User> user1 = userRepository.findByLoginId(loginId);
		User availableUser = user1.get();
		User updatedUser = new User(loginId, availableUser.getFirstName(), availableUser.getLastName(),
				availableUser.getEmail(), availableUser.getContactNumber(),
				passwordEncoder.encode(loginRequest.getPassword()));
		updatedUser.set_id(availableUser.get_id());
		updatedUser.setRoles(availableUser.getRoles());
		userRepository.save(updatedUser);
		log.debug(loginRequest.getLoginId() + "has password changed successfully");
		return new ResponseEntity<>("User Password changed successfully", HttpStatus.OK);
	}

	@GetMapping("/all")
	@SecurityRequirement(name = "Bearer Authentication")
	@Operation(summary = "search all movies")
	public ResponseEntity<List<Movie>> getAllMovies() {
		log.debug("here you can access all the available movies");
		List<Movie> movieList = movieService.getAllMovies();
		if (movieList.isEmpty()) {
			log.debug("currently no movies are available");
			throw new MoviesNotFound("No movies are available");
		} else {
			log.debug("listed are available movies");
			return new ResponseEntity<>(movieList, HttpStatus.OK);
		}
	}
	
	@GetMapping("/movies/search/{movieName}")
	@SecurityRequirement(name = "Bearer Authentication")
	@Operation(summary = "search movies by movie name")
	public ResponseEntity<List<Movie>> getMovieByName(@PathVariable String movieName) {
		log.debug("here search a movie by its name");
		List<Movie> movieList = movieService.getMovieByName(movieName);
		if (movieList.isEmpty()) {
			log.debug("searched movies are not available");
			throw new MoviesNotFound("Movies Not Found");
		} else {
			log.debug("listed the available movies with title: " + movieName);
			return new ResponseEntity<>(movieList, HttpStatus.OK);
		}
	}
	

	@PutMapping("/{movieName}/update/{ticketId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> updateTicketStatus(@PathVariable String movieName, @PathVariable ObjectId ticketId) {
		List<Movie> movie = movieRepository.findByMovieName(movieName);
		List<Ticket> ticket = ticketRepository.findBy_id(ticketId);
		if(movie == null) {
			throw new MoviesNotFound("Movie not found: " + movieName);
		}
		
		if(ticket ==  null) {
			throw new NoSuchElementException("Ticket Not found: " + ticketId);
		}
		int ticketsBooked = movieService.getTotalNoTickets(movieName);
		for(Movie movies : movie) {
			if(ticketsBooked >= movies.getNoOfTicketsAvailable()) {
				movies.setTicketStatus("SOLD OUT");
			} else {
				movies.setTicketStatus("BOOK ASAP");
			}
			movieService.saveMovie(movies);
		}

		return new ResponseEntity<>("Ticket status updated successfully", HttpStatus.OK);
 	}

	@PostMapping("/{movieName}/add")
	@SecurityRequirement(name = "Bearer Authentication")
	@Operation(summary = "book ticket")
//	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> bookTickets(@RequestBody Ticket ticket, @PathVariable String movieName) {
		log.debug(ticket.getLoginId() + "entered to book tickets");
		List<Ticket> allTickets = movieService.findSeats(movieName, ticket.getTheatreName());
		for (Ticket each : allTickets) {
			for (int i = 0; i < ticket.getNoOfTickets(); i++) {
				if (each.getSeatNumber().contains(ticket.getSeatNumber().get(i))) {
					log.debug("seat is already booked");
					throw new SeatAlreadyBooked("Seat number " + ticket.getSeatNumber().get(i) + "is already booked");
				}
			}
		}
		if (movieService.findAvailableTickets(movieName, ticket.getTheatreName()).get(0)
				.getNoOfTicketsAvailable() >= ticket.getNoOfTickets()) {
			log.info("available tickets " + movieService.findAvailableTickets(movieName, ticket.getTheatreName()).get(0)
					.getNoOfTicketsAvailable());
			movieService.saveTicket(ticket);
			log.debug(ticket.getLoginId() + " booked " + ticket.getNoOfTickets() + " tickets");
			kafkaProducerService.sendMessage("Movie ticket booked." + "Booking Details are: " + ticket);
			List<Movie> movies = movieRepository.findByMovieName(movieName);
			int available_tickets = 0;
			for (Movie movie : movies) {
				available_tickets = movie.getNoOfTicketsAvailable() - ticket.getNoOfTickets();
				movie.setNoOfTicketsAvailable(available_tickets);
				movieService.saveMovie(movie);
			}
			return new ResponseEntity<>("Ticket Booked Successfully with seat numbers" + ticket.getSeatNumber(),
					HttpStatus.OK);
		} else {
			log.debug("tickets sold out");
			return new ResponseEntity<>("\"All tickets sold out\"", HttpStatus.OK);
		}
	}

	@GetMapping("/getallbookedtickets/{movieName}")
	@SecurityRequirement(name = "Bearer Authentication")
	@Operation(summary = "get all booked tickets(Admin Only)")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<Ticket>> getAllBookedTickets(@PathVariable String movieName) {
		return new ResponseEntity<>(movieService.getAllBookedTickets(movieName), HttpStatus.OK);
	}

	@PutMapping("/admin/{movieName}/update")
	@SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> updateTicketStatus(@PathVariable String movieName) {
	    List<Movie> movie = movieRepository.findByMovieName(movieName);
	    if (movie == null) {
	        throw new MoviesNotFound("Movie not found: " + movieName);
	    }

	    int ticketsBooked = movieService.getTotalNoTickets(movieName);
	    for (Movie movies : movie) {
	        if (ticketsBooked >= movies.getNoOfTicketsAvailable()) {
	            movies.setTicketStatus("SOLD OUT");
	        } else {
	            movies.setTicketStatus("BOOK ASAP");
	        }
	        movieService.saveMovie(movies);
	    }
	    kafkaProducerService.sendMessage("ticket status updated by the Admin for movie: " + movieName);
	    return new ResponseEntity<>("Ticket status updated successfully", HttpStatus.OK);
	}

	@DeleteMapping("/{movieName}/delete")
	@SecurityRequirement(name = "Bearer Authentication")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> deleteMovie(@PathVariable String movieName) {
		List<Movie> availableMovies = movieService.findByMovieName(movieName);
		if (availableMovies.isEmpty()) {
			throw new MoviesNotFound("No movies Available with movie name: " + movieName);
		} else {
			movieService.deleteByMovieName(movieName);
			kafkaProducerService.sendMessage("Movie Deleted by the Admin. " + movieName + "is now not available");
			return new ResponseEntity<>("Movie deleted Successfully", HttpStatus.OK);
		}
	}

}
