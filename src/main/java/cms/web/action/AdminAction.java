package cms.web.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cms.bean.QueryResult;
import cms.bean.RequestResult;
import cms.bean.ResultCode;
import cms.bean.staff.StaffLoginLog;
import cms.bean.staff.SysUsers;
import cms.service.feedback.FeedbackService;
import cms.service.question.AnswerService;
import cms.service.question.QuestionService;
import cms.service.staff.StaffService;
import cms.service.topic.CommentService;
import cms.service.topic.TopicService;
import cms.utils.FileUtil;
import cms.utils.HexConversion;
import cms.utils.IpAddress;
import cms.utils.JsonUtils;
import cms.utils.PathUtil;
import cms.utils.WebUtil;
import cms.web.action.fileSystem.FileManage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 后台框架页面
 *
 */
@Controller
public class AdminAction {
	private static final Logger logger = LogManager.getLogger(AdminAction.class);
	
	
	@Resource StaffService staffService;
	@Resource TopicService topicService;
	@Resource CommentService commentService;
	@Resource QuestionService questionService;
	@Resource AnswerService answerService;
	@Resource FeedbackService feedbackService;
	@Resource FileManage fileManage;
	
	
	/**
	 * 后台管理框架页
	 * @return
	 */
	@ResponseBody
	@RequestMapping("control/manage/index") 
	public String framework(ModelMap model){
		Map<String,Object> returnValue = new HashMap<String,Object>();
		SysUsers sysUserView = new SysUsers();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication != null){
			 Object o = authentication.getPrincipal();
			 if(o instanceof SysUsers){
				 SysUsers sysUsers = (SysUsers)o;
				 
				 sysUserView.setUserId(sysUsers.getUserId());//用户id
				 sysUserView.setUserAccount(sysUsers.getUserAccount());//用户账号
				 sysUserView.setFullName(sysUsers.getFullName());//姓名
				 sysUserView.setUserDuty(sysUsers.getUserDuty());//用户的职位
				 sysUserView.setIssys(sysUsers.isIssys());//是否是超级用户
			 }
		}
		returnValue.put("sysUsers", sysUserView);
		returnValue.put("fileStorageSystem", fileManage.getFileSystem());//文件存储系统 0.本地系统 10.SeaweedFS 20.MinIO 30.阿里云OSS
		
		
		return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,returnValue));	
	}

	/**
	 * 后台首页
	 * @return
	 */
	@ResponseBody
	@RequestMapping("control/manage/home")
	public String home(ModelMap model,HttpServletRequest request, HttpServletResponse response)throws Exception{
		Map<String,Object> returnValue = new HashMap<String,Object>();

		SysUsers sysUsers = null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication != null){
			 Object o = authentication.getPrincipal();
			 if(o instanceof SysUsers){
				 sysUsers = (SysUsers)o;
			 }
		}
		
		
		QueryResult<StaffLoginLog> qr = staffService.findStaffLoginLogPage(sysUsers.getUserId(), 0, 5);
		List<StaffLoginLog> staffLoginLogList = qr.getResultlist();
		if(staffLoginLogList != null && staffLoginLogList.size() >0){
			for(StaffLoginLog staffLoginLog : staffLoginLogList){
				if(staffLoginLog.getIp() != null && !"".equals(staffLoginLog.getIp().trim())){
					staffLoginLog.setIpAddress(IpAddress.queryAddress(staffLoginLog.getIp()));
				}
			}
		}
		returnValue.put("staffLoginLogList", staffLoginLogList);
		
		
		
		
		
		
		Long auditTopicCount = topicService.auditTopicCount();
		returnValue.put("auditTopicCount", auditTopicCount);
		
		Long auditCommentCount = commentService.auditCommentCount();
		returnValue.put("auditCommentCount", auditCommentCount);
		
		Long auditCommentReplyCount = commentService.auditReplyCount();
		returnValue.put("auditCommentReplyCount", auditCommentReplyCount);

		
		Long auditQuestionCount = questionService.auditQuestionCount();
		returnValue.put("auditQuestionCount", auditQuestionCount);
		Long auditAnswerCount = answerService.auditAnswerCount();
		returnValue.put("auditAnswerCount", auditAnswerCount);
		
		Long auditAnswerReplyCount = answerService.auditReplyCount();
		returnValue.put("auditAnswerReplyCount", auditAnswerReplyCount);
		
		Long feedbackCount = feedbackService.feedbackCount();
		returnValue.put("feedbackCount", feedbackCount);
		
		return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,returnValue));	
	}

	/**
	 * 后台管理框架页
	 * @return
	 */
	@RequestMapping("admin/control/**")
	public String index(ModelMap model,HttpServletRequest request, HttpServletResponse response)throws Exception{
		//List<String> templatePathList = WebUtil.templatePathList();
		//model.addAttribute("templatePathList", templatePathList);
		return "/manage/index";	
	}
	
	
	/**
	 * vue组件加载
	 * 本方法权限验证由cms.web.action.staff.MyBearerTokenExtractor.java和cms.web.action.staff.ResourceServerConfig.java和cms.web.action.staff.WebSecurityConfig.java进行拦截
	 * @return
	 */
	@RequestMapping("admin/component/{path}/{name}")
	public ResponseEntity<byte[]> component(ModelMap model,@PathVariable String path,@PathVariable String name,
			HttpServletRequest request, HttpServletResponse response){//throws Exception{
		ClassPathResource classPathResource = new ClassPathResource("WEB-INF/backstageView/manage/"+FileUtil.toRelativePath(path)+"/"+FileUtil.toRelativePath(FileUtil.getBaseName(name)+".vue"));
		if (classPathResource.exists()) {
			try (InputStream inputStream = classPathResource.getInputStream()){
				byte[] bytes = IOUtils.toByteArray(inputStream);
				return WebUtil.downloadResponse(bytes, FileUtil.getName(path),request);
			} catch (IOException e) {
				if (logger.isErrorEnabled()) {
	 	            logger.error("vue组件加载错误",e);
	 	        }
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
        }
		
		
		return null;
		
		
		/**
		String fullPath = PathUtil.autoRootPath()+File.separator+"WEB-INF"+File.separator+"manage"+File.separator+FileUtil.toRelativePath(path)+File.separator+FileUtil.toRelativePath(FileUtil.getBaseName(name)+".vue");

		File file = new File(fullPath);
		if(!file.exists()){
			return null;
		}
		return WebUtil.downloadResponse(FileUtils.readFileToByteArray(file), FileUtil.getName(path),request);**/
	}
}
