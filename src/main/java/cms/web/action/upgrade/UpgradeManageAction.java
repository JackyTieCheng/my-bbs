package cms.web.action.upgrade;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.core.type.TypeReference;

import cms.bean.RequestResult;
import cms.bean.ResultCode;
import cms.bean.upgrade.UpgradeLog;
import cms.bean.upgrade.UpgradePackage;
import cms.bean.upgrade.UpgradeSystem;
import cms.service.upgrade.UpgradeService;
import cms.utils.CommentedProperties;
import cms.utils.FileUtil;
import cms.utils.JsonUtils;
import cms.utils.PathUtil;
import cms.web.action.TextFilterManage;
import cms.web.action.fileSystem.localImpl.LocalFileManage;


/**
 * 升级管理
 *
 */
@Controller
@RequestMapping("/control/upgrade/manage") 
public class UpgradeManageAction {
	private static final Logger logger = LogManager.getLogger(UpgradeManageAction.class);
	@Resource LocalFileManage localFileManage;
	@Resource UpgradeService upgradeService;
	@Resource TextFilterManage textFilterManage;
	@Resource UpgradeManage upgradeManage;
	
	
	/**
	 * 升级列表
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(params="method=upgradeSystemList",method=RequestMethod.GET)
	public String upgradeSystemList(ModelMap model,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String,Object> returnValue = new HashMap<String,Object>();
		
		String currentVersion = "";
		//读取当前系统版本
		ClassPathResource classPathResource = new ClassPathResource("WEB-INF/data/systemVersion.txt");
		try (InputStream inputStream = classPathResource.getInputStream()){
			currentVersion = IOUtils.toString(inputStream, StandardCharsets.UTF_8); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
			if (logger.isErrorEnabled()) {
	            logger.error("读取当前系统版本IO异常",e);
	        }
		}
		
		
		List<UpgradeSystem> upgradeSystemList = upgradeService.findAllUpgradeSystem();
		UpgradeSystem notCompletedUpgrade = null;//未完成升级
		
		boolean isCompletedUpgrade = false;//是否完成升级
		
		
		if(upgradeSystemList != null && upgradeSystemList.size() >0){
			for(UpgradeSystem upgradeSystem : upgradeSystemList){
				//删除最后一个逗号
				String _upgradeLog = StringUtils.substringBeforeLast(upgradeSystem.getUpgradeLog(), ",");//从右往左截取到相等的字符,保留左边的
		
				List<UpgradeLog> upgradeLogList = JsonUtils.toGenericObject(_upgradeLog+"]", new TypeReference< List<UpgradeLog> >(){});
				upgradeSystem.setUpgradeLogList(upgradeLogList);
			}
			
			for(int i =0; i<upgradeSystemList.size(); i++ ){
				UpgradeSystem upgradeSystem = upgradeSystemList.get(i);

				if(upgradeSystem.getRunningStatus() <9999){
					//未完成升级
					notCompletedUpgrade = upgradeSystem;
					break;
				}else{
					if(currentVersion.equals(upgradeSystem.getId())){//如果当前系统版本已升级完成
						isCompletedUpgrade = true;
					}
				}
				
			}
		}
		if(notCompletedUpgrade == null && !isCompletedUpgrade){
			List<String> folderList = upgradeManage.querySubdirectoryList("WEB-INF/data/upgrade");
			for(String folder : folderList){
				if(folder.endsWith("to"+currentVersion)){
					ClassPathResource resource_prop = new ClassPathResource("WEB-INF/data/upgrade/"+folder+"/config.properties");
					
					try (InputStream inputStream = resource_prop.getInputStream()){
						
						
						CommentedProperties props = new CommentedProperties();
						try {
							props.load(inputStream,"utf-8");
							//旧版本
							String oldSystemVersion = props.getProperty("oldSystemVersion");
							//升级包版本
							String updatePackageVersion = props.getProperty("updatePackageVersion");
							//新版本
							String newSystemVersion = props.getProperty("newSystemVersion");
							//说明
							String explanation = props.getProperty("explanation");
							//排序
							String sort = props.getProperty("sort");
							
							if(currentVersion.equals(newSystemVersion)){
								notCompletedUpgrade = new UpgradeSystem();
								notCompletedUpgrade.setId(newSystemVersion);
								notCompletedUpgrade.setOldSystemVersion(oldSystemVersion);
								notCompletedUpgrade.setUpdatePackageVersion(updatePackageVersion);
								notCompletedUpgrade.setSort(Long.parseLong(sort));
								notCompletedUpgrade.setExplanation(textFilterManage.filterTag_br(explanation));
								notCompletedUpgrade.setUpdatePackageFirstDirectory(folder);
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
						//	e.printStackTrace();
							if (logger.isErrorEnabled()) {
					            logger.error("读取配置文件config.properties错误",e);
					        }
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						//e1.printStackTrace();
						if (logger.isErrorEnabled()) {
				            logger.error("读取配置文件IO异常 WEB-INF/data/upgrade/"+folder+"/config.properties",e);
				        }
					}
					break;
				}
			}
			
			
			
		}
		
		returnValue.put("currentVersion", currentVersion);
		returnValue.put("notCompletedUpgrade", notCompletedUpgrade);
		returnValue.put("upgradeSystemList", upgradeSystemList);
		return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,returnValue));
	}
	
	/**
	 * 根据Id查询升级
	 */
	@ResponseBody
	@RequestMapping(params="method=queryUpgrade",method=RequestMethod.GET)
	public String queryUpgrade(ModelMap model,String upgradeSystemId,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		//错误
		Map<String,Object> error = new HashMap<String,Object>();
		if(upgradeSystemId != null && !"".equals(upgradeSystemId.trim())){
			UpgradeSystem upgradeSystem = upgradeService.findUpgradeSystemById(upgradeSystemId);
			if(upgradeSystem != null){
				//删除最后一个逗号
				String _upgradeLog = StringUtils.substringBeforeLast(upgradeSystem.getUpgradeLog(), ",");//从右往左截取到相等的字符,保留左边的

				List<UpgradeLog> upgradeLogList = JsonUtils.toGenericObject(_upgradeLog+"]", new TypeReference< List<UpgradeLog> >(){});
				upgradeSystem.setUpgradeLogList(upgradeLogList);
			
				return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,upgradeSystem));
			}else{
				error.put("upgradeSystemId", "系统升级不存在");
			}
		}else{
			error.put("upgradeSystemId", "系统升级Id不能为空");
		}
		
		
		return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
		
	}
	
	/**
	 * 立即升级
	 * @param model
	 * @param updatePackageFirstDirectory 升级包目录
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(params="method=upgradeNow",method=RequestMethod.POST)
	public String upgradeNow(ModelMap model,String updatePackageFirstDirectory,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		Map<String,String> error = new HashMap<String,String>();
		
		Long count = upgradeManage.taskRunMark_add(-1L);
		if(count >=0L){
			error.put("upgradeNow", "任务正在运行,不能升级");
		}else{
			
			upgradeManage.taskRunMark_delete();
			upgradeManage.taskRunMark_add(1L);
			
		
			
			if(updatePackageFirstDirectory != null && !"".equals(updatePackageFirstDirectory.trim())){
				
				
				//校验版本
				boolean verificationVersion = false;
				
				//上一版本号
				String beforeVersion = StringUtils.substringBefore(updatePackageFirstDirectory, "to");//截取到等于第二个参数的字符串为止
				
				UpgradeSystem notCompletedUpgrade = null;//未完成升级
				List<UpgradeSystem> upgradeSystemList = upgradeService.findAllUpgradeSystem();
				
				if(upgradeSystemList != null && upgradeSystemList.size() >0){
					for(int i =0; i<upgradeSystemList.size(); i++ ){
						UpgradeSystem upgradeSystem = upgradeSystemList.get(i);

						
						if(upgradeSystem.getId().equals(beforeVersion)){
							verificationVersion = true;
						}
						
						
						if(updatePackageFirstDirectory.equals(upgradeSystem.getUpdatePackageFirstDirectory())){
							if(upgradeSystem.getRunningStatus().equals(9999)){
								error.put("upgradeNow", "升级已完成，不能执行本操作");
							}else{
								//未完成升级
								notCompletedUpgrade = upgradeSystem;
								break;
							}
						}
					}
					if(error.size() ==0 && !verificationVersion){
						error.put("upgradeNow", "版本校验不通过");
					}
				}else{
					verificationVersion = true;
				}
				
				
				
				
				if(verificationVersion && notCompletedUpgrade == null){
					ClassPathResource resource_prop = new ClassPathResource("WEB-INF/data/upgrade/"+FileUtil.toRelativePath(updatePackageFirstDirectory)+"/config.properties");
					
					try (InputStream inputStream = resource_prop.getInputStream()){
						
						
						CommentedProperties props = new CommentedProperties();
						try {
							props.load(inputStream,"utf-8");
							//旧版本
							String oldSystemVersion = props.getProperty("oldSystemVersion");
							//升级包版本
							String updatePackageVersion = props.getProperty("updatePackageVersion");
							//新版本
							String newSystemVersion = props.getProperty("newSystemVersion");
							//说明
							String explanation = props.getProperty("explanation");
							//排序
							String sort = props.getProperty("sort");
							
							UpgradeSystem upgradeSystem = new UpgradeSystem();

							upgradeSystem.setId(newSystemVersion);
							upgradeSystem.setOldSystemVersion(oldSystemVersion);
							upgradeSystem.setUpdatePackageVersion(updatePackageVersion);
							upgradeSystem.setSort(Long.parseLong(sort));
							upgradeSystem.setRunningStatus(1);
							upgradeSystem.setExplanation(textFilterManage.filterTag_br(explanation));
							upgradeSystem.setUpdatePackageFirstDirectory(updatePackageFirstDirectory);
							
							List<String> deleteFilePathList = new ArrayList<String>();;
							
							Set<String> keyList = props.propertyNames();
							if(keyList != null && keyList.size() >0){
								for(String key : keyList){
									if(key != null && !"".equals(key.trim())){
										if(key.startsWith("delete_")){
											
											String value = props.getProperty(key);
											if(value != null && !"".equals(value.trim())){
												deleteFilePathList.add(value.trim());
											}
										}
										
									}
									
								}
							}
							upgradeSystem.setDeleteFilePath(JsonUtils.toJSONString(deleteFilePathList));
							
							UpgradeLog upgradeLog = new UpgradeLog();
							upgradeLog.setTime(new Date());
							upgradeLog.setGrade(1);
							upgradeLog.setContent("升级初始化成功");
							String upgradeLog_json = JsonUtils.toJSONString(upgradeLog);
							upgradeSystem.setUpgradeLog("["+upgradeLog_json+",");
							
							
							
							
							
							if(error.size()==0){
								try {
									upgradeService.save(upgradeSystem);
								} catch (Exception e) {
									error.put("upgradeNow", "升级错误");
									//e.printStackTrace();
									if (logger.isErrorEnabled()) {
							            logger.error("升级错误",e);
							        }
								}
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
						//	e.printStackTrace();
							if (logger.isErrorEnabled()) {
					            logger.error("读取配置文件config.properties错误",e);
					        }
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						//e1.printStackTrace();
						if (logger.isErrorEnabled()) {
				            logger.error("读取配置文件IO异常 WEB-INF/data/upgrade/"+updatePackageFirstDirectory+"/config.properties",e);
				        }
					}
				}
				
				
				
				
				
				
			}else{
				error.put("upgradeNow", "当前操作已完成");
			}
			
			
		}
		upgradeManage.taskRunMark_delete();
		
		
		if(error.size() >0){
			return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
		}else{
			return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,null));
		}
	}
	
	/**
	 * 继续升级
	 * @param model
	 * @param upgradeId 升级Id
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(params="method=continueUpgrade",method=RequestMethod.POST)
	public String continueUpgrade(ModelMap model,String upgradeId,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String,String> error = new HashMap<String,String>();
		
		
		
		Long count = upgradeManage.taskRunMark_add(-1L);
		if(count >=0L){
			error.put("upgradeNow", "任务正在运行,不能升级");
		}else{
			
			upgradeManage.taskRunMark_delete();
			upgradeManage.taskRunMark_add(1L);
			if(upgradeId != null && !"".equals(upgradeId.trim())){
				UpgradeSystem upgradeSystem = upgradeService.findUpgradeSystemById(upgradeId);
				if(upgradeSystem != null && upgradeSystem.getRunningStatus() <9999){
					
					
					//复制文件
					if(upgradeSystem.getRunningStatus() <15){
						boolean flag = true;
						
						//迁移旧模板文件
						DateTime dateTime = new DateTime();
						String dateTimeFormat = dateTime.toString("yyyy-MM-dd_HH-mm-ss");
						
						//目录后面部分
						String after = upgradeSystem.getOldSystemVersion()+"_"+dateTimeFormat;
						
						boolean common_flag = FileUtil.renameDirectory("common", "common_"+after);
						if(common_flag){
							upgradeService.addLog(upgradeId, JsonUtils.toJSONString(new UpgradeLog(new Date(),"旧模板资源 /common 迁移到 common_"+after+" 成功",1))+",");
							
						}else{
							upgradeService.addLog(upgradeId, JsonUtils.toJSONString(new UpgradeLog(new Date(),"旧模板资源 /common 迁移到 common_"+after+" 失败",2))+",");
							flag = false;
							//添加错误中断
							upgradeService.updateInterruptStatus(upgradeId, 1, JsonUtils.toJSONString(new UpgradeLog(new Date(),"出现错误中断升级过程",2))+",");
							
						}
						FileUtil.createFolder("common");
						
						boolean templates_flag = FileUtil.renameDirectory("WEB-INF"+File.separator+"foregroundView"+File.separator+"templates", "templates_"+after);
						if(templates_flag){
							upgradeService.addLog(upgradeId, JsonUtils.toJSONString(new UpgradeLog(new Date(),"旧模板文件 /WEB-INF/foregroundView/templates 迁移到 /WEB-INF/foregroundView/templates_"+after+" 成功",1))+",");
							
						}else{
							upgradeService.addLog(upgradeId, JsonUtils.toJSONString(new UpgradeLog(new Date(),"旧模板文件 /WEB-INF/foregroundView/templates 迁移到 /WEB-INF/foregroundView/templates_"+after+" 失败",2))+",");
							flag = false;
							//添加错误中断
							upgradeService.updateInterruptStatus(upgradeId, 1, JsonUtils.toJSONString(new UpgradeLog(new Date(),"出现错误中断升级过程",2))+",");
							
						}
						FileUtil.createFolder("WEB-INF"+File.separator+"foregroundView"+File.separator+"templates");
						
						if(flag){
							//更改运行状态
							upgradeService.updateRunningStatus(upgradeId ,15,JsonUtils.toJSONString(new UpgradeLog(new Date(),"旧模板文件迁移完成",1))+",");
						}
					}
					
					UpgradeSystem upgradeSystem_1 = upgradeService.findUpgradeSystemById(upgradeId);
					if(upgradeSystem_1.getInterruptStatus() != 1 && upgradeSystem_1.getRunningStatus() >=15 && upgradeSystem_1.getRunningStatus() <20){
						boolean flag = true;
						//复制新模板文件
						boolean t_common_flag = FileUtil.copyResourceDirectory("static/common", "common");
						if(t_common_flag){
							upgradeService.addLog(upgradeId, JsonUtils.toJSONString(new UpgradeLog(new Date(),"新模板资源从包内 /static/common 复制到外部目录 common 成功",1))+",");
							
						}else{
							upgradeService.addLog(upgradeId, JsonUtils.toJSONString(new UpgradeLog(new Date(),"新模板资源从包内 /static/common 复制到外部目录 common 失败",2))+",");
							flag = false;
							//添加错误中断
							upgradeService.updateInterruptStatus(upgradeId, 1, JsonUtils.toJSONString(new UpgradeLog(new Date(),"出现错误中断升级过程",2))+",");
						}
						
						boolean t_templates_flag = FileUtil.copyResourceDirectory("WEB-INF/foregroundView/templates", "WEB-INF/foregroundView/templates");
						if(t_templates_flag){
							upgradeService.addLog(upgradeId, JsonUtils.toJSONString(new UpgradeLog(new Date(),"新模板文件从包内 /WEB-INF/foregroundView/templates 复制到外部目录 /WEB-INF/foregroundView/templates 成功",1))+",");
							
						}else{
							upgradeService.addLog(upgradeId, JsonUtils.toJSONString(new UpgradeLog(new Date(),"新模板文件从包内 /WEB-INF/foregroundView/templates 复制到外部目录 /WEB-INF/foregroundView/templates 失败",2))+",");
							flag = false;
							//添加错误中断
							upgradeService.updateInterruptStatus(upgradeId, 1, JsonUtils.toJSONString(new UpgradeLog(new Date(),"出现错误中断升级过程",2))+",");
						}
						
						
						boolean upgrade_flag = FileUtil.copyResourceDirectory("WEB-INF/data/upgrade/"+upgradeSystem.getUpdatePackageFirstDirectory()+"/cms", "");
						if(upgrade_flag){
							upgradeService.addLog(upgradeId, JsonUtils.toJSONString(new UpgradeLog(new Date(),"新文件从包内 WEB-INF/data/upgrade/"+upgradeSystem.getUpdatePackageFirstDirectory()+"/cms 复制到外部目录成功",1))+",");
							
						}else{
							upgradeService.addLog(upgradeId, JsonUtils.toJSONString(new UpgradeLog(new Date(),"新文件从包内 WEB-INF/data/upgrade/"+upgradeSystem.getUpdatePackageFirstDirectory()+"/cms 复制到外部目录失败",2))+",");
							flag = false;
							//添加错误中断
							upgradeService.updateInterruptStatus(upgradeId, 1, JsonUtils.toJSONString(new UpgradeLog(new Date(),"出现错误中断升级过程",2))+",");
						}
						
						
						
							
						if(flag){
							//更改运行状态
							upgradeService.updateRunningStatus(upgradeId ,20,JsonUtils.toJSONString(new UpgradeLog(new Date(),"复制文件到外部目录完成",1))+",");
						}
						
					}
					
					
					
					
					
					UpgradeSystem upgradeSystem_2 = upgradeService.findUpgradeSystemById(upgradeId);
					//删除文件
					if(upgradeSystem_2.getInterruptStatus() != 1 && upgradeSystem_2.getRunningStatus() >=20 && upgradeSystem_2.getRunningStatus()<40){
						boolean flag = true;
						if(upgradeSystem_2.getDeleteFilePath() != null && !"".equals(upgradeSystem_2.getDeleteFilePath().trim())){
							upgradeService.updateRunningStatus(upgradeId ,30,JsonUtils.toJSONString(new UpgradeLog(new Date(),"执行删除文件任务",1))+",");
							
							List<String> deleteFilePathList = JsonUtils.toGenericObject(upgradeSystem_2.getDeleteFilePath(), new TypeReference< List<String> >(){});
							if(deleteFilePathList != null && deleteFilePathList.size() >0){
								
								for(String deleteFilePath : deleteFilePathList){
									File file = new File(PathUtil.defaultExternalDirectory()+File.separator+FileUtil.toSystemPath(deleteFilePath));
									if(file.exists()){
										if(file.isDirectory()){//目录
											try {
												localFileManage.removeDirectory(FileUtil.toSystemPath(deleteFilePath));
											} catch (Exception e) {
												flag = false;
												upgradeService.addLog(upgradeId, JsonUtils.toJSONString(new UpgradeLog(new Date(),"删除目录失败--> "+deleteFilePath,1))+",");
												//添加错误中断
												upgradeService.updateInterruptStatus(upgradeId, 1, JsonUtils.toJSONString(new UpgradeLog(new Date(),"出现错误中断升级过程",2))+",");
												
												if (logger.isErrorEnabled()) {
										            logger.error("删除目录失败"+deleteFilePath,e);
										        }
												break;
												
											}
											
										}else{//文件
											try {
												localFileManage.deleteFile(FileUtil.toSystemPath(deleteFilePath));
											} catch (Exception e) {
												flag = false;
												upgradeService.addLog(upgradeId, JsonUtils.toJSONString(new UpgradeLog(new Date(),"删除文件失败--> "+deleteFilePath,1))+",");
												//添加错误中断
												upgradeService.updateInterruptStatus(upgradeId, 1, JsonUtils.toJSONString(new UpgradeLog(new Date(),"出现错误中断升级过程",2))+",");
												
												if (logger.isErrorEnabled()) {
										            logger.error("删除文件失败"+deleteFilePath,e);
										        }
												break;
												
											}
											
										}
										
									}
								}	
							}
						}
						if(flag){
							//更改运行状态
							upgradeService.updateRunningStatus(upgradeId ,40,JsonUtils.toJSONString(new UpgradeLog(new Date(),"删除文件任务结束",1))+",");
					
						}
					}
					
					
					
				//	upgradeService.updateInterruptStatus(upgradeId, 2, JsonUtils.toJSONString(new UpgradeLog(new Date(),"升级过程中断,等待应用服务器重启",1))+",");
					/**
					
					UpgradeSystem upgradeSystem_3 = upgradeService.findUpgradeSystemById(upgradeId);
					//重启服务器
					if(upgradeSystem_3.getInterruptStatus() != 1 && upgradeSystem_3.getRunningStatus() >=40 && upgradeSystem_3.getRunningStatus()<100){
						if(restart == true){//未重启
							//添加重启服务器中断
							upgradeService.updateInterruptStatus(upgradeId, 2, JsonUtils.toJSONString(new UpgradeLog(new Date(),"需要重启应用服务器才能继续升级",1))+",");
							
						}else{//已重启
							//更改运行状态
							upgradeService.updateRunningStatus(upgradeId ,100,JsonUtils.toJSONString(new UpgradeLog(new Date(),"系统已重启完成",1))+",");
					
						}
					}
					**/

					UpgradeSystem upgradeSystem_4 = upgradeService.findUpgradeSystemById(upgradeId);
					if(upgradeSystem_4.getInterruptStatus() != 1 && upgradeSystem_4.getRunningStatus() >= 40){
						//执行处理数据
						upgradeManage.manipulationData(upgradeId);
					}
					
				}else{
					error.put("upgradeNow", "当前升级不存在");
				}
			}else{
				error.put("upgradeNow", "升级参数错误");
			}
			
		}

		if(error.size() >0){
			return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
		}else{
			return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,null));
		}
	}
	
	
	
	/**
	 * 查询升级包列表(本方法不用)
	 */
	@ResponseBody
	@RequestMapping(params="method=queryUpgradePackageList",method=RequestMethod.GET)
	public String queryUpgradePackageList(ModelMap model,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		List<UpgradePackage> upgradePackageList = new ArrayList<UpgradePackage>();

		return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,upgradePackageList));
	}
	
	/**
	 * 上传升级包(本方法不用)
	 */
	@ResponseBody
	@RequestMapping(params="method=uploadUpgradePackage",method=RequestMethod.POST)
	public String uploadUpgradePackage(ModelMap model,
			MultipartHttpServletRequest request) throws Exception {
		Map<String,String> error = new HashMap<String,String>();
		
		if(error.size() >0){
			return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
		}else{
			return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,null));
		}
	}
	
	/**
	 * 删除升级包(本方法不用)
	 */
	@ResponseBody
	@RequestMapping(params="method=deleteUpgradePackage",method=RequestMethod.POST)
	public String deleteUpgradePackage(ModelMap model,String fileName,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		//错误
		Map<String,Object> error = new HashMap<String,Object>();
		
		return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
	}
}
