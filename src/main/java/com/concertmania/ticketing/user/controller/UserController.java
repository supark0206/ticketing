package com.concertmania.ticketing.user.controller;

import com.concertmania.ticketing.user.dto.UserResponse;
import com.concertmania.ticketing.user.dto.LoginRequest;
import com.concertmania.ticketing.user.dto.SignupRequest;
import com.concertmania.ticketing.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "인증 관련 API")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        UserResponse response = userService.signup(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그인", description = "사용자 로그인을 수행합니다.")
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        UserResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }
}