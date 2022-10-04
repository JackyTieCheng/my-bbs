package cms.web.filter;

import java.io.File;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.StrLookup;

import cms.utils.PathUtil;
import cms.utils.YmlUtils;

/**
 * Log4j2自定义变量
 * 需要在log4j2.xml中设置本类所在的包目录packages="cms.web.filter"才能生效
 * @author Gao
 *
 */
@Plugin(name = "custom", category = StrLookup.CATEGORY)
public class Log4j2Lookup implements StrLookup{

	@Override
	public String lookup(String key) {
		/**
		//日志根路径
		if ("rootDir".equals(key)) {
			Object externalDirectory = YmlUtils.getYmlProperty("application.yml", "bbs.externalDirectory");
	    	
			if(externalDirectory != null && !"".equals(externalDirectory.toString().trim())){//如果已设置了论坛外部目录
				return externalDirectory.toString().trim();
			}else{//未设置论坛外部目录，则目录为jar文件的同级bbs目录
				return PathUtil.defaultExternalDirectory();
			}
		}**/
		//日志完整路径
		if ("fullPath".equals(key)) {
			return PathUtil.defaultExternalDirectory()+File.separator+"WEB-INF"+File.separator+"log"+File.separator;
			
		}
		return null;
	}

	@Override
	public String lookup(LogEvent event, String key) {
		return null;
	}

}
