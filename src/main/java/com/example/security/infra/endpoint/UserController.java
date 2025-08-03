package com.example.security.infra.endpoint;

import com.example.security.domain.model.UserEntity;
import com.example.security.domain.usecase.UserService;
import com.example.security.infra.endpoint.dto.AuthResponse;
import com.example.security.domain.usecase.JwtService;
import com.example.security.infra.adapter.UserRepository;
import com.example.security.infra.endpoint.dto.SignInRequest;
import com.example.security.infra.endpoint.dto.SignUpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/profile")
    public ResponseEntity<Object> profile(Authentication authentication) {
        var response = new HashMap<String, Object>();
        response.put("username", authentication.getName());
        response.put("roles", authentication.getAuthorities());
        var user = userRepository.findByUsername(authentication.getName());
        response.put("profile", user);
        return ResponseEntity.ok("Profile");
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> signUp(@RequestBody SignUpRequest signUpRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            var errorsList = bindingResult.getAllErrors();
            var errorsMap = new HashMap<String, String>();

            for (var error : errorsList) {
                errorsMap.put(((FieldError) error).getField(), error.getDefaultMessage());
            }

            return ResponseEntity.badRequest().body(errorsMap);
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(signUpRequest.getUsername());
        userEntity.setPassword(signUpRequest.getPassword());
        userEntity.setRole("USER");

        var encoder = new BCryptPasswordEncoder();
        userEntity.setPassword(encoder.encode(signUpRequest.getPassword()));

        try {
            var checkUser = userRepository.findByUsername(signUpRequest.getUsername());
            if (checkUser != null) {
                return ResponseEntity.badRequest().body("UserEntity already exists");
            }
            userRepository.save(userEntity);

            String token = jwtService.getToken(userEntity);
            /*var response = new HashMap<String, Object>();
            response.put("token", token);
            response.put("userEntity", userEntity);
            return ResponseEntity.ok(response);*/
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return ResponseEntity.badRequest().body("Error!");
    }

    @PostMapping("/signin")
    public ResponseEntity<Object> signIn(@RequestBody SignInRequest signInRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            var errorsList = bindingResult.getAllErrors();
            var errorsMap = new HashMap<String, String>();

            for (var error : errorsList) {
                errorsMap.put(((FieldError) error).getField(), error.getDefaultMessage());
            }

            return ResponseEntity.badRequest().body(errorsMap);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            signInRequest.getUsername(),
                            signInRequest.getPassword()
                    )
            );

            var userEntity = userRepository.findByUsername(signInRequest.getUsername());
            String token = jwtService.getToken(userEntity.get());
            /*var response = new HashMap<String, Object>();
            response.put("token", token);
            response.put("userEntity", userEntity);
            return ResponseEntity.ok(response);*/
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return ResponseEntity.badRequest().body("Bad Password!");
    }
}
