package cms.web.action;


import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;


/**
 * 验让码配置
 * @author Gao
 *
 */
@Configuration
public class KaptchaConfig {
	/**
	 * 是否有边框 默认为true 我们可以自己设置yes，no
	 */
	@Value("${kaptcha.border}")
	private String border;
	
	/**
	 * 边框颜色 默认为Color.BLACKkaptcha.border.thickness 边框粗细度 默认为1
	 */
	@Value("${kaptcha.border.color}")
	private String borderColor;
	
	/**
	 * 验证码文本字符颜色 默认为Color.BLACK
	 */
	@Value("${kaptcha.textproducer.font.color}")
	private String textproducerFontColor;

	/**
	 * 图片宽度 默认为200
	 */
	@Value("${kaptcha.image.width}")
	private String imageWidth;
	
	/**
	 * 图片高度 默认为50
	 */
	@Value("${kaptcha.image.height}")
	private String imageHeight;
	
	/**
	 * 文本字符间距 默认为2
	 */
	@Value("${kaptcha.textproducer.char.space}")
	private String textproducerCharSpace;
	
	/**
	 * 文本字符大小 默认为40
	 */
	@Value("${kaptcha.textproducer.font.size}")
	private String textproducerFontSize;
	
	/**
	 * 噪点颜色 默认为Color.BLACK black
	 */
	@Value("${kaptcha.noise.color}")
	private String noiseColor;            
                   
	/**
	 * 文本字符渲染
	 */
	@Value("${kaptcha.word.impl}")
	private String wordImpl;    
	
	/**
	 * 图片样式  com.google.code.kaptcha.impl.WaterRipple水纹     com.google.code.kaptcha.impl.ShadowGimpy阴影效果   cms.web.action.FishEyeGimpy鱼眼效果
	 */
	@Value("${kaptcha.obscurificator.impl}")
	private String obscurificatorImpl; 
	
	/**
	 * 背景颜色渐变，开始颜色
	 */
	@Value("${kaptcha.background.clear.from}")
	private String backgroundClearFrom; 
                   
	/**
	 * 背景颜色渐变，结束颜色
	 */
	@Value("${kaptcha.background.clear.to}")
	private String backgroundClearTo;       
	
	/**
	 * 放入session的KEY名称
	 */
	@Value("${kaptcha.session.key}")
	private String sessionKey;  
	
	/**
	 * 验证码文本字符长度 默认为5
	 */
	@Value("${kaptcha.textproducer.char.length}")
	private String textproducerCharLength;  
	
	/**
	 * 文本字体样式 默认为new Font("Arial", 1, fontSize), new Font("Courier", 1, fontSize)
	 */
	@Value("${kaptcha.textproducer.font.names}")
	private String textproducerFontNames;  
                  
    
	
	/**
	 * 配置 Kapcha 参数
	 * @return
	 */
	@Bean
	public DefaultKaptcha getDefaultKapcha() {	
		DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
		Properties properties = new Properties();
		properties.setProperty("kaptcha.border", border);
		properties.setProperty("kaptcha.border.color", borderColor);
		properties.setProperty("kaptcha.textproducer.font.color", textproducerFontColor);
		properties.setProperty("kaptcha.image.width", imageWidth);
		properties.setProperty("kaptcha.image.height", imageHeight);
		properties.setProperty("kaptcha.textproducer.char.space", textproducerCharSpace);
		properties.setProperty("kaptcha.textproducer.font.size", textproducerFontSize);
		properties.setProperty("kaptcha.noise.color", noiseColor);
		properties.setProperty("kaptcha.word.impl", wordImpl);
		properties.setProperty("kaptcha.obscurificator.impl", obscurificatorImpl);
		properties.setProperty("kaptcha.background.clear.from", backgroundClearFrom);
		properties.setProperty("kaptcha.background.clear.to", backgroundClearTo);
		properties.setProperty("kaptcha.session.key", sessionKey);
		properties.setProperty("kaptcha.textproducer.char.length", textproducerCharLength);
		properties.setProperty("kaptcha.textproducer.font.names", textproducerFontNames);
		
		

		Config config = new Config(properties);
		defaultKaptcha.setConfig(config);
		return defaultKaptcha;
	}
	
	
	
	
}
