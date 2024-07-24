package com.example.autoprovisioning.component.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.autoprovisioning.component.helper.RequestResponse;
import com.example.autoprovisioning.component.service.DeviceManagerLoginService;

@RequestMapping("/api/deviceManagerlogin")
@RestController
@CrossOrigin
public class DeviceManagerLoginController {
    
    @Autowired
    private DeviceManagerLoginService service;
    
    @GetMapping("/checkToken")
    public ResponseEntity<RequestResponse> checkToken(@RequestHeader("Authorization") String tokenHeader) {
    RequestResponse returnValue = new RequestResponse();
    try {
        String token = tokenHeader.replace("Bearer ", "");
        boolean isValid = service.validateToken(token);
        if (isValid) {
            returnValue.setStatus(0);
            returnValue.setMessage("Token is valid");
            return ResponseEntity.ok(returnValue);
        } else {
            returnValue.setStatus(1); 
            returnValue.setMessage("Token expired or invalid");
            return ResponseEntity.badRequest().body(returnValue);
        }
    } catch (Exception e) {
        returnValue.setStatus(1); 
        returnValue.setMessage("Invalid token format or missing token");
        return ResponseEntity.badRequest().body(returnValue);
    }
}
    @PostMapping("/newlogin")
    public ResponseEntity<RequestResponse> findByLogin(@RequestHeader String userName, @RequestHeader String password) {
    RequestResponse returnValue = new RequestResponse();
    try {
        returnValue = service.getUserByUserName(userName, password);
        if (returnValue.getStatus() == 0) {
            return ResponseEntity.ok(returnValue); 
        } else {
            returnValue.setMessage("Login api call failed."); 
            return ResponseEntity.badRequest().body(returnValue); 
        }
    } catch (Exception e) {
        returnValue.setMessage("Exception occurred: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnValue); 
    }
}
}
