package com.myapp.reservations.controller;

import com.myapp.reservations.dto.userdto.UserRequest;
import com.myapp.reservations.dto.userdto.UserResponse;
import com.myapp.reservations.service.UserService;
import com.myapp.reservations.entities.User.Role;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public List<UserResponse> findAll(){
        return   userService.getUsers();
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable (value = "id") UUID id){
        return userService.findById(id);
    }

    @PostMapping("/create")
    public UserResponse save(@Valid @RequestBody UserRequest user){
        return userService.createUser(user);
    }

    @GetMapping("/by-name/{name}")
    public UserResponse findByName(@PathVariable (value = "name") String name){
        return userService.findByName(name);
    }

    @GetMapping("/by-role/{role}")
    public List<UserResponse> findByRoles(@PathVariable (value = "role") Role role){
        return userService.getUsersByRoles(role);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable (value = "id") UUID id){
        if(userService.findById(id) == null) return;
        userService.deleteUserById(id);
    }

    @PutMapping("/update/{id}")
    public void update(@PathVariable UUID id, @Valid @RequestBody UserRequest userRequest){
        if(userRequest == null || id == null) return ;
        userService.updateUser(id, userRequest);
    }
}
