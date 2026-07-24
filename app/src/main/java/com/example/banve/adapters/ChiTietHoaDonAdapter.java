package com.example.banve.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.banve.R;
import com.example.banve.models.ChiTietHoaDon;
import com.example.banve.models.Ve;
import com.example.banve.utils.BoNhoAnh;
import com.example.banve.utils.DinhDangTien;
import com.example.banve.utils.TienIch;

import java.util.ArrayList;
import java.util.List;

public class ChiTietHoaDonAdapter extends RecyclerView.Adapter<ChiTietHoaDonAdapter.ChiTietHoaDonViewHolder> {
    private final List<ChiTietHoaDon> danhSachChiTiet = new ArrayList<>();

    public void capNhatDuLieu(List<ChiTietHoaDon> duLieuMoi) {
        danhSachChiTiet.clear();
        if (duLieuMoi != null) {
            danhSachChiTiet.addAll(duLieuMoi);
        }
        notifyDataSetChanged();
    }

    public static void hienThiChiTiet(View view, ChiTietHoaDon chiTiet) {
        ImageView imgAnhVe = view.findViewById(R.id.imgAnhVe);
        TextView lblTenVe = view.findViewById(R.id.lblTenVe);
        TextView lblNgaySuDung = view.findViewById(R.id.lblNgaySuDung);
        TextView lblSoLuong = view.findViewById(R.id.lblSoLuong);
        TextView lblDonGia = view.findViewById(R.id.lblDonGia);
        TextView lblThanhTien = view.findViewById(R.id.lblThanhTien);

        Ve ve = chiTiet.getVe();
        String tenVe = ve == null ? "Vé mã " + chiTiet.getMaVe() : ve.getTenVe();

        lblTenVe.setText(tenVe);
        lblNgaySuDung.setText(
                "Ngày sử dụng: " + TienIch.dinhDangNgay(chiTiet.getNgaySuDung())
        );
        lblSoLuong.setText(
                "Số lượng: Người lớn " + chiTiet.getSoLuongNguoiLon()
                        + " • Trẻ em " + chiTiet.getSoLuongTreEm()
                        + " • Người cao tuổi " + chiTiet.getSoLuongNguoiCaoTuoi()
        );
        lblDonGia.setText(
                "Đơn giá: Người lớn " + DinhDangTien.dinhDang(chiTiet.getDonGiaNguoiLon())
                        + " • Trẻ em " + DinhDangTien.dinhDang(chiTiet.getDonGiaTreEm())
                        + " • Người cao tuổi " + DinhDangTien.dinhDang(chiTiet.getDonGiaNguoiCaoTuoi())
        );
        lblThanhTien.setText("Thành tiền: " + DinhDangTien.dinhDang(chiTiet.getThanhTien()));
        BoNhoAnh.taiAnh(ve == null ? "" : ve.getAnhVe(), imgAnhVe, R.mipmap.ic_launcher);
    }

    @NonNull
    @Override
    public ChiTietHoaDonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_chi_tiet_hoa_don, parent, false);
        return new ChiTietHoaDonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChiTietHoaDonViewHolder holder, int position) {
        hienThiChiTiet(holder.itemView, danhSachChiTiet.get(position));
    }

    @Override
    public int getItemCount() {
        return danhSachChiTiet.size();
    }

    static class ChiTietHoaDonViewHolder extends RecyclerView.ViewHolder {
        public ChiTietHoaDonViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
