// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.hydralab.center.config;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.microsoft.hydralab.center.interceptor.BaseInterceptor;
import com.microsoft.hydralab.center.interceptor.CorsInterceptor;
import com.microsoft.hydralab.common.util.Const;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * @author shbu
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    @Resource
    private BaseInterceptor baseInterceptor;
    @Resource
    private CorsInterceptor corsInterceptor;
    @Resource
    private FastJsonHttpMessageConverter fastJsonHttpMessageConverter;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/portal/").setViewName("forward:" + Const.FrontEndPath.INDEX_PATH);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(baseInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/dist/**", "/store/**", "/static/**", Const.FrontEndPath.SWAGGER_DOC_PATH);
        registry.addInterceptor(corsInterceptor)
                .addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/portal/**")
                .addResourceLocations("classpath:/static/dist/");
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/favicon.ico");
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // Sequence should be kept, currently this converter is only used for /v3/api-docs endpoint to avoid malformed content which should be in json format
        // https://springdoc.org/#why-am-i-getting-an-error-swagger-ui-unable-to-render-definition-when-overriding-the-default-spring-registered-httpmessageconverter
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(fastJsonHttpMessageConverter);
    }

}
