package com.example.banve.dao;

import com.example.banve.models.ChiTietHoaDon;
import com.example.banve.models.HoaDon;
import com.example.banve.network.ApiCallback;
import com.example.banve.network.ApiService;
import com.example.banve.network.SupabaseClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HoaDonDAO {
    private final ApiService apiService;

    public HoaDonDAO() {
        apiService = SupabaseClient.layApiService();
    }

    public void taoHoaDon(HoaDon hoaDon, ApiCallback<HoaDon> callback) {
        apiService.taoHoaDon(taoDuLieuHoaDon(hoaDon)).enqueue(new Callback<List<HoaDon>>() {
            @Override
            public void onResponse(Call<List<HoaDon>> call, Response<List<HoaDon>> response) {
                xuLyMotHoaDon(response, callback, "Không thể tạo hóa đơn");
            }

            @Override
            public void onFailure(Call<List<HoaDon>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void capNhatTrangThai(int maHoaDon, String trangThai, ApiCallback<HoaDon> callback) {
        Map<String, Object> duLieuCapNhat = new HashMap<>();
        duLieuCapNhat.put("TrangThai", trangThai);

        capNhatHoaDon(maHoaDon, duLieuCapNhat, callback);
    }

    public void layTheoMa(int maHoaDon, ApiCallback<HoaDon> callback) {
        Map<String, String> filter = new HashMap<>();
        filter.put("MaHoaDon", "eq." + maHoaDon);
        filter.put("select", "*");
        filter.put("limit", "1");

        apiService.timHoaDon(filter).enqueue(new Callback<List<HoaDon>>() {
            @Override
            public void onResponse(Call<List<HoaDon>> call, Response<List<HoaDon>> response) {
                xuLyMotHoaDon(response, callback, "Không thể tải trạng thái thanh toán");
            }

            @Override
            public void onFailure(Call<List<HoaDon>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void layDanhSachDaThanhToan(int maNguoiDung, ApiCallback<List<HoaDon>> callback) {
        Map<String, String> filter = new HashMap<>();
        filter.put("MaNguoiDung", "eq." + maNguoiDung);
        filter.put("TrangThai", "eq.DaThanhToan");
        filter.put("select", "*");
        filter.put("order", "NgayLap.desc");

        apiService.timHoaDon(filter).enqueue(new Callback<List<HoaDon>>() {
            @Override
            public void onResponse(Call<List<HoaDon>> call, Response<List<HoaDon>> response) {
                if (!response.isSuccessful()) {
                    callback.onError("Không thể tải lịch sử đơn hàng");
                    return;
                }

                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<List<HoaDon>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void layDanhSachHoaDonQuanLy(ApiCallback<List<HoaDon>> callback) {
        Map<String, String> filter = new HashMap<>();
        filter.put("select", "*,NguoiDung(HoTen)");
        filter.put("order", "NgayLap.desc");

        apiService.timHoaDon(filter).enqueue(new Callback<List<HoaDon>>() {
            @Override
            public void onResponse(Call<List<HoaDon>> call, Response<List<HoaDon>> response) {
                if (!response.isSuccessful()) {
                    callback.onError("Không thể tải danh sách hóa đơn");
                    return;
                }

                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<List<HoaDon>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void layChiTietHoaDon(int maHoaDon, ApiCallback<List<ChiTietHoaDon>> callback) {
        Map<String, String> filter = new HashMap<>();
        filter.put("MaHoaDon", "eq." + maHoaDon);
        filter.put("select", "*,Ve(*,LoaiVe(*))");
        filter.put("order", "MaChiTietHoaDon.asc");

        apiService.timChiTietHoaDon(filter).enqueue(new Callback<List<ChiTietHoaDon>>() {
            @Override
            public void onResponse(Call<List<ChiTietHoaDon>> call, Response<List<ChiTietHoaDon>> response) {
                if (!response.isSuccessful()) {
                    callback.onError("Không thể tải chi tiết hóa đơn");
                    return;
                }

                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<List<ChiTietHoaDon>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void layChiTietHoaDonTheoDanhSach(List<Integer> danhSachMaHoaDon, ApiCallback<List<ChiTietHoaDon>> callback) {
        if (danhSachMaHoaDon == null || danhSachMaHoaDon.isEmpty()) {
            callback.onSuccess(null);
            return;
        }

        Map<String, String> filter = new HashMap<>();
        filter.put("MaHoaDon", "in.(" + noiDanhSachMa(danhSachMaHoaDon) + ")");
        filter.put("select", "*,Ve(*,LoaiVe(*))");

        apiService.timChiTietHoaDon(filter).enqueue(new Callback<List<ChiTietHoaDon>>() {
            @Override
            public void onResponse(Call<List<ChiTietHoaDon>> call, Response<List<ChiTietHoaDon>> response) {
                if (!response.isSuccessful()) {
                    callback.onError("Không thể tải chi tiết hóa đơn thống kê");
                    return;
                }

                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<List<ChiTietHoaDon>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void capNhatHoaDon(int maHoaDon, Map<String, Object> duLieuCapNhat, ApiCallback<HoaDon> callback) {
        apiService.capNhatHoaDon("eq." + maHoaDon, duLieuCapNhat).enqueue(new Callback<List<HoaDon>>() {
            @Override
            public void onResponse(Call<List<HoaDon>> call, Response<List<HoaDon>> response) {
                xuLyMotHoaDon(response, callback, "Không thể cập nhật hóa đơn");
            }

            @Override
            public void onFailure(Call<List<HoaDon>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void xuLyMotHoaDon(Response<List<HoaDon>> response, ApiCallback<HoaDon> callback, String thongBaoLoi) {
        if (!response.isSuccessful()) {
            callback.onError(thongBaoLoi);
            return;
        }

        List<HoaDon> danhSachHoaDon = response.body();
        if (danhSachHoaDon == null || danhSachHoaDon.isEmpty()) {
            callback.onError("Không nhận được dữ liệu hóa đơn");
            return;
        }

        callback.onSuccess(danhSachHoaDon.get(0));
    }

    private String noiDanhSachMa(List<Integer> danhSachMa) {
        StringBuilder ketQua = new StringBuilder();
        for (int i = 0; i < danhSachMa.size(); i++) {
            if (i > 0) {
                ketQua.append(",");
            }
            ketQua.append(danhSachMa.get(i));
        }
        return ketQua.toString();
    }

    private Map<String, Object> taoDuLieuHoaDon(HoaDon hoaDon) {
        Map<String, Object> duLieu = new HashMap<>();
        duLieu.put("MaNguoiDung", hoaDon.getMaNguoiDung());
        duLieu.put("TongTien", hoaDon.getTongTien());
        duLieu.put("MaVoucher", hoaDon.getMaVoucher());
        duLieu.put("TienGiam", hoaDon.getTienGiam());
        duLieu.put("ThanhToan", hoaDon.getThanhToan());
        duLieu.put("TrangThai", hoaDon.getTrangThai());
        duLieu.put("NoiDungChuyenKhoan", hoaDon.getNoiDungChuyenKhoan());
        return duLieu;
    }
}
