package com.nageoffer.shortlink.gateway.filter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.nageoffer.shortlink.gateway.config.Config;
import com.nageoffer.shortlink.gateway.dto.GatewayErrorResult;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * SpringCloud Gateway Token 拦截器
 */
@Component
public class TokenValidateGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {

    private final StringRedisTemplate stringRedisTemplate;

    // 构造方法，注入 StringRedisTemplate 用于 Redis 操作
    public TokenValidateGatewayFilterFactory(StringRedisTemplate stringRedisTemplate) {
        super(Config.class);
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest(); // 获取请求
            String requestPath = request.getPath().toString(); // 获取请求路径
            String requestMethod = request.getMethod().name(); // 获取请求方法

            // 判断请求路径和方法是否在白名单内
            if (!isPathInWhiteList(requestPath, requestMethod, config.getWhitePathList())) {
                //不在白名单需要判断
                String username = request.getHeaders().getFirst("username"); // 获取请求头中的用户名
                String token = request.getHeaders().getFirst("token"); // 获取请求头中的 token
                Object userInfo;

                // 验证用户名和 token 是否存在，以及 Redis 中是否存在对应的用户信息
                if (StringUtils.hasText(username) && StringUtils.hasText(token)
                        && (userInfo = stringRedisTemplate.opsForHash().get("short-link:login:" + username, token)) != null) {
                    // 存在是正确请求
                    // 将用户信息转换为 JSON 对象
                    JSONObject userInfoJsonObject = JSON.parseObject(userInfo.toString());
                    // 修改请求头，添加用户ID和真实姓名
                    ServerHttpRequest.Builder builder = exchange.getRequest().mutate().headers(httpHeaders -> {
                        httpHeaders.set("userId", userInfoJsonObject.getString("id"));
                        httpHeaders.set("realName", URLEncoder.encode(userInfoJsonObject.getString("realName"), StandardCharsets.UTF_8));
                    });
                    // 继续执行下一个过滤器
                    return chain.filter(exchange.mutate().request(builder.build()).build());
                }
                //不存在是错误请求
                // 如果 token 验证失败，返回 401 未授权状态
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);  // 设置响应状态为 401 未授权
                // 使用 Mono.fromSupplier 异步生成响应数据
                return response.writeWith(Mono.fromSupplier(() -> {
                    DataBufferFactory bufferFactory = response.bufferFactory();  // 获取数据缓冲工厂，用于创建响应数据
                    // 构建错误信息对象，包含状态码和错误信息
                    GatewayErrorResult resultMessage = GatewayErrorResult.builder()
                            .status(HttpStatus.UNAUTHORIZED.value())  // 设置错误状态码为 401
                            .message("Token validation error")  // 设置错误消息为 "Token validation error"
                            .build();
                    // 将错误信息对象转换为 JSON 字符串并封装为数据缓冲区返回
                    return bufferFactory.wrap(JSON.toJSONString(resultMessage).getBytes(StandardCharsets.UTF_8));
                }));
            }

            // 如果在白名单内，继续执行下一个过滤器
            return chain.filter(exchange);
        };
    }

    // 判断请求路径和方法是否在白名单内
    private boolean isPathInWhiteList(String requestPath, String requestMethod, List<String> whitePathList) {
        return (!CollectionUtils.isEmpty(whitePathList) && whitePathList.stream().anyMatch(requestPath::startsWith))
                || (Objects.equals(requestPath, "/api/short-link/admin/v1/user") && Objects.equals(requestMethod, "POST"));
    }
}
