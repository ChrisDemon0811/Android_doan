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
import com.example.banve.utils.DinhDangTien;
import com.example.banve.utils.HienThi;
import com.example.banve.utils.TienIch;

import java.util.ArrayList;
import java.util.List;

public class HoaDonAdapter extends RecyclerView.Adapter<HoaDonAdapter.HoaDonViewHolder> {
    public interface OnHoaDonClickListener {
        void onXemChiTiet(HoaDon hoaDon);
    }

    private final List<HoaDon> danhSachHoaDon = new ArrayList<>();
    private final OnHoaDonClickListener listener;

    public HoaDonAdapter(OnHoaDonClickListener listener) {
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
    public HoaDonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_hoa_don, parent, false);
        return new HoaDonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HoaDonViewHolder holder, int position) {
        HoaDon hoaDon = danhSachHoaDon.get(position);
        holder.lblMaHoaDon.setText("Mã hóa đơn: " + hoaDon.getMaHoaDon());
        holder.lblNgayLap.setText("Ngày lập: " + TienIch.dinhDangNgayGio(hoaDon.getNgayLap()));
        holder.lblTongTien.setText("Tổng tiền: " + DinhDangTien.dinhDang(hoaDon.getTongTien()));
        holder.lblTrangThai.setText("Trạng thái: " + HienThi.trangThai(hoaDon.getTrangThai()));
        holder.btnXemChiTiet.setOnClickListener(v -> listener.onXemChiTiet(hoaDon));
    }

    @Override
    public int getItemCount() {
        return danhSachHoaDon.size();
    }

    static class HoaDonViewHolder extends RecyclerView.ViewHolder {
        private final TextView lblMaHoaDon;
        private final TextView lblNgayLap;
        private final TextView lblTongTien;
        private final TextView lblTrangThai;
        private final Button btnXemChiTiet;

        public HoaDonViewHolder(@NonNull View itemView) {
            super(itemView);
            lblMaHoaDon = itemView.findViewById(R.id.lblMaHoaDon);
            lblNgayLap = itemView.findViewById(R.id.lblNgayLap);
            lblTongTien = itemView.findViewById(R.id.lblTongTien);
            lblTrangThai = itemView.findViewById(R.id.lblTrangThai);
            btnXemChiTiet = itemView.findViewById(R.id.btnXemChiTiet);
        }
    }
}
