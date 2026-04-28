package com.lovemaster.dto.response;

import com.lovemaster.entity.User;
import lombok.Data;

@Data
public class PartnerResponse {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer gender;

    public static PartnerResponse fromEntity(User user) {
        PartnerResponse response = new PartnerResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setGender(user.getGender());
        return response;
    }
}
