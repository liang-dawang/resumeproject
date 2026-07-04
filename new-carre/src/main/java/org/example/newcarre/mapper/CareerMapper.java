package org.example.newcarre.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.newcarre.dto.CareerData;
import org.mybatis.spring.annotation.MapperScan;

@Mapper
public interface CareerMapper extends BaseMapper<CareerData> {


}
