package com.choi.springmall2.service;

import com.choi.springmall2.domain.CustomUser;
import com.choi.springmall2.domain.dto.ProductDto;
import com.choi.springmall2.domain.entity.Product;
import com.choi.springmall2.domain.entity.ProductImage;
import com.choi.springmall2.domain.entity.User;
import com.choi.springmall2.domain.vo.FileVo;
import com.choi.springmall2.error.exceptions.ProductDetailNotFoundException;
import com.choi.springmall2.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageService productImageService;
    private final UserService userService;

    @Transactional
    public ProductDto saveProduct(ProductDto productDto, CustomUser customUser) {
        // 사용자 정보 가져오기
        int userId = customUser.getId();
        User user = userService.getUserById(userId);

        // 상품 정보 저장
        Product product = new Product();
        product.setTitle(productDto.getTitle());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setStock(productDto.getStock());
        product.setSeller(user);

        product = productRepository.save(product);  // 상품 저장 후 반환된 product (id 자동 생성됨)

        // 상품 이미지 저장
        List<ProductImage> productImages = productImageService.saveProductImages(productDto, product);  // 상품 이미지 저장

        // 이미지 키들을 ProductDto 에 반영
        if (!productImages.isEmpty()) {
            FileVo thumbnailImage = new FileVo(productImages.get(0).getImageName()
                    , productImages.get(0).getImageKey()); // 첫 번째 이미지를 썸네일로 세팅
            productDto.setThumbnailImage(thumbnailImage);

            // 나머지 이미지는 contentImageKeys 에 추가
            List<FileVo> imageKeys = new ArrayList<>();
            for (int i = 1; i < productImages.size(); i++) { // 첫 번째 이미지는 제외
                FileVo contentImage = new FileVo(productImages.get(i).getImageName()
                        , productImages.get(i).getImageKey());

                imageKeys.add(contentImage);
            }
            productDto.setContentImages(imageKeys);
        }

        return productDto;
    }

    public Page<ProductDto> getProductsPage(int page, int size) {
        // 상품 페이지를 조회하기 위한 Pageable 객체 생성
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Product> productPage = productRepository.findAll(pageable);

        // Dto 로 변환

        List<Product> productList = productPage.getContent();
        List<ProductDto> productDtoList = new ArrayList<>();
        for (Product product : productList) {
            FileVo thumbnailImage = productImageService.getThumbnailImage(product.getId());
            List<FileVo> contentImages = productImageService.getContentImages(product.getId());

            ProductDto productDto = new ProductDto();
            productDto.setId(product.getId());
            productDto.setTitle(product.getTitle());
            productDto.setDescription(product.getDescription());
            productDto.setPrice(product.getPrice());
            productDto.setStock(product.getStock());
            productDto.setThumbnailImage(thumbnailImage);
            productDto.setContentImages(contentImages);

            productDtoList.add(productDto);
        }

        // Page 형태로 변환하여 반환
        return new PageImpl<>(productDtoList, pageable, productPage.getTotalElements());
    }

    public ProductDto getProductDetail(int productId) {
        // 상품 상세정보를 조회하기 위하여 상품 ID로 상품을 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductDetailNotFoundException("해당 상품이 존재하지 않습니다."));

        FileVo thumbnailImage = productImageService.getThumbnailImage(productId);
        List<FileVo> contentImages = productImageService.getContentImages(productId);

        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setTitle(product.getTitle());
        productDto.setDescription(product.getDescription());
        productDto.setPrice(product.getPrice());
        productDto.setStock(product.getStock());
        productDto.setThumbnailImage(thumbnailImage);
        productDto.setContentImages(contentImages);

        return productDto;
    }
}
