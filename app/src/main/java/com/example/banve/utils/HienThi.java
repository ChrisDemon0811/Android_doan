package com.example.banve.utils;

public final class HienThi {
    private HienThi() {
    }

    public static String vaiTro(String vaiTro) {
        if ("QuanLy".equals(vaiTro)) {
            return "Quản lý";
        }
        if ("NguoiDung".equals(vaiTro)) {
            return "Người dùng";
        }
        return giaTri(vaiTro);
    }

    public static String trangThai(String trangThai) {
        if ("HoatDong".equals(trangThai)) {
            return "Hoạt động";
        }
        if ("Khoa".equals(trangThai)) {
            return "Khóa";
        }
        if ("DaThanhToan".equals(trangThai)) {
            return "Đã thanh toán";
        }
        if ("ChuaThanhToan".equals(trangThai)) {
            return "Chưa thanh toán";
        }
        if ("ChoThanhToan".equals(trangThai)) {
            return "Chờ thanh toán";
        }
        if ("DangXuLy".equals(trangThai)) {
            return "Đang xử lý";
        }
        if ("LoiThanhToan".equals(trangThai)) {
            return "Lỗi thanh toán";
        }
        return giaTri(trangThai);
    }

    public static String hinhThucThanhToan(String thanhToan) {
        if ("ChuyenKhoan".equals(thanhToan)) {
            return "Chuyển khoản";
        }
        if ("VNPay".equals(thanhToan)) {
            return "VNPay";
        }
        if ("TheQuocTe".equals(thanhToan) || "TienMat".equals(thanhToan)) {
            return "Thẻ tín dụng/ghi nợ quốc tế";
        }
        return giaTri(thanhToan);
    }

    public static String kieuGiamGia(String kieuGiamGia) {
        if ("PhanTram".equals(kieuGiamGia)) {
            return "Phần trăm";
        }
        if ("TienMat".equals(kieuGiamGia)) {
            return "Tiền mặt";
        }
        return giaTri(kieuGiamGia);
    }

    private static String giaTri(String giaTri) {
        return giaTri == null ? "" : giaTri;
    }
}
