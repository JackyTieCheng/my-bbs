package cms.web.action.common;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import cms.service.template.TemplateService;
import cms.web.action.AccessSourceDeviceManage;

/**
 * favicon.ico图标
 *
 */
@Controller
public class FaviconAction {
	//注入业务bean
	@Resource(name="templateServiceBean")
	private TemplateService templateService;//通过接口引用代理返回的对象
	
	@Resource AccessSourceDeviceManage accessSourceDeviceManage;
		
	
	@RequestMapping(value={"/favicon.ico","/apple-touch-icon.png","/apple-touch-icon-120x120.png"})
	public String favicon(HttpServletRequest request, HttpServletResponse response
			)throws Exception {
		
		//当前模板使用的目录
		String dirName = templateService.findTemplateDir_cache();

		return "forward:/common/"+dirName+"/"+accessSourceDeviceManage.accessDevices(request)+"/images/favicon.ico";
	}
}
