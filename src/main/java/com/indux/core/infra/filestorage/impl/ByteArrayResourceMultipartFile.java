package com.indux.core.infra.filestorage.impl;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class ByteArrayResourceMultipartFile implements MultipartFile {
    private final ByteArrayResource resource;
    private final String name;
    private final String originalFilename;
    private final String contentType;

    public ByteArrayResourceMultipartFile(ByteArrayResource resource,
                                          String name,
                                          String originalFilename,
                                          String contentType) {
        this.resource = resource;
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return resource.contentLength() == 0;
    }

    @Override
    public long getSize() {
        return resource.contentLength();
    }

    @Override
    public byte[] getBytes() throws IOException {
        return resource.getByteArray();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return resource.getInputStream();
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        try (InputStream in = getInputStream();
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }
}
