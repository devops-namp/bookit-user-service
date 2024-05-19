package uns.ac.rs.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import uns.ac.rs.entity.TempUser;

@ApplicationScoped
public class EmailService {

    @Inject
    Mailer mailer;

    @Inject
    Template registrationCodeTemplate;

    public void sendRegistrationCodeEmail(TempUser tempUser) {
        var instance = registrationCodeTemplate
            .data("name", tempUser.getFirstName())
            .data("code", tempUser.getCode());
        mailer.send(Mail.withHtml(tempUser.getEmail(), "[BookIt] Registration code", instance.render()));
    }
}
