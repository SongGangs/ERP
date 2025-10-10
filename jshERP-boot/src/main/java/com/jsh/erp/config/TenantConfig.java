package com.jsh.erp.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.jsh.erp.utils.Tools;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * MyBatis-Plus 租户配置
 * <p>配置租户拦截器和分页插件，实现多租户数据隔离</p>
 */
@Configuration
public class TenantConfig {

    /** 租户字段名称 */
    private static final String TENANT_ID_COLUMN = "tenant_id";

    /** 超级管理员租户 ID */
    private static final Long SUPER_ADMIN_TENANT_ID = 0L;

    /** 需要忽略租户过滤的表集合 */
    private static final Set<String> IGNORED_TABLES = new HashSet<>(Arrays.asList(
            "jsh_sequence",
            "jsh_function",
            "jsh_platform_config",
            "jsh_tenant"
    ));

    /**
     * 配置 MyBatis-Plus 拦截器
     *
     * @param request HTTP 请求对象
     * @return MyBatis-Plus 拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(HttpServletRequest request) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 添加租户拦截器
        interceptor.addInnerInterceptor(createTenantLineInterceptor(request));

        // 添加分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());

        return interceptor;
    }

    /**
     * 创建租户拦截器
     *
     * @param request HTTP 请求对象
     * @return 自定义租户拦截器
     */
    private CustomTenantLineInnerInterceptor createTenantLineInterceptor(HttpServletRequest request) {
        CustomTenantLineInnerInterceptor interceptor = new CustomTenantLineInnerInterceptor();
        interceptor.setTenantLineHandler(new TenantLineHandler() {

            @Override
            public Expression getTenantId() {
                Long tenantId = extractTenantId(request);
                return isSuperAdmin(tenantId) ? null : new LongValue(tenantId);
            }

            @Override
            public String getTenantIdColumn() {
                return TENANT_ID_COLUMN;
            }

            @Override
            public boolean ignoreTable(@NonNull String tableName) {
                Long tenantId = extractTenantId(request);
                // 超级管理员不过滤任何表
                if (isSuperAdmin(tenantId)) {
                    return true;
                }
                // 普通租户过滤指定的表
                return IGNORED_TABLES.contains(tableName);
            }
        });
        return interceptor;
    }

    /**
     * 从请求中提取租户 ID
     *
     * @param request HTTP 请求对象
     * @return 租户 ID
     */
    private Long extractTenantId(HttpServletRequest request) {
        String token = request.getHeader("X-Access-Token");
        return Tools.getTenantIdByToken(token);
    }

    /**
     * 判断是否为超级管理员
     *
     * @param tenantId 租户 ID
     * @return 是否为超级管理员
     */
    private boolean isSuperAdmin(Long tenantId) {
        return Objects.equals(SUPER_ADMIN_TENANT_ID, tenantId);
    }

    /**
     * 配置 Mapper 扫描器
     * <p>扫描指定包下的所有 Mapper 接口</p>
     *
     * @return Mapper 扫描配置器
     */
    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer configurer = new MapperScannerConfigurer();
        configurer.setBasePackage("com.jsh.erp.datasource.mappers*");
        return configurer;
    }
}
