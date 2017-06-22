package online.wangxuan.controller;

import online.wangxuan.dao.ProductMapper;
import online.wangxuan.entity.Product;
import online.wangxuan.exception.ProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.web.bind.annotation.*;

/**
 * Created by wangxuan on 2017/6/21.
 */
@RestController // 该注解是@Controller和@ResponseBody的组合注解
@RequestMapping("/product")
public class ProductController {

    @Autowired(required = false)
    private ProductMapper productMapper;

    @GetMapping("/{id}") // 表示这是一个Get HTTP接口
    public Product getProductInfo(@PathVariable("id") Long productId) {
        return productMapper.get(productId);
    }

    @PutMapping("/{id}") // 表示这是一个Put HTTP接口
    public Product updateProductInfo(@PathVariable("id") Long productId, @RequestBody Product newProduct) {
        Product product = productMapper.get(productId);
        if (product == null) {
            throw new ProductNotFoundException(productId);
        }
        product.setName(newProduct.getName());
        product.setPrice(newProduct.getPrice());
        productMapper.update(product);
        return product;
    }

    @DeleteMapping("/{id}")
    public boolean deleteProduct(@PathVariable("id") Long productId) {
        return productMapper.delete(productId);
    }
}
