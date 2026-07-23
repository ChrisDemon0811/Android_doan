package com.example.banve.controllers;

import com.example.banve.dao.HoaDonDAO;
import com.example.banve.models.ChiTietHoaDon;
import com.example.banve.models.DuLieuChiTietHoaDon;
import com.example.banve.models.HoaDon;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.Session;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HoaDonController {
    private final HoaDonDAO hoaDonDAO;

    public HoaDonController() {
        hoaDonDAO = new HoaDonDAO();
    }

    public void layDanhSachDaThanhToan(int maNguoiDung, ApiCallback<List<HoaDon>> callback) {
        if (maNguoiDung <= 0) {
            callback.onError("Người dùng không hợp lệ");
            return;
        }

        hoaDonDAO.layDanhSachDaThanhToan(maNguoiDung, callback);
    }

    public void layDanhSachHoaDonQuanLy(ApiCallback<List<HoaDon>> callback) {
        hoaDonDAO.layDanhSachHoaDonQuanLy(callback);
    }

    public void layChiTietHoaDon(int maHoaDon, ApiCallback<List<ChiTietHoaDon>> callback) {
        if (maHoaDon <= 0) {
            callback.onError("Hóa đơn không hợp lệ");
            return;
        }

        hoaDonDAO.layChiTietHoaDon(maHoaDon, callback);
    }

    public void layHoaDonDayDu(
            int maHoaDon,
            int maNguoiDung,
            ApiCallback<DuLieuChiTietHoaDon> callback
    ) {
        AtomicBoolean daPhanHoi = new AtomicBoolean(false);
        if (maHoaDon <= 0) {
            phanHoiLoiMotLan(daPhanHoi, callback, "Mã hóa đơn không hợp lệ");
            return;
        }
        if (maNguoiDung <= 0 && !Session.laQuanLy()) {
            phanHoiLoiMotLan(daPhanHoi, callback, "Người dùng không hợp lệ");
            return;
        }

        hoaDonDAO.layTheoMa(maHoaDon, new ApiCallback<HoaDon>() {
            @Override
            public void onSuccess(HoaDon hoaDon) {
                if (hoaDon == null) {
                    phanHoiLoiMotLan(daPhanHoi, callback, "Hóa đơn không tồn tại");
                    return;
                }
                if (!Session.laQuanLy() && hoaDon.getMaNguoiDung() != maNguoiDung) {
                    phanHoiLoiMotLan(
                            daPhanHoi,
                            callback,
                            "Bạn không có quyền xem hóa đơn này"
                    );
                    return;
                }

                hoaDonDAO.layChiTietHoaDon(maHoaDon, new ApiCallback<List<ChiTietHoaDon>>() {
                    @Override
                    public void onSuccess(List<ChiTietHoaDon> danhSachChiTiet) {
                        if (daPhanHoi.compareAndSet(false, true)) {
                            callback.onSuccess(new DuLieuChiTietHoaDon(hoaDon, danhSachChiTiet));
                        }
                    }

                    @Override
                    public void onError(String thongBao) {
                        phanHoiLoiMotLan(daPhanHoi, callback, thongBao);
                    }
                });
            }

            @Override
            public void onError(String thongBao) {
                phanHoiLoiMotLan(daPhanHoi, callback, thongBao);
            }
        });
    }

    private <T> void phanHoiLoiMotLan(
            AtomicBoolean daPhanHoi,
            ApiCallback<T> callback,
            String thongBao
    ) {
        if (daPhanHoi.compareAndSet(false, true)) {
            callback.onError(thongBao);
        }
    }
}
