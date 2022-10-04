package cms.web.action.install;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cms.bean.RequestResult;
import cms.bean.ResultCode;
import cms.bean.install.Install;
import cms.service.data.DataService;
import cms.utils.FileUtil;
import cms.utils.JasyptUtil;
import cms.utils.JsonUtils;
import cms.utils.PathUtil;
import cms.utils.SHA;
import cms.utils.SqlFile;
import cms.utils.UUIDUtil;


/**
 * 安装系统
 *
 */
@Controller
public class InstallManageAction {
	private static final Logger logger = LogManager.getLogger(InstallManageAction.class);
	
	@Resource InstallManage installManage; 
	@Resource DataService mySqlDataService;
	
	
	@Autowired
    private DataSource dataSource;
	
	//数据库密码
	@Value("${spring.datasource.password}") 
	private String password;
	
	//盐值 数据库密码加密所需的salt(盐)
	@Value("${jasypt.encryptor.password}") 
	private String salt;
	
	//是否允许显示图形界面安装系统
	@Value("${bbs.allowInstallSystem}") 
    private boolean allowInstallSystem = false; 
	
	
	
	/**
	 * 安装 添加
	 */
	@RequestMapping(value="/install",method=RequestMethod.GET)
	public String addUI(ModelMap model,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		if(!allowInstallSystem){
			return null;
		}
		
		if(isCreateFolder()){
			LinkedHashMap<String,Integer> folderInfoMap = installManage.createFolder(installManage.folderList());
			
			model.addAttribute("folderInfoMap",folderInfoMap);
			
			Map<String,String> errorMap = installManage.createInstallStatus();
			model.addAttribute("errorMap",errorMap);
		}else{
			List<String> pathList = new ArrayList<String>();
			pathList.add("WEB-INF/data/install/status.txt");
			Map<String,String> errorMap = installManage.isFilePermission(pathList);
			for (Map.Entry<String, String> entry : errorMap.entrySet()) {
				model.addAttribute("status", "安装状态文件"+entry.getKey()+" "+entry.getValue());
			}
		}
		
		
		
		
		if(isInstallSystem()){
			return "/install";	//WEB-INF/data/install/install.html
		}
		
		
		return null;
	}

	
	/**
	 * 安装 添加
	 */
	@ResponseBody
	@RequestMapping(value="/install",method=RequestMethod.POST)
	public String add(ModelMap model,Install formbean,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		if(!allowInstallSystem){
			return null;
		}
		
		
		Map<String,String> error = new HashMap<String,String>();
		
		
		if(!this.isInstallSystem()){
			error.put("installSystem", "不允许安装系统");
		}else{
			//检测文件权限
			List<String> pathList = new ArrayList<String>();
			pathList.add("WEB-INF/data/install/status.txt");//待检测文件
			Map<String,String> errorMap = installManage.isFilePermission(pathList);
			for (Map.Entry<String, String> entry : errorMap.entrySet()) {
				error.put("status", "安装状态文件"+entry.getKey()+" "+entry.getValue());
			}
			
			if(formbean.getDatabasePassword() == null || "".equals(formbean.getDatabasePassword().trim())){
				error.put("databasePassword", "数据库密码不能为空");
			}
			if(formbean.getUserAccount() == null || "".equals(formbean.getUserAccount().trim())){
				error.put("userAccount", "管理员账号不能为空");
			}
			if(formbean.getUserPassword() == null || "".equals(formbean.getUserPassword().trim())){
				error.put("userPassword", "管理员密码不能为空");
			}
			
			//校验数据库密码
			if(error.size() ==0){
				String jdbc_password = password;
				
				if(salt != null && !"".equals(salt.trim())){
					PooledPBEStringEncryptor encryptor = JasyptUtil.config(salt);
					encryptor.setPassword(salt.trim());//加密所需的salt(盐)
					if(PropertyValueEncryptionUtils.isEncryptedValue(password)){//判断是否为加密值 被ENC()包裹
						jdbc_password = PropertyValueEncryptionUtils.decrypt(password, encryptor);
					}
				}
				if(!jdbc_password.equals(formbean.getDatabasePassword().trim())){
					error.put("databasePassword", "数据库密码错误");
				}
			}
			
			//校验数据库连接
			if(error.size() ==0){
				Connection conn = null;
				ResultSet rs = null;
				ResultSet rs2 = null;
				try {
					conn = dataSource.getConnection();
					
					//获取所有表
					rs = conn.getMetaData().getTables(conn.getCatalog(), "%", "%", new String[]{"TABLE"});
					List<String> tableNameList = new ArrayList<String>();
					while(rs.next()){ 
						tableNameList.add(rs.getString("TABLE_NAME"));
					} 
					
					
				
					//通过查询运行设置字符集的命令
					rs2 = conn.prepareStatement("show variables like 'char%'").executeQuery();
					
					String character_set_database = "";
					
					while(rs2.next()){ 
						if("character_set_database".equalsIgnoreCase(rs2.getString(1))){
							character_set_database = rs2.getString(2);
						}	
					}
					if(!"utf8mb4".equalsIgnoreCase(character_set_database)){
						error.put("databaseName", "数据库必须为utf8mb4编码");
					}
					
					int count = 0;
					//如果数据库表已有数据则不允许安装
					for(String tableName : tableNameList){
						Long c = mySqlDataService.findCount(tableName);
						if(c >0L){
							count++;
						}
					}
					
					if(count >0){
						error.put("installSystem", "数据库表已含有数据，不允许安装");
					}
	
				}catch (Exception e) {
					error.put("databaseLink", "数据库连接错误");
					if (logger.isErrorEnabled()) {
    		            logger.error("校验数据库连接错误",e);
    		        }
				//	e.printStackTrace();
				}finally {
					if(rs != null){
						try {
							rs.close();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
						//	e.printStackTrace();
							error.put("databaseLink", "数据库关闭查询所有表错误");
						}
					}
					if(rs2 != null){
						try {
							rs2.close();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
						//	e.printStackTrace();
							error.put("databaseLink", "数据库关闭查询字符集编码错误");
						}
					}
					if(conn != null){
						try {
							conn.close();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
						//	e.printStackTrace();
							error.put("databaseLink", "数据库关闭错误");
						}
					}
				}
				
			}
			
			if(error.size() ==0){
				Connection conn = null;
	    		Statement stmt = null;
	    		InputStream quartz_inputStream = null;
	    		InputStream data_inputStream = null;
				try {
					conn = dataSource.getConnection();
					//通过查询运行设置字符集的命令
	                conn.prepareStatement("set names utf8mb4").executeQuery();
			
					//String path = PathUtil.path()+File.separator+"WEB-INF"+File.separator+"data"+File.separator+"install"+File.separator;
					
					
					
					ClassPathResource data_classPathResource = new ClassPathResource("WEB-INF/data/install/data_tables_mysql.sql");	
					data_inputStream = data_classPathResource.getInputStream();
					//导入quartz SQL文件
					SqlFile.importSQL(conn, new InputStreamReader(data_inputStream,Charset.forName("utf-8")));
					
					
					//插入管理员数据
					//INSERT INTO `sysusers` (`userId`,`enabled`,`fullName`,`issys`,`userAccount`,`userDesc`,`userDuty`,`userPassword`) VALUES ('0e2abc06-a71a-40ed-b449-a55c1a5b6a68',b'1','fdsf',b'0','fdsfds',NULL,NULL,'5cf74b96bcc721bf1a97674550dff37e225d72766c3d5969e8638f57d8d4809e7e7b7b87f796582c')
					PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();//BCrypt密码算法,Bcrypt加密最长为72个字符，超过72字符的部分被截断丢弃了
					
					// 密码通过盐值加密以备存储入数据库
					String newPassword = passwordEncoder.encode(SHA.sha256Hex(formbean.getUserPassword().trim()));
					
					String sql = "INSERT INTO `sysusers` (`userId`,`enabled`,`fullName`,`issys`,`securityDigest`,`userAccount`,`userDesc`,`userDuty`,`userPassword`) VALUES ('"+UUIDUtil.getUUID32()+"',b'1','"+formbean.getUserAccount().trim()+"',b'1','"+UUIDUtil.getUUID32()+"','"+formbean.getUserAccount().trim()+"',NULL,'管理员','"+newPassword+"')";
					
					stmt = conn.createStatement();
					stmt.executeUpdate(sql);
					conn.commit();  
					
				}catch (RuntimeException e) {
					error.put("installSystem", "导入数据库错误");
					
				}catch (Exception e) {
					error.put("installSystem", "安装轻论坛系统数据库连接数据库错误");
					if (logger.isErrorEnabled()) {
			            logger.error("安装轻论坛系统数据库连接数据库错误",e);
			        }
			//		e.printStackTrace();
				}finally {
					if(quartz_inputStream != null){
						quartz_inputStream.close();
					}
					if(data_inputStream != null){
						data_inputStream.close();
					}
					
					if(stmt != null){
						try {
							stmt.close();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
						//	e.printStackTrace();
							if (logger.isErrorEnabled()) {
					            logger.error("安装轻论坛系统数据库关闭Statement错误",e);
					        }
						}
					}
					
					
					if(conn != null){
						try {
							conn.close();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
						//	e.printStackTrace();
							if (logger.isErrorEnabled()) {
					            logger.error("安装轻论坛系统数据库关闭错误",e);
					        }
						}
					}
				}
			}
			
			//复制文件
			if(error.size() ==0){
				FileUtil.copyResourceDirectory("WEB-INF/foregroundView/templates", "WEB-INF/foregroundView/templates");
				FileUtil.copyResourceDirectory("static/common", "common");
				
				
				//写入禁止安装系统
				FileUtil.writeStringToFile("WEB-INF"+File.separator+"data"+File.separator+"install"+File.separator+"status.txt","1","utf-8",false);
			}
			
			
			
			if(error.size() ==0){
				return JsonUtils.toJSONString(new RequestResult(ResultCode.SUCCESS,null));
			}
			
		}
		return JsonUtils.toJSONString(new RequestResult(ResultCode.FAILURE,error));
	}






	/**
	 * 是否允许安装系统
	 * @return
	 */
	private boolean isInstallSystem(){
		
		//读取版本文件
    	String version = FileUtil.readFileToString("WEB-INF"+File.separator+"data"+File.separator+"install"+File.separator+"status.txt","UTF-8");
    	if(version.equals("0")){
    		return true;
    	}else{
    		return false;
    	}
	}
	
	/**
	 * 是否创建文件夹
	 * @return
	 */
	private boolean isCreateFolder(){
		//生成文件目录
		Path path = Paths.get(PathUtil.defaultExternalDirectory()+File.separator+"WEB-INF"+File.separator+"data"+File.separator+"install"+File.separator+"status.txt");
		if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {//文件不存在
			return true;
		}else{
			return false;
		}
	}
}

