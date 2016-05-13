package leap.oauth2.rs.token;

import leap.core.annotation.Inject;
import leap.core.security.token.jwt.JwtVerifier;
import leap.core.security.token.jwt.RsaVerifier;
import leap.lang.Result;
import leap.lang.security.RSA;
import leap.web.security.SecurityConfig;
import leap.web.security.user.UserDetails;

import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.Map;

/**
 * Created by KAEL on 2016/5/8.
 */
public class JwtBearerResAccessTokenStore implements ResBearerAccessTokenStore  {

    protected RSAPublicKey               publicKey;
    protected final JwtVerifier         verifier;
    protected @Inject SecurityConfig     sc;

    public JwtBearerResAccessTokenStore(String publicKey) {
        this.publicKey = RSA.decodePublicKey(publicKey);
        verifier = new RsaVerifier(this.publicKey);
    }


    @Override
    public Result<ResAccessTokenDetails> loadAccessTokenDetails(ResAccessToken token) {
        Map<String,Object> jwtDetail = verifier.verify(token.getToken());
        SimpleResAccessTokenDetails resAccessTokenDetails = new SimpleResAccessTokenDetails();
        UserDetails ud = sc.getUserStore().loadUserDetailsByLoginName((String)jwtDetail.remove("username"));
        resAccessTokenDetails.setUserId(ud.getIdAsString());
        resAccessTokenDetails.setScope((String)jwtDetail.remove("scope"));
        resAccessTokenDetails.setClientId((String)jwtDetail.remove("client_id"));
        //TODO How to ensure is expired?
        resAccessTokenDetails.setCreated(Calendar.getInstance().getTimeInMillis());
        resAccessTokenDetails.setExpiresIn(120*1000);
        return Result.of(resAccessTokenDetails);
    }

    @Override
    public void removeAccessToken(ResAccessToken token) {
        //Do nothing
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(RSAPublicKey publicKey) {
        this.publicKey = publicKey;
    }
}
