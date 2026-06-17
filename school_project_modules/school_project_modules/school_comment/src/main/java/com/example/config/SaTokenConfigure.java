package com.example.config;

import com.example.entity.WhiteListConfig;
import com.example.entity.BlackListConfig;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Resource
    private WhiteListConfig whiteListConfig;

    @Resource
    private BlackListConfig blackListConfig;

    @Value("${file.path}")
    private String filePath;

    @Value("${file.url-prefix}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler(urlPrefix + "**").addResourceLocations("file:" + filePath);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {

                    // ===================== 修复：兼容老版本，判断上下文是否存在 =====================
                    try {
                        // 尝试获取 request，如果报错就直接跳过
                        SaHolder.getRequest();
                    } catch (Exception e) {
                        return;
                    }
                    // ============================================================================

                    // 1. 收集所有需要放行的路径（白名单）
                    List<String> freePaths = new ArrayList<>();

                    // 固定放行的接口文档
                    freePaths.add("/doc.html");
                    freePaths.add("/doc.html/**");
                    freePaths.add("/swagger-resources/**");
                    freePaths.add("/webjars/**");
                    freePaths.add("/login/in");
                    freePaths.add("/v2/api-docs");
                    freePaths.add("/swagger-ui.html");
                    freePaths.add("/error");
                    freePaths.add("/**/*.jpg");
                    freePaths.add("/**/*.png");
                    freePaths.add("/**/*.jpeg");
                    freePaths.add("/**/*.gif");
                    freePaths.add("/static/**");
                    freePaths.add(urlPrefix + "**");
                    freePaths.add("/common/upload");
                    freePaths.add("/book/banner");


                    // 业务白名单（从配置文件读取）
                    List<String> whitePaths = whiteListConfig.getPaths();
                    if (whitePaths != null && !whitePaths.isEmpty()) {
                        freePaths.addAll(whitePaths);
                    }

                    // 2. 黑名单路径
                    List<String> blackPaths = blackListConfig.getPaths();

                    // 3. 先处理黑名单（使用 SaRouter 直接匹配）
                    if (blackPaths != null && !blackPaths.isEmpty()) {
                        SaRouter.match(blackPaths).check(r -> {
                            throw new RuntimeException("该路径已被拉黑，禁止访问");
                        });
                    }

                    // 4. 处理白名单和登录验证
                    // 匹配所有路径，排除白名单，然后检查登录
                    SaRouter.match("/**")
                            .notMatch(freePaths.toArray(new String[0]))
                            .check(r -> StpUtil.checkLogin());

                }))
                .addPathPatterns("/**")
                .excludePathPatterns("/book/ai/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}