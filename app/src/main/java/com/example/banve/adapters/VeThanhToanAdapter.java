package com.example.banve.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.banve.R;
import com.example.banve.models.MucGioHang;
import com.example.banve.utils.DinhDangTien;
import com.example.banve.utils.TienIch;

import java.util.ArrayList;
import java.util.List;

public class VeThanhToanAdapter extends RecyclerView.Adapter<VeThanhToanAdapter.VeThanhToanViewHolder> {
    private final List<MucGioHang> danhSachMuc = new ArrayList<>();

    public void capNhatDuLieu(List<MucGioHang> duLieuMoi) {
        danhSachMuc.clear();
        if (duLieuMoi != null) {
            danhSachMuc.addAll(duLieuMoi);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VeThanhToanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_chi_tiet_gio_hang, parent, false);
        return new VeThanhToanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VeThanhToanViewHolder holder, int position) {
        MucGioHang muc = danhSachMuc.get(position);
        holder.imgAnhVe.setVisibility(View.GONE);
        holder.lblTenVe.setText(muc.getVe().getTenVe());
        holder.lblNgaySuDung.setText(
                "Ngày sử dụng: "
                        + TienIch.dinhDangNgay(muc.getChiTietGioHang().getNgaySuDung())
        );
        holder.lblSoLuong.setText(
                "NL: " + muc.getChiTietGioHang().getSoLuongNguoiLon()
                        + " | TE: " + muc.getChiTietGioHang().getSoLuongTreEm()
                        + " | CT: " + muc.getChiTietGioHang().getSoLuongNguoiCaoTuoi()
        );
        holder.lblThanhTien.setText("Thành tiền: " + DinhDangTien.dinhDang(muc.tinhThanhTien()));
        holder.btnSua.setVisibility(View.GONE);
        holder.btnXoa.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return danhSachMuc.size();
    }

    static class VeThanhToanViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgAnhVe;
        private final TextView lblTenVe;
        private final TextView lblNgaySuDung;
        private final TextView lblSoLuong;
        private final TextView lblThanhTien;
        private final Button btnSua;
        private final Button btnXoa;

        public VeThanhToanViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAnhVe = itemView.findViewById(R.id.imgAnhVe);
            lblTenVe = itemView.findViewById(R.id.lblTenVe);
            lblNgaySuDung = itemView.findViewById(R.id.lblNgaySuDung);
            lblSoLuong = itemView.findViewById(R.id.lblSoLuong);
            lblThanhTien = itemView.findViewById(R.id.lblThanhTien);
            btnSua = itemView.findViewById(R.id.btnSua);
            btnXoa = itemView.findViewById(R.id.btnXoa);
        }
    }
}
