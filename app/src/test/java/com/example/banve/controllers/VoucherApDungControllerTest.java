package com.example.banve.controllers;

import com.example.banve.models.ChiTietGioHang;
import com.example.banve.models.HoaDon;
import com.example.banve.models.KetQuaKiemTraVoucher;
import com.example.banve.models.MucGioHang;
import com.example.banve.models.Ve;
import com.example.banve.models.Voucher;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VoucherApDungControllerTest {
    private VoucherApDungController controller;
    private List<MucGioHang> gioHangHonHop;

    @Before
    public void setUp() {
        controller = new VoucherApDungController();
        gioHangHonHop = Arrays.asList(
                taoMuc(1, 10, 1, 300000),
                taoMuc(2, 20, 1, 500000)
        );
    }

    @Test
    public void phanTramCoTranKhongVuotQuaNamMuoiNghin() {
        Voucher voucher = taoVoucher(1, "PhanTram", 10);
        voucher.setGiamToiDa(50000);

        KetQuaKiemTraVoucher ketQua = kiemTra(voucher, gioHangHonHop, Collections.emptyList());

        assertTrue(ketQua.isHopLe());
        assertEquals(50000, ketQua.getTienGiam(), 0.001);
    }

    @Test
    public void donChuaDatToiThieuTraDungSoTienConThieu() {
        Voucher voucher = taoVoucher(2, "TienMat", 50000);
        voucher.setDonToiThieu(900000);

        KetQuaKiemTraVoucher ketQua = kiemTra(voucher, gioHangHonHop, Collections.emptyList());

        assertFalse(ketQua.isHopLe());
        assertEquals(100000, ketQua.getSoTienConThieu(), 0.001);
        assertTrue(ketQua.getLyDo().contains("100,000 VNĐ"));
    }

    @Test
    public void chuaDuSoVeTraDungSoVeConThieu() {
        Voucher voucher = taoVoucher(3, "TienMat", 50000);
        voucher.setSoLuongVeToiThieu(4);

        KetQuaKiemTraVoucher ketQua = kiemTra(voucher, gioHangHonHop, Collections.emptyList());

        assertFalse(ketQua.isHopLe());
        assertEquals(2, ketQua.getSoVeConThieu());
    }

    @Test
    public void voucherTheoLoaiChiGiamPhanThuocLoaiDo() {
        Voucher voucher = taoVoucher(4, "PhanTram", 10);
        voucher.setMaLoaiVeApDung(10);

        KetQuaKiemTraVoucher ketQua = kiemTra(voucher, gioHangHonHop, Collections.emptyList());

        assertTrue(ketQua.isHopLe());
        assertEquals(300000, ketQua.getTamTinhDuocApDung(), 0.001);
        assertEquals(30000, ketQua.getTienGiam(), 0.001);
    }

    @Test
    public void voucherTheoVeKhongGiamCacVeKhac() {
        Voucher voucher = taoVoucher(5, "TienMat", 500000);
        voucher.setMaVeApDung(1);

        KetQuaKiemTraVoucher ketQua = kiemTra(voucher, gioHangHonHop, Collections.emptyList());

        assertTrue(ketQua.isHopLe());
        assertEquals(300000, ketQua.getTamTinhDuocApDung(), 0.001);
        assertEquals(300000, ketQua.getTienGiam(), 0.001);
    }

    @Test
    public void khachMoiChuaCoHoaDonDuocSuDung() {
        Voucher voucher = taoVoucher(6, "TienMat", 50000);
        voucher.setChiApDungKhachMoi(true);

        KetQuaKiemTraVoucher ketQua = kiemTra(voucher, gioHangHonHop, Collections.emptyList());

        assertTrue(ketQua.isHopLe());
    }

    @Test
    public void nguoiDaCoHoaDonKhongDuocDungVoucherKhachMoi() {
        Voucher voucher = taoVoucher(7, "TienMat", 50000);
        voucher.setChiApDungKhachMoi(true);
        HoaDon hoaDonCu = taoHoaDon(99, null);

        KetQuaKiemTraVoucher ketQua = kiemTra(voucher, gioHangHonHop, Collections.singletonList(hoaDonCu));

        assertFalse(ketQua.isHopLe());
        assertEquals("Voucher chỉ dành cho khách hàng mới", ketQua.getLyDo());
    }

    @Test
    public void nguoiDungVuotGioiHanKhongDuocSuDung() {
        Voucher voucher = taoVoucher(8, "TienMat", 50000);
        voucher.setSoLanDungToiDaMoiNguoi(1);
        HoaDon hoaDonCu = taoHoaDon(100, voucher.getMaVoucher());

        KetQuaKiemTraVoucher ketQua = kiemTra(voucher, gioHangHonHop, Collections.singletonList(hoaDonCu));

        assertFalse(ketQua.isHopLe());
        assertEquals("Bạn đã sử dụng hết số lần cho phép", ketQua.getLyDo());
    }

    @Test
    public void voucherTienMatKhongVuotQuaPhanVeDuocApDung() {
        Voucher voucher = taoVoucher(9, "TienMat", 900000);
        voucher.setMaVeApDung(1);

        KetQuaKiemTraVoucher ketQua = kiemTra(voucher, gioHangHonHop, new ArrayList<>());

        assertTrue(ketQua.isHopLe());
        assertEquals(300000, ketQua.getTienGiam(), 0.001);
    }

    private KetQuaKiemTraVoucher kiemTra(Voucher voucher, List<MucGioHang> gioHang, List<HoaDon> hoaDon) {
        return controller.kiemTraTaiNgay(voucher, gioHang, 1, hoaDon, "2026-07-15");
    }

    private Voucher taoVoucher(int maVoucher, String kieuGiamGia, double giaTriGiam) {
        Voucher voucher = new Voucher();
        voucher.setMaVoucher(maVoucher);
        voucher.setKieuGiamGia(kieuGiamGia);
        voucher.setGiaTriGiam(giaTriGiam);
        voucher.setNgayBatDau("2026-01-01");
        voucher.setNgayKetThuc("2026-12-31");
        voucher.setSoLuong(100);
        voucher.setSoLuongVeToiThieu(1);
        voucher.setTrangThai("HoatDong");
        return voucher;
    }

    private MucGioHang taoMuc(int maVe, int maLoaiVe, int soLuongNguoiLon, double donGiaNguoiLon) {
        Ve ve = new Ve();
        ve.setMaVe(maVe);
        ve.setMaLoaiVe(maLoaiVe);
        ve.setTenVe("Vé " + maVe);
        ve.setTrangThai("HoatDong");

        ChiTietGioHang chiTiet = new ChiTietGioHang();
        chiTiet.setMaVe(maVe);
        chiTiet.setSoLuongNguoiLon(soLuongNguoiLon);
        chiTiet.setDonGiaNguoiLon(donGiaNguoiLon);
        return new MucGioHang(chiTiet, ve);
    }

    private HoaDon taoHoaDon(int maHoaDon, Integer maVoucher) {
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon(maHoaDon);
        hoaDon.setMaVoucher(maVoucher);
        hoaDon.setTrangThai("DaThanhToan");
        return hoaDon;
    }
}
