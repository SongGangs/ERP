package com.jsh.erp.config;

import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.util.Optional;

/**
 * 自定义租户拦截器
 * <p>解决多表查询时没有别名导致的列歧义问题，通过表别名或表名限定 tenant_id 列</p>
 */
public class CustomTenantLineInnerInterceptor extends TenantLineInnerInterceptor {

    /**
     * 获取带别名或表名限定的租户字段列
     *
     * @param table SQL 表对象
     * @return 限定后的租户列对象
     */
    @Override
    protected Column getAliasColumn(Table table) {
        String tableQualifier = Optional.ofNullable(table.getAlias())
                .map(Alias::getName)
                .orElseGet(table::getName);

        String columnName = String.format("%s.%s", tableQualifier, getTenantLineHandler().getTenantIdColumn());
        return new Column(columnName);
    }
}
