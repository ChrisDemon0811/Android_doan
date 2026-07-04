package com.example.banve.dao;

import com.example.banve.models.Ve;
import com.example.banve.network.ApiCallback;
import com.example.banve.network.ApiService;
import com.example.banve.network.SupabaseClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VeDAO {
    private final ApiService apiService;

    public VeDAO() {
        apiService = SupabaseClient.layApiService();
    }

    public void layDanhSachVe(Integer maLoaiVe, ApiCallback<List<Ve>> callback) {
        Map<String, String> filter = new HashMap<>();
        filter.put("TrangThai", "eq.HoatDong");
        filter.put("select", "*");
        filter.put("order", "GiaVe.asc,MaVe.asc");
        if (maLoaiVe != null) {
            filter.put("MaLoaiVe", "eq." + maLoaiVe);
        }

        apiService.layDanhSachVe(filter).enqueue(new Callback<List<Ve>>() {
            @Override
            public void onResponse(Call<List<Ve>> call, Response<List<Ve>> response) {
                if (!response.isSuccessful()) {
                    callback.onError("Không thể tải danh sách vé");
                    return;
                }

                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<List<Ve>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void layTheoMa(int maVe, ApiCallback<Ve> callback) {
        Map<String, String> filter = new HashMap<>();
        filter.put("MaVe", "eq." + maVe);
        filter.put("select", "*");

        apiService.layDanhSachVe(filter).enqueue(new Callback<List<Ve>>() {
            @Override
            public void onResponse(Call<List<Ve>> call, Response<List<Ve>> response) {
                if (!response.isSuccessful()) {
                    callback.onError("Không thể tải thông tin vé");
                    return;
                }

                List<Ve> danhSachVe = response.body();
                if (danhSachVe == null || danhSachVe.isEmpty()) {
                    callback.onError("Không tìm thấy vé");
                    return;
                }

                callback.onSuccess(danhSachVe.get(0));
            }

            @Override
            public void onFailure(Call<List<Ve>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void layDanhSachVeQuanLy(ApiCallback<List<Ve>> callback) {
        Map<String, String> filter = new HashMap<>();
        filter.put("select", "*,LoaiVe(*)");
        filter.put("order", "GiaVe.asc,MaVe.asc");

        apiService.layDanhSachVe(filter).enqueue(new Callback<List<Ve>>() {
            @Override
            public void onResponse(Call<List<Ve>> call, Response<List<Ve>> response) {
                if (!response.isSuccessful()) {
                    callback.onError("Không thể tải danh sách vé quản lý");
                    return;
                }

                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<List<Ve>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void themVe(Ve ve, ApiCallback<Ve> callback) {
        apiService.themVe(taoDuLieuVe(ve)).enqueue(new Callback<List<Ve>>() {
            @Override
            public void onResponse(Call<List<Ve>> call, Response<List<Ve>> response) {
                xuLyKetQuaMotVe(response, callback, "Không thể thêm vé");
            }

            @Override
            public void onFailure(Call<List<Ve>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void suaVe(Ve ve, ApiCallback<Ve> callback) {
        apiService.capNhatVe("eq." + ve.getMaVe(), taoDuLieuVe(ve)).enqueue(new Callback<List<Ve>>() {
            @Override
            public void onResponse(Call<List<Ve>> call, Response<List<Ve>> response) {
                xuLyKetQuaMotVe(response, callback, "Không thể cập nhật vé");
            }

            @Override
            public void onFailure(Call<List<Ve>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void xoaVe(int maVe, ApiCallback<Boolean> callback) {
        apiService.xoaVe("eq." + maVe).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    callback.onError("Không thể xóa vé");
                    return;
                }

                callback.onSuccess(true);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void capNhatSoLuong(int maVe, int soLuongMoi, ApiCallback<Ve> callback) {
        Map<String, Object> duLieuCapNhat = new HashMap<>();
        duLieuCapNhat.put("SoLuong", soLuongMoi);

        apiService.capNhatVe("eq." + maVe, duLieuCapNhat).enqueue(new Callback<List<Ve>>() {
            @Override
            public void onResponse(Call<List<Ve>> call, Response<List<Ve>> response) {
                if (!response.isSuccessful()) {
                    callback.onError("Không thể cập nhật số lượng vé");
                    return;
                }

                List<Ve> danhSachVe = response.body();
                if (danhSachVe == null || danhSachVe.isEmpty()) {
                    callback.onError("Không nhận được dữ liệu vé");
                    return;
                }

                callback.onSuccess(danhSachVe.get(0));
            }

            @Override
            public void onFailure(Call<List<Ve>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private Map<String, Object> taoDuLieuVe(Ve ve) {
        Map<String, Object> duLieu = new HashMap<>();
        duLieu.put("MaLoaiVe", ve.getMaLoaiVe());
        duLieu.put("TenVe", ve.getTenVe());
        duLieu.put("GiaVe", ve.getGiaVe());
        duLieu.put("GiaNguoiLon", ve.getGiaNguoiLon());
        duLieu.put("GiaTreEm", ve.getGiaTreEm());
        duLieu.put("GiaNguoiCaoTuoi", ve.getGiaNguoiCaoTuoi());
        duLieu.put("SoLuong", ve.getSoLuong());
        duLieu.put("MoTa", ve.getMoTa());
        duLieu.put("ThongTinVe", ve.getThongTinVe());
        duLieu.put("AnhVe", ve.getAnhVe());
        duLieu.put("TrangThai", ve.getTrangThai());
        return duLieu;
    }

    private void xuLyKetQuaMotVe(Response<List<Ve>> response, ApiCallback<Ve> callback, String thongBaoLoi) {
        if (!response.isSuccessful()) {
            callback.onError(thongBaoLoi);
            return;
        }

        List<Ve> danhSachVe = response.body();
        if (danhSachVe == null || danhSachVe.isEmpty()) {
            callback.onError("Không nhận được dữ liệu vé");
            return;
        }

        callback.onSuccess(danhSachVe.get(0));
    }
}
