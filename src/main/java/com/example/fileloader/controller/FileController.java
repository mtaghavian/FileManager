package com.example.fileloader.controller;

import com.example.fileloader.misc.StreamUtils;
import com.example.fileloader.model.FileEntry;
import com.example.fileloader.service.FileEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zeroturnaround.zip.ZipUtil;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/general")
public class FileController {

    @Autowired
    private FileEntryService fileEntryService;

    @Autowired
    private ServletContext servletContext;

    public FileController() {
    }

    public MediaType getMediaType(String fileName) {
        try {
            String mimeType = servletContext.getMimeType(fileName);
            MediaType mediaType = MediaType.parseMediaType(mimeType);
            return mediaType;
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    @GetMapping(value = "/files/download/{name}")
    public ResponseEntity<InputStreamResource> download(HttpServletRequest request, HttpServletResponse response, @PathVariable String name) {
        final File originalFile = new File(name);
        File sendFile = null;
        try {
            if (!originalFile.exists()) {
                throw new RuntimeException("File not found! Name = " + name);
            }
            if (originalFile.isDirectory()) {
                sendFile = new File(originalFile.getName() + ".zip");
                ZipUtil.pack(originalFile, sendFile);
            } else {
                sendFile = originalFile;
            }
            MediaType mediaType = getMediaType(sendFile.getName());
            final File sendFileRef = sendFile;
            InputStreamResource resource = new InputStreamResource(new FileInputStream(sendFileRef) {
                @Override
                public void close() throws IOException {
                    super.close();
                    if (originalFile.isDirectory()) {
                        sendFileRef.delete();
                    }
                }
            });
            ResponseEntity<InputStreamResource> body = ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; "
                            + "filename=\"" + sendFile.getName() + "\"; "
                            + "filename*=UTF-8''" + URLEncoder.encode(sendFile.getName(), "UTF-8").replace("+", "%20"))
                    .contentLength(sendFile.length())
                    .body(resource);
            return body;
        } catch (Exception ioex) {
            throw new RuntimeException("Exception while reading file: " + name);
        }
    }

    @PostMapping(value = "/files/upload")
    @ResponseBody
    public void uploadFile(HttpServletRequest request, @RequestParam("file") MultipartFile[] files) {
        for (MultipartFile file : files) {
            File fileObj = new File(file.getOriginalFilename());
            try {
                FileOutputStream os = new FileOutputStream(fileObj);
                StreamUtils.copy(file.getInputStream(), os, false, true);
            } catch (Exception e) {
                fileObj.delete();
            }
        }
    }

    @GetMapping(value = "/files/delete/{name}")
    public void delete(HttpServletRequest request, HttpServletResponse response, @PathVariable String name) {
        try {
            File file = new File(name);
            if (!file.exists()) {
                throw new RuntimeException("File not found! Name = " + name);
            }
            StreamUtils.delete(file);
        } catch (Exception ioex) {
            throw new RuntimeException("Exception while deleting file: " + name);
        }
    }

    @GetMapping("/files/list")
    public List<FileEntry> list(HttpServletRequest request, HttpServletResponse response) throws IOException {
        File[] files = new File(".").listFiles();
        List<FileEntry> list = new ArrayList<>();
        for (File file : files) {
            FileEntry ent = new FileEntry();
            ent.setName(file.getName());
            ent.setIsDirectory(file.isDirectory());
            ent.setLength(file.length());
            list.add(ent);
        }
        Collections.sort(list, Comparator.comparing(FileEntry::getIsDirectory).reversed().thenComparing(FileEntry::getName));
        return list;
    }
}
