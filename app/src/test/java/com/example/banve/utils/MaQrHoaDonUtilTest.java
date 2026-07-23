package com.example.banve.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.banve.models.HoaDon;
import com.example.banve.models.NguoiDung;

import org.junit.Test;

public class MaQrHoaDonUtilTest {
    private static final String UUID_MOT = "550e8400-e29b-41d4-a716-446655440000";
    private static final String UUID_HAI = "550e8400-e29b-41d4-a716-446655440001";

    @Test
    public void cungHoaDon_taoCungNoiDungQr() {
        HoaDon hoaDon = taoHoaDon(12, UUID_MOT);

        assertEquals(
                MaQrHoaDonUtil.taoNoiDungQr(hoaDon),
                MaQrHoaDonUtil.taoNoiDungQr(hoaDon)
        );
    }

    @Test
    public void haiHoaDonKhacNhau_taoNoiDungQrKhacNhau() {
        String noiDungMot = MaQrHoaDonUtil.taoNoiDungQr(taoHoaDon(12, UUID_MOT));
        String noiDungHai = MaQrHoaDonUtil.taoNoiDungQr(taoHoaDon(13, UUID_HAI));

        assertFalse(noiDungMot.equals(noiDungHai));
    }

    @Test
    public void noiDungQr_chuaDungMaHoaDon() {
        String noiDung = MaQrHoaDonUtil.taoNoiDungQr(taoHoaDon(125, UUID_MOT));

        assertTrue(noiDung.contains("MaHoaDon=125"));
    }

    @Test
    public void noiDungQr_chuaDungMaXacThuc() {
        String noiDung = MaQrHoaDonUtil.taoNoiDungQr(taoHoaDon(125, UUID_MOT));

        assertTrue(noiDung.contains("MaXacThuc=" + UUID_MOT));
    }

    @Test
    public void noiDungQr_khongChuaHoTen() {
        HoaDon hoaDon = taoHoaDonCoThongTinCaNhan();

        assertFalse(MaQrHoaDonUtil.taoNoiDungQr(hoaDon).contains("Nguyễn Văn A"));
    }

    @Test
    public void noiDungQr_khongChuaEmail() {
        HoaDon hoaDon = taoHoaDonCoThongTinCaNhan();

        assertFalse(MaQrHoaDonUtil.taoNoiDungQr(hoaDon).contains("khach@example.com"));
    }

    @Test
    public void noiDungQr_khongChuaSoDienThoai() {
        HoaDon hoaDon = taoHoaDonCoThongTinCaNhan();

        assertFalse(MaQrHoaDonUtil.taoNoiDungQr(hoaDon).contains("0912345678"));
    }

    @Test
    public void noiDungQr_khongChuaTongTien() {
        HoaDon hoaDon = taoHoaDonCoThongTinCaNhan();

        assertFalse(MaQrHoaDonUtil.taoNoiDungQr(hoaDon).contains("987654"));
    }

    @Test
    public void thieuMaXacThuc_traLoiRoRang() {
        HoaDon hoaDon = taoHoaDon(125, null);

        try {
            MaQrHoaDonUtil.taoNoiDungQr(hoaDon);
            fail("Phải từ chối hóa đơn chưa có mã xác thực");
        } catch (IllegalArgumentException exception) {
            assertTrue(exception.getMessage().contains("chưa có mã xác thực"));
        }
    }

    @Test
    public void maXacThucKhongHopLe_khongGayLoiNgoaiKiemSoat() {
        HoaDon hoaDon = taoHoaDon(125, "khong-phai-uuid");

        try {
            MaQrHoaDonUtil.taoNoiDungQr(hoaDon);
            fail("Phải từ chối mã xác thực không hợp lệ");
        } catch (IllegalArgumentException exception) {
            assertTrue(exception.getMessage().contains("UUID"));
        }
    }

    private HoaDon taoHoaDonCoThongTinCaNhan() {
        HoaDon hoaDon = taoHoaDon(125, UUID_MOT);
        hoaDon.setTongTien(987654);

        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setHoTen("Nguyễn Văn A");
        nguoiDung.setEmail("khach@example.com");
        nguoiDung.setSoDienThoai("0912345678");
        hoaDon.setNguoiDung(nguoiDung);
        return hoaDon;
    }

    private HoaDon taoHoaDon(int maHoaDon, String maXacThuc) {
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon(maHoaDon);
        hoaDon.setMaXacThuc(maXacThuc);
        return hoaDon;
    }
}
