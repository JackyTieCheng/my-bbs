package cms.web.action.template;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import cms.utils.PathUtil;
import cms.web.taglib.Base64Tag;
import cms.web.taglib.EncodeURL;
import freemarker.core.TemplateClassResolver;
import freemarker.template.TemplateException;

/**
 * Freemarker配置
 *
 */
@Configuration
public class FreemarkerConfig{
	@Resource TemplateCustomMethods templateCustomMethods;
	@Resource TemplateIncludeMethods templateIncludeMethods;
	@Resource TemplateObjectMethods templateObjectMethods;
	@Resource Base64Tag base64Tag;
	@Resource EncodeURL encodeURL;
	
	@Bean
    public FreeMarkerConfigurer freeMarkerConfigurer() throws IOException, TemplateException {
		MyFreeMarkerConfigurer factory = new MyFreeMarkerConfigurer();
		
		if(PathUtil.isStartupFromJar()){//jar启动
			factory.setTemplateLoaderPaths("file:"+PathUtil.defaultExternalDirectory()+File.separator+"WEB-INF"+File.separator+"foregroundView"+File.separator,"classpath:WEB-INF/backstageView","classpath:WEB-INF/data/install");//模板路径
		}else{//IDE启动
			factory.setTemplateLoaderPaths("classpath:WEB-INF/foregroundView","classpath:WEB-INF/backstageView","classpath:WEB-INF/data/install");//模板路径 classpath:static/view/
		}
		
		
       // factory.setTemplateLoaderPaths("classpath:templates/","classpath:manage/");//模板路径 classpath:static/view/
		factory.setDefaultEncoding("UTF-8");//编码设置
        //factory.setPreferFileSystemAccess(false);//是否优先从从文件系统中获取模板，以支持热加载，默认为true。  要继承父模板，读取父模板内容，需要设置prefer-file-system-access=false，否则会报404无法找到视图。并且设置为false后，数据热加载测试依然可以正常运行

        freemarker.template.Configuration configuration = factory.createConfiguration();
        
        configuration.setTemplateUpdateDelayMilliseconds(5000); //刷新模板的周期，单位为毫秒;如果模板不经常更新，此属性设置更新延迟时间
        configuration.setDefaultEncoding("UTF-8");//模板的编码格式
        configuration.setLocale(Locale.CHINA);//本地化设置
        configuration.setClassicCompatible(true);//空值处理 模板解析引擎是否工作在“Classic Compatibile”模式下
        configuration.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");//时间格式化
        configuration.setTimeFormat("HH:mm:ss");//时间格式化 
        configuration.setNumberFormat("0.######");//设置数字格式 以免出现 000.00
        configuration.setBooleanFormat("true,false");//布尔值格式化输出的格式
        configuration.setWhitespaceStripping(true);//剥去空白区域
        configuration.setTagSyntax(freemarker.template.Configuration.AUTO_DETECT_TAG_SYNTAX);//tag_syntax = square_bracket||auto_detect 设置标签类型 两种：[] 和 <> 。[] 这种标记解析要快些
        configuration.setURLEscapingCharset("UTF-8");//URL编码的字符集
        
        configuration.setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);//禁止解析任何类
        
        configuration.setIncompatibleImprovements(freemarker.template.Configuration.VERSION_2_3_31);
        
        configuration.setSharedVariable("function", templateCustomMethods);//自定义方法
        configuration.setSharedVariable("include", templateIncludeMethods);//自定义引入指令
        configuration.setSharedVariable("object", templateObjectMethods);//自定义调用对象
        factory.setConfiguration(configuration);
       
        /**
        Properties settings = new Properties();
       // settings.put("template_update_delay", "5");//刷新模板的周期，单位为秒;如果模板不经常更新，此属性设置更新延迟时间
      //  settings.put("default_encoding", "UTF-8");//模板的编码格式
      //  settings.put("locale", "zh_CN");//本地化设置
     //   settings.put("classic_compatible", true);//空值处理
     //   settings.put("datetime_format", "yyyy-MM-dd HH:mm:ss");//时间格式化
     //   settings.put("time_format", "HH:mm:ss");//时间格式化 
     //   settings.put("number_format", "0.######");//设置数字格式 以免出现 000.00
     //   settings.put("boolean_format", "true,false");//布尔值格式化输出的格式
    //    settings.put("whitespace_stripping", true);//剥去空白区域
        settings.put("tag_syntax", "auto_detect");//tag_syntax = square_bracket||auto_detect 设置标签类型 两种：[] 和 <> 。[] 这种标记解析要快些
        settings.put("url_escaping_charset", "UTF-8");//URL编码的字符集
        //settings.put("template_exception_handler", "ignore");//模板异常处理
        factory.setFreemarkerSettings(settings);
        
        
        
        Map<String, Object> sharedVariables = new HashMap<String, Object>();
        
        //自定义方法
        sharedVariables.put("function", templateCustomMethods);
        //自定义引入指令
      //  sharedVariables.put("include", new TemplateIncludeMethods());
        //自定义调用对象
        sharedVariables.put("object", templateObjectMethods);
        //从模板中访问类的公有字段 
        //sharedVariables.put("objectWrapper", new DefaultObjectWrapper());
        

        
        factory.setFreemarkerVariables(sharedVariables);
		**/
        return factory;
    }
	

	

	
	/**
	 * FreeMarker页面解析器
	 * @return
	 */
	@Bean
    public ViewResolver freemarkerViewResolver() {
        FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
        //resolver.setCache(false);
        
        resolver.setOrder(1);
        resolver.setSuffix(".html");//后缀
        resolver.setContentType("text/html;charset=UTF-8");//页面编码
        resolver.setViewClass(FreeMarkerView.class);
        
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("encodeBase64URL", base64Tag);//自定义引入函数 url安全转义
       // attributes.put("encodeURL", encodeURL);//实现response.encodeURL(url)功能
        resolver.setAttributesMap(attributes);
       // resolver.setRequestContextAttribute("request");
       // resolver.setExposeRequestAttributes(true);
       // resolver.setExposeSessionAttributes(true);
       
        
        

      
        /**
        //添加自定义解析器
        resolver.setAttributesMap(new FreemarkerStaticModels(new HashMap<String, String>()
        {{
            put("Json", "com.skindow.util.MyJson");
        }}));**/
        return resolver;
    }
}
