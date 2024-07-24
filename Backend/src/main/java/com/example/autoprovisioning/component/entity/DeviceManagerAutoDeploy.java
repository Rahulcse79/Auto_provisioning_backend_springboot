package com.example.autoprovisioning.component.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Getter
@Setter
@Table(name = "device_manager_auto_deploy")
public class DeviceManagerAutoDeploy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "files")
    private byte[] files;

    @Column(name = "mac_address")
    private String macAddress;

    @Column(name = "product_class")
    private String productClass;

    @Column(name = "version")
    private String version;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_format")
    private String fileFormat;

    @Column(name = "date")
    private String date;

    @Column(name = "time")
    private String time;
}
