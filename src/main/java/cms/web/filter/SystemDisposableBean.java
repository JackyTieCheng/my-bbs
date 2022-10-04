package cms.web.filter;

import javax.annotation.Resource;
import javax.cache.CacheManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import cms.utils.SpringConfigTool;
import cms.web.action.topic.TopicManage;

/**
 * SpringBoot关闭时事件
 * @author Gao
 *
 */
@Component
public class SystemDisposableBean implements DisposableBean{
	private static final Logger logger = LogManager.getLogger(SystemDisposableBean.class);
	@Resource CacheManager jCacheManager;
	
	
	/**
	 * 关闭系统时执行
	 */
	@Override
	public void destroy() throws Exception {
		jCacheManager.close();
		
		
		Scheduler scheduler = (Scheduler)SpringConfigTool.getContext().getBean("schedulerFactoryBean");
		//关闭Quartz调度器 true:等待作业执行完成时才关闭调度器    false:立即关闭调度器
		try {
			scheduler.shutdown(true);
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
			if (logger.isErrorEnabled()) {
			    logger.error("关闭Quartz调度器错误",e);
	    	}
		}
		
	}

}
