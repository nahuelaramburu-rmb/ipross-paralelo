package com.capacidad.validationapi.module.render.service.impl;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.render.service.RenderService;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.qrcode.EncodeHintType;
import com.itextpdf.text.pdf.qrcode.ErrorCorrectionLevel;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@Log4j2
public class RenderServiceImpl implements RenderService {

    private final TemplateEngine templateEngine;

    @Autowired
    public RenderServiceImpl(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public ByteArrayOutputStream renderPDF(String templateName, Map<String, Object> templateValues) throws ObjectNotValidException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Context context = new Context();
            templateValues.forEach(context::setVariable);
            String html = templateEngine.process(templateName, context);
            String xhtml = convertToXhtml(html);
            ITextRenderer iTextRenderer = new ITextRenderer();
            iTextRenderer.setDocumentFromString(xhtml);
            iTextRenderer.layout();
            iTextRenderer.createPDF(bos);
            return bos;
        } catch (IOException | DocumentException e) {
            log.error("Error generating PDF File {}: {}", e.getClass(), e.getMessage());
            throw new ObjectNotValidException("render.pdfGenerationError");
        }
    }

    @Override
    public String renderQrCode(String content) {
        String base64QrCode = "";
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Map<EncodeHintType, Object> qrParam = new HashMap<>();
            qrParam.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            qrParam.put(EncodeHintType.CHARACTER_SET, UTF_8.name());
            BarcodeQRCode qrCode = new BarcodeQRCode(content, 250, 250, qrParam);
            Image qrImage = qrCode.createAwtImage(Color.BLACK, Color.WHITE);
            BufferedImage bufferedQrImage = new BufferedImage(qrImage.getWidth(null), qrImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics graphics = bufferedQrImage.createGraphics();
            graphics.drawImage(qrImage, 0, 0, null);
            graphics.dispose();
            ImageIO.write(bufferedQrImage, "png", output);
            base64QrCode = Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (IOException e) {
            log.error("Error generating QR Code: {}", e.getMessage());
        }
        return base64QrCode;
    }

    private String convertToXhtml(String html) {
        Tidy tidy = new Tidy();
        tidy.setInputEncoding(UTF_8.name());
        tidy.setOutputEncoding(UTF_8.name());
        tidy.setXHTML(true);
        tidy.setShowWarnings(false);
        tidy.setQuiet(true);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(html.getBytes(UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        tidy.parseDOM(inputStream, outputStream);
        return outputStream.toString(UTF_8);
    }

}
