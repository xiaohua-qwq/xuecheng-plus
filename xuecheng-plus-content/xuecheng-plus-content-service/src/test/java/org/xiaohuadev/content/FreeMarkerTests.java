package org.xiaohuadev.content;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.xiaohuadev.content.mapper.TeachplanMapper;
import org.xiaohuadev.content.model.dto.CoursePreviewDto;
import org.xiaohuadev.content.model.dto.TeachplanDto;
import org.xiaohuadev.content.service.CoursePublishService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

@SpringBootTest
public class FreeMarkerTests {
    @Autowired
    private CoursePublishService coursePublishService;

    @Test
    void testFreeMarker() throws IOException, TemplateException {
        //获取freemarker的configuration
        Configuration configuration = new Configuration(Configuration.getVersion());

        //获取类路径
        String classPath = this.getClass().getResource("/").getPath();
        //指定模板目录
        configuration.setDirectoryForTemplateLoading(new File(classPath + "\\templates\\"));
        //设置编码
        configuration.setDefaultEncoding("utf-8");

        //获取template模板
        Template template = configuration.getTemplate("course_template.ftl");

        CoursePreviewDto coursePreviewDto = coursePublishService.preview(120L);
        HashMap<String, Object> map = new HashMap<>();
        map.put("model", coursePreviewDto);

        //Template template, Object model
        String htmlString = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

        //输出文件
        InputStream inputStream = IOUtils.toInputStream(htmlString, "utf-8");
        FileOutputStream outputStream = new FileOutputStream(new File("C:\\Users\\PC\\Desktop\\test.html"));
        IOUtils.copy(inputStream, outputStream); //使用流写出html文件
    }
}
