package com.example.autoprovisioning.component.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.autoprovisioning.component.entity.DeviceManagerAutoDeploy;
import com.example.autoprovisioning.component.entity.DeviceManagerHistory;
import com.example.autoprovisioning.component.entity.DeviceManagerInfo;
import com.example.autoprovisioning.component.repository.DeviceManagerInfoRespository;
import com.example.autoprovisioning.component.helper.Constants;
import com.example.autoprovisioning.component.helper.RequestResponse;
import com.example.autoprovisioning.component.model.DeviceManagerExtension;
import com.example.autoprovisioning.component.model.DeviceManagerInfoModel;

@Service
public class DeviceManagerService {

    private static final Logger logger = LogManager.getLogger(DeviceManagerService.class);

    @Autowired
    private DeviceManagerInfoRespository deviceManagerInfoRespository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DeviceManagerHistoryService deviceManagerHistoryService;

    @Autowired
    private DeviceManagerAutoDeployService deviceManagerAutoDeployService;

    private String AcsTokenForAutoDeploy;

    // Function of get product details.
    public DeviceManagerInfoModel getProductDetails(String macAddress) {
        DeviceManagerInfoModel returnValue = new DeviceManagerInfoModel();
        DeviceManagerInfo productDetail = deviceManagerInfoRespository.findByMacAddress(macAddress);

        if (productDetail != null) {
            returnValue.setConfigurationVersion(productDetail.getConfigurationVersion());
            returnValue.setFirmwareVersion(productDetail.getFirmwareVersion());
            returnValue.setMacAddress(productDetail.getMacAddress());
            returnValue.setManufacturer(productDetail.getManufacturer());
            returnValue.setOui(productDetail.getOui());
            returnValue.setProductClass(productDetail.getProductClass());
        }
        return returnValue;
    }

    // Method to read the file content.
    private byte[] readFileContent(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    // Method to reset or reboot.
    public RequestResponse resetOrReboot(String token, String macAddress, String ApiName) {
        RequestResponse returnValue = new RequestResponse();
        try {
            DeviceManagerInfoModel deviceData = new DeviceManagerInfoModel();
            deviceData = getProductDetails(macAddress);
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Cookie", "session={\"AuthToken\":\"" + token + "\"}");
            String requestBody = "[{\"device\": \"" + deviceData.getOui() + "-" + deviceData.getProductClass() + "-"
                    + macAddress + "\", \"name\": \"" + ApiName + "\", \"status\": \"success\"}]";
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            String url = "http://" + Constants.DEVICE_MANAGER_IP + "/device-manager/api/devices/" + deviceData.getOui()
                    + "-" + deviceData.getProductClass() + "-" + macAddress + "/tasks";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity,
                    String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info(ApiName + " api call successful.");
                returnValue.setStatus(0);
                returnValue.setMessage(response.getBody());
                returnValue.setMessageDetail(ApiName + " api call successfully.");
                return returnValue;
            } else {
                logger.error("API call failed with status code: ", response.getStatusCode());
                returnValue.setStatus(-1);
                returnValue.setMessage(ApiName + " API call failed.");
                return returnValue;
            }
        } catch (Exception e) {
            logger.error("Exception error :{}", e.getMessage());
            returnValue.setStatus(-1);
            returnValue.setMessage("Error while " + ApiName + " the device.");
            return returnValue;
        }
    }

    // Method to update a file.
    public RequestResponse update(String token, String macAddress, String updateCall) {
        RequestResponse returnValue = new RequestResponse();
        try {
            DeviceManagerInfoModel deviceData = new DeviceManagerInfoModel();
            deviceData = getProductDetails(macAddress);
            String fileName = ""; // FileName.
            String bodyFileName = ""; // Body file name.
            if (updateCall.equals("firmware")) {
                fileName = deviceData.getProductClass() + ".rom";
                bodyFileName = "1 Firmware Upgrade Image";
            } else if (updateCall.equals("configuration")) {
                fileName = "cfg" + macAddress + ".xml";
                bodyFileName = "3 Vendor Configuration File";
            }
            HttpHeaders header = new HttpHeaders();
            header.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            header.setContentType(MediaType.APPLICATION_JSON);
            header.set("Cookie", "session={\"AuthToken\":\"" + token + "\"}");
            String bodyData = "[{\"device\": \"" + deviceData.getOui() + "-" + deviceData.getProductClass() + "-"
                    + macAddress
                    + "\", \"name\": \"download\", \"fileName\": \"" + fileName
                    + "\", \"fileType\": \"" + bodyFileName + "\", \"status\": \"pending\"}]";
            HttpEntity<String> entity = new HttpEntity<>(bodyData, header);
            String url = "http://" + Constants.DEVICE_MANAGER_IP + "/device-manager/api/devices/" + deviceData.getOui()
                    + "-"
                    + deviceData.getProductClass() + "-" + macAddress + "/tasks";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Update file api call successful.");
                returnValue.setStatus(0);
                returnValue.setMessage(response.getBody());
                returnValue.setMessageDetail("Update file api call successfully.");
                return returnValue;
            } else {
                logger.error("Update api file failed: ");
                returnValue.setStatus(-1);
                returnValue.setMessage("Api update file failed to update.");
                return returnValue;
            }
        } catch (Exception e) {
            logger.error("Update api file failed:{}", e.getMessage());
            returnValue.setStatus(-1);
            returnValue.setMessage("Error in Update api of file failed.");
            return returnValue;
        }
    }

    // Method to upload and update a file.
    public RequestResponse upload(String token, byte[] FileData, String macAddress, String updateCall, String Version) {
        RequestResponse returnValue = new RequestResponse();
        try {
            DeviceManagerInfoModel deviceData = new DeviceManagerInfoModel();
            deviceData = getProductDetails(macAddress);
            DeviceManagerInfo data = deviceManagerInfoRespository.findByMacAddress(macAddress);
            if (deviceData != null) {
                String fileName = ""; // FileName.
                String fileDir = ""; // FileDir.
                String bodyFileName = ""; // BodyFileName.
                String ApiName = ""; // ApiName.
                String DeviceVersion = null;
                byte[] fileContent = null;
                if (updateCall.equals("firmware")) {
                    fileName = deviceData.getProductClass() + ".rom";
                    fileDir = "firmware";
                    bodyFileName = "1 Firmware Upgrade Image";
                    ApiName = "firmware";
                    DeviceVersion = deviceData.getFirmwareVersion();
                    int versionNumber = Integer.parseInt(DeviceVersion);
                    DeviceVersion = Integer.toString(versionNumber + 1);
                    if (DeviceVersion != null && !DeviceVersion.isEmpty()) {
                        DeviceManagerInfo deviceModel = data;
                        deviceModel.setFirmwareVersion(DeviceVersion);
                        DeviceManagerInfo updatedData = deviceManagerInfoRespository.save(deviceModel);
                        logger.info("Current firmware version is updated in device model table." + updatedData);
                    }
                } else if (updateCall.equals("configuration")) {
                    fileName = "cfg" + macAddress + ".xml";
                    fileDir = "configs";
                    bodyFileName = "3 Vendor Configuration File";
                    ApiName = "configuration";
                    DeviceVersion = deviceData.getConfigurationVersion();
                    int versionNumber = Integer.parseInt(DeviceVersion.trim());
                    versionNumber += 1;
                    DeviceVersion = Integer.toString(versionNumber);
                    if (DeviceVersion != null && !DeviceVersion.isEmpty()) {
                        DeviceManagerInfo deviceModel = data;
                        deviceModel.setConfigurationVersion(DeviceVersion);
                        DeviceManagerInfo updatedData = deviceManagerInfoRespository.save(deviceModel);
                        logger.info("Current configuration version is updated in device model table." + updatedData);
                    }
                }
                if (FileData == null || FileData.length == 0) {
                    String filePath = "/var/www/html/" + fileDir + "/" + fileName; // fileName;
                    fileContent = readFileContent(filePath);
                } else {
                    fileContent = FileData;
                    if (updateCall.equals("configuration")) {
                        DeviceVersion = deviceData.getConfigurationVersion();
                    } else if (updateCall.equals("firmware")) {
                        DeviceVersion = deviceData.getFirmwareVersion();
                    }
                    int versionNumber = Integer.parseInt(DeviceVersion);
                    DeviceVersion = Integer.toString(versionNumber + 1);
                    if (DeviceVersion != null && !DeviceVersion.isEmpty()
                            && updateCall.equals("configuration")) {
                        DeviceManagerInfo deviceModel = data;
                        deviceModel.setConfigurationVersion(DeviceVersion);
                        DeviceManagerInfo updatedData = deviceManagerInfoRespository.save(deviceModel);
                        logger.info("Current configuration version is updated in device model table." + updatedData);
                    } else if (DeviceVersion != null && !DeviceVersion.isEmpty()
                            && updateCall.equals("firmware")) {
                        DeviceManagerInfo deviceModel = data;
                        deviceModel.setFirmwareVersion(DeviceVersion);
                        DeviceManagerInfo updatedData = deviceManagerInfoRespository.save(deviceModel);
                        logger.info("Current firmware version is updated in device model table." + updatedData);
                    }
                }
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.set("Cookie", "session={\"AuthToken\":\"" + token + "\"}");
                headers.set("Metadata-Filetype", bodyFileName);
                headers.set("Metadata-Oui", deviceData.getOui());
                headers.set("Metadata-Productclass", deviceData.getProductClass());
                headers.set("Metadata-Version", DeviceVersion);
                HttpEntity<byte[]> entity = new HttpEntity<>(fileContent, headers);
                String url = "http://" + Constants.DEVICE_MANAGER_IP + "/device-manager/api/files/" + fileName;
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity,
                        String.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    RequestResponse updateResponse = update(token, macAddress, updateCall);
                    RequestResponse setHistoryResponse = setHistory(macAddress, fileContent,
                            DeviceVersion,
                            deviceData.getProductClass(), bodyFileName, fileName);
                    logger.info(ApiName + " File upload and update api call successfully.");
                    returnValue.setStatus(0);
                    returnValue.setMessage(response.getBody());
                    returnValue.setMessageDetail(ApiName + " File upload and update api call successfully.");
                    if (updateResponse.getStatus() == -1) {
                        returnValue.setStatus(-1);
                        returnValue.setMessage("API to upload " + ApiName + " file upload and update fail.");
                        return returnValue;
                    } else if (setHistoryResponse.getStatus() == -1) {
                        returnValue.setStatus(-1);
                        returnValue.setMessage("API to history " + ApiName + " file upload fail.");
                        return returnValue;
                    } else {
                        return returnValue;
                    }
                } else {
                    returnValue.setStatus(-1);
                    returnValue.setMessage("Upload " + ApiName + " to device manager failed for : " + macAddress);
                    logger.info(returnValue.getMessage());
                    return returnValue;
                }
            } else {
                returnValue.setStatus(-1);
                returnValue.setMessage(
                        "Upload to device manager failed for : " + macAddress
                                + " : Product class not found");
                logger.info(returnValue.getMessage());
                return returnValue;
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage("Upload to device manager failed for : " + macAddress);
            logger.error(returnValue.getMessage(), e);
            return returnValue;
        }
    }

    // Api for configuration upload call.
    public RequestResponse methodOfUploadFirmware(String token, String extensionName, byte[] FileData,
            String macAddress) {
        RequestResponse returnValue = new RequestResponse();
        try {
            DeviceManagerInfoModel deviceData = new DeviceManagerInfoModel();
            deviceData = getProductDetails(macAddress);
            DeviceManagerInfo data = deviceManagerInfoRespository.findByMacAddress(macAddress);
            String DeviceVersion = deviceData.getFirmwareVersion();
            int versionNumber = Integer.parseInt(DeviceVersion);
            DeviceVersion = Integer.toString(versionNumber + 1);
            String fileName = deviceData.getProductClass() + "." + extensionName;
            String bodyFileName = "1 Firmware Upgrade Image";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set("Cookie", "session={\"AuthToken\":\"" + token + "\"}");
            headers.set("Metadata-Filetype", bodyFileName);
            headers.set("Metadata-Oui", deviceData.getOui());
            headers.set("Metadata-Productclass", deviceData.getProductClass());
            headers.set("Metadata-Version", DeviceVersion);
            HttpEntity<byte[]> entity = new HttpEntity<>(FileData, headers);
            String url = "http://" + Constants.DEVICE_MANAGER_IP + "/device-manager/api/files/" + fileName;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity,
                    String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                if (DeviceVersion != null && !DeviceVersion.isEmpty()) {
                    DeviceManagerInfo deviceModel = data;
                    deviceModel.setFirmwareVersion(DeviceVersion);
                    DeviceManagerInfo updatedData = deviceManagerInfoRespository.save(deviceModel);
                    logger.info("Current firmware version is updated in device model table." + updatedData);
                }
                RequestResponse setHistoryResponse = setHistory(macAddress, FileData,
                        DeviceVersion,
                        deviceData.getProductClass(), bodyFileName, fileName);
                logger.info("Firmware file upload api call successfully.");
                returnValue.setStatus(0);
                returnValue.setMessage(response.getBody());
                returnValue.setMessageDetail("Firmware file upload api call successfully and history added.");
                if (setHistoryResponse.getStatus() == -1)
                    returnValue.setMessageDetail("Firmware file upload api call successfully and history api fail.");
                return returnValue;
            } else {
                returnValue.setStatus(-1);
                returnValue.setMessage("Firmware file upload to device manager failed for : " + macAddress);
                logger.info(returnValue.getMessage());
                return returnValue;
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage("Firmware file upload Internal server error macAddress: " + macAddress);
            logger.error(returnValue.getMessage(), e);
            return returnValue;
        }
    }

    // Api for configuration upload call.
    public RequestResponse methodOfUploadConfig(String token, String fileName, byte[] FileData, String macAddress) {
        RequestResponse returnValue = new RequestResponse();
        try {
            DeviceManagerInfoModel deviceData = new DeviceManagerInfoModel();
            deviceData = getProductDetails(macAddress);
            DeviceManagerInfo data = deviceManagerInfoRespository.findByMacAddress(macAddress);
            String DeviceVersion = deviceData.getConfigurationVersion();
            int versionNumber = Integer.parseInt(DeviceVersion);
            DeviceVersion = Integer.toString(versionNumber + 1);
            String bodyFileName = "3 Vendor Configuration File";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set("Cookie", "session={\"AuthToken\":\"" + token + "\"}");
            headers.set("Metadata-Filetype", bodyFileName);
            headers.set("Metadata-Oui", deviceData.getOui());
            headers.set("Metadata-Productclass", deviceData.getProductClass());
            headers.set("Metadata-Version", DeviceVersion);
            HttpEntity<byte[]> entity = new HttpEntity<>(FileData, headers);
            String url = "http://" + Constants.DEVICE_MANAGER_IP + "/device-manager/api/files/" + fileName;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity,
                    String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                if (DeviceVersion != null && !DeviceVersion.isEmpty()) {
                    DeviceManagerInfo deviceModel = data;
                    deviceModel.setConfigurationVersion(DeviceVersion);
                    DeviceManagerInfo updatedData = deviceManagerInfoRespository.save(deviceModel);
                    logger.info("Current configuration version is updated in device model table." + updatedData);
                }
                RequestResponse setHistoryResponse = setHistory(macAddress, FileData,
                        DeviceVersion,
                        deviceData.getProductClass(), bodyFileName, fileName);
                logger.info("Configuration file upload api call successfully.");
                returnValue.setStatus(0);
                returnValue.setMessage(response.getBody());
                returnValue.setMessageDetail("Configuration file upload api call successfully and history added.");
                if (setHistoryResponse.getStatus() == -1)
                    returnValue
                            .setMessageDetail("Configuration file upload api call successfully and history api fail.");
                return returnValue;
            } else {
                returnValue.setStatus(-1);
                returnValue.setMessage("Configuration file upload to device manager failed for : " + macAddress);
                logger.info(returnValue.getMessage());
                return returnValue;
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage("Configuration file upload Internal server error macAddress: " + macAddress);
            logger.error(returnValue.getMessage(), e);
            return returnValue;
        }
    }

    // Function of get currect date and time.
    public static String[] getCurrentDateTimeIST() {
        LocalDateTime currentIST = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String currentDate = currentIST.format(dateFormatter);
        String currentTime = currentIST.format(timeFormatter);
        return new String[] { currentDate, currentTime };
    }

    // Method of set history.
    public RequestResponse setHistory(String macAddress, byte[] fileContent, String version, String productClass,
            String fileFormat, String fileName) {
        RequestResponse returnValue = new RequestResponse();
        try {
            String[] dateTimeIST = getCurrentDateTimeIST(); // Date and time.
            DeviceManagerHistory history = new DeviceManagerHistory();
            history.setFiles(fileContent);
            history.setMacAddress(macAddress);
            history.setFileName(fileName);
            history.setProductClass(productClass);
            history.setTime(dateTimeIST[1]);
            history.setDate(dateTimeIST[0]);
            history.setFileFormat(fileFormat);
            history.setVersion(version);
            DeviceManagerHistory responseData = deviceManagerHistoryService.saveHistory(history);
            if (responseData.getId() != null) {
                logger.info("A new history record was inserted successfully.");
                returnValue.setStatus(0);
                returnValue.setMessage("History inserted successfully.");
            } else {
                returnValue.setStatus(-1);
                returnValue.setMessage("Failed to insert history for MAC address: " + macAddress);
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage("Failed to insert history for MAC address: " + macAddress);
            logger.error(returnValue.getMessage(), e);
        }
        return returnValue;
    }

    // Method of get history.
    public RequestResponse getHistory(String macAddress, String version, String token, String fileFormat) {
        RequestResponse returnValue = new RequestResponse();
        try {
            DeviceManagerInfoModel deviceData = new DeviceManagerInfoModel();
            deviceData = getProductDetails(macAddress);
            String productClass = deviceData.getProductClass();
            RequestResponse returnValueOfUpload = new RequestResponse();
            DeviceManagerHistory responseData = deviceManagerHistoryService
                    .getHistoryByVersionAndMacAddressAndProductClassFileFormat(version, macAddress, productClass,
                            fileFormat);
            byte[] files = responseData.getFiles();
            String retrievedVersion = responseData.getVersion();
            if ((files != null) && (retrievedVersion != null)) {
                String ApiCall = "";
                if (fileFormat.equals("3 Vendor Configuration File")) {
                    ApiCall = "configuration";
                } else if (fileFormat.equals("1 Firmware Upgrade Image")) {
                    ApiCall = "firmware";
                }
                // Upload and update file.
                returnValueOfUpload = upload(token, files, macAddress, ApiCall, retrievedVersion);
            }
            if (returnValueOfUpload.getStatus() == 0) {
                returnValue.setStatus(0);
                returnValue.setMessage("History retrieved and file upload and update successfully.");
                return returnValue;
            } else {
                returnValue.setStatus(-1);
                returnValue.setMessage("History retrieved fail.");
                return returnValue;
            }
        } catch (Exception e) {
            returnValue.setMessage("Failed to retrieve history for MAC address: " + macAddress);
            logger.error(returnValue.getMessage(), e);
            return returnValue;
        }
    }

    // Automatically delete backup files after three months.
    public RequestResponse deleteBackup() {
        RequestResponse returnValue = new RequestResponse();
        try {
            String[] dateTimeIST = getCurrentDateTimeIST();
            String currentDate = dateTimeIST[0];
            LocalDate currentDateParsed = LocalDate.parse(currentDate);
            List<DeviceManagerHistory> resultList = (List<DeviceManagerHistory>) deviceManagerHistoryService
                    .getHistories();
            int resultListSize = resultList.size();
            for (int i = 0; i < resultListSize; i++) {
                String macAddress = resultList.get(i).getMacAddress();
                String productclass = resultList.get(i).getProductClass();
                String fileName = resultList.get(i).getFileName();
                int id = resultList.get(i).getId();
                String version = resultList.get(i).getVersion();
                LocalDate dbDate = LocalDate.parse(resultList.get(i).getDate());
                LocalDate compareDate = currentDateParsed.minusMonths(3);
                if (dbDate.isBefore(compareDate)) {
                    int DeletedHistory = deviceManagerHistoryService.deleteHistory(id);
                    if (DeletedHistory == id) {
                        logger.info("History was deleted successfully for macaddress: " + macAddress + " version: "
                                + version + " product class: " + productclass + " file name: " + fileName);
                    } else {
                        logger.info("History was not deleted for macaddress: " + macAddress + " version: " + version
                                + " product class: " + productclass + " file name: " + fileName);
                    }
                }
            }
            returnValue.setStatus(0);
            returnValue.setMessage("Three month's back history was deleted successfully.");
            return returnValue;
        } catch (Exception e) {
            logger.error("Exception Error.", e);
            returnValue.setStatus(-1);
            returnValue.setMessage("Exception Error.");
            return returnValue;
        }
    }

    // Method of get acs token.
    public RequestResponse ACSLoginCall() {
        RequestResponse returnValue = new RequestResponse();
        try {
            String url = "http://" + Constants.DEVICE_MANAGER_IP + "/device-manager/login";
            String username = "admin";
            String password = "admin";
            String requestBody = "{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                String Token = response.getBody();
                int startIndex = Token.indexOf(":") + 2;
                int endIndex = Token.lastIndexOf("\"");
                Token = Token.substring(startIndex, endIndex);
                returnValue.setStatus(0);
                returnValue.setMessageDetail("Get acs token call successfully.");
                returnValue.setMessage(Token);
                logger.error(returnValue.getMessageDetail());
                setAcsGlobelToken(Token);
                return returnValue;
            } else {
                returnValue.setStatus(-1);
                returnValue.setMessage(null);
                returnValue.setMessageDetail("Get acs token call failed.");
                logger.error(returnValue.getMessageDetail());
                return returnValue;
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage(null);
            returnValue.setMessageDetail("Exception error in get acs token call failed.");
            logger.error(returnValue.getMessageDetail(), e);
            return returnValue;
        }
    }

    public String getAcsGlobelToken() {
        if (AcsTokenForAutoDeploy == null) {
            RequestResponse returnValue = ACSLoginCall();
            return returnValue.getMessage();
        }
        return AcsTokenForAutoDeploy;
    }

    public void setAcsGlobelToken(String token) {
        this.AcsTokenForAutoDeploy = token;
    }

    // Function of automatically upload and update file.
    public RequestResponse automaticallyUploadAndUpdateStart() {
        RequestResponse returnValue = new RequestResponse();
        try {
            String[] dateTimeIST = getCurrentDateTimeIST();
            String currentDate = dateTimeIST[0];
            String curretTime = dateTimeIST[1];
            String[] curretTimeParts = curretTime.split(":");
            String curretModifiedTime = curretTimeParts[0] + ":" + curretTimeParts[1];
            String token = getAcsGlobelToken();
            String version = "", fileFormatName = "", macAddress = "", fileFormat = "", productclass = "";
            byte[] fileData = null;
            List<DeviceManagerAutoDeploy> resultList = (List<DeviceManagerAutoDeploy>) deviceManagerAutoDeployService
                    .getAllAutoDeployData();
            int resultListSize = resultList.size();
            RequestResponse responseOfUpload = new RequestResponse();
            if (token != null && token != "") {
                for (int i = 0; i < resultListSize; i++) {
                    macAddress = resultList.get(i).getMacAddress();
                    productclass = resultList.get(i).getProductClass();
                    int id = resultList.get(i).getId();
                    fileData = resultList.get(i).getFiles();
                    fileFormat = resultList.get(i).getFileFormat();
                    version = resultList.get(i).getVersion();
                    String DbDate = resultList.get(i).getDate();
                    String DbTime = resultList.get(i).getTime();
                    String[] dbTimeParts = DbTime.split(":");
                    String dbModifiedTime = dbTimeParts[0] + ":" + dbTimeParts[1];
                    DbDate = DbDate.replaceAll("\\D", "");
                    currentDate = currentDate.replaceAll("\\D", "");
                    String rearrangedDate = currentDate.substring(6, 8) + // Day
                            currentDate.substring(4, 6) + // Month
                            currentDate.substring(0, 4); // Year
                    System.out.println(DbDate + " db");
                    System.out.println(rearrangedDate);
                    System.out.println(dbModifiedTime);
                    System.out.println(curretModifiedTime);
                    if (DbDate.equals(rearrangedDate) && dbModifiedTime.equals(curretModifiedTime)) {
                        System.out.println("I am call");
                        if (fileFormat.equals("3 Vendor Configuration File")) {
                            fileFormatName = "configuration";
                        } else if (fileFormat.equals("1 Firmware Upgrade Images")) {
                            fileFormatName = "firmware";
                        }
                        responseOfUpload = upload(token, fileData, macAddress, fileFormatName, version);
                        if (responseOfUpload.getStatus() == 0) {
                            int deleteItem = deviceManagerAutoDeployService.deleteAutoDeployData(id);
                            logger.info("Device manager auto deploy row deleted successfully." + deleteItem);
                        }
                    }
                }
            }
            if (responseOfUpload.getStatus() == 0) {
                returnValue.setStatus(0);
                returnValue.setMessage(
                        "Upload and update files automatically at scheduled times and date successfully for macAddress: "
                                + macAddress + " ,fileFormat " + fileFormat + " and product class " + productclass);
                return returnValue;
            } else {
                returnValue.setStatus(-1);
                returnValue.setMessage(
                        "Upload and update files automatically at scheduled times and date failed for macAddress: "
                                + macAddress + " ,fileFormat " + fileFormat + " and product class " + productclass);
                logger.info(returnValue.getMessage());
                return returnValue;
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage("Upload and update files automatically at scheduled times and date failed.");
            logger.error(returnValue.getMessage(), e);
            return returnValue;
        }
    }

    // Add file in automatically update and upload api call.
    public RequestResponse addItemInAutoDeploy(String token, String macAddress, String date, String time,
            String fileFormat, byte[] fileInByte) {
        RequestResponse returnValue = new RequestResponse();
        try {
            DeviceManagerInfoModel deviceData = new DeviceManagerInfoModel();
            deviceData = getProductDetails(macAddress);
            DeviceManagerAutoDeploy Data = new DeviceManagerAutoDeploy();
            String productClass = deviceData.getProductClass();
            String fileName = "";
            if (fileFormat.equals("3 Vendor Configuration File")) {
                fileName = "cfg" + macAddress + ".xml";
            } else if (fileFormat.equals("1 Firmware Upgrade Images")) {
                fileName = productClass + ".rom";
            }
            Data.setDate(date);
            Data.setTime(time);
            Data.setFiles(fileInByte);
            Data.setFileName(fileName);
            Data.setFileFormat(fileFormat);
            Data.setProductClass(productClass);
            Data.setVersion("0");
            Data.setMacAddress(macAddress);
            DeviceManagerAutoDeploy responseData = deviceManagerAutoDeployService.saveAutoDeployData(Data);
            if (responseData.getMacAddress() == macAddress) {
                returnValue.setStatus(0);
                returnValue.setMessage(
                        "Add file in automatically update and upload api call successfully macAddress: " + macAddress);
                logger.info(returnValue.getMessage());
                return returnValue;
            } else {
                returnValue.setStatus(-1);
                returnValue.setMessage(
                        "Add file in automatically update and upload api call failed macAddress: " + macAddress);
                logger.info(returnValue.getMessage());
                return returnValue;
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage("Exception Error in automatically update and upload api call.");
            logger.error(returnValue.getMessage(), e);
            return returnValue;
        }
    }

    // Method of listing devices
    public RequestResponse methodOfListingDevices(String token) {
        RequestResponse returnValue = new RequestResponse();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set("Cookie", "session={\"AuthToken\":\"" + token + "\"}");
            HttpEntity<byte[]> entity = new HttpEntity<>(headers);
            String url = "http://" + Constants.DEVICE_MANAGER_IP + "/device-manager/api/files/?filter=true";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity,
                    String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                returnValue.setStatus(0);
                returnValue.setMessage(response.getBody());
                returnValue.setMessageDetail("Listing device api call successfully.");
                return returnValue;
            } else {
                returnValue.setStatus(-1);
                returnValue.setMessage("Listing device api call fail.");
                logger.info(returnValue.getMessage());
                return returnValue;
            }
        } catch (Exception e) {
            returnValue.setStatus(-1);
            returnValue.setMessage("Listing device internal server error.");
            logger.error(returnValue.getMessage(), e);
            return returnValue;
        }
    }

    public static String base64Encode(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    public RequestResponse updateSipExtension(String userName, String password, String token ,String macAddress, DeviceManagerExtension request) {
        RequestResponse result = new RequestResponse();
        try {
            String url = "http://" + macAddress + "/download_xml_cfg";
            String encodedCredentials = base64Encode(userName + ":" + password);
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                byte[] fileData = responseBody.getBytes(StandardCharsets.UTF_8);
                //UpdateConfigFileDate(fileData,request);
                result.setStatus(0);
                result.setMessage("SIP extension updated successfully.");
            } else {
                result.setStatus(response.getStatusCodeValue());
                result.setMessage("Failed to update SIP extension. HTTP Status Code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            result.setMessage("Internal server error: " + e.getMessage());
        }
        return result;
    }
}
