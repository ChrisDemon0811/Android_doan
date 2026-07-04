package com.example.banve.controllers;

import com.example.banve.dao.ChiTietGioHangDAO;
import com.example.banve.dao.ChiTietHoaDonDAO;
import com.example.banve.dao.ChiTietThanhToanTamDAO;
import com.example.banve.dao.HoaDonDAO;
import com.example.banve.dao.ThanhToanTamDAO;
import com.example.banve.dao.VeDAO;
import com.example.banve.dao.VoucherDAO;
import com.example.banve.models.ChiTietGioHang;
import com.example.banve.models.ChiTietHoaDon;
import com.example.banve.models.ChiTietThanhToanTam;
import com.example.banve.models.HoaDon;
import com.example.banve.models.MucGioHang;
import com.example.banve.models.ThanhToanTam;
import com.example.banve.models.Ve;
import com.example.banve.models.Voucher;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.SePayUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ThanhToanController {
    private final VeDAO veDAO;
    private final VoucherDAO voucherDAO;
    private final HoaDonDAO hoaDonDAO;
    private final ThanhToanTamDAO thanhToanTamDAO;
    private final ChiTietHoaDonDAO chiTietHoaDonDAO;
    private final ChiTietThanhToanTamDAO chiTietThanhToanTamDAO;
    private final ChiTietGioHangDAO chiTietGioHangDAO;

    public ThanhToanController() {
        veDAO = new VeDAO();
        voucherDAO = new VoucherDAO();
        hoaDonDAO = new HoaDonDAO();
        thanhToanTamDAO = new ThanhToanTamDAO();
        chiTietHoaDonDAO = new ChiTietHoaDonDAO();
        chiTietThanhToanTamDAO = new ChiTietThanhToanTamDAO();
        chiTietGioHangDAO = new ChiTietGioHangDAO();
    }

    public double apDungVoucher(Voucher voucher, double tongTien) {
        if (voucher == null || tongTien <= 0) {
            return 0;
        }

        double tienGiam;
        if ("PhanTram".equals(voucher.getKieuGiamGia())) {
            tienGiam = tongTien * voucher.getGiaTriGiam() / 100;
        } else {
            tienGiam = voucher.getGiaTriGiam();
        }

        return Math.min(tienGiam, tongTien);
    }

    public void hoanTatThanhToan(
            int maNguoiDung,
            List<MucGioHang> danhSachMuc,
            Voucher voucher,
            String hinhThucThanhToan,
            ApiCallback<HoaDon> callback
    ) {
        String loiDuLieu = kiemTraDuLieuThanhToan(maNguoiDung, danhSachMuc, hinhThucThanhToan);
        if (loiDuLieu != null) {
            callback.onError(loiDuLieu);
            return;
        }

        kiemTraTonVe(danhSachMuc, callback, 0, () -> truVoucherVaTaoHoaDon(maNguoiDung, danhSachMuc, voucher, hinhThucThanhToan, callback));
    }

    public void taoThanhToanSePay(
            int maNguoiDung,
            List<MucGioHang> danhSachMuc,
            Voucher voucher,
            ApiCallback<ThanhToanTam> callback
    ) {
        String loiDuLieu = kiemTraDuLieuThanhToan(maNguoiDung, danhSachMuc, "ChuyenKhoan");
        if (loiDuLieu != null) {
            callback.onError(loiDuLieu);
            return;
        }
        if (voucher != null && voucher.getSoLuong() <= 0) {
            callback.onError("Voucher đã hết lượt sử dụng");
            return;
        }

        kiemTraTonVe(danhSachMuc, callback, 0, () -> xoaThanhToanTamCuVaTaoMoi(maNguoiDung, danhSachMuc, voucher, callback));
    }

    private String kiemTraDuLieuThanhToan(int maNguoiDung, List<MucGioHang> danhSachMuc, String hinhThucThanhToan) {
        if (maNguoiDung <= 0) {
            return "Người dùng không hợp lệ";
        }
        if (danhSachMuc == null || danhSachMuc.isEmpty()) {
            return "Giỏ hàng đang trống";
        }
        if (hinhThucThanhToan == null || hinhThucThanhToan.trim().isEmpty()) {
            return "Vui lòng chọn hình thức thanh toán";
        }

        return kiemTraNgaySuDungHopLe(danhSachMuc);
    }

    private void kiemTraTonVe(
            List<MucGioHang> danhSachMuc,
            ApiCallback<?> callback,
            int viTri,
            Runnable khiHopLe
    ) {
        if (viTri >= danhSachMuc.size()) {
            khiHopLe.run();
            return;
        }

        MucGioHang muc = danhSachMuc.get(viTri);
        veDAO.layTheoMa(muc.getChiTietGioHang().getMaVe(), new ApiCallback<Ve>() {
            @Override
            public void onSuccess(Ve ve) {
                muc.setVe(ve);
                kiemTraSoLuongDaBanTheoNgay(danhSachMuc, callback, viTri, muc, ve, khiHopLe);
            }

            @Override
            public void onError(String thongBao) {
                callback.onError(thongBao);
            }
        });
    }

    private void kiemTraSoLuongDaBanTheoNgay(
            List<MucGioHang> danhSachMuc,
            ApiCallback<?> callback,
            int viTri,
            MucGioHang muc,
            Ve ve,
            Runnable khiHopLe
    ) {
        ChiTietGioHang chiTiet = muc.getChiTietGioHang();
        chiTietHoaDonDAO.layTheoVeVaNgay(chiTiet.getMaVe(), chiTiet.getNgaySuDung(), new ApiCallback<List<ChiTietHoaDon>>() {
            @Override
            public void onSuccess(List<ChiTietHoaDon> data) {
                int soLuongDaBan = tinhTongSoLuongDaBan(data);
                int soLuongDangMua = tinhSoLuongDangMuaCungNgay(danhSachMuc, chiTiet.getMaVe(), chiTiet.getNgaySuDung());
                int soLuongConLai = ve.getSoLuong() - soLuongDaBan;

                if (soLuongDangMua > soLuongConLai) {
                    callback.onError("Vé '" + ve.getTenVe() + "' ngày " + dinhDangNgayHienThi(chiTiet.getNgaySuDung())
                            + " chỉ còn " + Math.max(0, soLuongConLai) + " vé");
                    return;
                }

                kiemTraTonVe(danhSachMuc, callback, viTri + 1, khiHopLe);
            }

            @Override
            public void onError(String thongBao) {
                callback.onError(thongBao);
            }
        });
    }

    private void xoaThanhToanTamCuVaTaoMoi(
            int maNguoiDung,
            List<MucGioHang> danhSachMuc,
            Voucher voucher,
            ApiCallback<ThanhToanTam> callback
    ) {
        thanhToanTamDAO.xoaDangChoTheoNguoiDung(maNguoiDung, new ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                taoThanhToanTamMoi(maNguoiDung, danhSachMuc, voucher, callback);
            }

            @Override
            public void onError(String thongBao) {
                callback.onError(thongBao);
            }
        });
    }

    private void taoThanhToanTamMoi(
            int maNguoiDung,
            List<MucGioHang> danhSachMuc,
            Voucher voucher,
            ApiCallback<ThanhToanTam> callback
    ) {
        double tongTien = tinhTongTien(danhSachMuc);
        double tienGiam = apDungVoucher(voucher, tongTien);

        ThanhToanTam thanhToanTam = new ThanhToanTam();
        thanhToanTam.setMaNguoiDung(maNguoiDung);
        thanhToanTam.setTongTien(tongTien);
        thanhToanTam.setTienGiam(tienGiam);
        thanhToanTam.setMaVoucher(voucher == null ? null : voucher.getMaVoucher());
        thanhToanTam.setThanhToan("ChuyenKhoan");
        thanhToanTam.setTrangThai("ChoThanhToan");

        thanhToanTamDAO.taoThanhToanTam(thanhToanTam, new ApiCallback<ThanhToanTam>() {
            @Override
            public void onSuccess(ThanhToanTam data) {
                capNhatNoiDungSePay(data, danhSachMuc, callback);
            }

            @Override
            public void onError(String thongBao) {
                callback.onError(thongBao);
            }
        });
    }

    private void capNhatNoiDungSePay(
            ThanhToanTam thanhToanTam,
            List<MucGioHang> danhSachMuc,
            ApiCallback<ThanhToanTam> callback
    ) {
        String noiDungChuyenKhoan = SePayUtil.taoNoiDungChuyenKhoan(thanhToanTam.getMaHoaDon());
        thanhToanTamDAO.capNhatNoiDungChuyenKhoan(thanhToanTam.getMaHoaDon(), noiDungChuyenKhoan, new ApiCallback<ThanhToanTam>() {
            @Override
            public void onSuccess(ThanhToanTam data) {
                taoChiTietThanhToanTam(data, danhSachMuc, callback, 0);
            }

            @Override
            public void onError(String thongBao) {
                callback.onError(thongBao);
            }
        });
    }

    private void taoChiTietThanhToanTam(
            ThanhToanTam thanhToanTam,
            List<MucGioHang> danhSachMuc,
            ApiCallback<ThanhToanTam> callback,
            int viTri
    ) {
        if (viTri >= danhSachMuc.size()) {
            callback.onSuccess(thanhToanTam);
            return;
        }

        ChiTietThanhToanTam chiTiet = taoChiTietThanhToanTamTuMuc(thanhToanTam.getMaHoaDon(), danhSachMuc.get(viTri));
        chiTietThanhToanTamDAO.taoChiTietThanhToanTam(chiTiet, new ApiCallback<ChiTietThanhToanTam>() {
            @Override
            public void onSuccess(ChiTietThanhToanTam data) {
                taoChiTietThanhToanTam(thanhToanTam, danhSachMuc, callback, viTri + 1);
            }

            @Override
            public void onError(String thongBao) {
                thanhToanTamDAO.xoaTheoMa(thanhToanTam.getMaHoaDon(), new ApiCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean data) {
                        callback.onError(thongBao);
                    }

                    @Override
                    public void onError(String loiXoa) {
                        callback.onError(thongBao);
                    }
                });
            }
        });
    }

    private void truVoucherVaTaoHoaDon(
            int maNguoiDung,
            List<MucGioHang> danhSachMuc,
            Voucher voucher,
            String hinhThucThanhToan,
            ApiCallback<HoaDon> callback
    ) {
        if (voucher == null) {
            taoHoaDon(maNguoiDung, danhSachMuc, null, hinhThucThanhToan, callback);
            return;
        }

        if (voucher.getSoLuong() <= 0) {
            callback.onError("Voucher đã hết lượt sử dụng");
            return;
        }

        voucherDAO.capNhatSoLuong(voucher.getMaVoucher(), voucher.getSoLuong() - 1, new ApiCallback<Voucher>() {
            @Override
            public void onSuccess(Voucher data) {
                taoHoaDon(maNguoiDung, danhSachMuc, data, hinhThucThanhToan, callback);
            }

            @Override
            public void onError(String thongBao) {
                callback.onError(thongBao);
            }
        });
    }

    private void taoHoaDon(
            int maNguoiDung,
            List<MucGioHang> danhSachMuc,
            Voucher voucher,
            String hinhThucThanhToan,
            ApiCallback<HoaDon> callback
    ) {
        double tongTien = tinhTongTien(danhSachMuc);
        double tienGiam = apDungVoucher(voucher, tongTien);

        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaNguoiDung(maNguoiDung);
        hoaDon.setTongTien(tongTien);
        hoaDon.setTienGiam(tienGiam);
        hoaDon.setMaVoucher(voucher == null ? null : voucher.getMaVoucher());
        hoaDon.setThanhToan(hinhThucThanhToan);
        hoaDon.setTrangThai("DaThanhToan");

        hoaDonDAO.taoHoaDon(hoaDon, new ApiCallback<HoaDon>() {
            @Override
            public void onSuccess(HoaDon data) {
                taoChiTietHoaDon(data, danhSachMuc, callback, 0);
            }

            @Override
            public void onError(String thongBao) {
                callback.onError(thongBao);
            }
        });
    }

    private void taoChiTietHoaDon(
            HoaDon hoaDon,
            List<MucGioHang> danhSachMuc,
            ApiCallback<HoaDon> callback,
            int viTri
    ) {
        if (viTri >= danhSachMuc.size()) {
            xoaGioHangSauThanhToan(hoaDon, danhSachMuc, callback, 0);
            return;
        }

        ChiTietHoaDon chiTietHoaDon = taoChiTietHoaDonTuMuc(hoaDon.getMaHoaDon(), danhSachMuc.get(viTri));
        chiTietHoaDonDAO.taoChiTietHoaDon(chiTietHoaDon, new ApiCallback<ChiTietHoaDon>() {
            @Override
            public void onSuccess(ChiTietHoaDon data) {
                taoChiTietHoaDon(hoaDon, danhSachMuc, callback, viTri + 1);
            }

            @Override
            public void onError(String thongBao) {
                callback.onError(thongBao);
            }
        });
    }

    private void xoaGioHangSauThanhToan(
            HoaDon hoaDon,
            List<MucGioHang> danhSachMuc,
            ApiCallback<HoaDon> callback,
            int viTri
    ) {
        if (viTri >= danhSachMuc.size()) {
            callback.onSuccess(hoaDon);
            return;
        }

        int maChiTietGioHang = danhSachMuc.get(viTri).getChiTietGioHang().getMaChiTietGioHang();
        if (maChiTietGioHang <= 0) {
            xoaGioHangSauThanhToan(hoaDon, danhSachMuc, callback, viTri + 1);
            return;
        }

        chiTietGioHangDAO.xoaMuc(maChiTietGioHang, new ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                xoaGioHangSauThanhToan(hoaDon, danhSachMuc, callback, viTri + 1);
            }

            @Override
            public void onError(String thongBao) {
                callback.onError(thongBao);
            }
        });
    }

    private ChiTietHoaDon taoChiTietHoaDonTuMuc(int maHoaDon, MucGioHang muc) {
        ChiTietGioHang chiTietGioHang = muc.getChiTietGioHang();
        ChiTietHoaDon chiTietHoaDon = new ChiTietHoaDon();
        chiTietHoaDon.setMaHoaDon(maHoaDon);
        chiTietHoaDon.setMaVe(chiTietGioHang.getMaVe());
        chiTietHoaDon.setNgaySuDung(chiTietGioHang.getNgaySuDung());
        chiTietHoaDon.setSoLuongNguoiLon(chiTietGioHang.getSoLuongNguoiLon());
        chiTietHoaDon.setSoLuongTreEm(chiTietGioHang.getSoLuongTreEm());
        chiTietHoaDon.setSoLuongNguoiCaoTuoi(chiTietGioHang.getSoLuongNguoiCaoTuoi());
        chiTietHoaDon.setDonGiaNguoiLon(chiTietGioHang.getDonGiaNguoiLon());
        chiTietHoaDon.setDonGiaTreEm(chiTietGioHang.getDonGiaTreEm());
        chiTietHoaDon.setDonGiaNguoiCaoTuoi(chiTietGioHang.getDonGiaNguoiCaoTuoi());
        chiTietHoaDon.setThanhTien(muc.tinhThanhTien());
        return chiTietHoaDon;
    }

    private ChiTietThanhToanTam taoChiTietThanhToanTamTuMuc(int maHoaDon, MucGioHang muc) {
        ChiTietGioHang chiTietGioHang = muc.getChiTietGioHang();
        ChiTietThanhToanTam chiTiet = new ChiTietThanhToanTam();
        chiTiet.setMaHoaDon(maHoaDon);
        chiTiet.setMaChiTietGioHang(chiTietGioHang.getMaChiTietGioHang());
        chiTiet.setMaVe(chiTietGioHang.getMaVe());
        chiTiet.setNgaySuDung(chiTietGioHang.getNgaySuDung());
        chiTiet.setSoLuongNguoiLon(chiTietGioHang.getSoLuongNguoiLon());
        chiTiet.setSoLuongTreEm(chiTietGioHang.getSoLuongTreEm());
        chiTiet.setSoLuongNguoiCaoTuoi(chiTietGioHang.getSoLuongNguoiCaoTuoi());
        chiTiet.setDonGiaNguoiLon(chiTietGioHang.getDonGiaNguoiLon());
        chiTiet.setDonGiaTreEm(chiTietGioHang.getDonGiaTreEm());
        chiTiet.setDonGiaNguoiCaoTuoi(chiTietGioHang.getDonGiaNguoiCaoTuoi());
        chiTiet.setThanhTien(muc.tinhThanhTien());
        return chiTiet;
    }

    public double tinhTongTien(List<MucGioHang> danhSachMuc) {
        double tongTien = 0;
        if (danhSachMuc == null) {
            return tongTien;
        }

        for (MucGioHang muc : danhSachMuc) {
            tongTien += muc.tinhThanhTien();
        }
        return tongTien;
    }

    private int tinhTongSoLuong(ChiTietGioHang chiTietGioHang) {
        return chiTietGioHang.getSoLuongNguoiLon()
                + chiTietGioHang.getSoLuongTreEm()
                + chiTietGioHang.getSoLuongNguoiCaoTuoi();
    }

    private int tinhTongSoLuong(ChiTietHoaDon chiTietHoaDon) {
        return chiTietHoaDon.getSoLuongNguoiLon()
                + chiTietHoaDon.getSoLuongTreEm()
                + chiTietHoaDon.getSoLuongNguoiCaoTuoi();
    }

    private int tinhTongSoLuongDaBan(List<ChiTietHoaDon> danhSachChiTiet) {
        int tongSoLuong = 0;
        if (danhSachChiTiet == null) {
            return tongSoLuong;
        }

        for (ChiTietHoaDon chiTiet : danhSachChiTiet) {
            tongSoLuong += tinhTongSoLuong(chiTiet);
        }
        return tongSoLuong;
    }

    private int tinhSoLuongDangMuaCungNgay(List<MucGioHang> danhSachMuc, int maVe, String ngaySuDung) {
        int tongSoLuong = 0;
        for (MucGioHang muc : danhSachMuc) {
            ChiTietGioHang chiTiet = muc.getChiTietGioHang();
            if (chiTiet.getMaVe() == maVe && ngaySuDung.equals(chiTiet.getNgaySuDung())) {
                tongSoLuong += tinhTongSoLuong(chiTiet);
            }
        }
        return tongSoLuong;
    }

    private String dinhDangNgayHienThi(String ngaySuDung) {
        Date ngay = parseNgaySuDung(ngaySuDung);
        if (ngay == null) {
            return ngaySuDung;
        }
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(ngay);
    }

    private String kiemTraNgaySuDungHopLe(List<MucGioHang> danhSachMuc) {
        Calendar homNay = Calendar.getInstance();
        homNay.set(Calendar.HOUR_OF_DAY, 0);
        homNay.set(Calendar.MINUTE, 0);
        homNay.set(Calendar.SECOND, 0);
        homNay.set(Calendar.MILLISECOND, 0);

        for (MucGioHang muc : danhSachMuc) {
            Date ngaySuDung = parseNgaySuDung(muc.getChiTietGioHang().getNgaySuDung());
            if (ngaySuDung != null && ngaySuDung.before(homNay.getTime())) {
                String tenVe = muc.getVe() == null ? "vé" : muc.getVe().getTenVe();
                return "Vé \"" + tenVe + "\" có ngày sử dụng trong quá khứ. Vui lòng sửa ngày trước khi thanh toán.";
            }
        }
        return null;
    }

    private Date parseNgaySuDung(String ngaySuDung) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(ngaySuDung);
        } catch (ParseException e) {
            return null;
        }
    }
}
