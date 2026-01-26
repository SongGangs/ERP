package com.jsh.erp.datasource.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jsh.erp.datasource.entities.DepotCheckHead;
import org.apache.ibatis.annotations.Mapper;

/**
 * 盘点主表Mapper接口
 */
@Mapper
public interface DepotCheckHeadMapperEx extends BaseMapper<DepotCheckHead> {
    // 继承 BaseMapper 后，自动拥有基础的 CRUD 方法
    // 复杂查询可以在 Service 层使用 LambdaQueryWrapper
}