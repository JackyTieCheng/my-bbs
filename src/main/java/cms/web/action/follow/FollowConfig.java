package cms.web.action.follow;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 关注配置
 *
 */
@Component("followConfig")
public class FollowConfig {
	@Resource FollowManage followManage;
	
	/** 分表数量 **/
	@Value("${bbs.sharding.followConfig_tableQuantity}") 
	private Integer tableQuantity = 1;
	
	public Integer getTableQuantity() {
		return tableQuantity;
	}
	
	/**
	  * 根据关注Id查询分配到表编号
	  * 根据关注Id和关注分表数量求余
	  * @param followId 关注Id
	  * 注意：关注Id要先验证
	  * followManage.verificationFollowId(?)
	  * @return
	 */
	public Integer followIdRemainder(String followId){
	   int userId = followManage.getFollowAfterId(followId);
	   return userId % this.getTableQuantity();
	} 
   /**
    * 根据用户Id查询分配到表编号
    * 根据用户Id和关注分表数量求余(用户Id后四位)
    * @param userId 用户Id
    * @return
    */
	public Integer userIdRemainder(Long userId){
	   	//选取得后N位用户Id
	   	String afterUserId = String.format("%04d", userId%10000);
	   	return Integer.parseInt(afterUserId) % this.getTableQuantity();
	}
	 
}
