package cms.web.action.upgrade;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import cms.bean.upgrade.UpgradeLog;
import cms.bean.upgrade.UpgradeSystem;
import cms.service.upgrade.UpgradeService;
import cms.utils.FileUtil;
import cms.utils.JsonUtils;
import cms.utils.PathUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

/**
 * 升级管理
 *
 */
@Component("upgradeManage")
public class UpgradeManage {
	private static final Logger logger = LogManager.getLogger(UpgradeManage.class);
	@Resource UpgradeService upgradeService;
	
	/**
	 * 处理数据
	 * @param upgradeId 升级Id
	 */
	@Async
	public void manipulationData(String upgradeId){
		UpgradeSystem upgradeSystem = upgradeService.findUpgradeSystemById(upgradeId);
		//执行升级数据处理
		if(upgradeSystem.getRunningStatus() >= 40){
			String oldVersion = upgradeSystem.getOldSystemVersion().replaceAll("\\.", "_");//点替换为下划线
			String newVersion = upgradeSystem.getId().replaceAll("\\.", "_");//点替换为下划线
	
			boolean error = false;
			//反射调用升级数据处理类
			try {
				Class<?> cls = Class.forName("cms.web.action.upgrade.impl.Upgrade"+oldVersion.trim()+"to"+newVersion.trim());
				Method m = cls.getDeclaredMethod("run",new Class[]{String.class});  
				m.invoke(cls,upgradeId);
			} catch (ClassNotFoundException e) {
				error = true;
				if (logger.isErrorEnabled()) {
		            logger.error("升级处理数据错误",e);
		        }
			//	e.printStackTrace();
			} catch (NoSuchMethodException e) {
				error = true;
				if (logger.isErrorEnabled()) {
		            logger.error("升级处理数据错误",e);
		        }
			//	e.printStackTrace();
			} catch (SecurityException e) {
				error = true;
				if (logger.isErrorEnabled()) {
		            logger.error("升级处理数据错误",e);
		        }
			//	e.printStackTrace();
			} catch (IllegalAccessException e) {
				error = true;
				if (logger.isErrorEnabled()) {
		            logger.error("升级处理数据错误",e);
		        }
			//	e.printStackTrace();
			} catch (IllegalArgumentException e) {
				error = true;
				if (logger.isErrorEnabled()) {
		            logger.error("升级处理数据错误",e);
		        }
			//	e.printStackTrace();
			} catch (InvocationTargetException e) {
				error = true;
				if (logger.isErrorEnabled()) {
		            logger.error("升级处理数据错误",e);
		        }
			//	e.printStackTrace();
			} catch (Exception e) {
				error = true;
				if (logger.isErrorEnabled()) {
		            logger.error("升级处理数据错误",e);
		        }
			//	e.printStackTrace();
			}finally{
				if(error){
					upgradeService.addLog(upgradeId, JsonUtils.toJSONString(new UpgradeLog(new Date(),"调用升级数据处理类出错",2))+",");
				}
			}
				
		}
	}
	
	/**
	 * 查询下级目录列表
	 * @param path 路径 WEB-INF/data/upgrade
	 */
	public List<String> querySubdirectoryList(String path){
		Set<String> folderList = new LinkedHashSet<String>();
		
		try {
			org.springframework.core.io.Resource[] resources = new PathMatchingResourcePatternResolver().
			        getResources(ResourceUtils.CLASSPATH_URL_PREFIX + path+"/**");
			
			String formatRootPath = FileUtil.toLeftSlant(PathUtil.rootPath());
			
			// 遍历文件内容
	        for(org.springframework.core.io.Resource resource : resources) {
	        	
	        	if("jar".equals(resource.getURL().getProtocol())){//jar:开头
	        		//jar:file:/D:/test2/test.jar!/BOOT-INF/classes!/WEB-INF/data/upgrade/5.1to5.2/cms/
	        		if (resource.getURL().getPath().endsWith("/")) { //如果是文件夹
	        			//源相对路径
	        			String sourceRelativePath = StringUtils.substringAfter(StringUtils.substringAfterLast(resource.getURL().getPath(), "!/"), path+"/");// 5.1to5.2/cms/
	        			//目标路径
	        			String distPath = StringUtils.substringBefore(sourceRelativePath, "/");//截取到等于第二个参数的字符串为止
	        			if(distPath != null && !"".equals(distPath.trim())){
	        				folderList.add(distPath);
	        			}
	        			
	        		}
	        	}else{//file:开头
	        		//file:/F:/JAVA/cms-pro/target/classes/WEB-INF/data/upgrade/5.1to5.2/cms/
        			if (resource.getURL().getPath().endsWith("/")) { //如果是文件夹
        				String sourceRelativePath = StringUtils.substringAfter(resource.getURL().getPath(), formatRootPath+"/"+path+"/");//5.1to5.2/cms/
        				//目标路径
	        			String distPath = StringUtils.substringBefore(sourceRelativePath, "/");//截取到等于第二个参数的字符串为止
	        			if(distPath != null && !"".equals(distPath.trim())){
	        				folderList.add(distPath);
	        			}
	        		}
	        	}
	        }
		} catch (IOException e) {
			if (logger.isErrorEnabled()) {
		        logger.error("查询下级目录列表异常 "+path,e);
		    }
		}
		
		
		
		return new ArrayList<>(folderList);
	}

	
	/**
	 * 查询/添加任务运行标记
	 * @param count 次数  -1为查询方式
	 * @return
	 */
	@Cacheable(value="upgradeManage_cache_taskRunMark",key="'taskRunMark'")
	public Long taskRunMark_add(Long count){
		return count;
	}
	/**
	 * 删除任务运行标记
	 * @return
	 */
	@CacheEvict(value="upgradeManage_cache_taskRunMark",key="'taskRunMark'")
	public void taskRunMark_delete(){
	}
	
	
	
	
}
