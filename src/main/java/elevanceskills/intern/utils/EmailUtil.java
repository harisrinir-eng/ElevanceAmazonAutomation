package elevanceskills.intern.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/** Sends e-mail through an SMTP server configured ONLY via environment variables (Jakarta Mail). */
public final class EmailUtil {

    /** SMTP connection settings. The password is never included in toString(). */
    public record SmtpSettings(String host, int port, String username, String password, String from, String to) {

        public static SmtpSettings fromEnv() {
            return fromMap(System.getenv());
        }

        /** Validates all six variables and reports every problem at once. */
        public static SmtpSettings fromMap(Map<String, String> env) {
            List<String> problems = new ArrayList<>();
            String host = value(env, "SMTP_HOST", problems);
            String portText = value(env, "SMTP_PORT", problems);
            String user = value(env, "SMTP_USERNAME", problems);
            String pass = value(env, "SMTP_PASSWORD", problems);
            String from = value(env, "SMTP_FROM", problems);
            String to = value(env, "SMTP_TO", problems);

            int port = 0;
            if (portText != null) {
                try {
                    port = Integer.parseInt(portText);
                    if (port < 1 || port > 65535) {
                        problems.add("SMTP_PORT must be between 1 and 65535");
                    }
                } catch (NumberFormatException e) {
                    problems.add("SMTP_PORT must be a number");
                }
            }
            if (from != null && !from.contains("@")) {
                problems.add("SMTP_FROM must be an e-mail address");
            }
            if (to != null && !to.contains("@")) {
                problems.add("SMTP_TO must be an e-mail address");
            }
            if (!problems.isEmpty()) {
                throw new IllegalStateException("Invalid SMTP environment configuration: " + problems);
            }
            return new SmtpSettings(host, port, user, pass, from, to);
        }

        private static String value(Map<String, String> env, String name, List<String> problems) {
            String v = env.get(name);
            if (v == null || v.isBlank()) {
                problems.add(name + " is not set");
                return null;
            }
            return v.trim();
        }

        @Override
        public String toString() {
            return "SmtpSettings[host=" + host + ", port=" + port + ", username=" + username
                    + ", password=****, from=" + from + ", to=" + to + "]";
        }
    }

    private final SmtpSettings settings;

    public EmailUtil(SmtpSettings settings) {
        this.settings = settings;
    }

    /**
     * Submits one message to the SMTP server.
     * Returning normally means only that the server ACCEPTED the message - not that it was delivered.
     */
    public void send(String subject, String body) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", settings.host());
        props.put("mail.smtp.port", String.valueOf(settings.port()));
        props.put("mail.smtp.auth", "true");
        if (settings.port() == 465) {
            props.put("mail.smtp.ssl.enable", "true");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
        }
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout", "15000");
        props.put("mail.smtp.writetimeout", "15000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(settings.username(), settings.password());
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(settings.from()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(settings.to()));
        message.setSubject(subject, "UTF-8");
        message.setText(body, "UTF-8");
        Transport.send(message);
    }
}
