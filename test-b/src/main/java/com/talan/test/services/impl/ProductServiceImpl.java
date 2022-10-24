package com.talan.test.services.impl;

import com.talan.test.entity.Product;
import com.talan.test.repositories.ProductRepo;
import com.talan.test.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.Files.*;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.http.entity.ContentType.*;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {
    public static final Path DIRECTORY = get(System.getProperty("user.home") + "/images/");

    private final ProductRepo productRepo;

    @Override
    public Product save(String name, double price, MultipartFile file) throws IOException {
        // 1. Check if image is not empty
        isFileEmpty(file);
        // 2. If file is an image
        isImage(file);

        if (!exists(DIRECTORY)) createDirectories(DIRECTORY);
        String filename = String.format("%s", file.getOriginalFilename());
        Path fileStorage = get(String.valueOf(DIRECTORY), filename).toAbsolutePath().normalize();
        copy(file.getInputStream(), fileStorage, REPLACE_EXISTING);

        return productRepo.save(Product.builder()
                .name(name)
                .price(price)
                .image(file.getOriginalFilename())
                .build());
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
            throw new IllegalStateException("Cannot upload empty file [" + file.getSize() + "]");
        }
    }

    @Override
    public Product findById(Long id) {
        return productRepo.findById(id).orElse(null);
    }

    @Override
    public List<Product> findAllProducts() {
        final List<Product> all = productRepo.findAll();
        all.forEach(product -> product.setImage(UriComponentsBuilder.newInstance().scheme("http").host("localhost").port(8080).path("api/products/images/" + product.getImage()).toUriString()));
        return all;
    }

    @Override
    public void delete(Long id) {
        productRepo.deleteById(id);
    }
}
