package com.example.spring_security2.config.server;

import com.example.spring_security2.config.jwt.JwtTokenEnhancer;
import com.example.spring_security2.service.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * 授权服务器
 * @author Slxin
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    @Qualifier("jwtTokenStore")
    private TokenStore tokenStore;
    @Autowired
    private JwtAccessTokenConverter jwtAccessTokenConverter;
    @Autowired
    private JwtTokenEnhancer jwtTokenEnhancer;

//    @Autowired
//    @Qualifier("redisTokenStore") //value=注入bean的方法名
//    private TokenStore tokenStore;

    /**
     * 密码模式
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        //导入chain,chain中添加enhancer内容
        TokenEnhancerChain chain = new TokenEnhancerChain();
        List<TokenEnhancer> delegates = new ArrayList<>();
        delegates.add(jwtTokenEnhancer);
        delegates.add(jwtAccessTokenConverter);
        chain.setTokenEnhancers(delegates);

        endpoints.authenticationManager(authenticationManager)
        .userDetailsService(userDetailsService)
        .tokenStore(tokenStore)
        .accessTokenConverter(jwtAccessTokenConverter)
        .tokenEnhancer(chain)
        ;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("client")//客户端id
                .secret(passwordEncoder.encode("123456"))//密钥
                .redirectUris("http://localhost:8081/login") //重定向
                .scopes("all")//授权范围
                .authorizedGrantTypes("authorization_code","password","refresh_token")//授权模式+刷新令牌
                .accessTokenValiditySeconds(60) //access token失效时间
                .refreshTokenValiditySeconds(600) //刷新令牌失效时间
//                .autoApprove() //自动授权
                ;
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        //获取密钥必须要身份验证，单点登录必须配置
        security.tokenKeyAccess("isAuthenticated()");
    }
}
