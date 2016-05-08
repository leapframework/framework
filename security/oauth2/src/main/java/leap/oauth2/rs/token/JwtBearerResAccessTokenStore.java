package leap.oauth2.rs.token;

import leap.core.security.token.jwt.JwtVerifier;
import leap.core.security.token.jwt.RsaVerifier;
import leap.lang.Result;
import leap.lang.security.RSA;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

/**
 * Created by KAEL on 2016/5/8.
 */
public class JwtBearerResAccessTokenStore implements ResBearerAccessTokenStore  {

    protected RSAPublicKey publicKey;
    protected final JwtVerifier verifier;

    public JwtBearerResAccessTokenStore(String publicKey) {
        this.publicKey = RSA.decodePublicKey(publicKey);
        verifier = new RsaVerifier(this.publicKey);
    }


    @Override
    public Result<ResAccessTokenDetails> loadAccessTokenDetails(ResAccessToken token) {
        Map<String,Object> jwtDetail = verifier.verify(token.getToken());
        SimpleResAccessTokenDetails resAccessTokenDetails = new SimpleResAccessTokenDetails();

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
