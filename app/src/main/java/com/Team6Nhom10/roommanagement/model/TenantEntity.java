package com.Team6Nhom10.roommanagement.model;

public class TenantEntity {

    public int id;
    public String name; // Họ tên
    public String phone; // Số điện thoại (unique)
    public String idCard; // CCCD/CMND (tuỳ chọn)
    public String notes; // Ghi chú (tuỳ chọn)

    public TenantEntity() {
    }

    /** Label hiển thị trong picker. */
    public String getDisplayLabel() {
        return name + "  |  " + phone;
    }
}
