package com.example.agenda_tutoria.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${app.resend.api-key:}")
    private String apiKey;

    @Value("${app.resend.from:onboarding@resend.dev}")
    private String fromAddress;

    @Async
    public void enviarCodigoVerificacion(String destinatario, String nombre, String codigo) {
        if (apiKey.isBlank()) {
            log.warn("RESEND_API_KEY no configurada");
            return;
        }

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
                        ingresa el siguiente código de verificación:
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
                        Este código es válido por 15 minutos.
                    </p>
                </div>
            </div>
            """.formatted(nombre, codigo);

        try {
            String json = """
                {"from":"%s","to":["%s"],"subject":"%s","html":"%s"}
                """.formatted(fromAddress, destinatario,
                    "Verifica tu cuenta — Agenda Tutorías",
                    html.replace("\"", "\\\"").replace("\n", "\\n"));

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.resend.com/emails"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .timeout(Duration.ofSeconds(15))
                .build();

            HttpResponse<String> res = HttpClient.newHttpClient()
                .send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() == 200) {
                log.info("Código enviado a {}", destinatario);
            } else {
                log.error("Resend error {}: {}", res.statusCode(), res.body());
            }
        } catch (Exception e) {
            log.error("Error enviando código a {}: {}", destinatario, e.getMessage());
        }
    }
}