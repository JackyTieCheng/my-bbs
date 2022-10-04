package cms.web.action.cache;

import java.util.Collection;
import java.util.LinkedHashSet;
import javax.cache.CacheManager;
import org.springframework.cache.Cache;
import org.springframework.cache.jcache.JCacheCache;
import org.springframework.cache.transaction.AbstractTransactionSupportingCacheManager;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;


/**
 * 系统缓存管理
 *
 */
public class SystemCacheManager extends AbstractTransactionSupportingCacheManager{
	
	@Nullable
	private CacheManager jCacheManager;
	//选择缓存产品
    private String selectCache; 
    
	private boolean isAllowNullValues = true;


	
	public SystemCacheManager() {}
	public SystemCacheManager(CacheManager jCacheManager, String selectCache) {
		this.jCacheManager = jCacheManager;
		this.selectCache = selectCache;
	}
	public CacheManager getjCacheManager() {
		return jCacheManager;
	}
	public void setjCacheManager(CacheManager jCacheManager) {
		this.jCacheManager = jCacheManager;
	}
	public String getSelectCache() {
		return selectCache;
	}
	public void setSelectCache(String selectCache) {
		this.selectCache = selectCache;
	}

	//@Override
	//public void afterPropertiesSet() {
		//if (getCacheManager() == null) {
		//	setCacheManager(Caching.getCachingProvider().getCacheManager());
		//}
	//	super.afterPropertiesSet();
	//}
	
	@Override
	protected Collection<? extends Cache> loadCaches() {
		if("ehcache".equals(selectCache)){//如果使用EhCache缓存
			Assert.state(jCacheManager != null, "No CacheManager set");
			//获取所有缓存名称
			Collection<Cache> caches = new LinkedHashSet<>();
			for (String cacheName : jCacheManager.getCacheNames()) {
				javax.cache.Cache<Object, Object> jcache = jCacheManager.getCache(cacheName);
				caches.add(new JCacheCache(jcache, isAllowNullValues));
			}
			return caches;
		}else{//如果使用Redis缓存
			
		}
		return new LinkedHashSet<Cache>();
	}
	
	
	//org.ehcache.jsr107.Eh107CacheManager的INFO级别信息已在log4j2.xml中屏蔽
	@Override
	protected Cache getMissingCache(String name) {
		if("ehcache".equals(selectCache)){//如果使用EhCache缓存
			Assert.state(jCacheManager != null, "No CacheManager set");
			// 再次检查JCache缓存（以防止在运行时添加缓存）
			javax.cache.Cache<Object, Object> jcache = jCacheManager.getCache(name);
			if (jcache != null) {
				return new JCacheCache(jcache, isAllowNullValues);
			}
		}else{//如果使用Redis缓存
			
		}
		
		return null;
	}
}
