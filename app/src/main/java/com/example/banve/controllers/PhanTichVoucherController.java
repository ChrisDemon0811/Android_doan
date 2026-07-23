package com.example.banve.controllers;

import com.example.banve.models.PhanTichVoucher;
import com.example.banve.models.Voucher;
import com.example.banve.utils.DinhDangTien;

import java.util.ArrayList;
import java.util.List;

public class PhanTichVoucherController {
    private static final String KHONG_XAC_DINH_DOANH_THU =
            "Không thể xác định doanh thu tối thiểu vì voucher không yêu cầu giá trị đơn hàng.";
    private static final String KHONG_XAC_DINH_NGAN_SACH =
            "Không thể xác định ngân sách tối đa vì voucher chưa giới hạn số tiền giảm trên mỗi đơn.";
    private static final String GHI_CHU_DOANH_THU =
            "Đây là doanh thu ước tính trước giá vốn và chi phí vận hành, không phải lợi nhuận.";

    public PhanTichVoucher phanTich(Voucher voucher) {
        Voucher duLieu = voucher == null ? new Voucher() : voucher;
        PhanTichVoucher ketQua = new PhanTichVoucher();
        int soLuong = Math.max(0, duLieu.getSoLuong());
        double donToiThieu = Math.max(0, duLieu.getDonToiThieu());
        double tienGiamTaiNguong = tinhTienGiamTaiNguong(duLieu, donToiThieu);

        ketQua.setSoLuotToiDa(soLuong);
        if (donToiThieu > 0) {
            double doanhThuToiThieu = donToiThieu * soLuong;
            double doanhThuSauGiam = Math.max(0, doanhThuToiThieu - tienGiamTaiNguong * soLuong);
            ketQua.setDoanhThuToiThieu(DinhDangTien.dinhDang(doanhThuToiThieu));
            ketQua.setDoanhThuSauGiam(DinhDangTien.dinhDang(doanhThuSauGiam) + "\n" + GHI_CHU_DOANH_THU);
        } else {
            ketQua.setDoanhThuToiThieu(KHONG_XAC_DINH_DOANH_THU);
            ketQua.setDoanhThuSauGiam("Chưa thể ước tính.\n" + GHI_CHU_DOANH_THU);
        }

        ketQua.setTienGiamMoiDon(DinhDangTien.dinhDang(tienGiamTaiNguong));
        ketQua.setNganSachGiamGia(tinhNganSachGiamGia(duLieu, soLuong));
        ketQua.setNguoiBanDuoc(noiDanhSach(taoLoiIch(duLieu)));
        ketQua.setRuiRo(noiDanhSach(taoRuiRo(duLieu)));
        ketQua.setDanhGiaAnToan(danhGiaAnToan(duLieu));
        return ketQua;
    }

    private double tinhTienGiamTaiNguong(Voucher voucher, double donToiThieu) {
        double tienGiam = "PhanTram".equals(voucher.getKieuGiamGia())
                ? donToiThieu * Math.max(0, voucher.getGiaTriGiam()) / 100
                : Math.max(0, voucher.getGiaTriGiam());
        if (voucher.getGiamToiDa() > 0) {
            tienGiam = Math.min(tienGiam, voucher.getGiamToiDa());
        }
        if (donToiThieu > 0) {
            tienGiam = Math.min(tienGiam, donToiThieu);
        }
        return Math.max(0, tienGiam);
    }

    private String tinhNganSachGiamGia(Voucher voucher, int soLuong) {
        if ("PhanTram".equals(voucher.getKieuGiamGia()) && voucher.getGiamToiDa() <= 0) {
            return KHONG_XAC_DINH_NGAN_SACH;
        }
        double giamToiDaMoiDon = "PhanTram".equals(voucher.getKieuGiamGia())
                ? voucher.getGiamToiDa()
                : voucher.getGiaTriGiam();
        if (voucher.getDonToiThieu() > 0) {
            giamToiDaMoiDon = Math.min(giamToiDaMoiDon, voucher.getDonToiThieu());
        }
        return DinhDangTien.dinhDang(Math.max(0, giamToiDaMoiDon) * soLuong);
    }

    private List<String> taoLoiIch(Voucher voucher) {
        List<String> danhSach = new ArrayList<>();
        if (voucher.getDonToiThieu() > 0) {
            danhSach.add("Khuyến khích khách tăng giá trị đơn hàng");
        }
        if (voucher.getSoLuongVeToiThieu() > 1) {
            danhSach.add("Khuyến khích khách mua nhiều vé trong một đơn");
        }
        if (voucher.getMaLoaiVeApDung() != null) {
            danhSach.add("Hỗ trợ tăng doanh số cho loại vé được chọn");
        } else if (voucher.getMaVeApDung() != null) {
            danhSach.add("Kích cầu cho vé cụ thể");
        }
        if (voucher.isChiApDungKhachMoi()) {
            danhSach.add("Hỗ trợ thu hút khách hàng mua lần đầu");
        }
        if (voucher.getSoLanDungToiDaMoiNguoi() > 0) {
            danhSach.add("Giảm nguy cơ người dùng lạm dụng voucher");
        }
        if (voucher.getGiamToiDa() > 0) {
            danhSach.add("Kiểm soát chi phí giảm giá trên mỗi đơn");
        }
        if (voucher.getMucTieu() != null && !voucher.getMucTieu().trim().isEmpty()) {
            danhSach.add("Mục tiêu quản lý: " + voucher.getMucTieu().trim());
        }
        if (danhSach.isEmpty()) {
            danhSach.add("Tạo thêm động lực mua vé cho khách hàng");
        }
        return danhSach;
    }

    private List<String> taoRuiRo(Voucher voucher) {
        List<String> danhSach = new ArrayList<>();
        danhSach.add("Lợi nhuận biên trên mỗi đơn có thể giảm");
        if (voucher.getDonToiThieu() <= 0) {
            danhSach.add("Voucher có thể được dùng cho đơn giá trị thấp");
        }
        if ("PhanTram".equals(voucher.getKieuGiamGia()) && voucher.getGiamToiDa() <= 0) {
            danhSach.add("Chi phí giảm giá trên đơn lớn không được kiểm soát");
        }
        if (voucher.getSoLuong() > 1000) {
            danhSach.add("Ngân sách khuyến mãi có thể tăng cao nếu voucher được sử dụng hết");
        }
        if (voucher.getDonToiThieu() > 0
                && tinhTienGiamTaiNguong(voucher, voucher.getDonToiThieu()) > voucher.getDonToiThieu() * 0.5) {
            danhSach.add("Mức giảm tại ngưỡng tối thiểu đang chiếm hơn một nửa giá trị đơn hàng");
        }
        if (voucher.getMaLoaiVeApDung() == null && voucher.getMaVeApDung() == null) {
            danhSach.add("Khuyến mãi không tập trung vào vé cần kích cầu");
        }
        if (voucher.getSoLanDungToiDaMoiNguoi() <= 0) {
            danhSach.add("Một người dùng có thể nhận ưu đãi nhiều lần");
        }
        return danhSach;
    }

    private String danhGiaAnToan(Voucher voucher) {
        int diemRuiRo = 0;
        if (voucher.getDonToiThieu() <= 0) {
            diemRuiRo += 2;
        }
        if ("PhanTram".equals(voucher.getKieuGiamGia()) && voucher.getGiamToiDa() <= 0) {
            diemRuiRo += 2;
        }
        if (voucher.getSoLanDungToiDaMoiNguoi() <= 0) {
            diemRuiRo++;
        }
        if (voucher.getSoLuong() > 1000) {
            diemRuiRo++;
        }
        if (voucher.getDonToiThieu() > 0) {
            double tyLe = tinhTienGiamTaiNguong(voucher, voucher.getDonToiThieu()) / voucher.getDonToiThieu();
            if (tyLe > 0.5) {
                diemRuiRo += 2;
            } else if (tyLe > 0.3) {
                diemRuiRo++;
            }
        }
        if (diemRuiRo >= 4) {
            return "Rủi ro cao";
        }
        if (diemRuiRo >= 2) {
            return "Cần cân nhắc";
        }
        return "An toàn";
    }

    private String noiDanhSach(List<String> danhSach) {
        StringBuilder builder = new StringBuilder();
        for (String noiDung : danhSach) {
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append("• ").append(noiDung);
        }
        return builder.toString();
    }
}
