package com.example.autoprovisioning.component.entity;

import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "device_info")
public class DeviceManagerInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String uuid;

    @PrePersist
    public void generateUniqueId() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
    }

    @Column(name = "product_class")
    private String productClass;

    @Column(name = "mac_address")
    private String macAddress;
    
    @Column(name = "oui")
    private String oui;
    
    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "firmware_version")
    private String firmwareVersion;

    @Column(name = "active_device")
    private boolean activeDevice;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "ping")
    private boolean ping;

    @Column(name = "configuration_version")
    private String configurationVersion;

}

