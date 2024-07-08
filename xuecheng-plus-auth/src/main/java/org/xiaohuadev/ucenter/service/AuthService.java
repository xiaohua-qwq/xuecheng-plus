package org.xiaohuadev.ucenter.service;

import org.xiaohuadev.ucenter.model.dto.AuthParamsDto;
import org.xiaohuadev.ucenter.model.dto.XcUserExt;

/**
 * @description 认证service
 * @author Mr.M
 * @date 2022/9/29 12:10
 * @version 1.0
 */
public interface AuthService {

   /**
    * 认证方法
    * @param authParamsDto 认证参数
    * @return XcUser 用户信息
   */
   XcUserExt execute(AuthParamsDto authParamsDto);

}
