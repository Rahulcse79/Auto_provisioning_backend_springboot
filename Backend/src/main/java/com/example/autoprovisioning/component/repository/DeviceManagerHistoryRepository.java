package com.example.autoprovisioning.component.repository;

import org.springframework.stereotype.Repository;
import com.example.autoprovisioning.component.entity.DeviceManagerHistory;
import org.springframework.data.repository.CrudRepository;

@Repository
public interface DeviceManagerHistoryRepository extends CrudRepository<DeviceManagerHistory,Integer> {
    DeviceManagerHistory findByVersionAndMacAddressAndProductClassAndFileFormat(String version, String macAddress, String productClass, String fileFormat);
}
