package com.moviebookingapp.services;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.moviebookingapp.entity.User;
import com.moviebookingapp.repository.UserRepository;
import com.moviebookingapp.services.UserDetailsServiceImpl;
import com.moviebookingapp.services.UserDetailsImpl;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setLoginId("testuser");
        // Set other properties of the user as necessary
    }

    @Test
    public void loadUserByUsername_userFound_returnsUserDetails() {
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        // Add more assertions based on the properties of UserDetailsImpl
    }

    @Test
    public void loadUserByUsername_userNotFound_throwsUsernameNotFoundException() {
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("testuser");
        });

        assertEquals("User Not Found with username: testuser", exception.getMessage());
    }
}
