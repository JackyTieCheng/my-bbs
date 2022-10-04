package cms.web.filter;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * 注册MVC自定义处理类
 * @author Gao
 *
 */
@Configuration
public class WebMvcRegistrationsConfig implements WebMvcRegistrations{
	@Autowired
    private TempletesInterceptor templetesInterceptor;
	
	/**
	 * 请求url(spring的url)映射到control的配置
	 */
	@Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
		RequestMappingHandlerMapping requestMappingHandlerMapping = new RequestMappingHandlerMapping();
		requestMappingHandlerMapping.setInterceptors(templetesInterceptor);//不拦截静态文件

	    return requestMappingHandlerMapping;
    }

}
