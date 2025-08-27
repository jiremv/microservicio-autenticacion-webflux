package com.pragma.service;

import com.pragma.dao.request.SignUpRequest;
import com.pragma.dao.request.SigninRequest;
import com.pragma.dao.response.JwtAuthenticationResponse;
import reactor.core.publisher.Mono;
public interface AuthenticationService {
    Mono<JwtAuthenticationResponse> signup(SignUpRequest request);
    Mono<JwtAuthenticationResponse> signin(SigninRequest request);
}
