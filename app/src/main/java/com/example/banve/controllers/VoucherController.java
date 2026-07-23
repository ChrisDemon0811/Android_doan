package com.example.banve.controllers;

import com.example.banve.dao.VoucherDAO;
import com.example.banve.models.Voucher;
import com.example.banve.network.ApiCallback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class VoucherController {
    private final VoucherDAO voucherDAO;

    public VoucherController() {
        voucherDAO = new VoucherDAO();
    }

    public void layDanhSachVoucher(ApiCallback<List<Voucher>> callback) {
        voucherDAO.layDanhSachVoucher(callback);
    }

    public void layDanhSachVoucherChoThanhToan(ApiCallback<List<Voucher>> callback) {
        voucherDAO.layDanhSachVoucherChoThanhToan(callback);
    }

    public void layTheoMa(int maVoucher, ApiCallback<Voucher> callback) {
        if (maVoucher <= 0) {
            callback.onError("Voucher không hợp lệ");
            return;
        }
        voucherDAO.layTheoMa(maVoucher, callback);
    }

    public void themVoucher(Voucher voucher, ApiCallback<Voucher> callback) {
        String loi = kiemTraVoucher(voucher, false);
        if (loi != null) {
            callback.onError(loi);
            return;
        }

        kiemTraTrungMa(voucher, new ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean biTrung) {
                if (biTrung) {
                    callback.onError("Mã giảm giá đã tồn tại");
                    return;
                }

                voucherDAO.themVoucher(chuanHoaVoucher(voucher), callback);
            }

            @Override
            public void onError(String thongBao) {
                callback.onError(thongBao);
            }
        });
    }

    public void suaVoucher(Voucher voucher, ApiCallback<Voucher> callback) {
        String loi = kiemTraVoucher(voucher, true);
        if (loi != null) {
            callback.onError(loi);
            return;
        }

        kiemTraTrungMa(voucher, new ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean biTrung) {
                if (biTrung) {
                    callback.onError("Mã giảm giá đã tồn tại");
                    return;
                }

                voucherDAO.suaVoucher(chuanHoaVoucher(voucher), callback);
            }

            @Override
            public void onError(String thongBao) {
                callback.onError(thongBao);
            }
        });
    }

    public void xoaVoucher(int maVoucher, ApiCallback<Boolean> callback) {
        if (maVoucher <= 0) {
            callback.onError("Voucher không hợp lệ");
            return;
        }

        voucherDAO.xoaVoucher(maVoucher, callback);
    }

    private void kiemTraTrungMa(Voucher voucher, ApiCallback<Boolean> callback) {
        voucherDAO.layDanhSachVoucher(new ApiCallback<List<Voucher>>() {
            @Override
            public void onSuccess(List<Voucher> data) {
                String maCanKiemTra = voucher.getMaGiamGia().trim();
                if (data != null) {
                    for (Voucher item : data) {
                        if (item.getMaVoucher() != voucher.getMaVoucher()
                                && item.getMaGiamGia() != null
                                && item.getMaGiamGia().trim().equalsIgnoreCase(maCanKiemTra)) {
                            callback.onSuccess(true);
                            return;
                        }
                    }
                }
                callback.onSuccess(false);
            }

            @Override
            public void onError(String thongBao) {
                callback.onError(thongBao);
            }
        });
    }

    private String kiemTraVoucher(Voucher voucher, boolean batBuocMaVoucher) {
        if (voucher == null) {
            return "Thông tin voucher không hợp lệ";
        }
        if (batBuocMaVoucher && voucher.getMaVoucher() <= 0) {
            return "Voucher không hợp lệ";
        }
        if (rong(voucher.getMaGiamGia())) {
            return "Mã giảm giá không được để trống";
        }
        if (rong(voucher.getTenVoucher())) {
            return "Tên voucher không được để trống";
        }
        if (!"PhanTram".equals(voucher.getKieuGiamGia()) && !"TienMat".equals(voucher.getKieuGiamGia())) {
            return "Kiểu giảm giá không hợp lệ";
        }
        if (voucher.getGiaTriGiam() <= 0) {
            return "Giá trị giảm phải lớn hơn 0";
        }
        if ("PhanTram".equals(voucher.getKieuGiamGia()) && voucher.getGiaTriGiam() > 100) {
            return "Giảm theo phần trăm không được vượt quá 100";
        }
        if (voucher.getSoLuong() < 0) {
            return "Số lượng không được âm";
        }
        if (voucher.getDonToiThieu() < 0) {
            return "Đơn tối thiểu không được âm";
        }
        if (voucher.getGiamToiDa() < 0) {
            return "Giảm tối đa không được âm";
        }
        if (voucher.getSoLuongVeToiThieu() < 1) {
            return "Số lượng vé tối thiểu phải từ 1 trở lên";
        }
        if (voucher.getSoLanDungToiDaMoiNguoi() < 0) {
            return "Số lần dùng tối đa mỗi người không được âm";
        }
        if (voucher.getMaLoaiVeApDung() != null && voucher.getMaVeApDung() != null) {
            return "Chỉ được chọn loại vé hoặc vé cụ thể, không được chọn đồng thời";
        }
        if (voucher.getMaLoaiVeApDung() != null && voucher.getMaLoaiVeApDung() <= 0) {
            return "Loại vé áp dụng không hợp lệ";
        }
        if (voucher.getMaVeApDung() != null && voucher.getMaVeApDung() <= 0) {
            return "Vé áp dụng không hợp lệ";
        }
        String phamViApDung = layPhamViApDung(voucher);
        if ("LoaiVe".equals(phamViApDung) && voucher.getMaLoaiVeApDung() == null) {
            return "Vui lòng chọn loại vé áp dụng";
        }
        if ("Ve".equals(phamViApDung) && voucher.getMaVeApDung() == null) {
            return "Vui lòng chọn vé áp dụng";
        }
        if ("TatCa".equals(phamViApDung)
                && (voucher.getMaLoaiVeApDung() != null || voucher.getMaVeApDung() != null)) {
            return "Phạm vi tất cả vé không được kèm vé hoặc loại vé cụ thể";
        }
        if (!"TatCa".equals(phamViApDung)
                && !"LoaiVe".equals(phamViApDung)
                && !"Ve".equals(phamViApDung)) {
            return "Phạm vi áp dụng không hợp lệ";
        }
        if (voucher.getMucTieu() != null
                && !voucher.getMucTieu().isEmpty()
                && voucher.getMucTieu().trim().isEmpty()) {
            return "Mục tiêu voucher không được chỉ chứa khoảng trắng";
        }
        if (!ngayHopLe(voucher.getNgayBatDau()) || !ngayHopLe(voucher.getNgayKetThuc())) {
            return "Ngày không hợp lệ";
        }
        if (voucher.getNgayBatDau().compareTo(voucher.getNgayKetThuc()) > 0) {
            return "Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc";
        }
        return null;
    }

    private Voucher chuanHoaVoucher(Voucher voucher) {
        voucher.setMaGiamGia(voucher.getMaGiamGia().trim().toUpperCase(Locale.ROOT));
        voucher.setTenVoucher(voucher.getTenVoucher().trim());
        voucher.setMucTieu(chuanHoaChuoi(voucher.getMucTieu()));
        voucher.setMoTaDieuKien(chuanHoaChuoi(voucher.getMoTaDieuKien()));
        if (rong(voucher.getTrangThai())) {
            voucher.setTrangThai("HoatDong");
        }
        return voucher;
    }

    private boolean ngayHopLe(String ngay) {
        if (rong(ngay)) {
            return false;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        simpleDateFormat.setLenient(false);
        try {
            simpleDateFormat.parse(ngay);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean rong(String chuoi) {
        return chuoi == null || chuoi.trim().isEmpty();
    }

    private String chuanHoaChuoi(String chuoi) {
        return chuoi == null ? "" : chuoi.trim();
    }

    private String layPhamViApDung(Voucher voucher) {
        if (voucher.getPhamViApDung() != null && !voucher.getPhamViApDung().trim().isEmpty()) {
            return voucher.getPhamViApDung().trim();
        }
        if (voucher.getMaVeApDung() != null) {
            return "Ve";
        }
        if (voucher.getMaLoaiVeApDung() != null) {
            return "LoaiVe";
        }
        return "TatCa";
    }
}
