package com.example.banve.dao;

import com.example.banve.models.Voucher;
import com.example.banve.network.ApiCallback;
import com.example.banve.network.ApiService;
import com.example.banve.network.SupabaseClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoucherDAO {
    private final ApiService apiService;

    public VoucherDAO() {
        apiService = SupabaseClient.layApiService();
    }

    public void layDanhSachVoucherKhaDung(ApiCallback<List<Voucher>> callback) {
        String homNay = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Map<String, String> filter = new HashMap<>();
        filter.put("TrangThai", "eq.HoatDong");
        filter.put("SoLuong", "gt.0");
        filter.put("NgayBatDau", "lte." + homNay);
        filter.put("NgayKetThuc", "gte." + homNay);
        filter.put("select", "*");

        apiService.timVoucher(filter).enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                if (!response.isSuccessful()) {
                    callback.onError("Không thể tải danh sách voucher");
                    return;
                }

                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void layDanhSachVoucher(ApiCallback<List<Voucher>> callback) {
        Map<String, String> filter = new HashMap<>();
        filter.put("select", "*");
        filter.put("order", "MaVoucher.desc");

        apiService.timVoucher(filter).enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                if (!response.isSuccessful()) {
                    callback.onError("Không thể tải danh sách voucher");
                    return;
                }

                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void layDanhSachVoucherChoThanhToan(ApiCallback<List<Voucher>> callback) {
        Map<String, String> filter = new HashMap<>();
        filter.put("TrangThai", "eq.HoatDong");
        filter.put("select", "*");
        filter.put("order", "MaVoucher.desc");

        apiService.timVoucher(filter).enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                if (!response.isSuccessful()) {
                    callback.onError("Không thể tải danh sách voucher");
                    return;
                }
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void layTheoMa(int maVoucher, ApiCallback<Voucher> callback) {
        Map<String, String> filter = new HashMap<>();
        filter.put("MaVoucher", "eq." + maVoucher);
        filter.put("select", "*");
        filter.put("limit", "1");

        apiService.timVoucher(filter).enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                xuLyKetQuaMotVoucher(response, callback, "Không thể tải voucher mới nhất");
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void layTheoMaGiamGia(String maGiamGia, ApiCallback<Voucher> callback) {
        Map<String, String> filter = new HashMap<>();
        filter.put("MaGiamGia", "eq." + maGiamGia);
        filter.put("select", "*");

        apiService.timVoucher(filter).enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                if (!response.isSuccessful()) {
                    callback.onError("Không thể kiểm tra voucher");
                    return;
                }

                List<Voucher> danhSachVoucher = response.body();
                if (danhSachVoucher == null || danhSachVoucher.isEmpty()) {
                    callback.onError("Không tìm thấy voucher");
                    return;
                }

                callback.onSuccess(danhSachVoucher.get(0));
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void capNhatSoLuong(int maVoucher, int soLuongMoi, ApiCallback<Voucher> callback) {
        Map<String, Object> duLieuCapNhat = new HashMap<>();
        duLieuCapNhat.put("SoLuong", soLuongMoi);

        apiService.capNhatVoucher("eq." + maVoucher, duLieuCapNhat).enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                if (!response.isSuccessful()) {
                    callback.onError("Không thể cập nhật voucher");
                    return;
                }

                List<Voucher> danhSachVoucher = response.body();
                if (danhSachVoucher == null || danhSachVoucher.isEmpty()) {
                    callback.onError("Không nhận được dữ liệu voucher");
                    return;
                }

                callback.onSuccess(danhSachVoucher.get(0));
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void themVoucher(Voucher voucher, ApiCallback<Voucher> callback) {
        apiService.themVoucher(taoDuLieuVoucher(voucher)).enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                xuLyKetQuaMotVoucher(response, callback, "Không thể thêm voucher");
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void suaVoucher(Voucher voucher, ApiCallback<Voucher> callback) {
        apiService.capNhatVoucher("eq." + voucher.getMaVoucher(), taoDuLieuVoucher(voucher)).enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                xuLyKetQuaMotVoucher(response, callback, "Không thể cập nhật voucher");
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void xoaVoucher(int maVoucher, ApiCallback<Boolean> callback) {
        apiService.xoaVoucher("eq." + maVoucher).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    callback.onError("Không thể xóa voucher");
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

    private Map<String, Object> taoDuLieuVoucher(Voucher voucher) {
        Map<String, Object> duLieu = new HashMap<>();
        duLieu.put("MaGiamGia", voucher.getMaGiamGia());
        duLieu.put("TenVoucher", voucher.getTenVoucher());
        duLieu.put("KieuGiamGia", voucher.getKieuGiamGia());
        duLieu.put("GiaTriGiam", voucher.getGiaTriGiam());
        duLieu.put("NgayBatDau", voucher.getNgayBatDau());
        duLieu.put("NgayKetThuc", voucher.getNgayKetThuc());
        duLieu.put("SoLuong", voucher.getSoLuong());
        duLieu.put("TrangThai", voucher.getTrangThai());
        duLieu.put("DonToiThieu", voucher.getDonToiThieu());
        duLieu.put("GiamToiDa", voucher.getGiamToiDa());
        duLieu.put("SoLuongVeToiThieu", voucher.getSoLuongVeToiThieu());
        duLieu.put("SoLanDungToiDaMoiNguoi", voucher.getSoLanDungToiDaMoiNguoi());
        duLieu.put("ChiApDungKhachMoi", voucher.isChiApDungKhachMoi());
        duLieu.put("MaLoaiVeApDung", voucher.getMaLoaiVeApDung());
        duLieu.put("MaVeApDung", voucher.getMaVeApDung());
        duLieu.put("MucTieu", voucher.getMucTieu());
        duLieu.put("MoTaDieuKien", voucher.getMoTaDieuKien());
        return duLieu;
    }

    private void xuLyKetQuaMotVoucher(Response<List<Voucher>> response, ApiCallback<Voucher> callback, String thongBaoLoi) {
        if (!response.isSuccessful()) {
            callback.onError(thongBaoLoi);
            return;
        }

        List<Voucher> danhSachVoucher = response.body();
        if (danhSachVoucher == null || danhSachVoucher.isEmpty()) {
            callback.onError("Không nhận được dữ liệu voucher");
            return;
        }

        callback.onSuccess(danhSachVoucher.get(0));
    }
}
