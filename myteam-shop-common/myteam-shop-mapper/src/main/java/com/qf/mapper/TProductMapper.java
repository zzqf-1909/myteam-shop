package com.qf.mapper;

import com.qf.entity.TProduct;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;


@Repository
@Mapper
public interface TProductMapper {

    TProduct selectByProductId(Integer pid);
}
