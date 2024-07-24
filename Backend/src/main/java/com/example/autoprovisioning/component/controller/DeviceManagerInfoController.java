package com.example.autoprovisioning.component.controller;

import java.util.List;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.autoprovisioning.component.entity.DeviceManagerInfo;
import com.example.autoprovisioning.component.helper.RequestResponse;
import com.example.autoprovisioning.component.service.DeviceManagerInfoService;

@CrossOrigin
@RequestMapping("/api/deviceManagerInfo")
@RestController
public class DeviceManagerInfoController {

    @Autowired
    private DeviceManagerInfoService service;

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteHistoryFunction(@PathVariable int id) {
        try {
            RequestResponse result = service.deleteInfo(id);
            if (result.getStatus() == 0) {
                return ResponseEntity.ok("DeviceManagerInfo with ID " + id + " deleted successfully.");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete DeviceManagerInfo with ID " + id);
        }
    }

    @GetMapping("/infobymacaddress")
    public ResponseEntity<RequestResponse> findInfoByMacAddress(@RequestParam String macAddress) {
        ResponseEntity<RequestResponse> responseEntity;
        try {
            RequestResponse result = service.getInfoByMacAddress(macAddress);
            responseEntity = ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            RequestResponse errorResponse = new RequestResponse();
            errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.setMessage("Failed to retrieve DeviceManagerInfo for Mac Address: " + macAddress);
            responseEntity = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
        return responseEntity;
    }

    @PostMapping("/createinfo")
    public ResponseEntity<RequestResponse> createinfodata(@RequestHeader String productClass, @RequestHeader String oui,
            @RequestHeader String macAddress, @RequestHeader String manufacturer) {
        RequestResponse returnValue = new RequestResponse();
        try {
            DeviceManagerInfo data = new DeviceManagerInfo();
            data.setConfigurationVersion("0");
            data.setFirmwareVersion("0");
            data.setMacAddress(macAddress);
            data.setManufacturer(manufacturer);
            data.setOui(oui);
            data.setProductClass(productClass);

            returnValue = service.createInfo(data);
            if (returnValue.getStatus() == 0) {
                return ResponseEntity.ok(returnValue);
            } else if (returnValue.getStatus() == 1) {
                return ResponseEntity.ok(returnValue);
            } else {
                returnValue.setMessage("Failed to create DeviceManagerInfo.");
                return ResponseEntity.badRequest().body(returnValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create DeviceManagerInfo");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Document>> getAllDevices() {
        try {
            List<Document> devices = service.getAllDevices();
            return ResponseEntity.ok(devices);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

}
