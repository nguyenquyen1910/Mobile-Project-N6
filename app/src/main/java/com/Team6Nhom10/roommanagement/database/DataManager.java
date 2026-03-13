package com.Team6Nhom10.roommanagement.database;

import android.content.Context;

import com.Team6Nhom10.roommanagement.model.RoomEntity;
import com.Team6Nhom10.roommanagement.model.TenantEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory data store. Data lives only for the duration of the app process.
 * No file I/O, no database — just plain Java Lists.
 */
public class DataManager {

    private static DataManager INSTANCE;

    private final List<RoomEntity> rooms = new ArrayList<>();
    private final List<TenantEntity> tenants = new ArrayList<>();
    private int nextRoomId = 1;
    private int nextTenantId = 1;

    private DataManager() {
        seedData();
    }

    /** Context parameter kept for call-site compatibility; not used internally. */
    public static synchronized DataManager getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DataManager();
        }
        return INSTANCE;
    }

    // ─── Seed data ────────────────────────────────────────────────────────────

    private void seedData() {
        TenantEntity t1 = new TenantEntity();
        t1.id = nextTenantId++;
        t1.name = "Nguyễn Văn A";
        t1.phone = "0912345678";
        t1.idCard = "001234567890";
        t1.notes = "";
        tenants.add(t1);

        TenantEntity t2 = new TenantEntity();
        t2.id = nextTenantId++;
        t2.name = "Trần Thị B";
        t2.phone = "0987654321";
        t2.idCard = "009876543210";
        t2.notes = "Sinh viên đại học";
        tenants.add(t2);

        RoomEntity r1 = new RoomEntity();
        r1.id = nextRoomId++;
        r1.roomCode = "P101";
        r1.roomName = "Phòng 101";
        r1.price = 3_000_000;
        r1.area = 20;
        r1.electricityPrice = 3_500;
        r1.waterPrice = 15_000;
        r1.description = "Phòng thoáng mát, có ban công";
        r1.status = 1;
        r1.tenantId = t1.id;
        r1.tenantName = t1.name;
        r1.tenantPhone = t1.phone;
        r1.startDate = "01/01/2025";
        rooms.add(r1);

        RoomEntity r2 = new RoomEntity();
        r2.id = nextRoomId++;
        r2.roomCode = "P102";
        r2.roomName = "Phòng 102";
        r2.price = 2_500_000;
        r2.area = 18;
        r2.electricityPrice = 3_500;
        r2.waterPrice = 15_000;
        r2.description = "";
        r2.status = 0;
        rooms.add(r2);

        RoomEntity r3 = new RoomEntity();
        r3.id = nextRoomId++;
        r3.roomCode = "P201";
        r3.roomName = "Phòng 201";
        r3.price = 3_500_000;
        r3.area = 25;
        r3.electricityPrice = 3_500;
        r3.waterPrice = 15_000;
        r3.description = "Phòng rộng, gần thang máy";
        r3.status = 1;
        r3.tenantId = t2.id;
        r3.tenantName = t2.name;
        r3.tenantPhone = t2.phone;
        r3.startDate = "15/02/2025";
        rooms.add(r3);
    }

    // ─── Room CRUD ─────────────────────────────────────────────────────────────

    public synchronized void addRoom(RoomEntity room) {
        room.id = nextRoomId++;
        rooms.add(room);
    }

    public synchronized void updateRoom(RoomEntity room) {
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).id == room.id) {
                rooms.set(i, room);
                return;
            }
        }
    }

    public synchronized void deleteRoom(RoomEntity room) {
        rooms.removeIf(r -> r.id == room.id);
    }

    public synchronized RoomEntity getRoomById(int id) {
        for (RoomEntity r : rooms) {
            if (r.id == id) return r;
        }
        return null;
    }

    public synchronized RoomEntity getRoomByCode(String code) {
        for (RoomEntity r : rooms) {
            if (r.roomCode != null && r.roomCode.equalsIgnoreCase(code)) return r;
        }
        return null;
    }

    /**
     * Returns rooms filtered by status (-1=all, 0=vacant, 1=occupied)
     * and matching query against code, name, tenantName, tenantPhone.
     */
    public synchronized List<RoomEntity> searchRooms(String query, int filter) {
        String q = (query == null) ? "" : query.trim().toLowerCase();
        List<RoomEntity> result = new ArrayList<>();
        for (RoomEntity r : rooms) {
            if (filter != -1 && r.status != filter) continue;
            if (!q.isEmpty()) {
                boolean match =
                        contains(r.roomCode, q) ||
                        contains(r.roomName, q) ||
                        contains(r.tenantName, q) ||
                        contains(r.tenantPhone, q);
                if (!match) continue;
            }
            result.add(r);
        }
        return result;
    }

    // ─── Tenant CRUD ───────────────────────────────────────────────────────────

    /**
     * Adds a new tenant. Returns null on success, or an error message string
     * if the phone number is already taken.
     */
    public synchronized String addTenant(TenantEntity tenant) {
        if (getTenantByPhone(tenant.phone) != null) {
            return "Số điện thoại đã tồn tại";
        }
        tenant.id = nextTenantId++;
        tenants.add(tenant);
        return null;
    }

    /**
     * Updates an existing tenant. Returns null on success, or an error message
     * if the phone is already used by a different tenant.
     * Also cascades name/phone changes to any linked room.
     */
    public synchronized String updateTenant(TenantEntity tenant) {
        TenantEntity existing = getTenantByPhone(tenant.phone);
        if (existing != null && existing.id != tenant.id) {
            return "Số điện thoại đã tồn tại";
        }
        for (int i = 0; i < tenants.size(); i++) {
            if (tenants.get(i).id == tenant.id) {
                tenants.set(i, tenant);
                cascadeTenantToRooms(tenant);
                return null;
            }
        }
        return "Không tìm thấy người thuê";
    }

    /**
     * Deletes a tenant. Returns false (and does nothing) if the tenant currently
     * has an active room rental (status == 1).
     */
    public synchronized boolean deleteTenant(TenantEntity tenant) {
        if (isTenantRenting(tenant.id)) return false;
        tenants.removeIf(t -> t.id == tenant.id);
        return true;
    }

    public synchronized List<TenantEntity> getAllTenants() {
        return new ArrayList<>(tenants);
    }

    public synchronized TenantEntity getTenantById(int id) {
        for (TenantEntity t : tenants) {
            if (t.id == id) return t;
        }
        return null;
    }

    public synchronized TenantEntity getTenantByPhone(String phone) {
        if (phone == null) return null;
        for (TenantEntity t : tenants) {
            if (phone.equals(t.phone)) return t;
        }
        return null;
    }

    public synchronized List<TenantEntity> searchTenants(String query) {
        String q = (query == null) ? "" : query.trim().toLowerCase();
        if (q.isEmpty()) return getAllTenants();
        List<TenantEntity> result = new ArrayList<>();
        for (TenantEntity t : tenants) {
            if (contains(t.name, q) || contains(t.phone, q) || contains(t.idCard, q)) {
                result.add(t);
            }
        }
        return result;
    }

    /** Returns tenants who are not currently occupying any room. */
    public synchronized List<TenantEntity> getAvailableTenants() {
        List<TenantEntity> result = new ArrayList<>();
        for (TenantEntity t : tenants) {
            if (!isTenantRenting(t.id)) result.add(t);
        }
        return result;
    }

    public synchronized boolean isTenantRenting(int tenantId) {
        for (RoomEntity r : rooms) {
            if (r.status == 1 && r.tenantId == tenantId) return true;
        }
        return false;
    }

    // ─── Statistics ────────────────────────────────────────────────────────────

    public synchronized int getTotalCount() {
        return rooms.size();
    }

    public synchronized int getVacantCount() {
        int n = 0;
        for (RoomEntity r : rooms) if (r.status == 0) n++;
        return n;
    }

    public synchronized int getOccupiedCount() {
        int n = 0;
        for (RoomEntity r : rooms) if (r.status == 1) n++;
        return n;
    }

    /** Sum of price for all occupied rooms. */
    public synchronized double getMonthlyRevenue() {
        double sum = 0;
        for (RoomEntity r : rooms) if (r.status == 1) sum += r.price;
        return sum;
    }

    /** Sum of price for ALL rooms (100% occupancy scenario). */
    public synchronized double getExpectedRevenue() {
        double sum = 0;
        for (RoomEntity r : rooms) sum += r.price;
        return sum;
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private void cascadeTenantToRooms(TenantEntity tenant) {
        for (RoomEntity r : rooms) {
            if (r.tenantId == tenant.id) {
                r.tenantName = tenant.name;
                r.tenantPhone = tenant.phone;
            }
        }
    }

    private static boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }
}
