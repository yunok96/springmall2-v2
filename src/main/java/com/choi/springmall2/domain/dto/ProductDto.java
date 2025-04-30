package com.choi.springmall2.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductDto {
    private String title;
    private String description;
    private Double price;
    private Integer stock;
    private FileVo thumbnailImage;
    private List<FileVo> contentImages;
}
