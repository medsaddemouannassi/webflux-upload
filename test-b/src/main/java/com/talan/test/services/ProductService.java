package com.talan.test.services;

import com.talan.test.dto.ProductDto;
import com.talan.test.entity.Product;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

public interface ProductService {
    Mono<Product> save(String name, double price, Mono<FilePart> file) throws IOException;
    Mono<ProductDto> findById(Long id);
    Flux<ProductDto> findAllProducts();
    Mono<Void> delete(Long id);
}
