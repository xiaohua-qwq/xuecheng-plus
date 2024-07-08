package org.xiaohuadev.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.xiaohuadev.ucenter.mapper.XcUserMapper;
import org.xiaohuadev.ucenter.model.dto.AuthParamsDto;
import org.xiaohuadev.ucenter.model.dto.XcUserExt;
import org.xiaohuadev.ucenter.model.po.XcUser;
import org.xiaohuadev.ucenter.service.AuthService;
import org.xiaohuadev.ucenter.service.WxAuthService;

import java.util.Map;

/**
 * 微信扫码认证
 */
@Service("wx_authservice")
public class WxAuthServiceImpl implements AuthService, WxAuthService {
    @Autowired
    private XcUserMapper xcUserMapper;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 认证方法
     *
     * @param authParamsDto 认证参数
     * @return XcUser 用户信息
     */
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        String username = authParamsDto.getUsername();
        XcUser xcUser = xcUserMapper.selectOne(Wrappers.<XcUser>lambdaQuery().eq(XcUser::getUsername, username));
        if (xcUser == null) throw new RuntimeException("空用户");


        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);
        return xcUserExt;
    }

    @Value("${weixin.appid}")
    String appid;
    @Value("${weixin.secret}")
    String secret;

    /**
     * 微信扫码认证: 申请令牌 携带令牌查询用户信息 将用户信息保存到数据库
     *
     * @param code 授权码
     * @return 用户信息
     */
    @Override
    public XcUser wxAuth(String code) {
        //申请令牌
        Map<String, String> accessToken = getAccessToken(code);

        //携带令牌查询用户信息


        //将用户信息保存到数据库
        return null;
    }

    //携带授权码申请令牌
    private Map<String, String> getAccessToken(String code) {
        String url_template = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        String url = String.format(url_template, appid, secret, code); //最终请求路径

        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, null, String.class);
        String result = exchange.getBody(); //响应结果
        //将resultJson转换为Map
        Map<String, String> map = JSON.parseObject(result, Map.class);
        if (map == null) throw new RuntimeException("请求微信登录服务异常");
        return map;
    }
}
