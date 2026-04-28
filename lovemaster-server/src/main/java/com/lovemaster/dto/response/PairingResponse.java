package com.lovemaster.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PairingResponse {

    private Boolean paired;
    private Long coupleId;
    private String pairCode;
    private PartnerResponse partner;
    private LocalDateTime pairedAt;

    public static PairingResponse unpaired(String pairCode) {
        PairingResponse response = new PairingResponse();
        response.setPaired(false);
        response.setPairCode(pairCode);
        return response;
    }
}
