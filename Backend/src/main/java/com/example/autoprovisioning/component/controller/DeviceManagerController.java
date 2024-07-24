package com.example.autoprovisioning.component.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.example.autoprovisioning.component.service.DeviceManagerService;
import com.example.autoprovisioning.component.helper.RequestResponse;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@CrossOrigin
@RequestMapping("/api/deviceManager")
public class DeviceManagerController {

    private static final Logger logger = LogManager.getLogger(DeviceManagerController.class);

    @Autowired
    private DeviceManagerService deviceService;

    // Api of reboot device.
    @GetMapping("/reboot/{macAddress}")
    public ResponseEntity<RequestResponse> rebootDevice(@RequestHeader("Authorization") String authHeader,
            @PathVariable String macAddress) {
        RequestResponse returnValue = new RequestResponse();
        String token = authHeader.substring(7);
        logger.info(token);
        String ApiName = "reboot";
        if (returnValue.getStatus() == 0) {
            returnValue = deviceService.resetOrReboot(token, macAddress, ApiName);
            return ResponseEntity.ok(returnValue);
        } else {
            returnValue.setMessage("Reboot api call fail.");
            return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
        }
    }

    // Api of reset device.
    @GetMapping("/reset/{macAddress}")
    public ResponseEntity<RequestResponse> resetDevice(@RequestHeader("Authorization") String authHeader,
            @PathVariable String macAddress) {
        RequestResponse returnValue = new RequestResponse();
        String token = authHeader.substring(7);
        logger.info(token);
        String ApiName = "factoryReset";
        if (returnValue.getStatus() == 0) {
            returnValue = deviceService.resetOrReboot(token, macAddress, ApiName);
            return ResponseEntity.ok(returnValue);
        } else {
            returnValue.setMessage("Reset api call fail.");
            return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
        }
    }

    // Api of configuration file upload
    @PostMapping("/uploadConfig/{macAddress}")
    public ResponseEntity<RequestResponse> apiCallOfConfigUpload(@RequestHeader String fileName,
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String macAddress, @RequestParam("file") MultipartFile file) {
        RequestResponse returnValue = new RequestResponse();
        String token = authHeader.substring(7);
        try {
            byte[] fileData = file.getBytes();
            if (returnValue.getStatus() == 0) {
                returnValue = deviceService.methodOfUploadConfig(token, fileName, fileData, macAddress);
                return ResponseEntity.ok(returnValue);
            } else {
                returnValue.setMessage("Api configuration file upload failed.");
                return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            logger.error("Exception occurred during file upload or service method invocation: {}", e.getMessage());
            returnValue.setMessage("Exception occurred during file upload or service method invocation.");
            return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Api of update configuration.
    @GetMapping("/updateConfig/{macAddress}")
    public ResponseEntity<RequestResponse> updateConfig(@RequestHeader("Authorization") String authHeader,
            @PathVariable String macAddress) {
        RequestResponse returnValue = new RequestResponse();
        String token = authHeader.substring(7);
        String updateCall = "configuration";
        if (returnValue.getStatus() == 0) {
            returnValue = deviceService.update(token, macAddress, updateCall);
            return ResponseEntity.ok(returnValue);
        } else {
            returnValue.setMessage("Api configuration file update failed.");
            return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
        }
    }

    // Api of update firmware file.
    @GetMapping("/updateFirmware/{macAddress}")
    public ResponseEntity<RequestResponse> methodOfFirmwareUpdate(
            @RequestHeader("Authorization") String authHeader, @PathVariable String macAddress) {
        RequestResponse returnValue = new RequestResponse();
        String token = authHeader.substring(7);
        String updateCall = "firmware";
        if (returnValue.getStatus() == 0) {
            returnValue = deviceService.update(token, macAddress, updateCall);
            return ResponseEntity.ok(returnValue);
        } else {
            returnValue.setMessage("Api firmware file upload and update failed.");
            return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
        }
    }

    // Api of upload firmware file.
    @PutMapping("/uploadFirmware/{macAddress}")
    public ResponseEntity<RequestResponse> methodOfFirmwareUpload(@RequestHeader String extensionName,
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String macAddress, @RequestParam("file") MultipartFile file) {
        RequestResponse returnValue = new RequestResponse();
        String token = authHeader.substring(7);
        try {
            byte[] fileData = file.getBytes();
            if (returnValue.getStatus() == 0) {
                returnValue = deviceService.methodOfUploadFirmware(token, extensionName, fileData, macAddress);
                return ResponseEntity.ok(returnValue);
            } else {
                returnValue.setMessage("Api firmware file upload failed.");
                return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            logger.error("Exception occurred during file upload or service method invocation: {}", e.getMessage());
            returnValue.setMessage("Exception occurred during file upload or service method invocation.");
            return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Api to fetch device manager history.
    @PostMapping("/searchHistory/{macAddress}")
    public ResponseEntity<RequestResponse> methodOfGetHistory(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String macAddress,
            @RequestHeader String Filetype,
            @RequestHeader String deviceVersion) {
        RequestResponse returnValue = new RequestResponse();
        String token = authHeader.substring(7);
        logger.info(token);
        String version = deviceVersion;
        String updateCall = Filetype;
        boolean SetUpdateCall = false;
        if (updateCall.equals("firmware")) {
            updateCall = "1 Firmware Upgrade Image";
            SetUpdateCall = true;
        } else if (updateCall.equals("configuration")) {
            updateCall = "3 Vendor Configuration File";
            SetUpdateCall = true;
        }
        if (returnValue.getStatus() == 0 && SetUpdateCall) {
            returnValue = deviceService.getHistory(macAddress, version, token, updateCall);
            return ResponseEntity.ok(returnValue);
        } else {
            returnValue.setMessage("Api for history failed.");
            return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
        }
    }

    // Api call of add data in automatic upload and update table.
    @PutMapping("/addFileAutoDeploy/{macAddress}")
    public ResponseEntity<RequestResponse> autoDeployFile(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String macAddress,
            @RequestHeader String dateoffile,
            @RequestHeader String time,
            @RequestHeader String Filetype,
            @RequestParam("file") MultipartFile file) {
        RequestResponse returnValue = new RequestResponse();
        String token = authHeader.substring(7);
        boolean SetUpdateCall = false;
        String fileFormat = Filetype;
        try {
            if (fileFormat.equals("firmware")) {
                fileFormat = "1 Firmware Upgrade Images";
                SetUpdateCall = true;
            } else if (fileFormat.equals("configuration")) {
                fileFormat = "3 Vendor Configuration File";
                SetUpdateCall = true;
            }
            logger.info("Token: " + token + " FileFormat: " + fileFormat);

            if (file.isEmpty()) {
                returnValue.setMessage("File is empty.");
                return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
            }

            byte[] fileInByte = file.getBytes();

            if (returnValue.getStatus() == 0 && SetUpdateCall) {
                returnValue = deviceService.addItemInAutoDeploy(token, macAddress, dateoffile, time, fileFormat,
                        fileInByte);
                return ResponseEntity.ok(returnValue);
            } else {
                returnValue.setMessage("API to add file in automatic update and upload API call failed.");
                return new ResponseEntity<>(returnValue, HttpStatus.BAD_REQUEST);
            }
        } catch (IOException e) {
            logger.error("Error reading file content: {}", e.getMessage());
            returnValue.setMessage("Error reading file content.");
            return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Exception occurred during file upload or service method invocation: {}", e.getMessage());
            returnValue.setMessage("Exception occurred during file upload or service method invocation.");
            return new ResponseEntity<>(returnValue, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
