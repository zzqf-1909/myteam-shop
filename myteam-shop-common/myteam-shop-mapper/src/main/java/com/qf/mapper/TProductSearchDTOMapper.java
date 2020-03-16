package com.qf.mapper;

import dto.TProductSearchDTO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface TProductSearchDTOMapper {


    TProductSearchDTO selectById(String pid);

}
