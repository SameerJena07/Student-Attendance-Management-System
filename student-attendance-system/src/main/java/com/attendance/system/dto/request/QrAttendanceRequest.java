package com.attendance.system.dto.request;

/**
 * DTO to handle QR-based attendance requests from the student's scanner.
 */
public class QrAttendanceRequest {

    private String scannedToken; // The JWT string extracted from the scanned QR code
    private Long studentId; // The unique ID of the student marking attendance

    // Optional fields for future Geofencing security (Best practice)
    private Double latitude;
    private Double longitude;

    // Default Constructor (Required by Jackson for JSON mapping)
    public QrAttendanceRequest() {
    }

    // Parameterized Constructor
    public QrAttendanceRequest(String scannedToken, Long studentId) {
        this.scannedToken = scannedToken;
        this.studentId = studentId;
    }

    // --- GETTERS AND SETTERS ---

    public String getScannedToken() {
        return scannedToken;
    }

    public void setScannedToken(String scannedToken) {
        this.scannedToken = scannedToken;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}