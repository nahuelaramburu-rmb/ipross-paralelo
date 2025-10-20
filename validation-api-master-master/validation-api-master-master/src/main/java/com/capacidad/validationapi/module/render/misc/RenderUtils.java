package com.capacidad.validationapi.module.render.misc;

import com.capacidad.validationapi.module.storage.dto.FileDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;

public final class RenderUtils {

    private static final String ATTACHMENT_CONTENT_DISPOSITION = "attachment";

    private RenderUtils() {

    }

    public static void initializeApplicationOctetStreamResponse(HttpServletResponse response, String filename) {
        response.setHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", filename));
    }

    public static ResponseEntity<byte[]> buildFileResponseEntity(FileDTO fileDTO) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(fileDTO.getFile().length);
        headers.setContentDispositionFormData(ATTACHMENT_CONTENT_DISPOSITION, fileDTO.getFilename());
        return ResponseEntity.ok().headers(headers).body(fileDTO.getFile());
    }

    public static ResponseEntity<byte[]> buildReceiptResponseEntity(ByteArrayOutputStream receipt, String filename) {
        return buildReceiptResponseEntity(receipt, filename, null);
    }

    public static ResponseEntity<byte[]> buildReceiptResponseEntity(ByteArrayOutputStream receipt, String filename, MultiValueMap<String, String> extraHeaders) {
        byte[] outputByteArray = receipt.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData(ATTACHMENT_CONTENT_DISPOSITION, filename);
        headers.setContentLength(outputByteArray.length);
        if (extraHeaders != null && !extraHeaders.isEmpty())
            headers.addAll(extraHeaders);
        return ResponseEntity.ok().headers(headers).body(receipt.toByteArray());
    }

}
