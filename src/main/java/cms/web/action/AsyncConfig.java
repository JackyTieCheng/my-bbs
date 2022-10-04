package cms.web.action;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 多线程执行定时任务
 * @author Gao
 *
 */
@Configuration
@EnableAsync// 开启异步事件的支持
public class AsyncConfig {
	
	//@Value("${taskExecutor.async.corePoolSize}")
    private int corePoolSize = 20;
    //@Value("${taskExecutor.async.maxPoolSize}")
    private int maxPoolSize = 500;
    
	
	//cms.web.action.quartz.Task.java类topicIndex()方法@Async("taskExecutor_topicIndex")调用
	@Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);//核心线程池大小
        executor.setMaxPoolSize(maxPoolSize);//最大线程数
        //线程池对拒绝任务(无线程可用)的处理策略 
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());//处理的方法是运行计算任务,除非ThreadPoolExecutor被关闭，这样的话，任务会被丢弃
       
        executor.initialize();
        return executor;
    }
	
	@Bean
    public Executor taskExecutor_topicIndexManage_topicIndex() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setBeanName("taskExecutor_topicIndexManage_topicIndex");
        executor.setCorePoolSize(1);//核心线程池大小
        
        executor.initialize();
        return executor;
    }
	@Bean
    public Executor taskExecutor_questionIndexManage_questionIndex() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setBeanName("taskExecutor_questionIndexManage_questionIndex");
        executor.setCorePoolSize(1);//核心线程池大小
        
        executor.initialize();
        return executor;
    }
	@Bean
    public Executor taskExecutor_thumbnailManage_treatmentThumbnail() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setBeanName("taskExecutor_thumbnailManage_treatmentThumbnail");
        executor.setCorePoolSize(1);//核心线程池大小
        
        executor.initialize();
        return executor;
    }
	@Bean
    public Executor taskExecutor_fileManage_deleteInvalidFile() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setBeanName("taskExecutor_fileManage_deleteInvalidFile");
        executor.setCorePoolSize(1);//核心线程池大小
        
        executor.initialize();
        return executor;
    }
	@Bean
    public Executor taskExecutor_layoutManage_timerProcessLayoutUrl() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setBeanName("taskExecutor_layoutManage_timerProcessLayoutUrl");//处理布局路径
        executor.setCorePoolSize(1);//核心线程池大小
        
        executor.initialize();
        return executor;
    }
}
