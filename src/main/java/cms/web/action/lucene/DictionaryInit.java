package cms.web.action.lucene;

import org.springframework.beans.factory.InitializingBean;
import org.wltea.analyzer.cfg.DefaultConfig;

/**
 * 词库初始化
 * @author Gao
 *
 */
public class DictionaryInit implements InitializingBean{

	@Override
	public void afterPropertiesSet() throws Exception {
		//系统启动时加载IKAnalyzer词典
		org.wltea.analyzer.dic.Dictionary.initial(DefaultConfig.getInstance());
	}

}
