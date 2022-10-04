package cms.web.filter;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;

import cms.utils.threadLocal.AccessUserThreadLocal;
import cms.utils.threadLocal.CSRFTokenThreadLocal;
import cms.utils.threadLocal.TemplateThreadLocal;

/**
 * 监听器清空ThreadLocal
 * @author Gao
 *
 */
@WebListener
public class ThreadLocalListener implements ServletRequestListener {

	/**
	 * 对销毁客户端进行监听，即当执行request.removeAttribute("XXX")时调用
	 */
	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		//删除当前线程副本
    	TemplateThreadLocal.removeThreadLocal();
    	AccessUserThreadLocal.removeThreadLocal();
    	CSRFTokenThreadLocal.removeThreadLocal();
	}

	
	
	
	/**
	 * request初始化，对实现客户端的请求进行监听
	 */
	@Override
	public void requestInitialized(ServletRequestEvent sre) {
		// TODO Auto-generated method stub
		
	}

}
