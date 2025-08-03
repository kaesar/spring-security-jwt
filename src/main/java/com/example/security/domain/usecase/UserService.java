package com.example.security.domain.usecase;

import com.example.security.domain.model.UserEntity;
import com.example.security.infra.adapter.UserRepository;
import com.example.security.infra.endpoint.dto.AuthResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService /*implements UserDetailsService*/ {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    /*@Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> userEntity = userRepository.findByUsername(username);
        return User.withUsername(userEntity.get().getUsername())
                .password(userEntity.get().getPassword())
                .roles(userEntity.get().getRole())
                .build();
    }*/

    public AuthResponse signIn(String username, String password) {
        Optional<UserEntity> userEntity = userRepository.findByUsername(username);

        if (userEntity != null && userEntity.get().getPassword().equals(password)) {
            String token = jwtService.getToken(userEntity.get());
            return new AuthResponse(token);
        }

        return null;
    }

    public AuthResponse  signUp(UserEntity userEntity) {
        Optional<UserEntity> checkUser = userRepository
                .findByUsername(userEntity.getUsername());

        userRepository.save(userEntity);

        String token = jwtService.getToken(userEntity);
        return new AuthResponse(token);
    }

}
