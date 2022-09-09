package com.talan.test.services.impl;

import com.talan.test.dto.ProductDto;
import com.talan.test.entity.Product;
import com.talan.test.repositories.ProductRepo;
import com.talan.test.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Paths.get;
import static org.apache.http.entity.ContentType.*;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {
    public static final String DIRECTORY = System.getProperty("user.home") + "/images/";

    private final ProductRepo productRepo;

    @Override
    public Mono<Product> save(String name, double price, Mono<FilePart> file) throws IOException {
        // 1. Check if image is not empty
//        isFileEmpty(file);
        // 2. If file is an image
//        isImage(file);
        return file.flatMap(data -> {
            String filename = StringUtils.cleanPath(Objects.requireNonNull(data.filename()));
            Path fileStorage = get(DIRECTORY, filename).normalize();
            if (!exists(fileStorage)) {
                try {
                    createDirectories(fileStorage);
                } catch (IOException e) {
                    return Mono.error(new RuntimeException(e));
                }
            }
            data.transferTo(fileStorage.resolve(data.filename())).then();
            Product product = new Product();
            product.setName(name);
            product.setPrice(price);
            product.setImage(data.filename());
            return productRepo.save(product);
        });
    }

    @Override
    public Mono<ProductDto> findById(Long id) {
        return productRepo.findById(id).map(product -> ProductDto.builder()
                .id(product.getId())
                .productName(product.getName())
                .image(UriComponentsBuilder.newInstance().scheme("http").host("localhost").port(8080).path("api/products/images/" + product.getImage()).toUriString())
                .price(product.getPrice())
                .build());
    }

    @Override
    public Flux<ProductDto> findAllProducts() {
        return productRepo.findAll().map(product -> ProductDto.builder()
                .id(product.getId())
                .productName(product.getName())
                .image(UriComponentsBuilder.newInstance().scheme("http").host("localhost").port(8080).path("api/products/images/" + product.getImage()).toUriString())
                .price(product.getPrice())
                .build()
        ).onErrorStop();
    }

    @Override
    public Mono<Void> delete(Long id) {
        return productRepo.deleteById(id);
    }

    private void isImage(MultipartFile file) {
        if (!Arrays.asList(
                IMAGE_JPEG.getMimeType(),
                IMAGE_PNG.getMimeType(),
                IMAGE_GIF.getMimeType()).contains(file.getContentType())) {
            throw new IllegalStateException("File must be an image [" + file.getContentType() + "]");
        }
    }

    private void isFileEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalStateException("Cannot upload empty file [ " + file.getSize() + "]");
        }
    }
}
