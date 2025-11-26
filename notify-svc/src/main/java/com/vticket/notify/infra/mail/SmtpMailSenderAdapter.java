package com.vticket.notify.infra.mail;

import com.vticket.notify.app.usecase.MessageService;
import com.vticket.notify.domain.port.MailSenderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpMailSenderAdapter implements MailSenderPort {

    private static final String LOG_PREFIX = "[SmtpMailSenderAdapter]";
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MessageService messageService;

    @Override
    public void sendOtpEmail(EmailRequest request) {
        log.info("{}|request={}", LOG_PREFIX, request);
        try {
//            //tạo email mime hỗ trợ HTML
//            MimeMessage msg = mailSender.createMimeMessage();
//
//            //set encoding và nội dung email
//            MimeMessageHelper helper = new MimeMessageHelper(msg, "UTF-8");
//
//            helper.setTo(request.getTo());
//            helper.setSubject(request.getSubject());
//
//            //tạo context thymeleaf để truyền biến vào template
//            Context ctx = new Context();
//            ctx.setVariables(request.getVariables());
//
//            //render template HTML to string
//            String html = templateEngine.process(request.getTemplateName(), ctx);
//
//            helper.setText(html, true);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getTo());
            message.setSubject(messageService.get("email.otp.subject"));
            message.setText(messageService.get("email.otp.body", request.getVariables().get("otp")));
            mailSender.send(message);

        } catch (Exception e) {
            log.error("{}|FAILED|Exception={}", LOG_PREFIX, e.getMessage(), e);
        }
    }

}
