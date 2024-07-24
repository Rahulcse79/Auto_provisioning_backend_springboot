package com.example.autoprovisioning.component.helper;

public class Constants {

    public enum NodeStatus {
        OK(0),
        WARNING(1),
        ERROR(2),
        CRITICAL(3),
        NA(4),
        BAD_CREDENTIAL(5),
        UNKNOWN_ERROR(6),
        RECORDS_NOT_FOUND(7),
        ACCOUNT_NOT_ENABLED(8),
        ACCOUNT_EXPIRED(9),
        CREDENTIAL_EXPIRED(10),
        ACCOUNT_BLOCKED(11),
        TOO_MANY_DAYS(12),
        TOO_MANY_FAILED_ATTEMPTS(13);

        private int value;

        private NodeStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static String DEVICE_MANAGER_IP = "192.168.20.178";
    public static String MongoDB_IP = "192.168.20.178";

}
