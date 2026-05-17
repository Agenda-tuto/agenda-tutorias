package com.example.agenda_tutoria.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from:agendatutorias@gmail.com}")
    private String fromAddress;

    public void enviarCodigoVerificacion(String destinatario, String nombre, String codigo) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(destinatario);
            helper.setSubject("Verifica tu cuenta — Agenda Tutorías");

            String html = """
                <div style="font-family:'Segoe UI',sans-serif;max-width:520px;margin:0 auto;background:#f4f6f9;padding:32px;border-radius:16px;">
                    <div style="background:#1a3a5c;border-radius:12px;padding:28px;text-align:center;margin-bottom:24px;">
                        <h2 style="color:white;margin:0;font-size:1.4rem;">📚 Agenda Tutorías</h2>
                        <p style="color:rgba(255,255,255,0.7);margin:8px 0 0;font-size:0.9rem;">Tecnológico Comfenalco</p>
                    </div>
                    <div style="background:white;border-radius:12px;padding:28px;">
                        <h3 style="color:#1a3a5c;margin-top:0;">Hola, %s 👋</h3>
                        <p style="color:#555;line-height:1.6;">
                            Gracias por registrarte en Agenda Tutorías. Para activar tu cuenta
                            e ingresa el siguiente código de verificación:
                        </p>
                        <div style="background:#f4f6f9;border-radius:12px;padding:24px;text-align:center;margin:24px 0;">
                            <p style="color:#7f8c8d;font-size:0.82rem;text-transform:uppercase;letter-spacing:1px;margin:0 0 8px;">
                                Tu código de verificación
                            </p>
                            <p style="font-size:2.5rem;font-weight:700;letter-spacing:10px;color:#1a3a5c;margin:0;">
                                %s
                            </p>
                        </div>
                        <p style="color:#999;font-size:0.85rem;">
                            Este código es válido por 15 minutos. Si no creaste esta cuenta, ignora este mensaje.
                        </p>
                        <div style="background:#eafaf1;border-radius:10px;padding:14px;margin-top:16px;">
                            <p style="color:#1e8449;margin:0;font-size:0.9rem;">
                                💰 Al verificar tu cuenta recibirás <strong>$50.000</strong> de bono de bienvenida.
                            </p>
                        </div>
                    </div>
                    <p style="text-align:center;color:#bbb;font-size:0.78rem;margin-top:20px;">
                        © 2026 Agenda Tutorías — Tecnológico Comfenalco
                    </p>
                </div>
                """.formatted(nombre, codigo);

            helper.setText(html, true);
            mailSender.send(mensaje);

        } catch (Exception e) {
            System.err.println("Error enviando correo: " + e.getMessage());
        }
    }

    public void enviarNotificacion(String destinatario, String nombre,
                                    String titulo, String cuerpo) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(destinatario);
            helper.setSubject(titulo + " — Agenda Tutorías");

            String html = """
                <div style="font-family:'Segoe UI',sans-serif;max-width:520px;margin:0 auto;background:#f4f6f9;padding:32px;border-radius:16px;">
                    <div style="background:#1a3a5c;border-radius:12px;padding:28px;text-align:center;margin-bottom:24px;">
                        <h2 style="color:white;margin:0;font-size:1.4rem;">📚 Agenda Tutorías</h2>
                    </div>
                    <div style="background:white;border-radius:12px;padding:28px;">
                        <h3 style="color:#1a3a5c;margin-top:0;">%s</h3>
                        <p style="color:#555;line-height:1.6;">Hola %s,</p>
                        <p style="color:#555;line-height:1.6;">%s</p>
                        <a href="http://localhost:8080/login"
                           style="display:inline-block;background:#c0392b;color:white;
                                  padding:12px 24px;border-radius:10px;text-decoration:none;
                                  font-weight:600;margin-top:16px;">
                            Ver en la plataforma
                        </a>
                    </div>
                    <p style="text-align:center;color:#bbb;font-size:0.78rem;margin-top:20px;">
                        © 2026 Agenda Tutorías — Tecnológico Comfenalco
                    </p>
                </div>
                """.formatted(titulo, nombre, cuerpo);

            helper.setText(html, true);
            mailSender.send(mensaje);

        } catch (Exception e) {
            System.err.println("Error enviando notificación: " + e.getMessage());
        }
    }
}