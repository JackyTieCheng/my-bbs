package cms.web.filter;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import cms.utils.PathUtil;

/**
 * 拦截器
 * @author Gao
 *
 */
@Configuration
//@EnableWebMvc//本注解会关闭默认配置
public class WebConfig implements WebMvcConfigurer{
	@Autowired
    private TempletesInterceptor templetesInterceptor;
	@Resource TaskExecutor taskExecutor;//多线程

	/**
	 * 必须配置application.yml中的spring: resources: addMappings: false 属性才不会出错
	 * @return
	 */
	@Bean
	public SimpleUrlHandlerMapping simpleUrlHandlerMapping() {
	    SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
	   // mapping.setOrder(300);
	    mapping.setOrder(Integer.MAX_VALUE);

	    Properties urlProperties = new Properties();
	    urlProperties.put("/**", "blankAction");//cms.web.action.common.BlankAction.java

	    mapping.setMappings(urlProperties);
	    mapping.setInterceptors(templetesInterceptor);

	    return mapping;
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		//默认值为false。值为true时,"/user" 就会匹配 "/user.*",也就是说,"/user.html" 的请求会被 "/user" 的 Controller所拦截.
	    configurer.setUseSuffixPatternMatch(true);

	    //默认值为true。 值为true时, "/user" 和 "/user/" 都会匹配到 "/user"的Controller
	    configurer.setUseTrailingSlashMatch(true);
	}
	
    /**
     * 增加处理静态资源的Handler
     * 源码 org.springframework.boot.autoconfigure.web.ResourceProperties CLASSPATH_RESOURCE_LOCATIONS
     * 自定义静态资源目录  越靠前的配置优先级越高
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    	registry.setOrder(100).addResourceHandler("/backstage/**")
        .addResourceLocations("classpath:/static/backstage/");
    	
    	if(PathUtil.isStartupFromJar()){//jar启动
    		registry.setOrder(110).addResourceHandler("/common/**")
            .addResourceLocations("file:"+PathUtil.defaultExternalDirectory()+File.separator+"common"+File.separator);
		}else{//IDE启动
			registry.setOrder(110).addResourceHandler("/common/**")
	        .addResourceLocations("classpath:/static/common/");
		}
    	
    	registry.setOrder(120).addResourceHandler("/file/**")
        .addResourceLocations("file:"+PathUtil.defaultExternalDirectory()+File.separator+"file"+File.separator);
    	
    	registry.setOrder(130).addResourceHandler("/robots.txt")
        .addResourceLocations("classpath:/static/robots.txt");
    	/**
    	registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/META-INF/resources/")
                .addResourceLocations("classpath:/resources/")
                .addResourceLocations("classpath:/static/")
                .addResourceLocations("classpath:/public/");
               // .addResourceLocations("file:E:/myimgs/");
    //	super.addResourceHandlers(registry);**/
    }

   
    /**
     * 参考org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport.java 的 addDefaultHttpMessageConverters方法
     * 
     * https://github.com/spring-projects/spring-framework/blob/708e61a7efbe727f91cc385c8d70a31f2fb0e972/spring-webmvc/src/main/java/org/springframework/web/servlet/config/annotation/WebMvcConfigurationSupport.java
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    	converters.clear();//清空默认添加的转换器
    	
    	//文件下载配置
        converters.add(new ByteArrayHttpMessageConverter());//读写二进制格式数据
        
    	
    	//Http消息转换器
        StringHttpMessageConverter html_converter  = new StringHttpMessageConverter();//读写字符串格式数据
        html_converter.setSupportedMediaTypes(Arrays.asList(new MediaType[] {
        	      new MediaType("text", "html", Charset.forName("UTF-8")),
        	  }));
        converters.add(html_converter);
    	
        
    	
        //Json转换器
        MappingJackson2HttpMessageConverter json_converter = new MappingJackson2HttpMessageConverter();
        json_converter.setSupportedMediaTypes(Arrays.asList(
        		new MediaType[] {
              	      new MediaType("application", "json", Charset.forName("UTF-8")),
        		}));
        converters.add(json_converter);
        
        
       // ByteArrayHttpMessageConverter: 读写二进制格式数据 
       // StringHttpMessageConverter： 读写字符串格式数据
       // ResourceHttpMessageConverter：读写资源文件数据
       // FormHttpMessageConverter： 读写form提交的数据（能读取的数据格式为 application/x-www-form-urlencoded，不能读取multipart/form-data格式数据）；可以写入application/x-www-from-urlencoded和multipart/form-data格式的数据；
       // MappingJacksonHttpMessageConverter: 负读写json格式的数据；
       // SourceHttpMessageConverter： 读写 xml 中javax.xml.transform.Source定义的数据
       // Jaxb2RootElementHttpMessageConverter: 读写xml 标签格式的数据
       // AtomFeedHttpMessageConverter: 读写Atom格式的数据
       // RssChannelHttpMessageConverter: 读写RSS格式的数据
    }
    
    /**
     * 配置异步请求处理选项
     * 
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        long timeout = 24 * 60 * 60 * 1000;//24小时
        WebMvcConfigurer.super.configureAsyncSupport(configurer);
        configurer.setDefaultTimeout(timeout);//超时时间，单位/毫秒;  -1表示无限制;  86400000表示24小时; 如果未设置此值，则使用基础实现的默认超时，例如，在带有Servlet 3的Tomcat上为10秒。
        configurer.setTaskExecutor((ThreadPoolTaskExecutor)taskExecutor);//指定自定义线程池
        
       // configurer.registerDeferredResultInterceptors(this.timeoutDeferredTimeoutInterceptor());//注册异步拦截器
    }
    
    
    
    //@Bean
	//public TimeoutDeferredResultProcessingInterceptor timeoutDeferredTimeoutInterceptor() {
	//	return new TimeoutDeferredResultProcessingInterceptor();
	//}
}
