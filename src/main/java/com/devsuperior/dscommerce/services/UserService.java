package com.devsuperior.dscommerce.services;

import com.devsuperior.dscommerce.dto.UserDTO;
import com.devsuperior.dscommerce.entities.Role;
import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.projection.UserDetailsProjection;
import com.devsuperior.dscommerce.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<UserDetailsProjection> users = repository.searchUserAndRoleByEmail(username);
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        User user = new User();
        configureUser(user, username, users);

        return user;
    }

    private void configureUser(User user, String username, List<UserDetailsProjection> users) {
        user.setName(username);
        user.setEmail(username);
        user.setPassword(users.get(0).getPassword());
        for (UserDetailsProjection u : users) {
            user.addRole(new Role(u.getRoleId(), u.getAuthority()));
        }
    }

    protected User authenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Jwt jwtPrincipal = (Jwt) authentication.getPrincipal();
            String username = jwtPrincipal.getClaim("username");
            return repository.findByEmail(username).get();
        }
        catch (Exception e){
            throw new UsernameNotFoundException("Email not found");
        }
    }
    @Transactional(readOnly = true)
    public UserDTO getMyUser(){
        User user = authenticated();
        return new UserDTO(user);
    }
}
