package com.aptible.api;

import org.springframework.http.HttpStatus;

public class Models {
    public record LoginRequest(String username, String password) implements ApiResponse {}
    public record FileUploadInitRequest(String tags) implements ApiResponse {}
    public record LoginResponse(String token) implements ApiResponse  {}
    public record ErrorMessage(String message, HttpStatus status) implements ApiResponse  {}
}
