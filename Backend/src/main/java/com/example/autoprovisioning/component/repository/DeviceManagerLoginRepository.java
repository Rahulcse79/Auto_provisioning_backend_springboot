package com.example.autoprovisioning.component.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.example.autoprovisioning.component.entity.DeviceManagerLogin;

@Repository
public interface DeviceManagerLoginRepository extends CrudRepository<DeviceManagerLogin,Integer>{
    DeviceManagerLogin findByUserName(String userName);
}
