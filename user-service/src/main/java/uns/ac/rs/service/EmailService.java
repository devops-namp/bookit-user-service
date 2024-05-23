package uns.ac.rs.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import uns.ac.rs.entity.RegistrationInfo;

@ApplicationScoped
public class EmailService {

    @Inject
    Mailer mailer;

    @Inject
    Template registrationCodeTemplate;

    public void sendRegistrationCodeEmail(RegistrationInfo registrationInfo) {
        var instance = registrationCodeTemplate
            .data("name", registrationInfo.getFirstName())
            .data("code", registrationInfo.getCode());
        mailer.send(Mail.withHtml(registrationInfo.getEmail(), "[BookIt] Registration code", instance.render()));
    }
}
