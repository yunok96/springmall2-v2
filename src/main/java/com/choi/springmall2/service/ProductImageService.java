package com.choi.springmall2.service;

import com.choi.springmall2.domain.dto.ProductDto;
import com.choi.springmall2.domain.entity.Product;
import com.choi.springmall2.domain.entity.ProductImage;
import com.choi.springmall2.domain.vo.FileVo;
import com.choi.springmall2.repository.ProductImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductImageRepository productImageRepository;

    public List<ProductImage> saveProductImages(ProductDto productDto, Product product) {
        // 상품 이미지 리스트 생성
        List<ProductImage> productImages = new ArrayList<>();

        // 썸네일 이미지 처리 (순서대로 추가)
        addProductImage(productDto.getThumbnailImage().fileKey()
                , productDto.getThumbnailImage().fileName()
                , product, "thumbnail", 0, productImages);

        // 상품 내용 이미지 처리 (순서대로 추가)
        List<FileVo> contentImages = productDto.getContentImages();
        for (int i = 0; i < contentImages.size(); i++) {
            addProductImage(contentImages.get(i).fileKey()
                    , contentImages.get(i).fileName()
                    , product, "content", i + 1, productImages);
        }

        return productImages;
    }

    private void addProductImage(String imageKey, String imageName, Product product, String type, int seq, List<ProductImage> productImages) {
        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setImageKey(imageKey);
        productImage.setImageName(imageName);
        productImage.setType(type);
        productImage.setSeq(seq);

        // 이미지 저장
        ProductImage savedImage = productImageRepository.save(productImage);

        // 저장된 이미지를 리스트에 추가
        productImages.add(savedImage);
    }

    public FileVo getThumbnailImage(int productId) {
        // ProductImage 를 조회한 뒤에 FileVo에 매핑한 뒤 반환.
        List<ProductImage> productImages = productImageRepository
                .findByProductIdAndTypeOrderBySeqAsc(productId, "thumbnail");
        if (!productImages.isEmpty()) {
            return new FileVo(productImages.get(0).getImageName(), productImages.get(0).getImageKey());
        }
        // 상품 썸네일 이미지는 없을 수 있으므로 예외를 발생시키지 않고 null 을 반환.
        return null;
    }

    public List<FileVo> getContentImages(int productId) {
        // ProductImage 를 조회한 뒤에 FileVo에 매핑한 뒤 반환.
        List<ProductImage> productImages = productImageRepository
                .findByProductIdAndTypeOrderBySeqAsc(productId, "content");
        if (!productImages.isEmpty()) {
            List<FileVo> fileVoList = new ArrayList<>();
            for (ProductImage productImage : productImages) {
                fileVoList.add(new FileVo(productImage.getImageName(), productImage.getImageKey()));
            }
            return fileVoList;
        }
        // 상품 내용 이미지는 없을 수 있으므로 예외를 발생시키지 않고 null 을 반환.
        return Collections.emptyList();
    }
}
