package online.wangxuan.dao;

import online.wangxuan.entity.Product;
import org.apache.ibatis.annotations.*;

/**
 * Created by wangxuan on 2017/6/21.
 */
// 这里只要加上@Mapper注解，Spring Boot在初始化mybatis时会自动加载该mapper类。
@Mapper
public interface ProductMapper {

    Product get(@Param("id") long id);

    void update(Product product);

    boolean delete(@Param("id") long id);
}
