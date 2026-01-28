package com.myapp.reservations.mapper;

import com.myapp.reservations.dto.userdto.UserRequest;
import com.myapp.reservations.dto.userdto.UserResponse;
import com.myapp.reservations.entities.User.User;

public class UserMapper {

    public static UserResponse toResponse(User user) {
        if(user==null){
            return null;
        }
        String avatarUrl = user.getAvatarPath() != null
                ? "/uploads/" + user.getAvatarPath()
                : null;
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRoles(),
                avatarUrl
        );
    }

    public static User toUser(UserRequest request) {
        if (request == null) return null;
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setPhone(request.phone());
        return user;
    }
}
