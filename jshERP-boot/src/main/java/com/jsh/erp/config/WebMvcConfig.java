package com.jsh.erp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.RequestParamMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Web MVC 配置
 * <p>解决 Spring Boot 升级后 @RequestParam 默认 required=true 导致的兼容性问题</p>
 * <p>通过自定义参数解析器，允许请求参数缺失或转换为 null，避免抛出异常</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    /**
     * 初始化时替换默认的 RequestParam 参数解析器
     * <p>将所有 RequestParamMethodArgumentResolver 替换为自定义实现，允许参数为空</p>
     */
    @PostConstruct
    public void configureArgumentResolvers() {
        List<HandlerMethodArgumentResolver> resolvers = requestMappingHandlerAdapter.getArgumentResolvers();
        if (Objects.isNull(resolvers)) {
            return;
        }

        List<HandlerMethodArgumentResolver> customResolvers = resolvers.stream()
                .map(this::wrapResolver)
                .collect(Collectors.toList());

        requestMappingHandlerAdapter.setArgumentResolvers(customResolvers);
    }

    /**
     * 包装解析器，将 RequestParamMethodArgumentResolver 替换为自定义实现
     *
     * @param resolver 原始解析器
     * @return 包装后的解析器
     */
    private HandlerMethodArgumentResolver wrapResolver(HandlerMethodArgumentResolver resolver) {
        return resolver instanceof RequestParamMethodArgumentResolver
                ? new OptionalRequestParamResolver()
                : resolver;
    }

    /**
     * 自定义 RequestParam 参数解析器
     * <p>允许参数缺失和转换失败，不抛出异常，提高系统容错性</p>
     */
    private static class OptionalRequestParamResolver extends RequestParamMethodArgumentResolver {

        private static final boolean USE_DEFAULT_RESOLUTION = false;

        public OptionalRequestParamResolver() {
            super(USE_DEFAULT_RESOLUTION);
        }

        /**
         * 处理参数缺失情况，不抛出异常
         *
         * @param name      参数名称
         * @param parameter 方法参数
         */
        @Override
        protected void handleMissingValue(@NonNull String name, @NonNull MethodParameter parameter) {
            // 允许参数缺失，不抛出异常
        }

        /**
         * 处理参数转换后为 null 的情况，不抛出异常
         *
         * @param name      参数名称
         * @param parameter 方法参数
         * @param request   Web 请求
         */
        @Override
        protected void handleMissingValueAfterConversion(
                @NonNull String name,
                @NonNull MethodParameter parameter,
                @NonNull NativeWebRequest request) {
            // 允许参数转换为 null，不抛出异常
        }
    }
}
