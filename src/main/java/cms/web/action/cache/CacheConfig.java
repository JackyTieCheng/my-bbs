package cms.web.action.cache;


import org.ehcache.jsr107.EhcacheCachingProvider;
import org.ehcache.xml.XmlConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.Caching;

@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

    /**
     * 选择缓存产品
     **/
    @Value("${bbs.selectCache}")
    private String selectCache;
    public static org.springframework.cache.CacheManager cacheManager;

    /**
     * @Bean public JCacheCacheManager jCacheCacheManager() {
     * JCacheCacheManager jCacheManager = new JCacheCacheManager(jCacheManager());
     * return jCacheManager;
     * }
     **/
    @Bean
    @Override
    public org.springframework.cache.CacheManager cacheManager() {
        if (cacheManager == null) {
            return new SystemCacheManager(jCacheManager(), selectCache);

        }
        return cacheManager;

    }


    //@Primary //优先选择这个CacheManager
    @Bean(destroyMethod = "close")
    public CacheManager jCacheManager() {
        //配置默认缓存属性
        XmlConfiguration xmlConfig = new XmlConfiguration(getClass().getResource("/ehcache.xml"));
        EhcacheCachingProvider provider = (EhcacheCachingProvider) Caching.getCachingProvider();
        return provider.getCacheManager(provider.getDefaultURI(), xmlConfig);
    }

    /**
     @Bean public SystemCacheManager systemCacheManager() {
     return new SystemCacheManager(jCacheManager(),selectCache);
     }**/
}
