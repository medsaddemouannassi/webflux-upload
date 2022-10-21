package com.talan.test.services;

import com.talan.test.dto.ProductDto;
import com.talan.test.entity.Product;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductService {
    Product save(String name, double price, MultipartFile file) throws IOException;
    Product findById(Long id);
    List<Product> findAllProducts();
    void delete(Long id);
}
