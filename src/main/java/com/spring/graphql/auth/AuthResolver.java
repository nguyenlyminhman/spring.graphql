package com.spring.graphql.auth;

import com.spring.graphql.config.JwtUtil;
import graphql.GraphQLException;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AuthResolver {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
//  private final UserRepository userRepository;

    @MutationMapping
    public AuthPayload login(
            @Argument String username,
            @Argument String password
    ) {
        try {
            // 1. Xác thực username/password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (BadCredentialsException e) {
            throw new GraphQLException("Invalid username or password");
        }

        // 2. Load UserDetails và generate token
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String token = jwtUtil.generateToken(userDetails);

        // 3. Trả về token + user info
        User user = new User(null, null, null); //userRepository.findByUsername(username).orElseThrow();
        return new AuthPayload(token, user);
    }
}
