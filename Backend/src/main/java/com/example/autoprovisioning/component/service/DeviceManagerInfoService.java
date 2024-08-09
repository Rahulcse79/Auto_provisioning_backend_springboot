package com.example.autoprovisioning.component.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import com.example.autoprovisioning.component.entity.DeviceManagerInfo;
import com.example.autoprovisioning.component.helper.MongoConfig;
import com.example.autoprovisioning.component.helper.RequestResponse;
import com.example.autoprovisioning.component.repository.DeviceManagerInfoRespository;
import java.util.regex.Matcher;

@Service
public class DeviceManagerInfoService {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public DeviceManagerInfoService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Autowired
    private DeviceManagerInfoRespository repository;

    public List<Document> getAllDevices() {
        try {
            List<Document> data = mongoTemplate.findAll(Document.class, MongoConfig.CollectionName);
            for (Document document : data) {
                String id = document.getString("_id");
                String[] parts = id.split("-");
                if (parts.length == 3) {
                    String oui = parts[0];
                    String productClass = parts[1];
                    String macAddress = parts[2];
                    String manufacturer = "CORAL";
                    String ipAddress = "";
                    boolean active = false;
                    int ping = -1;
                    boolean pinging = false;
                    ipAddress = findIpAddress(macAddress);
                    if (!ipAddress.isEmpty()) {
                        ping = findPing(ipAddress);
                        active = (ping == 1);
                    }
                    if (ping == 1) {
                        pinging = true;
                    }
                    DeviceManagerInfo user = new DeviceManagerInfo();
                    user.setConfigurationVersion("0");
                    user.setFirmwareVersion("0");
                    user.setMacAddress(macAddress);
                    user.setOui(oui);
                    user.setProductClass(productClass);
                    user.setManufacturer(manufacturer);
                    user.setPing(pinging);
                    user.setIpAddress(ipAddress);
                    user.setActiveDevice(active);
                    RequestResponse result = getInfoByMacAddress(macAddress);
                    if (result.getStatus() == 1) {
                        DeviceManagerInfo saveResult = saveInfo(user);
                    }
                }
            }
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve devices from MongoDB", e);
        }
    }

    public Iterable<DeviceManagerInfo> getInfos() {
        try {
            return repository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public DeviceManagerInfo saveInfo(DeviceManagerInfo info) {
        return repository.save(info);
    }

    public RequestResponse getInfoByMacAddress(String macAddress) {
        RequestResponse result = new RequestResponse();
        try {
            DeviceManagerInfo info = repository.findByMacAddress(macAddress);
            if (info != null) {
                result.setStatus(0);
                result.setData(info);
            } else {
                result.setStatus(1);
                result.setMessage("DeviceManagerInfo not found for Mac Address: " + macAddress);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(1);
            result.setMessage("Failed to fetch DeviceManagerInfo for Mac Address: " + macAddress);
        }
        return result;
    }

    public void UpdateIPPingActive(String macAddress)
    {
        String ipAddress = findIpAddress(macAddress);
       
        boolean pinging = false;
        boolean active = false;
        if(!ipAddress.isEmpty()){
            pinging = (findPing(ipAddress) == 1);
        }
       
        if(pinging) active = true;
        DeviceManagerInfo info = repository.findByMacAddress(macAddress);
        info.setPing(pinging);
        info.setIpAddress(ipAddress);
        info.setActiveDevice(active);
        saveInfo(info);
    }

    public RequestResponse getOnlineDevices(String token) {
        RequestResponse result = new RequestResponse();
        try {
            int onlineDeviceCount = 0;
            int totalDeviceCount = 0;
            Iterable<DeviceManagerInfo> devices = getDevices();
            for (DeviceManagerInfo device: devices) {
                totalDeviceCount++;
                //UpdateIPPingActive(device.getMacAddress());
                if (device.isActiveDevice()) {
                    onlineDeviceCount++;
                }
            }
            result.setStatus(0);
            result.setValue(onlineDeviceCount);
            result.setTotal(totalDeviceCount);
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(1);
            result.setMessage("Failed to fetch DeviceManagerInfo for Mac Address: ");
        }
        return result;
    }

    public Iterable<DeviceManagerInfo> getDevices() {
        return repository.findAll();
    }

    public RequestResponse deleteInfo(String macAddress) {
        RequestResponse result = new RequestResponse();
        try {
            DeviceManagerInfo res = repository.findByMacAddress(macAddress);
            if (res.getId() != null) {
                repository.deleteById(res.getId());
                result.setStatus(0);
                result.setMessage("DeviceManagerInfo deleted successfully for MAC Address: " + macAddress);
            } else {
                result.setStatus(1);
                result.setMessage("DeviceManagerInfo not found for MAC Address: " + macAddress);
            }
        } catch (Exception e) {
            result.setStatus(1);
            result.setMessage("Failed to delete DeviceManagerInfo with MAC Address: " + macAddress);
        }
        return result;
    }

    public String findIpAddress(String macAddress) {
        String formattedMacAddress = macAddress.toLowerCase().replaceAll("[^a-f0-9]", "");
        try {
            Process process = Runtime.getRuntime().exec("arp -a");
            process.waitFor();  
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            Pattern macPattern = Pattern.compile("([0-9a-fA-F]{2}[:-]){5}[0-9a-fA-F]{2}");
            Pattern ipPattern = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+");
            while ((line = reader.readLine()) != null) {
                Matcher macMatcher = macPattern.matcher(line);
                Matcher ipMatcher = ipPattern.matcher(line);
                while (macMatcher.find() && ipMatcher.find()) {
                    String mac = macMatcher.group().toLowerCase().replaceAll("[^a-f0-9]", "");
                    if (mac.equals(formattedMacAddress)) {
                        reader.close();  
                        return ipMatcher.group();
                    }
                }
            }
            reader.close();  
        } catch (Exception e) {
            e.printStackTrace();  
            return "Error: " + e.getMessage();
        }
        return "";
    }

    public static int findPing(String ipAddress) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("ping", "-c", "1", ipAddress);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return 1;
            } else {
                return 0;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public RequestResponse createInfo(DeviceManagerInfo data) {
        RequestResponse result = new RequestResponse();
        try {
            DeviceManagerInfo existingInfo = repository.findByMacAddress(data.getMacAddress());
            if (existingInfo != null) {
                result.setStatus(1);
                result.setMessage("DeviceManagerInfo with Mac Address " + data.getMacAddress() + " already exists.");
                return result;
            }
            String ipAddress = "";
            boolean active = false;
            int ping = -1;
            boolean pinging = false;
            ipAddress = findIpAddress(data.getMacAddress());
            if (!ipAddress.isEmpty()) {
                ping = findPing(ipAddress);
                active = (ping == 1);
            }
            if (ping == 1) {
                pinging = true;
            }
            data.setPing(pinging);
            data.setIpAddress(ipAddress);
            data.setActiveDevice(active);
            repository.save(data);
            result.setStatus(0);
            result.setMessage("DeviceManagerInfo saved successfully");
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(-1);
            result.setMessage("Failed to save DeviceManagerInfo");
        }
        return result;
    }
}
