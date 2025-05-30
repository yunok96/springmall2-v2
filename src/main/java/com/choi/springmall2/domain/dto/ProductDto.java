package com.choi.springmall2.domain.dto;

import com.choi.springmall2.domain.vo.FileVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductDto {
    private Integer id;
    private String title;
    private String description;
    private Double price;
    private Integer stock;
    private FileVo thumbnailImage;
    private List<FileVo> contentImages;
}
