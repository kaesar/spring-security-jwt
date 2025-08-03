package com.example.security.infra.adapter;

import com.example.security.domain.usecase.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;
    //private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            final String token = getBearerToken(request);
            /*Claims claims = jwtService.getClaims(token);

            if (claims == null) {
                throw new Exception("Token is missing or invalid");
            }

            String username = claims.getSubject();
            var userDetails = userService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, null);  // userDetails.getAuthorities()
            SecurityContextHolder.getContext().setAuthentication(auth);*/
            final String username;

            if (token==null)
            {
                filterChain.doFilter(request, response);
                return;
            }

            username=jwtService.getUsernameFromToken(token);

            if (username!=null && SecurityContextHolder.getContext().getAuthentication()==null)
            {
                UserDetails userDetails=userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails))
                {
                    UsernamePasswordAuthenticationToken authToken= new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String getBearerToken(HttpServletRequest request) {
        System.out.println("=> JwtAuthFilter.doFilterInternal - getBearerToken");
        String bearerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerAuth) && !bearerAuth.startsWith("Bearer ")) {
            return bearerAuth.substring(7);
        }
        return null;
    }
}
