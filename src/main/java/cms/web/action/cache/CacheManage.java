package cms.web.action.cache;

import java.time.Duration;

import javax.annotation.Resource;
import javax.cache.CacheManager;
import org.ehcache.config.CacheRuntimeConfiguration;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 缓存API管理
 *
 */
@Component("cacheManage")
public class CacheManage {
	
	@Resource CacheManager jCacheManager;
	
	
	/** 选择缓存产品**/
	@Value("${bbs.selectCache}") 
    private String selectCache; 
	 
	 
	/**
	 * 获取缓存
	 * @param cacheName 缓存名称
	 * @param key 缓存Key
	 */
	public Object getCache(String cacheName,final String key){
		if("ehcache".equals(selectCache)){//如果使用EhCache缓存
			
			//第一个参数: 缓存名称  第二个参数: key的类型   第三个参数: value的类型
			javax.cache.Cache<Object, Object> preConfigured = jCacheManager.getCache(cacheName, Object.class, Object.class);
			
			
			
			
			return preConfigured.get(key);
		}else{//如果使用Redis缓存
			
		}
		return null;
	}



	/**
	 * 添加缓存
	 * @param cacheName 缓存名称
	 * @param key 缓存Key
	 * @param value 缓存value
	 */
	public void addCache(String cacheName,final String key,final Object value){
		if("ehcache".equals(selectCache)){//如果使用EhCache缓存
			javax.cache.Cache<Object, Object> preConfigured = jCacheManager.getCache(cacheName, Object.class, Object.class);
			preConfigured.put(key, value);
			
			
		}else{//如果使用Redis缓存
			
			
		}
		
	}
	
	/**
	 * 删除缓存
	 * @param cacheName 缓存名称
	 * @param key 缓存Key
	 */
	public void deleteCache(String cacheName,final String key){
		if("ehcache".equals(selectCache)){//如果使用EhCache缓存
			javax.cache.Cache<Object, Object> preConfigured = jCacheManager.getCache(cacheName, Object.class, Object.class);
			preConfigured.remove(key);
		}else{//如果使用Redis缓存
			
		}
	}
	/**
	 * 清空缓存
	 * @param cacheName 缓存名称
	 */
	public void clearCache(String cacheName){
		if("ehcache".equals(selectCache)){//如果使用EhCache缓存
			javax.cache.Cache<Object, Object> preConfigured = jCacheManager.getCache(cacheName, Object.class, Object.class);
			preConfigured.clear();
		}else{//如果使用Redis缓存
			
		}
		
	}
	
	/**
	 * 清空所有缓存
	 */
	public void clearAllCache(){
		//获取所有缓存名称
		for (String cacheName : jCacheManager.getCacheNames()) {
			javax.cache.Cache<Object, Object> jcache = jCacheManager.getCache(cacheName);
			//缓存名称
			this.clearCache(jcache.getName());
		}
	}
	
	
}
