package com.lab06.jsonservice.soap;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * ================================================================
 * SoapClient - JSON Service-ээс SOAP Service руу дуудах client
 * ================================================================
 * JSON Service middleware нь энэ классыг ашиглаж
 * SOAP ValidateToken operation дуудна.
 *
 * HTTP POST + XML body ашиглан SOAP request илгээнэ.
 * Хариуг XML parse хийж valid эсэхийг авна.
 * ================================================================
 */
@Component
public class SoapClient {

    // application.properties-с SOAP service URL уншина
    @Value("${soap.service.url}")
    private String soapServiceUrl;

    // SOAP namespace - users.xsd-тэй яг таарах ёстой
    private static final String NAMESPACE = "http://lab06.com/soap/users";

    /**
     * ValidateToken SOAP operation дуудах
     *
     * @param token шалгах token (frontend-с Authorization header-ээр ирнэ)
     * @return ValidateResult - valid эсэх, username, userId
     */
    public ValidateResult validateToken(String token) {

        // Token хоосон бол шууд false буцаана
        if (token == null || token.trim().isEmpty()) {
            return new ValidateResult(false, null, null);
        }

        try {
            // SOAP request XML бэлтгэх
            // Spring-WS XML template ашиглахгүй, шууд HTTP POST хийнэ
            // (SOAP Service-ийн JAXB класcууд JSON Service-д байхгүй тул)
            String soapRequest = buildValidateTokenRequest(token.trim());

            // HTTP POST илгээх
            String soapResponse = sendSoapRequest(soapRequest);

            // Хариуг parse хийх
            return parseValidateTokenResponse(soapResponse);

        } catch (Exception e) {
            // SOAP дуудалт амжилтгүй болвол false буцаана
            // (SOAP Service ажиллахгүй байна гэсэн үг)
            System.err.println("[SoapClient] ValidateToken failed: " + e.getMessage());
            return new ValidateResult(false, null, null);
        }
    }

    /**
     * ValidateToken SOAP XML request бүтээх
     */
    private String buildValidateTokenRequest(String token) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<soapenv:Envelope " +
               "  xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
               "  xmlns:usr=\"" + NAMESPACE + "\">" +
               "  <soapenv:Body>" +
               "    <usr:ValidateTokenRequest>" +
               "      <usr:token>" + token + "</usr:token>" +
               "    </usr:ValidateTokenRequest>" +
               "  </soapenv:Body>" +
               "</soapenv:Envelope>";
    }

    /**
     * HTTP POST ашиглан SOAP request илгээж хариу авах
     */
    private String sendSoapRequest(String soapXml) throws Exception {
        URL url = new URL(soapServiceUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        connection.setRequestProperty("SOAPAction", "");
        connection.setDoOutput(true);
        connection.setConnectTimeout(5000); // 5 секунд timeout
        connection.setReadTimeout(5000);

        // Request body бичих
        try (OutputStream os = connection.getOutputStream()) {
            os.write(soapXml.getBytes(StandardCharsets.UTF_8));
        }

        // Хариу унших
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            return new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } else {
            throw new RuntimeException("SOAP service returned HTTP " + responseCode);
        }
    }

    /**
     * ValidateToken SOAP XML хариуг parse хийж ValidateResult буцаах
     * XML-с <valid>, <username>, <userId> утгуудыг авна
     */
    private ValidateResult parseValidateTokenResponse(String responseXml) {
        try {
            // Энгийн string parse - JAXB-гүйгээр
            boolean valid = responseXml.contains("<ns2:valid>true</ns2:valid>") ||
                            responseXml.contains("<valid>true</valid>");

            if (!valid) {
                return new ValidateResult(false, null, null);
            }

            // Username авах
            String username = extractXmlValue(responseXml, "username");

            // UserId авах
            Long userId = null;
            String userIdStr = extractXmlValue(responseXml, "userId");
            if (userIdStr != null) {
                userId = Long.parseLong(userIdStr);
            }

            return new ValidateResult(true, username, userId);

        } catch (Exception e) {
            System.err.println("[SoapClient] Parse error: " + e.getMessage());
            return new ValidateResult(false, null, null);
        }
    }

    /**
     * XML string-с тодорхой tag-ийн утгыг авах helper метод
     * Жишээ: <ns2:username>john</ns2:username> -> "john"
     */
    private String extractXmlValue(String xml, String tagName) {
        // namespace prefix-тэй болон prefix-гүй хоёр хэлбэрийг хайна
        String[] patterns = {
            "<" + tagName + ">",
            "<ns2:" + tagName + ">"
        };
        String[] endPatterns = {
            "</" + tagName + ">",
            "</ns2:" + tagName + ">"
        };

        for (int i = 0; i < patterns.length; i++) {
            int start = xml.indexOf(patterns[i]);
            if (start != -1) {
                start += patterns[i].length();
                int end = xml.indexOf(endPatterns[i], start);
                if (end != -1) {
                    return xml.substring(start, end).trim();
                }
            }
        }
        return null;
    }

    // ================================================================
    // Result класс
    // ================================================================

    public static class ValidateResult {
        public final boolean valid;
        public final String username;
        public final Long userId;

        public ValidateResult(boolean valid, String username, Long userId) {
            this.valid = valid;
            this.username = username;
            this.userId = userId;
        }
    }
}