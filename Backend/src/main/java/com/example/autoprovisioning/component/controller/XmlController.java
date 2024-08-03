package com.example.autoprovisioning.component.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

@RestController
@RequestMapping("/xml")
public class XmlController {

    // Endpoint to handle file upload
 @PostMapping("/upload")
public String handleFileUpload(@RequestBody byte[] fileBytes, HttpServletRequest request) throws IOException {
    // Check if the request contains a file and validate its content type
    String contentType = request.getContentType();
    if (contentType == null || !contentType.equals("application/xml")) {
        return "Unsupported file type. Please upload an XML file.";
    }

    XmlMapper xmlMapper = new XmlMapper();
    Object xmlObject = xmlMapper.readValue(fileBytes, Object.class);
    String updatedXml = xmlMapper.writeValueAsString(xmlObject);
    System.out.println(xmlObject);
    
    return updatedXml;
}


    public String writeValueAsString(Object xmlObject)
    {
        
        return null;  
    }
}
