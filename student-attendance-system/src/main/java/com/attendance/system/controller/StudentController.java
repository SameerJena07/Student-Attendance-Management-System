package com.attendance.system.controller;

import com.attendance.system.config.JwtUtils; // ADDED: For token validation
import com.attendance.system.dto.request.LeaveRequestDTO;
import com.attendance.system.dto.request.QrAttendanceRequest; // ADDED: New DTO
import com.attendance.system.dto.response.AttendanceDetailResponse;
import com.attendance.system.dto.response.LeaveRequestResponse;
import com.attendance.system.dto.response.StudentDashboardResponse;
import com.attendance.system.repository.StudentRepository;
import com.attendance.system.entity.Student;
import com.attendance.system.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private JwtUtils jwtUtils; // ADDED: Inject JwtUtils

    private Long getCurrentStudentId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return student.getId();
    }

    /**
     * ✅ NEW ENDPOINT: Marks attendance by processing the scanned QR token.
     * This is "free and best" because it reuses your existing JWT security.
     */
    @PostMapping("/attendance/qr")
    public ResponseEntity<String> markAttendanceByQr(@RequestBody QrAttendanceRequest request) {
        // 1. Validate if the QR token is authentic and not expired (60sec limit)
        if (!jwtUtils.validateJwtToken(request.getScannedToken())) {
            return ResponseEntity.badRequest().body("Error: Invalid or expired QR Code.");
        }

        // 2. Extract the Course ID (Class ID) hidden inside the token
        Long courseId = jwtUtils.getClassIdFromQrToken(request.getScannedToken());

        // 3. Mark the attendance in the database
        // Note: We use request.setStudentId(getCurrentStudentId()) to ensure security
        request.setStudentId(getCurrentStudentId());

        String result = studentService.markAttendanceViaQr(courseId, request.getStudentId());

        return ResponseEntity.ok(result);
    }

    // --- EXISTING ENDPOINTS ---

    @GetMapping("/dashboard")
    public ResponseEntity<StudentDashboardResponse> getDashboard(
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String semester) {

        if ("All".equalsIgnoreCase(academicYear) || "".equals(academicYear))
            academicYear = null;
        if ("All".equalsIgnoreCase(semester) || "".equals(semester))
            semester = null;

        return ResponseEntity.ok(studentService.getStudentDashboard(getCurrentStudentId(), academicYear, semester));
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<AttendanceDetailResponse>> getAttendanceHistory(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null)
            startDate = LocalDate.now().minusDays(30);
        if (endDate == null)
            endDate = LocalDate.now();

        return ResponseEntity
                .ok(studentService.getStudentAttendance(getCurrentStudentId(), courseId, startDate, endDate));
    }

    @PostMapping("/leave")
    public ResponseEntity<LeaveRequestResponse> createLeaveRequest(@Valid @RequestBody LeaveRequestDTO requestDTO) {
        return ResponseEntity.ok(studentService.createLeaveRequest(requestDTO, getCurrentStudentId()));
    }

    @GetMapping("/leave")
    public ResponseEntity<List<LeaveRequestResponse>> getLeaveRequests() {
        return ResponseEntity.ok(studentService.getStudentLeaveRequests(getCurrentStudentId()));
    }
}