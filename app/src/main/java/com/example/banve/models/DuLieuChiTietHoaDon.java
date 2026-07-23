package com.example.banve.models;

import java.util.Collections;
import java.util.List;

public class DuLieuChiTietHoaDon {
    private HoaDon hoaDon;
    private List<ChiTietHoaDon> danhSachChiTiet;

    public DuLieuChiTietHoaDon(HoaDon hoaDon, List<ChiTietHoaDon> danhSachChiTiet) {
        this.hoaDon = hoaDon;
        this.danhSachChiTiet = danhSachChiTiet == null
                ? Collections.emptyList()
                : danhSachChiTiet;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    public List<ChiTietHoaDon> getDanhSachChiTiet() {
        return danhSachChiTiet;
    }

    public void setDanhSachChiTiet(List<ChiTietHoaDon> danhSachChiTiet) {
        this.danhSachChiTiet = danhSachChiTiet == null
                ? Collections.emptyList()
                : danhSachChiTiet;
    }
}
