package com.example.banve.controllers;

import com.example.banve.dao.HoaDonDAO;
import com.example.banve.dao.VeDAO;
import com.example.banve.dao.VoucherDAO;
import com.example.banve.models.ChiTietHoaDon;
import com.example.banve.models.HoaDon;
import com.example.banve.models.LoaiVe;
import com.example.banve.models.Ve;
import com.example.banve.models.Voucher;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.DinhDangTien;
import com.example.banve.utils.Session;

import java.util.ArrayList;
import java.util.List;

public class AIContextController {
    private static final int SO_VE_TOI_DA = 30;
    private static final int SO_VOUCHER_TOI_DA = 10;
    private static final int SO_HOA_DON_TOI_DA = 5;

    private final VeDAO veDAO;
    private final VoucherDAO voucherDAO;
    private final HoaDonDAO hoaDonDAO;

    public AIContextController() {
        veDAO = new VeDAO();
        voucherDAO = new VoucherDAO();
        hoaDonDAO = new HoaDonDAO();
    }

    public void taoContextChat(ApiCallback<String> callback) {
        veDAO.layDanhSachVeQuanLy(new ApiCallback<List<Ve>>() {
            @Override
            public void onSuccess(List<Ve> danhSachVe) {
                voucherDAO.layDanhSachVoucherKhaDung(new ApiCallback<List<Voucher>>() {
                    @Override
                    public void onSuccess(List<Voucher> danhSachVoucher) {
                        taiHoaDonGanDay(danhSachVe, danhSachVoucher, callback);
                    }

                    @Override
                    public void onError(String thongBao) {
                        callback.onError("Không thể tải dữ liệu voucher từ hệ thống.");
                    }
                });
            }

            @Override
            public void onError(String thongBao) {
                callback.onError("Không thể tải dữ liệu vé từ hệ thống.");
            }
        });
    }

    private void taiHoaDonGanDay(List<Ve> danhSachVe, List<Voucher> danhSachVoucher, ApiCallback<String> callback) {
        if (!Session.dangDangNhap() || Session.nguoiDungHienTai == null || Session.nguoiDungHienTai.getMaNguoiDung() <= 0) {
            callback.onSuccess(ghepContext(danhSachVe, danhSachVoucher, new ArrayList<>()));
            return;
        }

        hoaDonDAO.layDanhSachDaThanhToan(Session.nguoiDungHienTai.getMaNguoiDung(), new ApiCallback<List<HoaDon>>() {
            @Override
            public void onSuccess(List<HoaDon> danhSachHoaDon) {
                List<HoaDon> danhSachGanDay = catDanhSach(danhSachHoaDon, SO_HOA_DON_TOI_DA);
                taiChiTietHoaDon(danhSachVe, danhSachVoucher, danhSachGanDay, 0, callback);
            }

            @Override
            public void onError(String thongBao) {
                callback.onSuccess(ghepContext(danhSachVe, danhSachVoucher, new ArrayList<>()));
            }
        });
    }

    private void taiChiTietHoaDon(
            List<Ve> danhSachVe,
            List<Voucher> danhSachVoucher,
            List<HoaDon> danhSachHoaDon,
            int viTri,
            ApiCallback<String> callback
    ) {
        if (viTri >= danhSachHoaDon.size()) {
            callback.onSuccess(ghepContext(danhSachVe, danhSachVoucher, danhSachHoaDon));
            return;
        }

        HoaDon hoaDon = danhSachHoaDon.get(viTri);
        hoaDonDAO.layChiTietHoaDon(hoaDon.getMaHoaDon(), new ApiCallback<List<ChiTietHoaDon>>() {
            @Override
            public void onSuccess(List<ChiTietHoaDon> data) {
                hoaDon.setDanhSachChiTiet(data);
                taiChiTietHoaDon(danhSachVe, danhSachVoucher, danhSachHoaDon, viTri + 1, callback);
            }

            @Override
            public void onError(String thongBao) {
                taiChiTietHoaDon(danhSachVe, danhSachVoucher, danhSachHoaDon, viTri + 1, callback);
            }
        });
    }

    private String ghepContext(List<Ve> danhSachVe, List<Voucher> danhSachVoucher, List<HoaDon> danhSachHoaDon) {
        StringBuilder builder = new StringBuilder();
        builder.append("DỮ LIỆU THẬT TỪ HỆ THỐNG\n");
        builder.append("Không được bịa dữ liệu ngoài các mục dưới đây.\n\n");
        builder.append(ghepDanhSachVe(danhSachVe));
        builder.append("\n");
        builder.append(ghepDanhSachVoucher(danhSachVoucher));
        builder.append("\n");
        builder.append(ghepHoaDonGanDay(danhSachHoaDon));
        return builder.toString();
    }

    private String ghepDanhSachVe(List<Ve> danhSachVe) {
        StringBuilder builder = new StringBuilder();
        builder.append("1. Vé đang bán:\n");
        List<Ve> danhSachHoatDong = new ArrayList<>();
        if (danhSachVe != null) {
            for (Ve ve : danhSachVe) {
                if ("HoatDong".equals(ve.getTrangThai())) {
                    danhSachHoatDong.add(ve);
                }
            }
        }

        if (danhSachHoatDong.isEmpty()) {
            builder.append("- Hiện tại hệ thống chưa có dữ liệu vé đang bán.\n");
            return builder.toString();
        }

        List<Ve> danhSachCat = catDanhSach(danhSachHoatDong, SO_VE_TOI_DA);
        for (Ve ve : danhSachCat) {
            builder.append("- MaVe ").append(ve.getMaVe())
                    .append(", TenVe: ").append(chuoi(ve.getTenVe()))
                    .append(", LoaiVe: ").append(layTenLoaiVe(ve))
                    .append(", GiaVe: ").append(DinhDangTien.dinhDang(ve.getGiaVe()))
                    .append(", GiaNguoiLon: ").append(DinhDangTien.dinhDang(ve.getGiaNguoiLon()))
                    .append(", GiaTreEm: ").append(DinhDangTien.dinhDang(ve.getGiaTreEm()))
                    .append(", GiaNguoiCaoTuoi: ").append(DinhDangTien.dinhDang(ve.getGiaNguoiCaoTuoi()))
                    .append(", SoLuongMoiNgay: ").append(ve.getSoLuong())
                    .append(", MoTa: ").append(chuoi(ve.getMoTa()))
                    .append(", ThongTinVe: ").append(chuoi(ve.getThongTinVe()))
                    .append(", TrangThai: ").append(chuoi(ve.getTrangThai()))
                    .append("\n");
        }
        return builder.toString();
    }

    private String ghepDanhSachVoucher(List<Voucher> danhSachVoucher) {
        StringBuilder builder = new StringBuilder();
        builder.append("2. Voucher khả dụng:\n");
        if (danhSachVoucher == null || danhSachVoucher.isEmpty()) {
            builder.append("- Hiện tại hệ thống chưa có voucher khả dụng.\n");
            return builder.toString();
        }

        for (Voucher voucher : catDanhSach(danhSachVoucher, SO_VOUCHER_TOI_DA)) {
            builder.append("- MaVoucher ").append(voucher.getMaVoucher())
                    .append(", MaGiamGia: ").append(chuoi(voucher.getMaGiamGia()))
                    .append(", TenVoucher: ").append(chuoi(voucher.getTenVoucher()))
                    .append(", KieuGiamGia: ").append(chuoi(voucher.getKieuGiamGia()))
                    .append(", GiaTriGiam: ").append(voucher.getGiaTriGiam())
                    .append(", NgayBatDau: ").append(chuoi(voucher.getNgayBatDau()))
                    .append(", NgayKetThuc: ").append(chuoi(voucher.getNgayKetThuc()))
                    .append(", SoLuong: ").append(voucher.getSoLuong())
                    .append(", DonToiThieu: ").append(DinhDangTien.dinhDang(voucher.getDonToiThieu()))
                    .append(", GiamToiDa: ").append(voucher.getGiamToiDa() > 0
                            ? DinhDangTien.dinhDang(voucher.getGiamToiDa()) : "Không giới hạn")
                    .append(", SoLuongVeToiThieu: ").append(Math.max(1, voucher.getSoLuongVeToiThieu()))
                    .append(", SoLanDungToiDaMoiNguoi: ").append(voucher.getSoLanDungToiDaMoiNguoi() > 0
                            ? voucher.getSoLanDungToiDaMoiNguoi() : "Không giới hạn")
                    .append(", ChiApDungKhachMoi: ").append(voucher.isChiApDungKhachMoi() ? "Có" : "Không")
                    .append(", MaLoaiVeApDung: ").append(voucher.getMaLoaiVeApDung())
                    .append(", MaVeApDung: ").append(voucher.getMaVeApDung())
                    .append(", MucTieu: ").append(chuoi(voucher.getMucTieu()))
                    .append(", MoTaDieuKien: ").append(chuoi(voucher.getMoTaDieuKien()))
                    .append(", TrangThai: ").append(chuoi(voucher.getTrangThai()))
                    .append("\n");
        }
        return builder.toString();
    }

    private String ghepHoaDonGanDay(List<HoaDon> danhSachHoaDon) {
        StringBuilder builder = new StringBuilder();
        builder.append("3. Hóa đơn gần đây của người dùng hiện tại:\n");
        if (danhSachHoaDon == null || danhSachHoaDon.isEmpty()) {
            builder.append("- Người dùng hiện tại chưa có hóa đơn đã thanh toán gần đây hoặc chưa đăng nhập.\n");
            return builder.toString();
        }

        for (HoaDon hoaDon : danhSachHoaDon) {
            builder.append("- MaHoaDon ").append(hoaDon.getMaHoaDon())
                    .append(", NgayLap: ").append(chuoi(hoaDon.getNgayLap()))
                    .append(", TongTien: ").append(DinhDangTien.dinhDang(hoaDon.getTongTien()))
                    .append(", TienGiam: ").append(DinhDangTien.dinhDang(hoaDon.getTienGiam()))
                    .append(", TrangThai: ").append(chuoi(hoaDon.getTrangThai()));
            if (hoaDon.getDanhSachChiTiet() != null && !hoaDon.getDanhSachChiTiet().isEmpty()) {
                builder.append(", Ve: ");
                for (ChiTietHoaDon chiTiet : hoaDon.getDanhSachChiTiet()) {
                    builder.append("[")
                            .append(chiTiet.getVe() == null ? "Vé đã xóa" : chuoi(chiTiet.getVe().getTenVe()))
                            .append(", NgaySuDung ").append(chuoi(chiTiet.getNgaySuDung()))
                            .append(", SL ")
                            .append(chiTiet.getSoLuongNguoiLon() + chiTiet.getSoLuongTreEm() + chiTiet.getSoLuongNguoiCaoTuoi())
                            .append("] ");
                }
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    private String layTenLoaiVe(Ve ve) {
        LoaiVe loaiVe = ve.getLoaiVe();
        if (loaiVe != null && !rong(loaiVe.getTenLoaiVe())) {
            return loaiVe.getTenLoaiVe();
        }
        return "Chưa rõ";
    }

    private <T> List<T> catDanhSach(List<T> danhSach, int gioiHan) {
        List<T> ketQua = new ArrayList<>();
        if (danhSach == null) {
            return ketQua;
        }
        for (int i = 0; i < danhSach.size() && i < gioiHan; i++) {
            ketQua.add(danhSach.get(i));
        }
        return ketQua;
    }

    private String chuoi(String giaTri) {
        return rong(giaTri) ? "Không có" : giaTri.trim();
    }

    private boolean rong(String chuoi) {
        return chuoi == null || chuoi.trim().isEmpty();
    }
}
