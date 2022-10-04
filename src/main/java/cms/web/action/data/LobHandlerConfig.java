package cms.web.action.data;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;

/**
 * 操作Clob字段
 * @author Gao
 *
 */
@Configuration
public class LobHandlerConfig {
	
	/**
	 * 指定lobHandler时，对于MySQL、DB2、MS SQL Server、Oracle 10g，使用DefaultLobHandler即可，而Oracle 9i，
	 * 则可以使用OracleLobHandler。因为Oracle9i处理lob的方式和不太一样,所以这里要用spring提供的SimpleNativeJdbcExtractor.
	 * @return
	 */
	@Bean
    public LobHandler lobHandler() {
        return new DefaultLobHandler();
    }
}
