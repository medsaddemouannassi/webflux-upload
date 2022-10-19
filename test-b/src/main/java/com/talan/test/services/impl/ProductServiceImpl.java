package com.talan.test.services.impl;

import com.talan.test.dto.ProductDto;
import com.talan.test.entity.Product;
import com.talan.test.repositories.ProductRepo;
import com.talan.test.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Paths.get;
import static org.apache.http.entity.ContentType.*;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {
    public static final Path DIRECTORY = get(System.getProperty("user.home") + "/images/");

    private final ProductRepo productRepo;

    @Override
    public Mono<Product> save(String name, double price, Mono<FilePart> file) throws IOException {
        Product product = new Product();
        return file.switchIfEmpty(Mono.error(() -> new IllegalStateException("Cannot upload empty file"))).then(file.flatMap(filePart -> {
            if (!Arrays.asList(
                    IMAGE_JPEG.getMimeType(),
                    IMAGE_PNG.getMimeType(),
                    IMAGE_GIF.getMimeType()).contains(String.valueOf(filePart.headers().getContentType()))) {
                return Mono.error(new IllegalStateException("File must be an image [" + filePart.headers().getContentType() + "]"));
            }
            return Mono.just("Ok");
        })).then(file.flatMap(data -> {
            if (!exists(DIRECTORY)) {
                try {
                    createDirectories(DIRECTORY);
                } catch (IOException e) {
                    return Mono.error(new RuntimeException(e));
                }
            }
            product.setName(name);
            product.setPrice(price);
            product.setImage(data.filename());
            return data.transferTo(DIRECTORY.resolve(data.filename()));
        }).then(productRepo.save(product)));
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
}
