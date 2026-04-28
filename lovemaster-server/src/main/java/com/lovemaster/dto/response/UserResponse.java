package com.lovemaster.dto.response;

import com.lovemaster.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private Integer gender;
    private String pairCode;
    private Long partnerId;
    private LocalDateTime pairedAt;

    public static UserResponse fromEntity(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setGender(user.getGender());
        response.setPairCode(user.getPairCode());
        response.setPartnerId(user.getPartnerId());
        response.setPairedAt(user.getPairedAt());
        return response;
    }
}
