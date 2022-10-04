package cms.web.action.quartz;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cms.web.action.fileSystem.FileManage;
import cms.web.action.lucene.QuestionIndexManage;
import cms.web.action.lucene.TopicIndexManage;
import cms.web.action.template.LayoutManage;
import cms.web.action.thumbnail.ThumbnailManage;

/**
 * 定时任务类
 *
 */
@Component("task")
public class Task {
	
	@Resource FileManage fileManage;
	
	@Resource ThumbnailManage thumbnailManage;
	@Resource TopicIndexManage topicIndexManage;
	@Resource QuestionIndexManage questionIndexManage;
	@Resource LayoutManage layoutManage;
	
	/**
	 * 话题全文索引
	 */
	@Async("taskExecutor_topicIndexManage_topicIndex")
	@Scheduled(cron = "0 0/1 * * * ?")//每隔1分钟执行一次
	public void topicIndex() {
		topicIndexManage.updateTopicIndex();
	}
	/**
	 * 问题全文索引
	 */
	@Async("taskExecutor_questionIndexManage_questionIndex")
	@Scheduled(cron = "0 0/1 * * * ?")//每隔1分钟执行一次
	public void questionIndex() {
		questionIndexManage.updateQuestionIndex();
	}
	
	/**
	 * 处理缩略图
	 */
	@Async("taskExecutor_thumbnailManage_treatmentThumbnail")
	@Scheduled(cron = "0 0/10 * * * ?")//每隔10分钟运行一次
	public void treatmentThumbnail() {
		thumbnailManage.treatmentThumbnail();
	}
	
	/**
	 * 删除无效的上传临时文件
	 */
	@Async("taskExecutor_fileManage_deleteInvalidFile")
	@Scheduled(cron = "0 50 0/2 * * ?")//每隔两小时的50分运行一次
	public void deleteInvalidFile() {
		fileManage.deleteInvalidFile();
	}
	
	/**
	 * 定时处理布局路径
	 * 异步任务配置cms.web.action.AsyncConfig.java
	 */
	@Async("taskExecutor_layoutManage_timerProcessLayoutUrl")
	@Scheduled(cron = "0/3 * * * * ?")//每隔3秒执行一次
	public void timerProcessLayoutUrl() {
		layoutManage.timerProcessLayoutUrl();
	}
}
