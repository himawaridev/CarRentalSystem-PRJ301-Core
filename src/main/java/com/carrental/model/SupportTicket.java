package com.carrental.model;

import java.time.LocalDateTime;

public class SupportTicket {
    private long ticketId;
    private String ticketCode;
    private int userId;
    private Long contractId;
    private String category;
    private String subject;
    private String message;
    private String status;
    private String priority;
    private String staffResponse;
    private Integer assignedToUserId;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String contractCode;
    private String assignedStaffName;

    public long getTicketId() { return ticketId; }
    public void setTicketId(long ticketId) { this.ticketId = ticketId; }

    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStaffResponse() { return staffResponse; }
    public void setStaffResponse(String staffResponse) { this.staffResponse = staffResponse; }

    public Integer getAssignedToUserId() { return assignedToUserId; }
    public void setAssignedToUserId(Integer assignedToUserId) { this.assignedToUserId = assignedToUserId; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getContractCode() { return contractCode; }
    public void setContractCode(String contractCode) { this.contractCode = contractCode; }

    public String getAssignedStaffName() { return assignedStaffName; }
    public void setAssignedStaffName(String assignedStaffName) { this.assignedStaffName = assignedStaffName; }

    public String getCategoryLabel() {
        if ("BANK_INFO".equals(category)) return "Sai tai khoan ngan hang";
        if ("PAYMENT".equals(category)) return "Thanh toan";
        if ("REFUND".equals(category)) return "Hoan coc";
        if ("CONTRACT".equals(category)) return "Hop dong";
        if ("ACCOUNT".equals(category)) return "Tai khoan";
        return "Khac";
    }

    public String getStatusLabel() {
        if ("OPEN".equals(status)) return "Moi";
        if ("IN_PROGRESS".equals(status)) return "Dang xu ly";
        if ("RESOLVED".equals(status)) return "Da xu ly";
        if ("REJECTED".equals(status)) return "Tu choi";
        return status;
    }

    public String getPriorityLabel() {
        if ("HIGH".equals(priority)) return "Cao";
        if ("LOW".equals(priority)) return "Thap";
        return "Binh thuong";
    }
}
