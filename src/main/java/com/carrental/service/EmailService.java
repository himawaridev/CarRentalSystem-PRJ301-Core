package com.carrental.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.net.ssl.SSLSocketFactory;

public class EmailService {
    private final AuthConfig config;

    public EmailService() {
        this(new AuthConfig());
    }

    public EmailService(AuthConfig config) {
        this.config = config;
    }

    public boolean sendVerificationCode(String to, String fullName, String code) {
        String subject = "CarRental - Ma xac minh email";
        String body = "Xin chao " + safeName(fullName) + ",\n\n"
                + "Ma xac minh dang ky CarRental cua ban la: " + code + "\n"
                + "Ma co hieu luc trong 15 phut. Neu ban khong dang ky tai khoan, hay bo qua email nay.\n\n"
                + "CarRental";
        return send(to, subject, body);
    }

    public boolean sendPasswordResetCode(String to, String fullName, String code) {
        String subject = "CarRental - Ma dat lai mat khau";
        String body = "Xin chao " + safeName(fullName) + ",\n\n"
                + "Ma dat lai mat khau CarRental cua ban la: " + code + "\n"
                + "Ma co hieu luc trong 15 phut. Neu ban khong yeu cau dat lai mat khau, hay bo qua email nay.\n\n"
                + "CarRental";
        return send(to, subject, body);
    }

    public boolean send(String to, String subject, String body) {
        if (!config.smtpConfigured()) {
            System.out.println("[AUTH MAIL DEV] SMTP is not configured. To=" + to
                    + ", Subject=" + subject + "\n" + body);
            return false;
        }

        try {
            sendSmtp(to, subject, body);
            return true;
        } catch (IOException e) {
            System.err.println("[AUTH MAIL] Cannot send email to " + to + ": " + e.getMessage());
            return false;
        }
    }

    private void sendSmtp(String to, String subject, String body) throws IOException {
        Socket socket = openSocket();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            expect(readResponse(reader), 220);
            sendCommand(writer, "EHLO localhost");
            expect(readResponse(reader), 250);

            if (config.smtpStartTls() && !config.smtpSsl()) {
                sendCommand(writer, "STARTTLS");
                expect(readResponse(reader), 220);
                socket = ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket(
                        socket,
                        config.smtpHost(),
                        config.smtpPort(),
                        true);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                sendCommand(writer, "EHLO localhost");
                expect(readResponse(reader), 250);
            }

            sendCommand(writer, "AUTH LOGIN");
            expect(readResponse(reader), 334);
            sendCommand(writer, base64(config.smtpUsername()));
            expect(readResponse(reader), 334);
            sendCommand(writer, base64(config.smtpPassword()));
            expect(readResponse(reader), 235);

            sendCommand(writer, "MAIL FROM:<" + config.smtpFrom() + ">");
            expect(readResponse(reader), 250);
            sendCommand(writer, "RCPT TO:<" + to + ">");
            expect(readResponse(reader), 250, 251);
            sendCommand(writer, "DATA");
            expect(readResponse(reader), 354);

            writer.write("From: " + config.smtpFrom() + "\r\n");
            writer.write("To: " + to + "\r\n");
            writer.write("Subject: " + subject + "\r\n");
            writer.write("MIME-Version: 1.0\r\n");
            writer.write("Content-Type: text/plain; charset=UTF-8\r\n");
            writer.write("\r\n");
            writer.write(dotStuff(body));
            writer.write("\r\n.\r\n");
            writer.flush();
            expect(readResponse(reader), 250);

            sendCommand(writer, "QUIT");
            readResponse(reader);
        } finally {
            socket.close();
        }
    }

    private Socket openSocket() throws IOException {
        if (config.smtpSsl()) {
            return SSLSocketFactory.getDefault().createSocket(config.smtpHost(), config.smtpPort());
        }
        return new Socket(config.smtpHost(), config.smtpPort());
    }

    private static void sendCommand(BufferedWriter writer, String command) throws IOException {
        writer.write(command);
        writer.write("\r\n");
        writer.flush();
    }

    private static String readResponse(BufferedReader reader) throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        do {
            line = reader.readLine();
            if (line == null) {
                throw new IOException("SMTP connection closed unexpectedly.");
            }
            response.append(line).append('\n');
        } while (line.length() > 3 && line.charAt(3) == '-');
        return response.toString();
    }

    private static void expect(String response, int... acceptedCodes) throws IOException {
        int code = parseCode(response);
        for (int acceptedCode : acceptedCodes) {
            if (code == acceptedCode) {
                return;
            }
        }
        throw new IOException("SMTP rejected command: " + response.trim());
    }

    private static int parseCode(String response) throws IOException {
        if (response == null || response.length() < 3) {
            throw new IOException("Invalid SMTP response.");
        }
        try {
            return Integer.parseInt(response.substring(0, 3));
        } catch (NumberFormatException e) {
            throw new IOException("Invalid SMTP response: " + response.trim(), e);
        }
    }

    private static String base64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String dotStuff(String body) {
        return body.replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("\n.", "\n..")
                .replace("\n", "\r\n");
    }

    private static String safeName(String fullName) {
        return fullName == null || fullName.isBlank() ? "ban" : fullName.trim();
    }
}
