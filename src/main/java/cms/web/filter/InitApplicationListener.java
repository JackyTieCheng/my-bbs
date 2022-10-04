package cms.web.filter;

import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;
import org.wltea.analyzer.cfg.DefaultConfig;

/**
 * 系统启动时执行
 * @author Gao
 *
 */
public class InitApplicationListener implements ApplicationListener<ApplicationStartingEvent>{

	@Override
	public void onApplicationEvent(ApplicationStartingEvent event) {
		//系统启动时加载IKAnalyzer词典
		org.wltea.analyzer.dic.Dictionary.initial(DefaultConfig.getInstance());
	}

	

}
