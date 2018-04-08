package leap.oauth2.server.authc;

import leap.lang.Out;

public interface AuthzAuthenticationHandler {

	/**
	 * 创建AuthzAuthentication的授权信息
	 * @param context 上下文
	 * @param authc 创建成功，待返回的AuthzAuthentication
	 * @return 返回true,表示已处理，结束后续的创建过程，false，表示未处理
	 */
	default boolean createAuthzAuthentication(AuthzAuthenticationContext context,Out<AuthzAuthentication> authc){
		return false;
	}
}
