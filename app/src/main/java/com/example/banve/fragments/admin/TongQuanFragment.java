package com.example.banve.fragments.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.banve.R;
import com.example.banve.controllers.TongQuanController;
import com.example.banve.models.TongQuanQuanLy;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.DinhDangTien;

public class TongQuanFragment extends Fragment {
    private ProgressBar pgbDangTai;
    private TextView lblDoanhThuHomNay;
    private TextView lblChiTietDoanhThuHomNay;
    private TextView lblDoanhThuThangNay;
    private TextView lblChiTietDoanhThuThangNay;
    private TextView lblHoaDonHomNay;
    private TextView lblHoaDonChoThanhToan;
    private TextView lblVeBanHomNay;
    private TextView lblVeBanThangNay;
    private TextView lblVeDangBan;
    private TextView lblVoucherHoatDong;
    private TextView lblKhachHang;
    private TextView lblVeBanChay;
    private TextView lblGoiYNhanh;
    private TongQuanController tongQuanController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_tong_quan, container, false);
        anhXa(view);
        tongQuanController = new TongQuanController();
        taiTongQuan();
        return view;
    }

    private void anhXa(View view) {
        pgbDangTai = view.findViewById(R.id.pgbDangTai);
        lblDoanhThuHomNay = view.findViewById(R.id.lblDoanhThuHomNay);
        lblChiTietDoanhThuHomNay = view.findViewById(R.id.lblChiTietDoanhThuHomNay);
        lblDoanhThuThangNay = view.findViewById(R.id.lblDoanhThuThangNay);
        lblChiTietDoanhThuThangNay = view.findViewById(R.id.lblChiTietDoanhThuThangNay);
        lblHoaDonHomNay = view.findViewById(R.id.lblHoaDonHomNay);
        lblHoaDonChoThanhToan = view.findViewById(R.id.lblHoaDonChoThanhToan);
        lblVeBanHomNay = view.findViewById(R.id.lblVeBanHomNay);
        lblVeBanThangNay = view.findViewById(R.id.lblVeBanThangNay);
        lblVeDangBan = view.findViewById(R.id.lblVeDangBan);
        lblVoucherHoatDong = view.findViewById(R.id.lblVoucherHoatDong);
        lblKhachHang = view.findViewById(R.id.lblKhachHang);
        lblVeBanChay = view.findViewById(R.id.lblVeBanChay);
        lblGoiYNhanh = view.findViewById(R.id.lblGoiYNhanh);
    }

    private void taiTongQuan() {
        hienDangTai(true);
        tongQuanController.layTongQuan(new ApiCallback<TongQuanQuanLy>() {
            @Override
            public void onSuccess(TongQuanQuanLy data) {
                hienDangTai(false);
                hienTongQuan(data);
            }

            @Override
            public void onError(String thongBao) {
                hienDangTai(false);
                hienDuLieuRong();
                baoLoi("Lỗi tổng quan", thongBao);
            }
        });
    }

    private void hienTongQuan(TongQuanQuanLy tongQuan) {
        if (tongQuan == null) {
            hienDuLieuRong();
            return;
        }

        lblDoanhThuHomNay.setText(DinhDangTien.dinhDang(tongQuan.getDoanhThuHomNay()));
        lblChiTietDoanhThuHomNay.setText("Doanh thu gộp: "
                + DinhDangTien.dinhDang(tongQuan.getDoanhThuGopHomNay())
                + "\nTổng giảm giá: "
                + DinhDangTien.dinhDang(tongQuan.getTongGiamGiaHomNay()));

        lblDoanhThuThangNay.setText(DinhDangTien.dinhDang(tongQuan.getDoanhThuThangNay()));
        lblChiTietDoanhThuThangNay.setText("Doanh thu gộp: "
                + DinhDangTien.dinhDang(tongQuan.getDoanhThuGopThangNay())
                + "\nTổng giảm giá: "
                + DinhDangTien.dinhDang(tongQuan.getTongGiamGiaThangNay()));

        lblHoaDonHomNay.setText("Hóa đơn hôm nay\n" + tongQuan.getHoaDonDaThanhToanHomNay());
        lblHoaDonChoThanhToan.setText("Chờ thanh toán\n" + tongQuan.getHoaDonChoThanhToan());
        lblVeBanHomNay.setText("Vé bán hôm nay\n" + tongQuan.getVeBanHomNay());
        lblVeBanThangNay.setText("Vé bán tháng này\n" + tongQuan.getVeBanThangNay());
        lblVeDangBan.setText("Vé đang bán\n" + tongQuan.getSoVeDangBan());
        lblVoucherHoatDong.setText("Voucher hoạt động\n" + tongQuan.getVoucherDangHoatDong());
        lblKhachHang.setText("Khách hàng\n" + tongQuan.getTongKhachHang());
        lblVeBanChay.setText("Bán chạy tháng này\n"
                + chuoiBanChay(tongQuan.getTenVeBanChay(), tongQuan.getSoLuongVeBanChay()));
        lblGoiYNhanh.setText(tongQuan.getGoiYNhanh());
    }

    private void hienDuLieuRong() {
        lblDoanhThuHomNay.setText("0 VNĐ");
        lblChiTietDoanhThuHomNay.setText("Doanh thu gộp: 0 VNĐ\nTổng giảm giá: 0 VNĐ");
        lblDoanhThuThangNay.setText("0 VNĐ");
        lblChiTietDoanhThuThangNay.setText("Doanh thu gộp: 0 VNĐ\nTổng giảm giá: 0 VNĐ");
        lblHoaDonHomNay.setText("Hóa đơn hôm nay\n0");
        lblHoaDonChoThanhToan.setText("Chờ thanh toán\n0");
        lblVeBanHomNay.setText("Vé bán hôm nay\n0");
        lblVeBanThangNay.setText("Vé bán tháng này\n0");
        lblVeDangBan.setText("Vé đang bán\n0");
        lblVoucherHoatDong.setText("Voucher hoạt động\n0");
        lblKhachHang.setText("Khách hàng\n0");
        lblVeBanChay.setText("Bán chạy tháng này\nChưa đủ dữ liệu");
        lblGoiYNhanh.setText("Chưa có dữ liệu tổng quan để hiển thị.");
    }

    private String chuoiBanChay(String tenVeBanChay, int soLuong) {
        if (tenVeBanChay == null || tenVeBanChay.trim().isEmpty() || soLuong <= 0) {
            return "Chưa đủ dữ liệu";
        }
        return tenVeBanChay + " (" + soLuong + " vé)";
    }

    private void hienDangTai(boolean dangTai) {
        pgbDangTai.setVisibility(dangTai ? View.VISIBLE : View.GONE);
    }

    private void baoLoi(String tieuDe, String noiDung) {
        if (getContext() == null) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(tieuDe)
                .setMessage(noiDung)
                .setPositiveButton("Đồng ý", null)
                .show();
    }
}
