package com.example.banve.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.example.banve.models.HoaDon;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public final class MaQrHoaDonUtil {
    private static final String TIEN_TO_NOI_DUNG = "BANVE_HOA_DON";

    private MaQrHoaDonUtil() {
    }

    public static String taoNoiDungQr(HoaDon hoaDon) {
        if (hoaDon == null) {
            throw new IllegalArgumentException("Hóa đơn không được để trống");
        }
        if (hoaDon.getMaHoaDon() <= 0) {
            throw new IllegalArgumentException("Mã hóa đơn không hợp lệ");
        }

        String maXacThuc = hoaDon.getMaXacThuc();
        if (maXacThuc == null || maXacThuc.trim().isEmpty()) {
            throw new IllegalArgumentException("Hóa đơn chưa có mã xác thực");
        }

        String maXacThucChuan = maXacThuc.trim();
        try {
            UUID uuid = UUID.fromString(maXacThucChuan);
            if (!uuid.toString().equalsIgnoreCase(maXacThucChuan)) {
                throw new IllegalArgumentException("Mã xác thực hóa đơn không đúng định dạng UUID");
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Mã xác thực hóa đơn không đúng định dạng UUID", exception);
        }

        return TIEN_TO_NOI_DUNG
                + "|MaHoaDon=" + hoaDon.getMaHoaDon()
                + "|MaXacThuc=" + maXacThucChuan;
    }

    public static Bitmap taoBitmapQr(String noiDung, int kichThuoc) {
        if (noiDung == null || noiDung.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung mã QR không được để trống");
        }
        if (kichThuoc <= 0) {
            throw new IllegalArgumentException("Kích thước mã QR phải lớn hơn 0");
        }

        Map<EncodeHintType, Object> tuyChon = new EnumMap<>(EncodeHintType.class);
        tuyChon.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        tuyChon.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        tuyChon.put(EncodeHintType.MARGIN, 2);

        try {
            BitMatrix maTran = new QRCodeWriter().encode(
                    noiDung,
                    BarcodeFormat.QR_CODE,
                    kichThuoc,
                    kichThuoc,
                    tuyChon
            );
            int[] diemAnh = new int[kichThuoc * kichThuoc];
            for (int y = 0; y < kichThuoc; y++) {
                int viTriDong = y * kichThuoc;
                for (int x = 0; x < kichThuoc; x++) {
                    diemAnh[viTriDong + x] = maTran.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(kichThuoc, kichThuoc, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(diemAnh, 0, kichThuoc, 0, 0, kichThuoc, kichThuoc);
            return bitmap;
        } catch (WriterException exception) {
            throw new IllegalArgumentException("Không thể tạo mã QR hóa đơn", exception);
        }
    }
}
