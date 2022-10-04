package cms.web.action.setting;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cms.bean.RequestResult;
import cms.bean.ResultCode;
import cms.bean.setting.AllowRegisterAccount;
import cms.bean.setting.EditorTag;
import cms.bean.setting.SystemNode;
import cms.bean.setting.SystemSetting;
import cms.service.setting.SettingService;
import cms.utils.CommentedProperties;
import cms.utils.JsonUtils;
import cms.utils.Verification;
import cms.web.action.cache.CacheManage;
import cms.web.action.fileSystem.FileManage;
import cms.web.action.lucene.QuestionIndexManage;
import cms.web.action.lucene.QuestionLuceneInit;
import cms.web.action.lucene.TopicIndexManage;
import cms.web.action.lucene.TopicLuceneInit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;



/**
 * 系统设置
 *
 */
@Controller
@RequestMapping("/control/systemSetting/manage") 
public class SystemSettingManageAction {
	private static final Logger logger = LogManager.getLogger(SystemSettingManageAction.class);
	
	@Resource SettingService settingService;
	@Resource SettingManage settingManage;
	
	
	/** 选择缓存产品**/
	@Value("${bbs.selectCache}") 
    private String selectCache; 
	
	@Resource CacheManage cacheManage;
	
	@Resource(name = "systemSettingValidator") 
	private Validator validator; 
	
	@Resource TopicIndexManage topicIndexManage;
	@Resource QuestionIndexManage questionIndexManage;
	@Resource FileManage fileManage;
	
	@Resource MessageSource messageSource;
	
	
	public static Long randInt(int min, int max) {

	    // NOTE: Usually this should be a field rather than a method
	    // variable so that it is not re-seeded every call.
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return (long)randomNum;
	}
	
	
	/**
	 * 系统设置 修改界面显示
	 */
	@ResponseBody
	@RequestMapping(value="edit",method=RequestMethod.GET)
	public String editSystemSettingUI(ModelMap model,SystemSetting formbean,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		Map<String,Object> returnValue = new HashMap<String,Object>();
		
		SystemSetting systemSite = settingService.findSystemSetting();
	
		if(systemSite.getAllowRegisterAccount() != null && !"".equals(systemSite.getAllowRegisterAccount().trim())){
			AllowRegisterAccount allowRegisterAccount = JsonUtils.toObject(systemSite.getAllowRegisterAccount(), AllowRegisterAccount.class);
			if(allowRegisterAccount != null){
				systemSite.setAllowRegisterAccountObject(allowRegisterAccount);
			}
		}
		
		if(systemSite.getEditorTag() != null && !"".equals(systemSite.getEditorTag().trim())){
			EditorTag editorTag = JsonUtils.toObject(systemSite.getEditorTag(), EditorTag.class);
			if(editorTag != null){
				systemSite.setEditorTagObject(editorTag);
			}
		}
		
		if(systemSite.getTopicEditorTag() != null && !"".equals(systemSite.getTopicEditorTag().trim())){
			EditorTag editorTag = JsonUtils.toObject(systemSite.getTopicEditorTag(), EditorTag.class);
			if(editorTag != null){
				systemSite.setTopicEditorTagObject(editorTag);
			}
		}
		if(systemSite.getQuestionEditorTag() != null && !"".equals(systemSite.getQuestionEditorTag().trim())){
			EditorTag editorTag = JsonUtils.toObject(systemSite.getQuestionEditorTag(), EditorTag.class);
			if(editorTag != null){
				systemSite.setQuestionEditorTagObject(editorTag);
			}
		}
		if(systemSite.getAnswerEditorTag() != null && !"".equals(systemSite.getAnswerEditorTag().trim())){
			EditorTag editorTag = JsonUtils.toObject(systemSite.getAnswerEditorTag(), EditorTag.class);
			if(editorTag != null){
				systemSite.setAnswerEditorTagObject(editorTag);
			}
		}
		//允许上传图片格式
		List<String> imageUploadFormatList = CommentedProperties.readRichTextAllowImageUploadFormat();
		returnValue.put("imageUploadFormatList",imageUploadFormatList);
		
		//允许上传文件格式
		List<String> fileUploadFormatList = CommentedProperties.readRichTextAllowFileUploadFormat();
		returnValue.put("fileUploadFormatList",fileUploadFormatList);
		
		//允许上传视频格式
		List<String> videoUploadFormatList = CommentedProperties.readRichTextAllowVideoUploadFormat();
		returnValue.put("videoUploadFormatList",videoUploadFormatList);
		
		returnValue.put("systemSetting",systemSite);
		return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,returnValue));
	}
	/**
	 * 系统设置 修改
	 */
	@ResponseBody
	@RequestMapping(value="edit",method=RequestMethod.POST)
	public String editSystemSetting(ModelMap model,SystemSetting formbean,BindingResult result,String[] supportBank,String[] couponMore_couponType,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		//错误
		Map<String,Object> error = new HashMap<String,Object>();
		
		//数据校验
		this.validator.validate(formbean, result); 
		if (result.hasErrors()) {  
			List<FieldError> fieldErrorList = result.getFieldErrors();
			if(fieldErrorList != null && fieldErrorList.size() >0){
				for(FieldError fieldError : fieldErrorList){
					error.put(fieldError.getField(), messageSource.getMessage(fieldError, null));//messageSource.getMessage(fieldError.getCode(), fieldError.getArguments(), myQuestionnaire.getLocale())
				}
			}
		}
		if(error.size() ==0){
			formbean.setAllowRegisterAccount(JsonUtils.toJSONString(formbean.getAllowRegisterAccountObject()));
			formbean.setTopicEditorTag(JsonUtils.toJSONString(formbean.getTopicEditorTagObject()));
			formbean.setEditorTag(JsonUtils.toJSONString(formbean.getEditorTagObject()));
			formbean.setQuestionEditorTag(JsonUtils.toJSONString(formbean.getQuestionEditorTagObject()));
			formbean.setAnswerEditorTag(JsonUtils.toJSONString(formbean.getAnswerEditorTagObject()));
			
			formbean.setId(1);
			formbean.setVersion(new Date().getTime());
			settingService.updateSystemSetting(formbean);
		}
		
		if(error.size() >0){
			return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
		}else{
			return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,null));
		}
	}
	
	/**
	 * 维护数据 界面显示
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(params="method=maintainData",method=RequestMethod.GET)
	public String rebuildIndexUI(ModelMap model
			) throws Exception {
		
		return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,null));
	}
	
	
	
	
	
	/**
	 * 清空所有缓存
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(params="method=clearAllCache",method=RequestMethod.POST)
	public String clearAllCache(ModelMap model
			) throws Exception {
		
		cacheManage.clearAllCache();
		
		return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,null));
	}
	
	/**
	 * 重建话题全文索引
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(params="method=rebuildTopicIndex",method=RequestMethod.POST)
	public String rebuildTopicIndex(ModelMap model
			) throws Exception {
		Map<String,String> error = new HashMap<String,String>();
		
		Long count = topicIndexManage.taskRunMark_add(-1L);
		
		if(count >=0L){
			error.put("rebuildTopicIndex", "任务正在运行");
		}else{
			boolean allow = TopicLuceneInit.INSTANCE.allowCreateIndexWriter();//是否允许创建IndexWriter
			if(allow){
				settingManage.addAllTopicIndex();
				return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,null));
			}else{
				error.put("rebuildTopicIndex", "索引运行过程中，不能执行重建");
			}
		}
		return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
	}
	
	/**
	 * 重建问题全文索引
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(params="method=rebuildQuestionIndex",method=RequestMethod.POST)
	public String rebuildQuestionIndex(ModelMap model
			) throws Exception {
		Map<String,String> error = new HashMap<String,String>();
		
		Long count = questionIndexManage.taskRunMark_add(-1L);
		
		if(count >=0L){
			error.put("rebuildQuestionIndex", "任务正在运行");
		}else{
			boolean allow = QuestionLuceneInit.INSTANCE.allowCreateIndexWriter();//是否允许创建IndexWriter
			if(allow){
				settingManage.addAllQuestionIndex();
				return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,null));
			}else{
				error.put("rebuildQuestionIndex", "索引运行过程中，不能执行重建");
			}
			
			
		}
		return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
	}
	
	
	

	
	
	
	/**
	 * 删除浏览量数据
	 * @param model
	 * @param beforeTime 删除指定时间之前的数据
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(params="method=deletePageViewData",method=RequestMethod.POST)
	public String deletePageViewData(ModelMap model,String deletePageViewData_beforeTime
			) throws Exception {	
		Map<String,String> error = new HashMap<String,String>();
		
		if(deletePageViewData_beforeTime != null && !"".equals(deletePageViewData_beforeTime.trim())){
			boolean beforeTimeVerification = Verification.isTime_minute(deletePageViewData_beforeTime.trim());
			if(beforeTimeVerification){
				DateFormat dd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");		
				settingManage.executeDeletePageViewData(dd.parse(deletePageViewData_beforeTime.trim()+":00"));	
				
				return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,null));
			}else{
				error.put("deletePageViewData_beforeTime", "时间格式错误");
			}
		}else{
			error.put("deletePageViewData_beforeTime", "时间不能为空");
		}
		return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
	}
	
	/**
	 * 删除用户登录日志数据
	 * @param model
	 * @param beforeTime 删除指定时间之前的数据
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(params="method=deleteUserLoginLogData",method=RequestMethod.POST)
	public String deleteUserLoginLogData(ModelMap model,String deleteUserLoginLogData_beforeTime
			) throws Exception {	
		Map<String,String> error = new HashMap<String,String>();
		
		if(deleteUserLoginLogData_beforeTime != null && !"".equals(deleteUserLoginLogData_beforeTime.trim())){
			boolean deleteUserLoginLogData_beforeTimeVerification = Verification.isTime_minute(deleteUserLoginLogData_beforeTime.trim());
			if(deleteUserLoginLogData_beforeTimeVerification){
				DateFormat dd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");		
				settingManage.executeDeleteUserLoginLogData(dd.parse(deleteUserLoginLogData_beforeTime.trim()+":00"));	
				return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,null));
			}else{
				error.put("deleteUserLoginLogData_beforeTime", "时间格式错误");
			}
		}else{
			error.put("deleteUserLoginLogData_beforeTime", "时间不能为空");
		}
		return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
	}
	
	
	
	/**
	 * 节点参数 界面显示
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(params="method=nodeParameter",method=RequestMethod.GET)
	public String nodeParameterUI(ModelMap model
			) throws Exception {
		Map<String,Object> returnValue = new HashMap<String,Object>();
		
		SystemNode systemNode = new SystemNode();
		systemNode.setMaxMemory(settingManage.maxMemory());//分配最大内存
		systemNode.setTotalMemory(settingManage.totalMemory());//已分配内存
		systemNode.setFreeMemory(settingManage.freeMemory());//已分配内存中的剩余空间
		systemNode.setUsableMemory(settingManage.maxMemory() - settingManage.totalMemory() + settingManage.freeMemory());//空闲内存
		
		returnValue.put("systemNode",systemNode);
		
		
		
		
		returnValue.put("cacheName", selectCache);
		
		
		return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,returnValue));
	}
	
	
}
