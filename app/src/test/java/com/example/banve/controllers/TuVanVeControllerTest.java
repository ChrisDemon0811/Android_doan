package com.example.banve.controllers;

import com.example.banve.models.KetQuaTuVanVe;
import com.example.banve.models.NhomKhachTuVan;
import com.example.banve.models.Ve;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TuVanVeControllerTest {
    @Test
    public void haiNguoiLonMotTreEmTinhDungTongTien() {
        NhomKhachTuVan nhomKhach = taoNhomKhach();
        Ve veRe = taoVe(1, 200000, 100000, "HoatDong");
        Ve veDat = taoVe(2, 250000, 120000, "HoatDong");

        KetQuaTuVanVe ketQua = new TuVanVeController().tinhKetQuaTuDanhSach(
                nhomKhach,
                Arrays.asList(veDat, veRe)
        );

        assertEquals(1, ketQua.getDeXuatChinh().getMaVe());
        assertEquals(500000, ketQua.getDeXuatChinh().getTongTienDuKien(), 0.001);
    }

    @Test
    public void khongCoVePhuHopTraThongBaoVaKhongCrash() {
        NhomKhachTuVan nhomKhach = taoNhomKhach();
        Ve veKhongPhuHop = taoVe(1, 200000, 0, "HoatDong");

        KetQuaTuVanVe ketQua = new TuVanVeController().tinhKetQuaTuDanhSach(
                nhomKhach,
                Collections.singletonList(veKhongPhuHop)
        );

        assertTrue(ketQua.getDanhSachLuaChon().isEmpty());
        assertNull(ketQua.getDeXuatChinh());
        assertTrue(ketQua.getNoiDungDuPhong().contains("chưa có vé phù hợp"));
    }

    private NhomKhachTuVan taoNhomKhach() {
        NhomKhachTuVan nhomKhach = new NhomKhachTuVan();
        nhomKhach.setSoLuongNguoiLon(2);
        nhomKhach.setSoLuongTreEm(1);
        nhomKhach.setNgaySuDung("2099-01-01");
        return nhomKhach;
    }

    private Ve taoVe(int maVe, double giaNguoiLon, double giaTreEm, String trangThai) {
        Ve ve = new Ve();
        ve.setMaVe(maVe);
        ve.setTenVe("Vé " + maVe);
        ve.setGiaNguoiLon(giaNguoiLon);
        ve.setGiaTreEm(giaTreEm);
        ve.setGiaNguoiCaoTuoi(100000);
        ve.setTrangThai(trangThai);
        return ve;
    }
}
