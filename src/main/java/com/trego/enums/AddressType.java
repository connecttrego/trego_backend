package com.trego.enums;

public enum AddressType {
    HOME(0, "Home"),
    WORK(1, "Work"),
    OTHERS(2, "Others");

    private final int code;
    private final String description;

    // Constructor
    AddressType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    // Getter for code
    public int getCode() {
        return code;
    }

    // Getter for description
    public String getDescription() {
        return description;
    }

    // Method to get AddressType from code
    public static AddressType fromCode(int code) {
        for (AddressType type : AddressType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid code for AddressType: " + code);
    }
}