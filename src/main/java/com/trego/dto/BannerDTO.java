package com.trego.dto;

import lombok.Data;

@Data
public class BannerDTO {
    private Long id;
    private String logo;
    private String bannerUrl;
    private String position;
    private String createdBy;
}