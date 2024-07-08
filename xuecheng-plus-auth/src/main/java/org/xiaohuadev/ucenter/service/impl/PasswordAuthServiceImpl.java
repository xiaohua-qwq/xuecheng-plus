package org.xiaohuadev.ucenter.service.impl;

import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.xiaohuadev.ucenter.feignclient.CheckCodeClient;
import org.xiaohuadev.ucenter.mapper.XcUserMapper;
import org.xiaohuadev.ucenter.model.dto.AuthParamsDto;
import org.xiaohuadev.ucenter.model.dto.XcUserExt;
import org.xiaohuadev.ucenter.model.po.XcUser;
import org.xiaohuadev.ucenter.service.AuthService;

/**
 * 账号密码方式认证
 */
@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {
    @Autowired
    private XcUserMapper xcUserMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CheckCodeClient checkCodeClient;

    /**
     * 认证方法
     *
     * @param authParamsDto 认证参数
     * @return XcUser 用户信息
     */
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        //账号
        String username = authParamsDto.getUsername();

        //远程调用验证码服务 校验验证码
        String checkCode = authParamsDto.getCheckcode(); //输入的验证码
        String checkCodeKey = authParamsDto.getCheckcodekey(); //验证码在Redis中对应的key
        if (StringUtils.isEmpty(checkCode) || StringUtils.isEmpty(checkCodeKey)) {
            //验证码或RedisKey为空
            throw new RuntimeException("请输入验证码");
        }

        Boolean verify = checkCodeClient.verify(checkCodeKey, checkCode);
        if (verify == null || !verify) { //降级方法中会返回空 所以这里要判断是不是调用了降级方法
            throw new RuntimeException("验证码输入错误");
        }

        //账号是否存在
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (xcUser == null) throw new RuntimeException("账号不存在");

        //校验密码
        String passwordDB = xcUser.getPassword(); //正确密码
        String passwordForm = authParamsDto.getPassword(); //用户输入的密码
        boolean matches = passwordEncoder.matches(passwordForm, passwordDB);
        if (!matches) throw new RuntimeException("账号或密码错误");

        //组装返回值
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);
        return xcUserExt;
    }
}
