package com.moviebookingapp.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.moviebookingapp.entity.Movie;
import com.moviebookingapp.entity.Ticket;
import com.moviebookingapp.repository.MovieRepository;
import com.moviebookingapp.repository.TicketRepository;

public class MovieServiceTest {

    @InjectMocks
    private MovieService movieService;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private TicketRepository ticketRepository;

    private Movie movie;
    private Ticket ticket;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        movie = new Movie();
        movie.setMovieName("Test Movie");
        movie.setNoOfTicketsAvailable(10);

        ticket = new Ticket();
        ticket.setMovieName("Test Movie");
        ticket.setTheatreName("Test Theatre");
        ticket.setNoOfTickets(2);
        ticket.setSeatNumber(Arrays.asList("A1", "A2"));
    }

    @Test
    public void testGetAllMovies() {
        when(movieRepository.findAll()).thenReturn(Arrays.asList(movie));

        List<Movie> movies = movieService.getAllMovies();
        assertEquals(1, movies.size());
        assertEquals("Test Movie", movies.get(0).getMovieName());
    }

    @Test
    public void testGetMovieByName() {
        when(movieRepository.findByMovieName(anyString())).thenReturn(Arrays.asList(movie));

        List<Movie> movies = movieService.getMovieByName("Test Movie");
        assertEquals(1, movies.size());
        assertEquals("Test Movie", movies.get(0).getMovieName());
    }

    @Test
    public void testFindSeats() {
        when(ticketRepository.findSeats(anyString(), anyString())).thenReturn(Arrays.asList(ticket));

        List<Ticket> tickets = movieService.findSeats("Test Movie", "Test Theatre");
        assertEquals(1, tickets.size());
        assertEquals("Test Movie", tickets.get(0).getMovieName());
        assertEquals("Test Theatre", tickets.get(0).getTheatreName());
    }

    @Test
    public void testFindAvailableTickets() {
        when(movieRepository.findAvailableTickets(anyString(), anyString())).thenReturn(Arrays.asList(movie));

        List<Movie> movies = movieService.findAvailableTickets("Test Movie", "Test Theatre");
        assertEquals(1, movies.size());
        assertEquals(10, movies.get(0).getNoOfTicketsAvailable());
    }

    @Test
    public void testDeleteByMovieName() {
        doNothing().when(movieRepository).deleteByMovieName(anyString());

        movieService.deleteByMovieName("Test Movie");

        verify(movieRepository, times(1)).deleteByMovieName("Test Movie");
    }

    @Test
    public void testSaveTicket() {
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        movieService.saveTicket(ticket);

        verify(ticketRepository, times(1)).save(ticket);
    }

    @Test
    public void testSaveMovie() {
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);

        movieService.saveMovie(movie);

        verify(movieRepository, times(1)).save(movie);
    }

    @Test
    public void testGetAllBookedTickets() {
        when(ticketRepository.findByMovieName(anyString())).thenReturn(Arrays.asList(ticket));

        List<Ticket> tickets = movieService.getAllBookedTickets("Test Movie");
        assertEquals(1, tickets.size());
        assertEquals("Test Movie", tickets.get(0).getMovieName());
    }

    @Test
    public void testGetTotalNoTickets() {
        when(ticketRepository.findByMovieName(anyString())).thenReturn(Arrays.asList(ticket));

        int totalTickets = movieService.getTotalNoTickets("Test Movie");
        assertEquals(2, totalTickets);
    }
}
