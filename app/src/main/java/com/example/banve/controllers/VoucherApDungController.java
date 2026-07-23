package com.example.banve.controllers;

import com.example.banve.models.HoaDon;
import com.example.banve.models.KetQuaKiemTraVoucher;
import com.example.banve.models.MucGioHang;
import com.example.banve.models.Ve;
import com.example.banve.models.Voucher;
import com.example.banve.utils.DinhDangTien;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VoucherApDungController {
    public KetQuaKiemTraVoucher kiemTra(
            Voucher voucher,
            List<MucGioHang> danhSachMuc,
            int maNguoiDung,
            List<HoaDon> danhSachHoaDonDaThanhToan
    ) {
        String homNay = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        return kiemTraTaiNgay(voucher, danhSachMuc, maNguoiDung, danhSachHoaDonDaThanhToan, homNay);
    }

    public KetQuaKiemTraVoucher kiemTraTaiNgay(
            Voucher voucher,
            List<MucGioHang> danhSachMuc,
            int maNguoiDung,
            List<HoaDon> danhSachHoaDonDaThanhToan,
            String homNay
    ) {
        KetQuaKiemTraVoucher ketQua = taoKetQua(voucher);
        if (voucher == null) {
            return khongHopLe(ketQua, "Voucher không tồn tại");
        }
        if (!"HoatDong".equals(voucher.getTrangThai())) {
            return khongHopLe(ketQua, "Voucher không còn hoạt động");
        }
        if (!ngayHopLe(homNay) || !ngayHopLe(voucher.getNgayBatDau()) || !ngayHopLe(voucher.getNgayKetThuc())) {
            return khongHopLe(ketQua, "Thời hạn voucher không hợp lệ");
        }
        if (homNay.compareTo(voucher.getNgayBatDau()) < 0) {
            return khongHopLe(ketQua, "Voucher chưa đến ngày áp dụng");
        }
        if (homNay.compareTo(voucher.getNgayKetThuc()) > 0) {
            return khongHopLe(ketQua, "Voucher đã hết hạn");
        }
        if (voucher.getSoLuong() <= 0) {
            return khongHopLe(ketQua, "Voucher đã hết lượt sử dụng");
        }

        double tongTien = tinhTongTien(danhSachMuc);
        int tongSoVe = tinhTongSoVe(danhSachMuc);
        if (tongTien <= 0 || tongSoVe <= 0) {
            return khongHopLe(ketQua, "Giỏ hàng đang trống");
        }
        if (tongTien < voucher.getDonToiThieu()) {
            double soTienConThieu = voucher.getDonToiThieu() - tongTien;
            ketQua.setSoTienConThieu(soTienConThieu);
            return khongHopLe(ketQua, "Cần thêm " + DinhDangTien.dinhDang(soTienConThieu) + " để sử dụng voucher này");
        }

        int soLuongVeToiThieu = Math.max(1, voucher.getSoLuongVeToiThieu());
        if (tongSoVe < soLuongVeToiThieu) {
            int soVeConThieu = soLuongVeToiThieu - tongSoVe;
            ketQua.setSoVeConThieu(soVeConThieu);
            return khongHopLe(ketQua, "Cần thêm " + soVeConThieu + " vé để sử dụng voucher này");
        }

        double tamTinhDuocApDung = tinhTamTinhDuocApDung(voucher, danhSachMuc);
        ketQua.setTamTinhDuocApDung(tamTinhDuocApDung);
        ketQua.setPhamViApDung(taoMoTaPhamVi(voucher, danhSachMuc));
        if (tamTinhDuocApDung <= 0) {
            return khongHopLe(ketQua, taoLyDoSaiPhamVi(voucher, danhSachMuc));
        }

        int soHoaDonThanhCong = demHoaDonThanhCong(danhSachHoaDonDaThanhToan);
        if (voucher.isChiApDungKhachMoi() && soHoaDonThanhCong > 0) {
            return khongHopLe(ketQua, "Voucher chỉ dành cho khách hàng mới");
        }
        if ((voucher.isChiApDungKhachMoi() || voucher.getSoLanDungToiDaMoiNguoi() > 0) && maNguoiDung <= 0) {
            return khongHopLe(ketQua, "Vui lòng đăng nhập để sử dụng voucher này");
        }

        int soLanDaDung = demSoLanDaDung(voucher.getMaVoucher(), danhSachHoaDonDaThanhToan);
        if (voucher.getSoLanDungToiDaMoiNguoi() > 0
                && soLanDaDung >= voucher.getSoLanDungToiDaMoiNguoi()) {
            return khongHopLe(ketQua, "Bạn đã sử dụng hết số lần cho phép");
        }

        double tienGiam = tinhTienGiam(voucher, tamTinhDuocApDung, tongTien);
        if (tienGiam <= 0) {
            return khongHopLe(ketQua, "Voucher không tạo ra giá trị giảm cho giỏ hàng này");
        }

        ketQua.setHopLe(true);
        ketQua.setTienGiam(tienGiam);
        ketQua.setLyDo("Đủ điều kiện, giảm " + DinhDangTien.dinhDang(tienGiam));
        return ketQua;
    }

    public List<KetQuaKiemTraVoucher> danhGiaDanhSach(
            List<Voucher> danhSachVoucher,
            List<MucGioHang> danhSachMuc,
            int maNguoiDung,
            List<HoaDon> danhSachHoaDonDaThanhToan
    ) {
        List<KetQuaKiemTraVoucher> ketQua = new ArrayList<>();
        if (danhSachVoucher != null) {
            for (Voucher voucher : danhSachVoucher) {
                ketQua.add(kiemTra(voucher, danhSachMuc, maNguoiDung, danhSachHoaDonDaThanhToan));
            }
        }
        Collections.sort(ketQua, new Comparator<KetQuaKiemTraVoucher>() {
            @Override
            public int compare(KetQuaKiemTraVoucher mot, KetQuaKiemTraVoucher hai) {
                if (mot.isHopLe() != hai.isHopLe()) {
                    return mot.isHopLe() ? -1 : 1;
                }
                return Double.compare(hai.getTienGiam(), mot.getTienGiam());
            }
        });
        return ketQua;
    }

    public double tinhTienGiam(Voucher voucher, double tamTinhDuocApDung, double tongTien) {
        if (voucher == null || tamTinhDuocApDung <= 0 || tongTien <= 0) {
            return 0;
        }

        double tienGiam = "PhanTram".equals(voucher.getKieuGiamGia())
                ? tamTinhDuocApDung * voucher.getGiaTriGiam() / 100
                : voucher.getGiaTriGiam();
        if (voucher.getGiamToiDa() > 0) {
            tienGiam = Math.min(tienGiam, voucher.getGiamToiDa());
        }
        tienGiam = Math.min(tienGiam, tamTinhDuocApDung);
        tienGiam = Math.min(tienGiam, tongTien);
        return Math.max(0, tienGiam);
    }

    private KetQuaKiemTraVoucher taoKetQua(Voucher voucher) {
        KetQuaKiemTraVoucher ketQua = new KetQuaKiemTraVoucher();
        ketQua.setVoucher(voucher);
        ketQua.setPhamViApDung(taoMoTaPhamVi(voucher, null));
        return ketQua;
    }

    private KetQuaKiemTraVoucher khongHopLe(KetQuaKiemTraVoucher ketQua, String lyDo) {
        ketQua.setHopLe(false);
        ketQua.setTienGiam(0);
        ketQua.setLyDo(lyDo);
        return ketQua;
    }

    private double tinhTongTien(List<MucGioHang> danhSachMuc) {
        double tongTien = 0;
        if (danhSachMuc != null) {
            for (MucGioHang muc : danhSachMuc) {
                if (muc != null && muc.getChiTietGioHang() != null) {
                    tongTien += Math.max(0, muc.tinhThanhTien());
                }
            }
        }
        return tongTien;
    }

    private int tinhTongSoVe(List<MucGioHang> danhSachMuc) {
        int tongSoVe = 0;
        if (danhSachMuc != null) {
            for (MucGioHang muc : danhSachMuc) {
                if (muc == null || muc.getChiTietGioHang() == null) {
                    continue;
                }
                tongSoVe += Math.max(0, muc.getChiTietGioHang().getSoLuongNguoiLon());
                tongSoVe += Math.max(0, muc.getChiTietGioHang().getSoLuongTreEm());
                tongSoVe += Math.max(0, muc.getChiTietGioHang().getSoLuongNguoiCaoTuoi());
            }
        }
        return tongSoVe;
    }

    private double tinhTamTinhDuocApDung(Voucher voucher, List<MucGioHang> danhSachMuc) {
        double tamTinh = 0;
        if (danhSachMuc == null) {
            return tamTinh;
        }
        for (MucGioHang muc : danhSachMuc) {
            if (muc == null || muc.getVe() == null || muc.getChiTietGioHang() == null) {
                continue;
            }
            if (voucher.getMaVeApDung() != null && muc.getVe().getMaVe() != voucher.getMaVeApDung()) {
                continue;
            }
            if (voucher.getMaLoaiVeApDung() != null
                    && muc.getVe().getMaLoaiVe() != voucher.getMaLoaiVeApDung()) {
                continue;
            }
            tamTinh += Math.max(0, muc.tinhThanhTien());
        }
        return tamTinh;
    }

    private int demHoaDonThanhCong(List<HoaDon> danhSachHoaDon) {
        int soLuong = 0;
        if (danhSachHoaDon != null) {
            for (HoaDon hoaDon : danhSachHoaDon) {
                if (hoaDon != null && "DaThanhToan".equals(hoaDon.getTrangThai())) {
                    soLuong++;
                }
            }
        }
        return soLuong;
    }

    private int demSoLanDaDung(int maVoucher, List<HoaDon> danhSachHoaDon) {
        int soLan = 0;
        if (danhSachHoaDon != null) {
            for (HoaDon hoaDon : danhSachHoaDon) {
                if (hoaDon != null
                        && "DaThanhToan".equals(hoaDon.getTrangThai())
                        && hoaDon.getMaVoucher() != null
                        && hoaDon.getMaVoucher() == maVoucher) {
                    soLan++;
                }
            }
        }
        return soLan;
    }

    private String taoMoTaPhamVi(Voucher voucher, List<MucGioHang> danhSachMuc) {
        if (voucher == null) {
            return "Tất cả vé";
        }
        if (voucher.getMaVeApDung() != null) {
            String tenVe = timTenVe(voucher.getMaVeApDung(), danhSachMuc);
            return tenVe == null ? "Vé mã " + voucher.getMaVeApDung() : "Vé " + tenVe;
        }
        if (voucher.getMaLoaiVeApDung() != null) {
            String tenLoaiVe = timTenLoaiVe(voucher.getMaLoaiVeApDung(), danhSachMuc);
            return tenLoaiVe == null ? "Loại vé mã " + voucher.getMaLoaiVeApDung() : "Loại vé " + tenLoaiVe;
        }
        return "Tất cả vé";
    }

    private String taoLyDoSaiPhamVi(Voucher voucher, List<MucGioHang> danhSachMuc) {
        if (voucher.getMaVeApDung() != null) {
            String tenVe = timTenVe(voucher.getMaVeApDung(), danhSachMuc);
            return tenVe == null
                    ? "Voucher chỉ áp dụng cho vé có mã " + voucher.getMaVeApDung()
                    : "Voucher chỉ áp dụng cho vé " + tenVe;
        }
        if (voucher.getMaLoaiVeApDung() != null) {
            String tenLoaiVe = timTenLoaiVe(voucher.getMaLoaiVeApDung(), danhSachMuc);
            return tenLoaiVe == null
                    ? "Voucher chỉ áp dụng cho loại vé có mã " + voucher.getMaLoaiVeApDung()
                    : "Voucher chỉ áp dụng cho loại vé " + tenLoaiVe;
        }
        return "Giỏ hàng không có vé thuộc phạm vi áp dụng";
    }

    private String timTenVe(int maVe, List<MucGioHang> danhSachMuc) {
        if (danhSachMuc != null) {
            for (MucGioHang muc : danhSachMuc) {
                Ve ve = muc == null ? null : muc.getVe();
                if (ve != null && ve.getMaVe() == maVe) {
                    return ve.getTenVe();
                }
            }
        }
        return null;
    }

    private String timTenLoaiVe(int maLoaiVe, List<MucGioHang> danhSachMuc) {
        if (danhSachMuc != null) {
            for (MucGioHang muc : danhSachMuc) {
                Ve ve = muc == null ? null : muc.getVe();
                if (ve != null && ve.getMaLoaiVe() == maLoaiVe && ve.getLoaiVe() != null) {
                    return ve.getLoaiVe().getTenLoaiVe();
                }
            }
        }
        return null;
    }

    private boolean ngayHopLe(String ngay) {
        if (ngay == null || ngay.trim().isEmpty()) {
            return false;
        }
        SimpleDateFormat dinhDang = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dinhDang.setLenient(false);
        try {
            dinhDang.parse(ngay);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
