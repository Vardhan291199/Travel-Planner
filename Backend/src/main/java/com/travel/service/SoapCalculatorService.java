package com.travel.service;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import reactor.core.publisher.Mono;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Service
public class SoapCalculatorService {
    private final WebClient http;
    private static final String SOAP_URL = "http://vhost3.cs.rit.edu/Calculator/Service.svc";
    private static final String SOAP_ACTION = "http://tempuri.org/IService/multiple";

    public SoapCalculatorService(WebClient http) { this.http = http; }

    public Mono<Double> multiply(double a, double b) {
        String v1 = new BigDecimal(Double.toString(a)).toPlainString();
        String v2 = new BigDecimal(Double.toString(b)).toPlainString();

        String envelope = ""
                + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                + "                  xmlns:tem=\"http://tempuri.org/\">"
                + "  <soapenv:Header/>"
                + "  <soapenv:Body>"
                + "    <tem:multiple>"
                + "      <tem:value1>" + v1 + "</tem:value1>"
                + "      <tem:value2>" + v2 + "</tem:value2>"
                + "    </tem:multiple>"
                + "  </soapenv:Body>"
                + "</soapenv:Envelope>";

        return http.post()
                .uri(SOAP_URL)
                .header(HttpHeaders.CONTENT_TYPE, "text/xml; charset=utf-8")
                .header("SOAPAction", "\"" + SOAP_ACTION + "\"")
                .bodyValue(envelope)
                .retrieve()
                .bodyToMono(byte[].class)
                .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                .map(xml -> {
                    try {
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        dbf.setNamespaceAware(true);
                        Document doc = dbf.newDocumentBuilder()
                                .parse(new java.io.ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

                        XPath xp = XPathFactory.newInstance().newXPath();
                        String val = xp.evaluate(
                                "/*[local-name()='Envelope']" +
                                        "/*[local-name()='Body']" +
                                        "/*[local-name()='multipleResponse']" +
                                        "/*[local-name()='multipleResult']/text()",
                                doc
                        );

                        if (val == null || val.isBlank()) {
                            throw new IllegalStateException("SOAP multiply: empty result. Raw: " + xml);
                        }
                        return Double.parseDouble(val.trim());
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse SOAP response", e);
                    }
                });
    }
}
