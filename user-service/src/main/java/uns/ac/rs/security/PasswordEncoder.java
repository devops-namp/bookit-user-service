package uns.ac.rs.security;

import jakarta.enterprise.context.RequestScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@RequestScoped
public class PasswordEncoder {
    @ConfigProperty(name = "quarkusjwt.password.secret")
    String secret;
    @ConfigProperty(name = "quarkusjwt.password.iteration")
    Integer iteration;
    @ConfigProperty(name = "quarkusjwt.password.keylength")
    Integer keyLength;


    public String encode(CharSequence cs) {
        try {
            var result = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
                    .generateSecret(new PBEKeySpec(cs.toString().toCharArray(), secret.getBytes(), iteration, keyLength))
                    .getEncoded();
            return Base64.getEncoder().encodeToString(result);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new RuntimeException(ex);
        }
    }
}
