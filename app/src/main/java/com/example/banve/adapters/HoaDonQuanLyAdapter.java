package com.example.banve.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.banve.R;
import com.example.banve.models.HoaDon;
import com.example.banve.models.NguoiDung;
import com.example.banve.utils.DinhDangTien;
import com.example.banve.utils.HienThi;
import com.example.banve.utils.TienIch;

import java.util.ArrayList;
import java.util.List;

public class HoaDonQuanLyAdapter extends RecyclerView.Adapter<HoaDonQuanLyAdapter.HoaDonQuanLyViewHolder> {
    public interface OnHoaDonQuanLyClickListener {
        void onXemChiTiet(HoaDon hoaDon);
    }

    private final List<HoaDon> danhSachHoaDon = new ArrayList<>();
    private final OnHoaDonQuanLyClickListener listener;

    public HoaDonQuanLyAdapter(OnHoaDonQuanLyClickListener listener) {
        this.listener = listener;
    }

    public void capNhatDuLieu(List<HoaDon> duLieuMoi) {
        danhSachHoaDon.clear();
        if (duLieuMoi != null) {
            danhSachHoaDon.addAll(duLieuMoi);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HoaDonQuanLyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_item_hoa_don_quan_ly, parent, false);
        return new HoaDonQuanLyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HoaDonQuanLyViewHolder holder, int position) {
        HoaDon hoaDon = danhSachHoaDon.get(position);
        holder.lblMaHoaDon.setText("Mã hóa đơn: " + hoaDon.getMaHoaDon());
        holder.lblHoTenKhach.setText("Khách hàng: " + layHoTenKhach(hoaDon));
        holder.lblNgayLap.setText("Ngày lập: " + TienIch.dinhDangNgayGio(hoaDon.getNgayLap()));
        holder.lblTongTien.setText("Tổng tiền: " + DinhDangTien.dinhDang(hoaDon.getTongTien()));
        holder.lblHinhThuc.setText("Hình thức: " + HienThi.hinhThucThanhToan(hoaDon.getThanhToan()));
        holder.btnXemChiTiet.setOnClickListener(v -> listener.onXemChiTiet(hoaDon));
    }

    @Override
    public int getItemCount() {
        return danhSachHoaDon.size();
    }

    private String layHoTenKhach(HoaDon hoaDon) {
        NguoiDung nguoiDung = hoaDon.getNguoiDung();
        if (nguoiDung == null || nguoiDung.getHoTen() == null || nguoiDung.getHoTen().trim().isEmpty()) {
            return "Mã người dùng " + hoaDon.getMaNguoiDung();
        }
        return nguoiDung.getHoTen();
    }

    private String giaTri(String giaTri) {
        return giaTri == null ? "" : giaTri;
    }

    private String hienThiHinhThucThanhToan(String thanhToan) {
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

    static class HoaDonQuanLyViewHolder extends RecyclerView.ViewHolder {
        private final TextView lblMaHoaDon;
        private final TextView lblHoTenKhach;
        private final TextView lblNgayLap;
        private final TextView lblTongTien;
        private final TextView lblHinhThuc;
        private final Button btnXemChiTiet;

        public HoaDonQuanLyViewHolder(@NonNull View itemView) {
            super(itemView);
            lblMaHoaDon = itemView.findViewById(R.id.lblMaHoaDon);
            lblHoTenKhach = itemView.findViewById(R.id.lblHoTenKhach);
            lblNgayLap = itemView.findViewById(R.id.lblNgayLap);
            lblTongTien = itemView.findViewById(R.id.lblTongTien);
            lblHinhThuc = itemView.findViewById(R.id.lblHinhThuc);
            btnXemChiTiet = itemView.findViewById(R.id.btnXemChiTiet);
        }
    }
}
