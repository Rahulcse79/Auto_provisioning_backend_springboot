package com.example.autoprovisioning.component.service;

import java.util.List;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import com.example.autoprovisioning.component.entity.DeviceManagerInfo;
import com.example.autoprovisioning.component.helper.MongoConfig;
import com.example.autoprovisioning.component.helper.RequestResponse;
import com.example.autoprovisioning.component.repository.DeviceManagerInfoRespository;

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
                    
                    DeviceManagerInfo user = new DeviceManagerInfo();
                    user.setConfigurationVersion("0");
                    user.setFirmwareVersion("0");
                    user.setMacAddress(macAddress);
                    user.setOui(oui);
                    user.setProductClass(productClass);
                    user.setManufacturer(manufacturer);

                    RequestResponse result = getInfoByMacAddress(macAddress);
    
                    if(result.getStatus() == 1)
                    {
                        DeviceManagerInfo saveResult = saveInfo(user);
                        System.out.println("Device ID: " + id);
                    }
                } else {
                    System.out.println("Invalid ID format: " + id);
                }
            }
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve devices from MongoDB", e);
        }
    }

    public Iterable<DeviceManagerInfo> getInfos() {
        return repository.findAll();
    }

    public DeviceManagerInfo saveInfo(DeviceManagerInfo info)
    {
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

    public RequestResponse deleteInfo(int id) {
        RequestResponse result = new RequestResponse();
        try {
            repository.deleteById(id);
            result.setStatus(0);
            result.setMessage("DeviceManagerInfo deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(1);
            result.setMessage("Failed to delete DeviceManagerInfo with ID: " + id);
        }
        return result;
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
