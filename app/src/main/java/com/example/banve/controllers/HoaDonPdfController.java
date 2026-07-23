package com.example.banve.controllers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import androidx.core.content.ContextCompat;

import com.example.banve.R;
import com.example.banve.models.ChiTietHoaDon;
import com.example.banve.models.HoaDon;
import com.example.banve.models.LoaiVe;
import com.example.banve.models.NguoiDung;
import com.example.banve.models.Ve;
import com.example.banve.utils.DinhDangTien;
import com.example.banve.utils.HienThi;
import com.example.banve.utils.MaQrHoaDonUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HoaDonPdfController {
    private static final int CHIEU_RONG_TRANG = 595;
    private static final int CHIEU_CAO_TRANG = 842;
    private static final float LE_TRANG = 36f;
    private static final float VI_TRI_CHAN_TRANG = 816f;
    private static final float CHIEU_RONG_NOI_DUNG = CHIEU_RONG_TRANG - LE_TRANG * 2;

    private final int mauChinh;
    private final int mauNhan;
    private final int mauChuChinh;
    private final int mauChuPhu;
    private final int mauVien;
    private final int mauNenNhat;
    private final Typeface phongThuong = Typeface.create("sans-serif", Typeface.NORMAL);
    private final Typeface phongDam = Typeface.create("sans-serif", Typeface.BOLD);

    public HoaDonPdfController(Context context) {
        Context ungDung = context.getApplicationContext();
        mauChinh = ContextCompat.getColor(ungDung, R.color.mauTimChinh);
        mauNhan = ContextCompat.getColor(ungDung, R.color.mauCamChinh);
        mauChuChinh = ContextCompat.getColor(ungDung, R.color.mauChuChinh);
        mauChuPhu = ContextCompat.getColor(ungDung, R.color.mauChuPhu);
        mauVien = ContextCompat.getColor(ungDung, R.color.mauVien);
        mauNenNhat = ContextCompat.getColor(ungDung, R.color.mauTimSieuNhat);
    }

    public void xuatPdf(
            HoaDon hoaDon,
            List<ChiTietHoaDon> danhSachChiTiet,
            Bitmap qrBitmap,
            OutputStream outputStream
    ) throws IOException {
        if (outputStream == null) {
            throw new IllegalArgumentException("Không mở được nơi lưu file PDF");
        }

        PdfDocument taiLieu = new PdfDocument();
        IOException loiXuat = null;
        try {
            kiemTraDuLieu(hoaDon, qrBitmap, outputStream);
            TrinhBayPdf trinhBay = new TrinhBayPdf(
                    taiLieu,
                    hoaDon,
                    danhSachChiTiet == null ? Collections.emptyList() : danhSachChiTiet,
                    qrBitmap
            );
            trinhBay.veHoaDon();
            taiLieu.writeTo(outputStream);
        } catch (IOException exception) {
            loiXuat = exception;
            throw exception;
        } catch (RuntimeException exception) {
            loiXuat = new IOException("Không thể dựng nội dung PDF hóa đơn", exception);
            throw loiXuat;
        } finally {
            IOException loiDong = null;
            try {
                taiLieu.close();
            } catch (RuntimeException exception) {
                loiDong = new IOException("Không thể đóng tài liệu PDF", exception);
            }
            try {
                outputStream.close();
            } catch (IOException exception) {
                if (loiXuat != null) {
                    loiXuat.addSuppressed(exception);
                } else {
                    loiDong = exception;
                }
            }
            if (loiXuat != null && loiDong != null) {
                loiXuat.addSuppressed(loiDong);
            } else if (loiXuat == null && loiDong != null) {
                throw loiDong;
            }
        }
    }

    public List<String> tachDongTheoChieuRong(String noiDung, Paint paint, float chieuRongToiDa) {
        List<String> ketQua = new ArrayList<>();
        if (noiDung == null || noiDung.isEmpty()) {
            ketQua.add("");
            return ketQua;
        }
        if (paint == null || chieuRongToiDa <= 0) {
            throw new IllegalArgumentException("Thông số xuống dòng PDF không hợp lệ");
        }

        String[] doanVan = noiDung.replace("\r", "").split("\n", -1);
        for (String doan : doanVan) {
            tachMotDoan(doan, paint, chieuRongToiDa, ketQua);
        }
        return ketQua;
    }

    private void tachMotDoan(String doan, Paint paint, float chieuRongToiDa, List<String> ketQua) {
        if (doan.trim().isEmpty()) {
            ketQua.add("");
            return;
        }

        String[] tu = doan.trim().split("\\s+");
        StringBuilder dongHienTai = new StringBuilder();
        for (String motTu : tu) {
            if (paint.measureText(motTu) > chieuRongToiDa) {
                if (dongHienTai.length() > 0) {
                    ketQua.add(dongHienTai.toString());
                    dongHienTai.setLength(0);
                }
                tachTuDai(motTu, paint, chieuRongToiDa, ketQua);
                continue;
            }

            String dongThu = dongHienTai.length() == 0
                    ? motTu
                    : dongHienTai + " " + motTu;
            if (paint.measureText(dongThu) <= chieuRongToiDa) {
                dongHienTai.setLength(0);
                dongHienTai.append(dongThu);
            } else {
                ketQua.add(dongHienTai.toString());
                dongHienTai.setLength(0);
                dongHienTai.append(motTu);
            }
        }

        if (dongHienTai.length() > 0) {
            ketQua.add(dongHienTai.toString());
        }
    }

    private void tachTuDai(String motTu, Paint paint, float chieuRongToiDa, List<String> ketQua) {
        StringBuilder phan = new StringBuilder();
        for (int i = 0; i < motTu.length(); i++) {
            String phanThu = phan.toString() + motTu.charAt(i);
            if (paint.measureText(phanThu) > chieuRongToiDa && phan.length() > 0) {
                ketQua.add(phan.toString());
                phan.setLength(0);
            }
            phan.append(motTu.charAt(i));
        }
        if (phan.length() > 0) {
            ketQua.add(phan.toString());
        }
    }

    private void kiemTraDuLieu(HoaDon hoaDon, Bitmap qrBitmap, OutputStream outputStream) {
        if (hoaDon == null || hoaDon.getMaHoaDon() <= 0) {
            throw new IllegalArgumentException("Hóa đơn không hợp lệ");
        }
        if (!"DaThanhToan".equals(hoaDon.getTrangThai())) {
            throw new IllegalArgumentException("Chỉ có thể xuất PDF cho hóa đơn đã thanh toán");
        }
        MaQrHoaDonUtil.taoNoiDungQr(hoaDon);
        if (qrBitmap == null || qrBitmap.isRecycled()) {
            throw new IllegalArgumentException("Mã QR hóa đơn chưa sẵn sàng");
        }
    }

    private class TrinhBayPdf {
        private final PdfDocument taiLieu;
        private final HoaDon hoaDon;
        private final List<ChiTietHoaDon> danhSachChiTiet;
        private final Bitmap qrBitmap;
        private PdfDocument.Page trang;
        private Canvas canvas;
        private int soTrang;
        private float y;

        private TrinhBayPdf(
                PdfDocument taiLieu,
                HoaDon hoaDon,
                List<ChiTietHoaDon> danhSachChiTiet,
                Bitmap qrBitmap
        ) {
            this.taiLieu = taiLieu;
            this.hoaDon = hoaDon;
            this.danhSachChiTiet = danhSachChiTiet;
            this.qrBitmap = qrBitmap;
        }

        private void veHoaDon() {
            moTrang(true);
            veTieuDe();
            veThongTinHoaDon();
            veMaQr();
            veChiTietVe();
            veTongKet();
            dongTrang();
        }

        private void moTrang(boolean laTrangDau) {
            soTrang++;
            PdfDocument.PageInfo thongTinTrang = new PdfDocument.PageInfo.Builder(
                    CHIEU_RONG_TRANG,
                    CHIEU_CAO_TRANG,
                    soTrang
            ).create();
            trang = taiLieu.startPage(thongTinTrang);
            canvas = trang.getCanvas();
            canvas.drawColor(Color.WHITE);
            y = LE_TRANG;

            if (!laTrangDau) {
                Paint tieuDe = taoPaint(14f, mauChinh, true);
                canvas.drawText("Hóa đơn #" + hoaDon.getMaHoaDon(), LE_TRANG, y + 14f, tieuDe);
                y += 24f;
                Paint moTa = taoPaint(11f, mauChuPhu, false);
                canvas.drawText("Chi tiết vé – trang tiếp theo", LE_TRANG, y + 11f, moTa);
                y += 26f;
                veDuongKe(y);
                y += 16f;
            }
        }

        private void dongTrang() {
            if (trang == null) {
                return;
            }
            Paint chanTrang = taoPaint(9f, mauChuPhu, false);
            String noiDung = "Hóa đơn #" + hoaDon.getMaHoaDon() + " • Trang " + soTrang;
            float x = CHIEU_RONG_TRANG - LE_TRANG - chanTrang.measureText(noiDung);
            canvas.drawText(noiDung, x, VI_TRI_CHAN_TRANG, chanTrang);
            taiLieu.finishPage(trang);
            trang = null;
            canvas = null;
        }

        private void sangTrangMoi() {
            dongTrang();
            moTrang(false);
        }

        private void damBaoKhoangTrong(float chieuCaoCan) {
            if (y + chieuCaoCan > VI_TRI_CHAN_TRANG - 22f) {
                sangTrangMoi();
            }
        }

        private void veTieuDe() {
            Paint tenUngDung = taoPaint(12f, mauChinh, true);
            canvas.drawText("ỨNG DỤNG QUẢN LÝ BÁN VÉ KHU DU LỊCH", LE_TRANG, y + 12f, tenUngDung);
            y += 32f;

            Paint tieuDe = taoPaint(24f, mauChuChinh, true);
            canvas.drawText("HÓA ĐƠN ĐIỆN TỬ", LE_TRANG, y + 24f, tieuDe);
            y += 36f;

            Paint phuDe = taoPaint(11f, mauChuPhu, false);
            canvas.drawText(
                    "Ngày xuất: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()),
                    LE_TRANG,
                    y + 11f,
                    phuDe
            );
            y += 26f;
            veDuongKe(y);
            y += 18f;
        }

        private void veThongTinHoaDon() {
            damBaoKhoangTrong(188f);
            float dauKhoi = y;
            Paint nen = new Paint(Paint.ANTI_ALIAS_FLAG);
            nen.setColor(mauNenNhat);
            canvas.drawRoundRect(
                    new RectF(LE_TRANG, dauKhoi, CHIEU_RONG_TRANG - LE_TRANG, dauKhoi + 176f),
                    12f,
                    12f,
                    nen
            );

            y += 20f;
            veNhanGiaTri("Mã hóa đơn", "#" + hoaDon.getMaHoaDon());
            veNhanGiaTri("Ngày lập", dinhDangNgayGio(hoaDon.getNgayLap()));
            veNhanGiaTri("Trạng thái", HienThi.trangThai(hoaDon.getTrangThai()));
            veNhanGiaTri("Hình thức thanh toán", HienThi.hinhThucThanhToan(hoaDon.getThanhToan()));

            NguoiDung nguoiDung = hoaDon.getNguoiDung();
            if (nguoiDung != null && coNoiDung(nguoiDung.getHoTen())) {
                veNhanGiaTri("Người mua", nguoiDung.getHoTen().trim());
            }
            if (hoaDon.getMaVoucher() != null) {
                veNhanGiaTri("Mã voucher", "#" + hoaDon.getMaVoucher());
            }

            y = dauKhoi + 194f;
            veNhanGiaTriTien("Tổng tiền trước giảm", hoaDon.getTongTien(), mauChuChinh);
            veNhanGiaTriTien("Tiền giảm", hoaDon.getTienGiam(), mauChinh);
            veNhanGiaTriTien(
                    "Thành tiền",
                    Math.max(0, hoaDon.getTongTien() - hoaDon.getTienGiam()),
                    mauNhan
            );
            y += 12f;
        }

        private void veMaQr() {
            damBaoKhoangTrong(250f);
            Paint tieuDe = taoPaint(16f, mauChuChinh, true);
            canvas.drawText("MÃ QR XÁC THỰC HÓA ĐƠN", LE_TRANG, y + 16f, tieuDe);
            y += 28f;

            int kichThuocQr = 136;
            float xQr = (CHIEU_RONG_TRANG - kichThuocQr) / 2f;
            Paint nenQr = new Paint(Paint.ANTI_ALIAS_FLAG);
            nenQr.setColor(Color.WHITE);
            canvas.drawRect(xQr - 6f, y - 6f, xQr + kichThuocQr + 6f, y + kichThuocQr + 6f, nenQr);
            canvas.drawBitmap(
                    qrBitmap,
                    null,
                    new Rect((int) xQr, (int) y, (int) xQr + kichThuocQr, (int) y + kichThuocQr),
                    null
            );
            y += kichThuocQr + 18f;

            Paint ghiChu = taoPaint(10f, mauChuPhu, false);
            veVanBanCanGiua("QR này dùng để xác thực hóa đơn, không phải QR thanh toán", ghiChu);
            y += 4f;
            Paint maXacThuc = taoPaint(9f, mauChuChinh, false);
            veVanBan("Mã xác thực: " + hoaDon.getMaXacThuc(), maXacThuc, LE_TRANG, CHIEU_RONG_NOI_DUNG, 13f);
            y += 12f;
        }

        private void veChiTietVe() {
            damBaoKhoangTrong(48f);
            Paint tieuDe = taoPaint(17f, mauChuChinh, true);
            canvas.drawText("CHI TIẾT VÉ", LE_TRANG, y + 17f, tieuDe);
            y += 30f;

            if (danhSachChiTiet.isEmpty()) {
                Paint thongBao = taoPaint(11f, mauChuPhu, false);
                veVanBan(
                        "Hóa đơn này chưa có dữ liệu chi tiết vé.",
                        thongBao,
                        LE_TRANG,
                        CHIEU_RONG_NOI_DUNG,
                        16f
                );
                y += 14f;
                return;
            }

            for (int i = 0; i < danhSachChiTiet.size(); i++) {
                veMotChiTietVe(danhSachChiTiet.get(i), i + 1);
            }
        }

        private void veMotChiTietVe(ChiTietHoaDon chiTiet, int soThuTu) {
            List<DongPdf> cacDong = taoDongChiTiet(chiTiet, soThuTu);
            float chieuRongChu = CHIEU_RONG_NOI_DUNG - 28f;
            float chieuCao = 24f;
            for (DongPdf dong : cacDong) {
                Paint paint = taoPaint(dong.kichThuoc, dong.mau, dong.dam);
                chieuCao += tachDongTheoChieuRong(dong.noiDung, paint, chieuRongChu).size() * dong.caoDong;
            }
            chieuCao += 10f;

            float chieuCaoToiDa = VI_TRI_CHAN_TRANG - 22f - 86f;
            if (chieuCao > chieuCaoToiDa) {
                veChiTietQuaDai(cacDong);
                return;
            }

            damBaoKhoangTrong(chieuCao + 12f);
            float dauKhoi = y;
            Paint nen = new Paint(Paint.ANTI_ALIAS_FLAG);
            nen.setColor(Color.WHITE);
            canvas.drawRoundRect(
                    new RectF(LE_TRANG, dauKhoi, CHIEU_RONG_TRANG - LE_TRANG, dauKhoi + chieuCao),
                    10f,
                    10f,
                    nen
            );
            Paint vien = new Paint(Paint.ANTI_ALIAS_FLAG);
            vien.setColor(mauVien);
            vien.setStyle(Paint.Style.STROKE);
            vien.setStrokeWidth(1f);
            canvas.drawRoundRect(
                    new RectF(LE_TRANG, dauKhoi, CHIEU_RONG_TRANG - LE_TRANG, dauKhoi + chieuCao),
                    10f,
                    10f,
                    vien
            );

            y += 18f;
            for (DongPdf dong : cacDong) {
                Paint paint = taoPaint(dong.kichThuoc, dong.mau, dong.dam);
                veVanBan(dong.noiDung, paint, LE_TRANG + 14f, chieuRongChu, dong.caoDong);
            }
            y = dauKhoi + chieuCao + 12f;
        }

        private void veChiTietQuaDai(List<DongPdf> cacDong) {
            for (DongPdf dong : cacDong) {
                Paint paint = taoPaint(dong.kichThuoc, dong.mau, dong.dam);
                List<String> cacDongDaTach = tachDongTheoChieuRong(
                        dong.noiDung,
                        paint,
                        CHIEU_RONG_NOI_DUNG
                );
                for (String motDong : cacDongDaTach) {
                    damBaoKhoangTrong(dong.caoDong + 4f);
                    canvas.drawText(motDong, LE_TRANG, y + dong.kichThuoc, paint);
                    y += dong.caoDong;
                }
            }
            y += 12f;
        }

        private List<DongPdf> taoDongChiTiet(ChiTietHoaDon chiTiet, int soThuTu) {
            List<DongPdf> cacDong = new ArrayList<>();
            Ve ve = chiTiet.getVe();
            String tenVe = ve == null || !coNoiDung(ve.getTenVe())
                    ? "Vé mã " + chiTiet.getMaVe()
                    : ve.getTenVe().trim();
            cacDong.add(new DongPdf(soThuTu + ". " + tenVe, 14f, 20f, mauChuChinh, true));

            LoaiVe loaiVe = ve == null ? null : ve.getLoaiVe();
            if (loaiVe != null && coNoiDung(loaiVe.getTenLoaiVe())) {
                cacDong.add(new DongPdf(
                        "Loại vé: " + loaiVe.getTenLoaiVe().trim(),
                        10f,
                        15f,
                        mauChuPhu,
                        false
                ));
            }
            cacDong.add(new DongPdf(
                    "Ngày sử dụng: " + dinhDangNgay(chiTiet.getNgaySuDung()),
                    10f,
                    15f,
                    mauChuPhu,
                    false
            ));

            themDongNhomKhach(
                    cacDong,
                    "Người lớn",
                    chiTiet.getSoLuongNguoiLon(),
                    chiTiet.getDonGiaNguoiLon()
            );
            themDongNhomKhach(
                    cacDong,
                    "Trẻ em",
                    chiTiet.getSoLuongTreEm(),
                    chiTiet.getDonGiaTreEm()
            );
            themDongNhomKhach(
                    cacDong,
                    "Người cao tuổi",
                    chiTiet.getSoLuongNguoiCaoTuoi(),
                    chiTiet.getDonGiaNguoiCaoTuoi()
            );
            cacDong.add(new DongPdf(
                    "Tổng tiền dòng vé: " + DinhDangTien.dinhDang(chiTiet.getThanhTien()),
                    12f,
                    18f,
                    mauNhan,
                    true
            ));
            return cacDong;
        }

        private void themDongNhomKhach(
                List<DongPdf> cacDong,
                String tenNhom,
                int soLuong,
                double donGia
        ) {
            if (soLuong <= 0) {
                return;
            }
            double thanhTienNhom = soLuong * donGia;
            cacDong.add(new DongPdf(
                    tenNhom + ": " + soLuong
                            + " × " + DinhDangTien.dinhDang(donGia)
                            + " = " + DinhDangTien.dinhDang(thanhTienNhom),
                    10f,
                    16f,
                    mauChuChinh,
                    false
            ));
        }

        private void veTongKet() {
            damBaoKhoangTrong(145f);
            veDuongKe(y);
            y += 20f;

            veNhanGiaTriTien("Tổng tiền trước giảm", hoaDon.getTongTien(), mauChuChinh);
            veNhanGiaTriTien("Tổng giảm giá", hoaDon.getTienGiam(), mauChinh);
            veNhanGiaTriTien(
                    "Thành tiền",
                    Math.max(0, hoaDon.getTongTien() - hoaDon.getTienGiam()),
                    mauNhan
            );
            y += 14f;

            Paint camOn = taoPaint(12f, mauChinh, true);
            veVanBanCanGiua("Cảm ơn bạn đã sử dụng dịch vụ!", camOn);
            Paint ghiChu = taoPaint(10f, mauChuPhu, false);
            veVanBanCanGiua("Vui lòng xuất trình mã QR khi kiểm tra vé tại khu du lịch.", ghiChu);
        }

        private void veNhanGiaTri(String nhan, String giaTri) {
            if (!coNoiDung(giaTri)) {
                return;
            }
            Paint paintNhan = taoPaint(10f, mauChuPhu, false);
            Paint paintGiaTri = taoPaint(10f, mauChuChinh, true);
            canvas.drawText(nhan + ":", LE_TRANG + 16f, y + 10f, paintNhan);
            float xGiaTri = LE_TRANG + 158f;
            float chieuRongGiaTri = CHIEU_RONG_TRANG - LE_TRANG - 16f - xGiaTri;
            List<String> cacDong = tachDongTheoChieuRong(
                    giaTri,
                    paintGiaTri,
                    chieuRongGiaTri
            );
            for (int i = 0; i < cacDong.size(); i++) {
                canvas.drawText(cacDong.get(i), xGiaTri, y + 10f + i * 15f, paintGiaTri);
            }
            y += Math.max(22f, cacDong.size() * 15f + 7f);
        }

        private void veNhanGiaTriTien(String nhan, double giaTri, int mauGiaTri) {
            Paint paintNhan = taoPaint(11f, mauChuPhu, false);
            Paint paintGiaTri = taoPaint(12f, mauGiaTri, true);
            canvas.drawText(nhan + ":", LE_TRANG, y + 12f, paintNhan);
            String tien = DinhDangTien.dinhDang(giaTri);
            float x = CHIEU_RONG_TRANG - LE_TRANG - paintGiaTri.measureText(tien);
            canvas.drawText(tien, x, y + 12f, paintGiaTri);
            y += 24f;
        }

        private void veVanBan(
                String noiDung,
                Paint paint,
                float x,
                float chieuRong,
                float caoDong
        ) {
            List<String> cacDong = tachDongTheoChieuRong(noiDung, paint, chieuRong);
            for (String motDong : cacDong) {
                damBaoKhoangTrong(caoDong + 2f);
                canvas.drawText(motDong, x, y + paint.getTextSize(), paint);
                y += caoDong;
            }
        }

        private void veVanBanCanGiua(String noiDung, Paint paint) {
            List<String> cacDong = tachDongTheoChieuRong(noiDung, paint, CHIEU_RONG_NOI_DUNG);
            for (String motDong : cacDong) {
                damBaoKhoangTrong(16f);
                float x = (CHIEU_RONG_TRANG - paint.measureText(motDong)) / 2f;
                canvas.drawText(motDong, x, y + paint.getTextSize(), paint);
                y += 16f;
            }
        }

        private void veDuongKe(float viTriY) {
            Paint duongKe = new Paint(Paint.ANTI_ALIAS_FLAG);
            duongKe.setColor(mauVien);
            duongKe.setStrokeWidth(1f);
            canvas.drawLine(LE_TRANG, viTriY, CHIEU_RONG_TRANG - LE_TRANG, viTriY, duongKe);
        }
    }

    private Paint taoPaint(float kichThuoc, int mau, boolean dam) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(mau);
        paint.setTextSize(kichThuoc);
        paint.setTypeface(dam ? phongDam : phongThuong);
        return paint;
    }

    private String dinhDangNgayGio(String ngayGio) {
        if (!coNoiDung(ngayGio)) {
            return "";
        }
        String[] dinhDangNguon = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                "yyyy-MM-dd'T'HH:mm:ssX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
        };
        for (String dinhDang : dinhDangNguon) {
            try {
                Date ngay = new SimpleDateFormat(dinhDang, Locale.getDefault()).parse(ngayGio);
                if (ngay != null) {
                    return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(ngay);
                }
            } catch (ParseException ignored) {
            }
        }
        return ngayGio;
    }

    private String dinhDangNgay(String ngay) {
        if (!coNoiDung(ngay)) {
            return "";
        }
        try {
            Date giaTri = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(ngay);
            if (giaTri != null) {
                return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(giaTri);
            }
        } catch (ParseException ignored) {
        }
        return ngay;
    }

    private boolean coNoiDung(String giaTri) {
        return giaTri != null && !giaTri.trim().isEmpty();
    }

    private static class DongPdf {
        private final String noiDung;
        private final float kichThuoc;
        private final float caoDong;
        private final int mau;
        private final boolean dam;

        private DongPdf(String noiDung, float kichThuoc, float caoDong, int mau, boolean dam) {
            this.noiDung = noiDung;
            this.kichThuoc = kichThuoc;
            this.caoDong = caoDong;
            this.mau = mau;
            this.dam = dam;
        }
    }
}
