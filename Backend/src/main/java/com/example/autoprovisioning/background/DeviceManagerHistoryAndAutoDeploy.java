package com.example.autoprovisioning.background;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.autoprovisioning.component.service.DeviceManagerService;
import jakarta.annotation.PostConstruct;
import com.example.autoprovisioning.component.helper.Constants;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class DeviceManagerHistoryAndAutoDeploy {

    private static final Logger logger = LoggerFactory.getLogger(DeviceManagerHistoryAndAutoDeploy.class);

    @Autowired
    DeviceManagerService deviceManagerService;

    @PostConstruct
    public void deviceManagerHistoryStart() {
        logger.info("Device Manager History Start process.");

        // Schedule the task to run every minute
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::runDeleteBackup, 0, 10, TimeUnit.HOURS);
    }

    private void runDeleteBackup() {
        // if (!Constants.DEVICE_MANAGER_IP.equals("")) {
        //     logger.info(
        //             "Running device manager history thread for refresh token every 14 minutes function name: ACSLoginCall()");
        //     deviceManagerService.ACSLoginCall();
        //     ScheduledExecutorService uploadExecutor = Executors.newSingleThreadScheduledExecutor();
        //     uploadExecutor.scheduleAtFixedRate(() -> {
                
        //             logger.info("Running device manager history thread function name: deleteBackup()");
        //             deviceManagerService.deleteBackup();
        //             logger.info(
        //                     "Running device manager automatically upload and update file thread function name: automaticallyUploadAndUpdateStart()");
        //             deviceManagerService.automaticallyUploadAndUpdateStart();
                
        //     }, 0, 1, TimeUnit.SECONDS);
        // }
    }
}
