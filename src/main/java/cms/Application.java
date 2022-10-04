package cms;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

import cms.utils.CreateEntityFile;
import cms.utils.YmlUtils;
import cms.web.action.install.InstallManage;
import cms.web.filter.InitApplicationListener;  
/** 
 * 注解@SpringBootApplication指定项目为springboot，由此类当作程序入口,自动装配 web 依赖的环境; 
 * 
 */
@EnableScheduling // 开启定时
@SpringBootApplication(exclude = {FreeMarkerAutoConfiguration.class})//取消freemarker自动配置
@ServletComponentScan("cms.web.filter")
public class Application extends SpringBootServletInitializer{  
	
    // 在main方法中启动一个应用，即：这个应用的入口  
    public static void main(String[] args) {  
    	boolean isMaven = false;//是否由maven插件启动
    	if(args != null && args.length >0){
    		for(String arg : args){
                if(arg.equals("maven")){
                	isMaven = true;
                }
            }
    	}
    	//创建数据库分表实体类文件
    	CreateEntityFile.create();
    	
    	if(!isMaven){
    		SpringApplication application = new SpringApplication(Application.class);
            application.addListeners(new InitApplicationListener());//注册监听器
            application.run(args);
    		
    		//ConfigurableApplicationContext context = SpringApplication.run(Application.class, args); 
        	//注册监听器
           //context.addApplicationListener(new InitApplicationListener());
    	}else{//maven命令执行
    		String user_dir = System.getProperty("user.dir"); 
    		
    		//默认外部目录
    		String defaultExternalDirectory = "";
    		
    		//论坛外部目录
    		Object externalDirectory = YmlUtils.getYmlProperty("application.yml","bbs.externalDirectory");
    		if(externalDirectory != null && !"".equals(externalDirectory.toString().trim())){//如果已设置了论坛外部目录
    			defaultExternalDirectory = externalDirectory.toString();
    		}else{
    			defaultExternalDirectory = user_dir + File.separator + "target"+ File.separator + "bbs";
    		}
    		
    		//生成外部文件夹
    		InstallManage installManage = new InstallManage();
    		List<String> folderList = installManage.folderList();
    		if(folderList != null && folderList.size() >0){
    			for(String folderPath : folderList){
    				//生成文件目录
    				Path path = Paths.get(defaultExternalDirectory+File.separator+StringUtils.replace(folderPath, "/", File.separator));
    				if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {//目录不存在
    					try {
    						Files.createDirectories(path);
    					} catch (IOException e) {
    						e.printStackTrace();
    					}
    				}
    			}
    		}
    	}
    	
       
    }  
    
    
    
    /**
     * 解决静态url带jsessionid问题
     */
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
	    super.onStartup(servletContext);
	    servletContext.setSessionTrackingModes(Collections.singleton(SessionTrackingMode.COOKIE));
	    SessionCookieConfig sessionCookieConfig=servletContext.getSessionCookieConfig();
	    sessionCookieConfig.setHttpOnly(true);
    }
    
    
    
    
}  
