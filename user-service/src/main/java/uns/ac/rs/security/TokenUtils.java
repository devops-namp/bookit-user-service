package uns.ac.rs.security;

import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import uns.ac.rs.entity.Role;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


@ApplicationScoped
public class TokenUtils {

    @Inject
    JWTParser jwtParser;

    @ConfigProperty(name = "quarkusjwt.jwt.duration")
    Long duration;
    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    public String generateToken(String username, Role role) throws Exception {
        var privateKeyLocation = "/privatekey.pem";
        var privateKey = readPrivateKey(privateKeyLocation);

        var claimsBuilder = Jwt.claims();
        var currentTimeInSeconds = currentTimeInSeconds();

        var groups = new HashSet<String>();
        groups.add(role.toString());

        claimsBuilder.issuer(issuer);
        claimsBuilder.subject(username);
        claimsBuilder.issuedAt(currentTimeInSeconds);
        claimsBuilder.expiresAt(currentTimeInSeconds + duration);
        claimsBuilder.groups(groups);

        return claimsBuilder.jws().keyId(privateKeyLocation).sign(privateKey);
    }

    public boolean validateToken(String token) {
        try {
            JsonWebToken jwt = jwtParser.parse(token);
            if (!jwt.getIssuer().equals(issuer) || jwt.getExpirationTime() <= new Date().getTime() / 1000) {
                return false;
            }
            return true;
        } catch (ParseException e) {
            return false;
        }
    }


    private PrivateKey readPrivateKey(final String pemResName) throws Exception {
        try (var contentIS = TokenUtils.class.getResourceAsStream(pemResName)) {
            byte[] tmp = new byte[4096];
            int length = contentIS.read(tmp);
            return decodePrivateKey(new String(tmp, 0, length, StandardCharsets.UTF_8));
        }
    }

    private PrivateKey decodePrivateKey(final String pemEncoded) throws Exception {
        byte[] encodedBytes = toEncodedBytes(pemEncoded);

        var keySpec = new PKCS8EncodedKeySpec(encodedBytes);
        var kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

    private byte[] toEncodedBytes(final String pemEncoded) {
        final String normalizedPem = removeBeginEnd(pemEncoded);
        return Base64.getDecoder().decode(normalizedPem);
    }

    private String removeBeginEnd(String pem) {
        pem = pem.replaceAll("-----BEGIN (.*)-----", "");
        pem = pem.replaceAll("-----END (.*)----", "");
        pem = pem.replaceAll("\r\n", "");
        pem = pem.replaceAll("\n", "");
        return pem.trim();
    }

    private int currentTimeInSeconds() {
        return (int) (System.currentTimeMillis() / 1000);
    }

}
