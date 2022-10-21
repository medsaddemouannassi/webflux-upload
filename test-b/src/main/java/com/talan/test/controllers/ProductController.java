package com.talan.test.controllers;

import com.talan.test.entity.Product;
import com.talan.test.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.springframework.http.MediaType.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin
public class ProductController {

    private final ProductService productService;

    @PostMapping
    Product save(@RequestParam String name,
                 @RequestParam String price,
                 @RequestParam MultipartFile image
    ) throws IOException {
        return productService.save(name, Double.parseDouble(price), image);
    }

    @GetMapping("{id}")
    Product getProductById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @GetMapping(path = "/images/{image}", produces = {IMAGE_PNG_VALUE, IMAGE_JPEG_VALUE, IMAGE_GIF_VALUE})
    byte[] getProductImage(@PathVariable String image) throws IOException {
        return Files.readAllBytes(Paths.get(System.getProperty("user.home") + "/images/" + image));
    }

    @GetMapping
    List<Product> getProducts() {
        return productService.findAllProducts();
    }

    @DeleteMapping("{id}")
    void deleteProduct(@PathVariable Long id) {
        productService.delete(id);
    }
}
