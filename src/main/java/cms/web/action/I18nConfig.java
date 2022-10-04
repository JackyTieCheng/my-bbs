package cms.web.action;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * 异常国际化文件
 * @author Gao
 *
 */
@Configuration
public class I18nConfig {

	@Primary
    @Bean(name = "messageSource")
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageBundle = new ReloadableResourceBundleMessageSource();
        messageBundle.setBasenames("classpath:i18n/errorMessages","classpath:i18n/springSecurityMessages");//即配置文件所在目录为 i18n，文件前缀为 errorMessages
        messageBundle.setDefaultEncoding("UTF-8");
        messageBundle.setFallbackToSystemLocale(false);//是否使用系统默认的编码  默认为true
        return messageBundle;
    }
}
