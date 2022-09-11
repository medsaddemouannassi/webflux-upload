package com.talan.test.controllers;

import com.talan.test.dto.ProductDto;
import com.talan.test.entity.Product;
import com.talan.test.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

import static org.springframework.http.MediaType.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin
public class ProductController {
    private final ProductService productService;

    @PostMapping
    Mono<Product> save(@RequestPart String name,
                       @RequestPart String price,
                       @RequestPart Mono<FilePart> image
    ) throws IOException {
        return productService.save(name, Double.parseDouble(price), image);
    }

    @GetMapping("{id}")
    Mono<ProductDto> getProductById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @GetMapping(path = "/images/{image}", produces = {IMAGE_PNG_VALUE, IMAGE_JPEG_VALUE, IMAGE_GIF_VALUE})
    byte[] getProductImage(@PathVariable String image) throws IOException {
        return Files.readAllBytes(Paths.get(System.getProperty("user.home") + "/images/" + image));
    }

    @GetMapping(path = "/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<ProductDto> getStream() {
        return Flux.zip(Flux.interval(Duration.ofMillis(1000)), productService.findAllProducts()).map(Tuple2::getT2).share();
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<ProductDto> getProducts() {
        return productService.findAllProducts();
    }

    @DeleteMapping("{id}")
    Mono<Void> deleteProduct(@PathVariable Long id) {
        return productService.delete(id);
    }
}
