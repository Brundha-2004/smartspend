package com.example.smartspend.controller;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.smartspend.dto.AuthRequest;
import com.example.smartspend.dto.RegisterRequest;
import com.example.smartspend.entity.User;
import com.example.smartspend.security.JwtUtil;
import com.example.smartspend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void registerUser_Success() throws Exception {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");

        User user = new User("test@example.com", "password123", "John", "Doe");
        
        when(userService.registerUser(any(User.class))).thenReturn(user);

        // When & Then
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully. Please check your email for verification."));

        verify(userService, times(1)).registerUser(any(User.class));
    }

    @Test
    void registerUser_EmailAlreadyExists() throws Exception {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("existing@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");

        when(userService.registerUser(any(User.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        // When & Then
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already exists"));

        verify(userService, times(1)).registerUser(any(User.class));
    }

    @Test
    void loginUser_Success() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("password123");

        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.setEnabled(true);

        Authentication authentication = mock(Authentication.class);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("test@example.com")).thenReturn("jwt-token");

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, times(1)).findByEmail("test@example.com");
        verify(jwtUtil, times(1)).generateToken("test@example.com");
    }

    @Test
    void loginUser_InvalidCredentials() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid credentials"));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, never()).findByEmail(anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void loginUser_UserNotVerified() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("password123");

        User user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.setEnabled(false); // User not verified

        Authentication authentication = mock(Authentication.class);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please verify your email first"));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, times(1)).findByEmail("test@example.com");
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void verifyEmail_Success() throws Exception {
        // Given
        String token = "valid-verification-token";
        when(userService.verifyUser(token)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/auth/verify")
                .param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string("Email verified successfully. You can now login."));

        verify(userService, times(1)).verifyUser(token);
    }

    @Test
    void verifyEmail_InvalidToken() throws Exception {
        // Given
        String token = "invalid-verification-token";
        when(userService.verifyUser(token)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/auth/verify")
                .param("token", token))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid verification token"));

        verify(userService, times(1)).verifyUser(token);
    }

    @Test
    void registerUser_InvalidInput() throws Exception {
        // Given - Invalid email format
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("invalid-email");
        registerRequest.setPassword("123"); // Too short
        registerRequest.setFirstName("");
        registerRequest.setLastName("");

        // When & Then
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(User.class));
    }
}