package com.example.banve.controllers;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.banve.models.ChiTietHoaDon;
import com.example.banve.models.HoaDon;
import com.example.banve.models.LoaiVe;
import com.example.banve.models.Ve;
import com.example.banve.utils.MaQrHoaDonUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RunWith(AndroidJUnit4.class)
public class HoaDonPdfControllerTest {
    @Test
    public void hoaDonNhieuVe_taoPdfNhieuTrang() throws Exception {
        Context context = getInstrumentation().getTargetContext();
        HoaDon hoaDon = taoHoaDon();
        Bitmap qrBitmap = MaQrHoaDonUtil.taoBitmapQr(
                MaQrHoaDonUtil.taoNoiDungQr(hoaDon),
                360
        );
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        new HoaDonPdfController(context).xuatPdf(
                hoaDon,
                taoDanhSachChiTiet(32),
                qrBitmap,
                outputStream
        );

        byte[] duLieuPdf = outputStream.toByteArray();
        assertTrue(duLieuPdf.length > 0);
        String noiDungPdf = new String(duLieuPdf, StandardCharsets.ISO_8859_1);
        assertTrue(demSoTrang(noiDungPdf) >= 2);
    }

    private HoaDon taoHoaDon() {
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon(125);
        hoaDon.setMaNguoiDung(2);
        hoaDon.setMaXacThuc("550e8400-e29b-41d4-a716-446655440000");
        hoaDon.setNgayLap("2026-07-23T10:15:00+07:00");
        hoaDon.setTongTien(9600000);
        hoaDon.setTienGiam(600000);
        hoaDon.setThanhToan("ChuyenKhoan");
        hoaDon.setTrangThai("DaThanhToan");
        return hoaDon;
    }

    private List<ChiTietHoaDon> taoDanhSachChiTiet(int soLuong) {
        List<ChiTietHoaDon> danhSach = new ArrayList<>();
        for (int i = 0; i < soLuong; i++) {
            LoaiVe loaiVe = new LoaiVe();
            loaiVe.setTenLoaiVe(i % 2 == 0 ? "Vé tham quan" : "Vé vui chơi");

            Ve ve = new Ve();
            ve.setTenVe("Vé trải nghiệm khu du lịch số " + (i + 1));
            ve.setLoaiVe(loaiVe);

            ChiTietHoaDon chiTiet = new ChiTietHoaDon();
            chiTiet.setMaHoaDon(125);
            chiTiet.setMaVe(i + 1);
            chiTiet.setNgaySuDung("2026-07-30");
            chiTiet.setSoLuongNguoiLon(1);
            chiTiet.setSoLuongTreEm(1);
            chiTiet.setSoLuongNguoiCaoTuoi(1);
            chiTiet.setDonGiaNguoiLon(120000);
            chiTiet.setDonGiaTreEm(90000);
            chiTiet.setDonGiaNguoiCaoTuoi(90000);
            chiTiet.setThanhTien(300000);
            chiTiet.setVe(ve);
            danhSach.add(chiTiet);
        }
        return danhSach;
    }

    private int demSoTrang(String noiDung) {
        Matcher matcher = Pattern.compile("/Type\\s*/Page(?!s)").matcher(noiDung);
        int soLan = 0;
        while (matcher.find()) {
            soLan++;
        }
        return soLan;
    }
}
