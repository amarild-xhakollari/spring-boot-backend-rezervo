package com.myapp.reservations.service;

import com.myapp.reservations.dto.userdto.ProfileUpdateRequest;
import com.myapp.reservations.dto.userdto.UserRequest;
import com.myapp.reservations.dto.userdto.UserResponse;
import com.myapp.reservations.mapper.UserMapper;
import com.myapp.reservations.repository.UserRepository;
import com.myapp.reservations.entities.User.Role;
import com.myapp.reservations.entities.User.User;
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
            return null;
        }
        User user = userRepository.findByName(name);

        return UserMapper.toResponse(user);
    }

    public UserResponse findById(UUID id) {

        if(id==null){
            return null;
        }
        Optional<User> user = userRepository.findById(id);
        return user.map(UserMapper::toResponse).orElse(null);

    }

    public UserResponse findByEmail(String email){
        if(email==null){
            return null;
        }
        Optional<User> user = userRepository.findByEmail(email);
        return user.map(UserMapper::toResponse).orElse(null);
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
        if (request == null) return null;
        User user = UserMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of("USER"));

        User savedUser = userRepository.save(user);
        return UserMapper.toResponse(savedUser);
    }

    public List<UserResponse> getUsersByRoles(Role role) {
        if(role==null){
            return null;
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
    public UserResponse updateUser(UUID id, UserRequest request) {

        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.name() != null) existing.setName(request.name());
        if (request.email() != null) existing.setEmail(request.email());
        if (request.phone() != null) existing.setPhone(request.phone());

        if (request.password() != null) {
            existing.setPassword(passwordEncoder.encode(request.password()));
        }

        User saved = userRepository.save(existing);
        return UserMapper.toResponse(saved);
    }

    @Transactional
    public UserResponse updateProfile(UUID id, ProfileUpdateRequest request) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.name() != null) existing.setName(request.name());
        if (request.email() != null) existing.setEmail(request.email());
        if (request.phone() != null) existing.setPhone(request.phone());

        if (request.password() != null && !request.password().isBlank()) {
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
            throw new RuntimeException("User not found. Please log in again.");
        }
        return user.getId();
    }

    @Transactional
    public void updateAvatar(UUID userId, String avatarPath) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setAvatarPath(avatarPath);
        userRepository.save(user);
    }

    public String getAvatarPath(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getAvatarPath();
    }
}
