package com.myapp.reservations.service;

import com.myapp.reservations.dto.userdto.UserRequest;
import com.myapp.reservations.dto.userdto.UserResponse;
import com.myapp.reservations.exception.notfoundexceptions.UserNotFoundException;
import com.myapp.reservations.mapper.UserMapper;
import com.myapp.reservations.repository.UserRepository;
import com.myapp.reservations.entities.user.Role;
import com.myapp.reservations.entities.user.User;
import com.myapp.reservations.security.AuthTokenFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse findByName(String name){
        if(name==null){
            throw new IllegalArgumentException("Name not provided");
        }
        User user = userRepository.findByName(name);
        if(user == null){
            throw new UserNotFoundException(name);
        }
        return UserMapper.toResponse(user);
    }

    public UserResponse findById(UUID id) {

        if(id==null){
            throw new IllegalArgumentException("Id not provided");
        }
        Optional<User> user = userRepository.findById(id);
        return user.map(UserMapper::toResponse).orElseThrow(() -> new UserNotFoundException(id));

    }

    public UserResponse findByEmail(String email){
        if(email==null){
            throw new IllegalArgumentException("Email not provided");
        }
        Optional<User> user = userRepository.findByEmail(email);
        return user.map(UserMapper::toResponse).orElseThrow(() -> new UserNotFoundException(email));
    }

    public boolean existsByEmail(String email){
        if(email==null){
            return false;
        }
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent();
    }

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("UserRequest not provided");
        }
        User user = UserMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of("USER"));

        User savedUser = userRepository.save(user);
        return UserMapper.toResponse(savedUser);
    }

    public List<UserResponse> getUsersByRoles(Role role) {
        if(role==null){
            throw new IllegalArgumentException("Role not provided");
        }
        return userRepository.findByRoles(role)
                .stream()
                .map(UserMapper::toResponse)
                .toList();
        }

    public void deleteUserById(UUID id) {
        if(id == null) {
            return;
        }
        userRepository.findById(id).ifPresent(userRepository::delete);

    }

    @Transactional
    public UserResponse updateUser(UUID userId, UserRequest request) {

        User existing = userRepository.findById(userId)
                .orElseThrow(() ->  new UserNotFoundException(userId));

        if (request.name() != null) existing.setName(request.name());
        if (request.email() != null) existing.setEmail(request.email());
        if (request.phone() != null) existing.setPhone(request.phone());

        if (request.password() != null) {
            existing.setPassword(passwordEncoder.encode(request.password()));
        }

        User saved = userRepository.save(existing);
        return UserMapper.toResponse(saved);
    }

    public UUID getCurrentUserId() {
        ServletRequestAttributes requestAttributes =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            UUID userId = (UUID) request.getAttribute(AuthTokenFilter.USER_ID_ATTRIBUTE);
            if (userId != null) {
                return userId;
            }
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByName(auth.getName());
        if (user == null) {
            throw new UserNotFoundException(auth.getName());
        }
        return user.getId();
    }

    @Transactional
    public void updateAvatar(UUID userId, String avatarPath) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->  new UserNotFoundException(userId));
        user.setAvatarPath(avatarPath);
        userRepository.save(user);
    }

    public String getAvatarPath(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new  UserNotFoundException(userId));
        return user.getAvatarPath();
    }
}
