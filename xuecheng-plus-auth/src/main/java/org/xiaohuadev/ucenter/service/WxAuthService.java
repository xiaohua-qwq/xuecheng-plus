package org.xiaohuadev.ucenter.service;

import org.xiaohuadev.ucenter.model.po.XcUser;

/**
 * 微信扫码接入
 */
public interface WxAuthService {

    /**
     * 微信扫码认证: 申请令牌 携带令牌查询用户信息 将用户信息保存到数据库
     *
     * @param code 授权码
     * @return 用户信息
     */
    public XcUser wxAuth(String code);

}
