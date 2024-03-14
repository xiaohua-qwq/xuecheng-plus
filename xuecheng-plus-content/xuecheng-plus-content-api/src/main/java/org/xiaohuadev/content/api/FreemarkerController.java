package org.xiaohuadev.content.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * 返回预览页面视图
 */
@Controller //不使用@RestController是因为不需要返回JSON数据
public class FreemarkerController {
    @GetMapping("/testfreemarker")
    public ModelAndView test(){
        ModelAndView modelAndView = new ModelAndView();
        //指定模板中的${name}部分要填写的值
        modelAndView.addObject("name","小明");
        //指定模板为test.ftl
        modelAndView.setViewName("test");
        return modelAndView;
    }
}
