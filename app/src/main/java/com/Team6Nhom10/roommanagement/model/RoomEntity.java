package com.Team6Nhom10.roommanagement.model;

public class RoomEntity {

    public int id;
    public String roomCode = ""; // Mã phòng (không được sửa)
    public String roomName;
    public double price;
    public double area;
    public String description;
    public double electricityPrice;
    public double waterPrice;
    public String photoPath;
    public int status; // 0 = còn trống, 1 = đã thuê
    public int tenantId = -1; // ID người thuê (-1 = chưa liên kết)
    public String tenantName;
    public String tenantPhone;
    public String startDate;

    public RoomEntity() {}

    public boolean isOccupied() {
        return status == 1;
    }
}
