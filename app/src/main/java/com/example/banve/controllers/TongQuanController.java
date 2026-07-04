package com.example.banve.controllers;

import com.example.banve.dao.HoaDonDAO;
import com.example.banve.dao.NguoiDungDAO;
import com.example.banve.dao.VeDAO;
import com.example.banve.dao.VoucherDAO;
import com.example.banve.models.ChiTietHoaDon;
import com.example.banve.models.HoaDon;
import com.example.banve.models.LoaiVe;
import com.example.banve.models.NguoiDung;
import com.example.banve.models.TongQuanQuanLy;
import com.example.banve.models.Ve;
import com.example.banve.models.Voucher;
import com.example.banve.network.ApiCallback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TongQuanController {
    private final HoaDonDAO hoaDonDAO;
    private final VeDAO veDAO;
    private final VoucherDAO voucherDAO;
    private final NguoiDungDAO nguoiDungDAO;

    public TongQuanController() {
        hoaDonDAO = new HoaDonDAO();
        veDAO = new VeDAO();
        voucherDAO = new VoucherDAO();
        nguoiDungDAO = new NguoiDungDAO();
    }

    public void layTongQuan(ApiCallback<TongQuanQuanLy> callback) {
        hoaDonDAO.layDanhSachHoaDonQuanLy(new ApiCallback<List<HoaDon>>() {
            @Override
            public void onSuccess(List<HoaDon> danhSachHoaDon) {
                taiChiTietHoaDon(danhSachHoaDon, callback);
            }

            @Override
            public void onError(String thongBao) {
                callback.onError("Không thể tải dữ liệu hóa đơn tổng quan");
            }
        });
    }

    private void taiChiTietHoaDon(List<HoaDon> danhSachHoaDon, ApiCallback<TongQuanQuanLy> callback) {
        List<Integer> danhSachMaHoaDonCanLay = new ArrayList<>();
        Set<Integer> danhSachMaHoaDonHomNay = new HashSet<>();
        if (danhSachHoaDon != null) {
            for (HoaDon hoaDon : danhSachHoaDon) {
                Date ngayLap = parseNgayGio(hoaDon.getNgayLap());
                if ("DaThanhToan".equals(hoaDon.getTrangThai()) && trongThangNay(ngayLap)) {
                    danhSachMaHoaDonCanLay.add(hoaDon.getMaHoaDon());
                    if (trongHomNay(ngayLap)) {
                        danhSachMaHoaDonHomNay.add(hoaDon.getMaHoaDon());
                    }
                }
            }
        }

        hoaDonDAO.layChiTietHoaDonTheoDanhSach(danhSachMaHoaDonCanLay, new ApiCallback<List<ChiTietHoaDon>>() {
            @Override
            public void onSuccess(List<ChiTietHoaDon> danhSachChiTiet) {
                taiVe(danhSachHoaDon, danhSachChiTiet, danhSachMaHoaDonHomNay, callback);
            }

            @Override
            public void onError(String thongBao) {
                callback.onError("Không thể tải chi tiết hóa đơn tổng quan");
            }
        });
    }

    private void taiVe(
            List<HoaDon> danhSachHoaDon,
            List<ChiTietHoaDon> danhSachChiTiet,
            Set<Integer> danhSachMaHoaDonHomNay,
            ApiCallback<TongQuanQuanLy> callback
    ) {
        veDAO.layDanhSachVeQuanLy(new ApiCallback<List<Ve>>() {
            @Override
            public void onSuccess(List<Ve> danhSachVe) {
                taiVoucher(danhSachHoaDon, danhSachChiTiet, danhSachMaHoaDonHomNay, danhSachVe, callback);
            }

            @Override
            public void onError(String thongBao) {
                callback.onError("Không thể tải dữ liệu vé tổng quan");
            }
        });
    }

    private void taiVoucher(
            List<HoaDon> danhSachHoaDon,
            List<ChiTietHoaDon> danhSachChiTiet,
            Set<Integer> danhSachMaHoaDonHomNay,
            List<Ve> danhSachVe,
            ApiCallback<TongQuanQuanLy> callback
    ) {
        voucherDAO.layDanhSachVoucherKhaDung(new ApiCallback<List<Voucher>>() {
            @Override
            public void onSuccess(List<Voucher> danhSachVoucher) {
                taiNguoiDung(danhSachHoaDon, danhSachChiTiet, danhSachMaHoaDonHomNay, danhSachVe, danhSachVoucher, callback);
            }

            @Override
            public void onError(String thongBao) {
                callback.onError("Không thể tải dữ liệu voucher tổng quan");
            }
        });
    }

    private void taiNguoiDung(
            List<HoaDon> danhSachHoaDon,
            List<ChiTietHoaDon> danhSachChiTiet,
            Set<Integer> danhSachMaHoaDonHomNay,
            List<Ve> danhSachVe,
            List<Voucher> danhSachVoucher,
            ApiCallback<TongQuanQuanLy> callback
    ) {
        nguoiDungDAO.layDanhSachNguoiDung(new ApiCallback<List<NguoiDung>>() {
            @Override
            public void onSuccess(List<NguoiDung> danhSachNguoiDung) {
                callback.onSuccess(tinhTongQuan(danhSachHoaDon, danhSachChiTiet, danhSachMaHoaDonHomNay, danhSachVe, danhSachVoucher, danhSachNguoiDung));
            }

            @Override
            public void onError(String thongBao) {
                callback.onError("Không thể tải dữ liệu người dùng tổng quan");
            }
        });
    }

    private TongQuanQuanLy tinhTongQuan(
            List<HoaDon> danhSachHoaDon,
            List<ChiTietHoaDon> danhSachChiTiet,
            Set<Integer> danhSachMaHoaDonHomNay,
            List<Ve> danhSachVe,
            List<Voucher> danhSachVoucher,
            List<NguoiDung> danhSachNguoiDung
    ) {
        TongQuanQuanLy tongQuan = new TongQuanQuanLy();
        tinhHoaDon(tongQuan, danhSachHoaDon);
        tinhChiTietVe(tongQuan, danhSachChiTiet, danhSachMaHoaDonHomNay);
        tongQuan.setSoVeDangBan(demVeDangBan(danhSachVe));
        tongQuan.setVoucherDangHoatDong(danhSachVoucher == null ? 0 : danhSachVoucher.size());
        tongQuan.setTongKhachHang(demKhachHang(danhSachNguoiDung));
        tongQuan.setGoiYNhanh(taoGoiYNhanh(tongQuan));
        return tongQuan;
    }

    private void tinhHoaDon(TongQuanQuanLy tongQuan, List<HoaDon> danhSachHoaDon) {
        if (danhSachHoaDon == null) {
            return;
        }

        for (HoaDon hoaDon : danhSachHoaDon) {
            Date ngayLap = parseNgayGio(hoaDon.getNgayLap());
            if (laHoaDonChoThanhToan(hoaDon)) {
                tongQuan.setHoaDonChoThanhToan(tongQuan.getHoaDonChoThanhToan() + 1);
            }

            if (!"DaThanhToan".equals(hoaDon.getTrangThai()) || ngayLap == null) {
                continue;
            }

            if (trongHomNay(ngayLap)) {
                tongQuan.setHoaDonDaThanhToanHomNay(tongQuan.getHoaDonDaThanhToanHomNay() + 1);
                tongQuan.setDoanhThuGopHomNay(tongQuan.getDoanhThuGopHomNay() + hoaDon.getTongTien());
                tongQuan.setTongGiamGiaHomNay(tongQuan.getTongGiamGiaHomNay() + hoaDon.getTienGiam());
                tongQuan.setDoanhThuHomNay(tongQuan.getDoanhThuHomNay() + doanhThuThucNhan(hoaDon));
            }

            if (trongThangNay(ngayLap)) {
                tongQuan.setDoanhThuGopThangNay(tongQuan.getDoanhThuGopThangNay() + hoaDon.getTongTien());
                tongQuan.setTongGiamGiaThangNay(tongQuan.getTongGiamGiaThangNay() + hoaDon.getTienGiam());
                tongQuan.setDoanhThuThangNay(tongQuan.getDoanhThuThangNay() + doanhThuThucNhan(hoaDon));
            }
        }
    }

    private void tinhChiTietVe(TongQuanQuanLy tongQuan, List<ChiTietHoaDon> danhSachChiTiet, Set<Integer> danhSachMaHoaDonHomNay) {
        if (danhSachChiTiet == null || danhSachChiTiet.isEmpty()) {
            tongQuan.setTenVeBanChay("Chưa đủ dữ liệu");
            return;
        }

        Map<String, Integer> soLuongTheoVe = new HashMap<>();
        for (ChiTietHoaDon chiTiet : danhSachChiTiet) {
            int soLuong = tinhSoLuongVe(chiTiet);
            if (danhSachMaHoaDonHomNay != null && danhSachMaHoaDonHomNay.contains(chiTiet.getMaHoaDon())) {
                tongQuan.setVeBanHomNay(tongQuan.getVeBanHomNay() + soLuong);
            }
            tongQuan.setVeBanThangNay(tongQuan.getVeBanThangNay() + soLuong);

            String tenVe = layTenNhomBanChay(chiTiet);
            soLuongTheoVe.put(tenVe, soLuongTheoVe.containsKey(tenVe) ? soLuongTheoVe.get(tenVe) + soLuong : soLuong);
        }

        capNhatVeBanChay(tongQuan, soLuongTheoVe);
    }

    private void capNhatVeBanChay(TongQuanQuanLy tongQuan, Map<String, Integer> soLuongTheoVe) {
        String tenBanChay = "Chưa đủ dữ liệu";
        int soLuongBanChay = 0;
        for (Map.Entry<String, Integer> item : soLuongTheoVe.entrySet()) {
            if (item.getValue() > soLuongBanChay) {
                tenBanChay = item.getKey();
                soLuongBanChay = item.getValue();
            }
        }

        tongQuan.setTenVeBanChay(tenBanChay);
        tongQuan.setSoLuongVeBanChay(soLuongBanChay);
    }

    private int demVeDangBan(List<Ve> danhSachVe) {
        int soLuong = 0;
        if (danhSachVe == null) {
            return soLuong;
        }
        for (Ve ve : danhSachVe) {
            if ("HoatDong".equals(ve.getTrangThai())) {
                soLuong++;
            }
        }
        return soLuong;
    }

    private int demKhachHang(List<NguoiDung> danhSachNguoiDung) {
        int soLuong = 0;
        if (danhSachNguoiDung == null) {
            return soLuong;
        }
        for (NguoiDung nguoiDung : danhSachNguoiDung) {
            if ("NguoiDung".equals(nguoiDung.getVaiTro())) {
                soLuong++;
            }
        }
        return soLuong;
    }

    private String taoGoiYNhanh(TongQuanQuanLy tongQuan) {
        StringBuilder builder = new StringBuilder();
        if (tongQuan.getHoaDonDaThanhToanHomNay() == 0) {
            builder.append("• Hôm nay chưa có hóa đơn thanh toán.\n");
        }
        if (tongQuan.getVoucherDangHoatDong() == 0) {
            builder.append("• Chưa có voucher đang hoạt động.\n");
        }
        if (tongQuan.getSoLuongVeBanChay() == 0) {
            builder.append("• Chưa đủ dữ liệu để xác định vé bán chạy.\n");
        }
        if (builder.length() == 0) {
            builder.append("• Hệ thống đang có dữ liệu kinh doanh ổn định. Bạn có thể xem màn Hóa đơn để kiểm tra chi tiết.");
        }
        return builder.toString().trim();
    }

    private boolean laHoaDonChoThanhToan(HoaDon hoaDon) {
        return "ChoThanhToan".equals(hoaDon.getTrangThai()) || "ChuaThanhToan".equals(hoaDon.getTrangThai());
    }

    private double doanhThuThucNhan(HoaDon hoaDon) {
        return Math.max(0, hoaDon.getTongTien() - hoaDon.getTienGiam());
    }

    private int tinhSoLuongVe(ChiTietHoaDon chiTiet) {
        return chiTiet.getSoLuongNguoiLon() + chiTiet.getSoLuongTreEm() + chiTiet.getSoLuongNguoiCaoTuoi();
    }

    private String layTenNhomBanChay(ChiTietHoaDon chiTiet) {
        Ve ve = chiTiet.getVe();
        if (ve == null) {
            return "Vé khác";
        }
        LoaiVe loaiVe = ve.getLoaiVe();
        if (loaiVe != null && coNoiDung(loaiVe.getTenLoaiVe())) {
            return loaiVe.getTenLoaiVe();
        }
        if (coNoiDung(ve.getTenVe())) {
            return ve.getTenVe();
        }
        return "Vé khác";
    }

    private boolean trongHomNay(Date ngay) {
        if (ngay == null) {
            return false;
        }
        Calendar canKiemTra = Calendar.getInstance();
        canKiemTra.setTime(ngay);
        Calendar homNay = Calendar.getInstance();
        return canKiemTra.get(Calendar.YEAR) == homNay.get(Calendar.YEAR)
                && canKiemTra.get(Calendar.DAY_OF_YEAR) == homNay.get(Calendar.DAY_OF_YEAR);
    }

    private boolean trongThangNay(Date ngay) {
        if (ngay == null) {
            return false;
        }
        Calendar canKiemTra = Calendar.getInstance();
        canKiemTra.setTime(ngay);
        Calendar homNay = Calendar.getInstance();
        return canKiemTra.get(Calendar.YEAR) == homNay.get(Calendar.YEAR)
                && canKiemTra.get(Calendar.MONTH) == homNay.get(Calendar.MONTH);
    }

    private Date parseNgay(String ngay) {
        return parseTheoDinhDang(ngay, new String[]{"yyyy-MM-dd"});
    }

    private Date parseNgayGio(String ngayGio) {
        return parseTheoDinhDang(ngayGio, new String[]{
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
        });
    }

    private Date parseTheoDinhDang(String giaTri, String[] danhSachDinhDang) {
        if (!coNoiDung(giaTri)) {
            return null;
        }
        for (String dinhDang : danhSachDinhDang) {
            try {
                return new SimpleDateFormat(dinhDang, Locale.getDefault()).parse(giaTri);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private boolean coNoiDung(String chuoi) {
        return chuoi != null && !chuoi.trim().isEmpty();
    }
}
