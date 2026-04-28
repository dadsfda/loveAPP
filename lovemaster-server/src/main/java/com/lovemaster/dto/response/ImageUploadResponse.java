package com.lovemaster.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResponse {

    private String url;

    private String filename;

    private String contentType;

    private Long size;
}
