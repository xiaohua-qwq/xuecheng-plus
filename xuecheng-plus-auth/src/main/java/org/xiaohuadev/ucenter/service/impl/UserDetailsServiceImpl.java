package org.xiaohuadev.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.xiaohuadev.ucenter.mapper.XcUserMapper;
import org.xiaohuadev.ucenter.model.dto.AuthParamsDto;
import org.xiaohuadev.ucenter.model.dto.XcUserExt;
import org.xiaohuadev.ucenter.service.AuthService;


@Slf4j
@Component //实现了UserDetailsService这个接口 因此要成为一个Bean
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private XcUserMapper xcUserMapper;
    @Autowired
    private ApplicationContext applicationContext; //Spring容器

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        //将传入的Json转为AuthParamsDto对象
        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            throw new RuntimeException("认证参数不符合要求");
        }

        //获取认证方式 通过认证方式名拼接出对应服务的Bean名
        String authType = authParamsDto.getAuthType();
        String beanName = authType + "_authservice";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);

        XcUserExt xcUserExt = authService.execute(authParamsDto);
        return getUserPrincipal(xcUserExt);
    }

    public UserDetails getUserPrincipal(XcUserExt user) {
        //封装返回对象 authorities是权限
        String[] authorities = {"root"};
        //将用户信息封装成JSON返回
        user.setPassword(null);
        String userJsonString = JSON.toJSONString(user);
        return User.withUsername(userJsonString).password(user.getPassword()).authorities(authorities).build();
    }
}
