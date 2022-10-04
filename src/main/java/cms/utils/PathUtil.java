package cms.utils;

import java.io.File;
import java.net.URL;

import org.springframework.boot.system.ApplicationHome;

import cms.Application;
import cms.web.action.KaptchaConfig;


/**
 * 获取项目jar的根目录
 * @author Administrator
 *
 */
public class PathUtil {
	//项目根目录
	private static String rootPath = "";
	
	//论坛外部目录
	private static String defaultExternalDirectory = "";
	
	
	public static String rootPath(){
		if("".equals(rootPath)){
			readPath();
		}
		return rootPath;
	}
	public static String defaultExternalDirectory(){
		if("".equals(defaultExternalDirectory)){
			readPath();
		}
		return defaultExternalDirectory;
	}

	private static void readPath() {
		ApplicationHome home = new ApplicationHome(Application.class);
		File jarFile = home.getSource();
		String path = jarFile.getParentFile().toString();
		rootPath = path + File.separator+"classes";//F:\JAVA\cms-pro\target\classes
		
		//论坛外部目录
		Object externalDirectory = YmlUtils.getYmlProperty("application.yml","bbs.externalDirectory");
		if(externalDirectory != null && !"".equals(externalDirectory.toString().trim())){//如果已设置了论坛外部目录   G:\bbs
			defaultExternalDirectory = externalDirectory.toString();
		}else{
			defaultExternalDirectory = path + File.separator + "bbs";//F:\JAVA\cms-pro\target\bbs    
		}
		
		
		
	}
	
	/**
	 * 自动路径 jar启动时使用外部路径 IDE启动时使用内部路径
	 * @return
	 */
	public static String autoRootPath() {
		if(isStartupFromJar()){//jar启动
			return defaultExternalDirectory();
		}else{//IDE启动
			return rootPath();
		}
	}
	
	/**
	 * 判断是在jar中运行,还是IDE中运行
	 * @return true: jar启动   false: IDE启动
	 */
	public static boolean isStartupFromJar() {
		URL url = KaptchaConfig.class.getResource("");
		String protocol = url.getProtocol();
		if ("jar".equals(protocol)) {
			return true;
		}	
		return false;
	}

}
