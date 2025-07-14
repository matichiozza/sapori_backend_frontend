package com.example.demo.services;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.sender.email}")
    private String senderEmail;

    public void enviarCodigoConfirmacion(String destinatario, String codigo) {
        Email from = new Email(senderEmail);
        String subject = "Tu código de confirmación";
        Email to = new Email(destinatario);
        String contenidoTexto = "Hola,\n\nTu código de confirmación es: " + codigo + "\nEste código expira en 30 minutos.";
        Content content = new Content("text/plain", contenidoTexto);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
        } catch (IOException ex) {
            throw new RuntimeException("Error al enviar email", ex);
        }
    }

}
