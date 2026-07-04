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
import com.example.banve.models.LoaiVe;
import com.example.banve.models.Ve;
import com.example.banve.utils.BoNhoAnh;
import com.example.banve.utils.DinhDangTien;
import com.example.banve.utils.HienThi;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VeQuanLyAdapter extends RecyclerView.Adapter<VeQuanLyAdapter.VeQuanLyViewHolder> {
    public interface OnVeQuanLyClickListener {
        void onSua(Ve ve);

        void onXoa(Ve ve);
    }

    private final List<Ve> danhSachGoc = new ArrayList<>();
    private final List<Ve> danhSachHienThi = new ArrayList<>();
    private final OnVeQuanLyClickListener listener;
    private String tuKhoa = "";
    private Integer maLoaiVeDangLoc;

    public VeQuanLyAdapter(OnVeQuanLyClickListener listener) {
        this.listener = listener;
    }

    public void capNhatDuLieu(List<Ve> duLieuMoi) {
        danhSachGoc.clear();
        if (duLieuMoi != null) {
            danhSachGoc.addAll(duLieuMoi);
        }
        locDuLieu(tuKhoa, maLoaiVeDangLoc);
    }

    public void locDuLieu(String tuKhoaMoi, Integer maLoaiVe) {
        tuKhoa = tuKhoaMoi == null ? "" : tuKhoaMoi.trim().toLowerCase(Locale.ROOT);
        maLoaiVeDangLoc = maLoaiVe;
        danhSachHienThi.clear();

        for (Ve ve : danhSachGoc) {
            boolean dungTen = tuKhoa.isEmpty()
                    || (ve.getTenVe() != null && ve.getTenVe().toLowerCase(Locale.ROOT).contains(tuKhoa));
            boolean dungLoai = maLoaiVeDangLoc == null || ve.getMaLoaiVe() == maLoaiVeDangLoc;
            if (dungTen && dungLoai) {
                danhSachHienThi.add(ve);
            }
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VeQuanLyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_item_ve_quan_ly, parent, false);
        return new VeQuanLyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VeQuanLyViewHolder holder, int position) {
        Ve ve = danhSachHienThi.get(position);
        holder.lblTenVe.setText(ve.getTenVe());
        holder.lblLoaiVe.setText("Loại vé: " + layTenLoaiVe(ve));
        holder.lblGiaVe.setText("Giá vé: " + DinhDangTien.dinhDang(ve.getGiaVe()));
        holder.lblSoLuong.setText("Số lượng: " + ve.getSoLuong());
        holder.lblTrangThai.setText("Trạng thái: " + HienThi.trangThai(ve.getTrangThai()));
        BoNhoAnh.taiAnh(ve.getAnhVe(), holder.imgAnhVe, R.mipmap.ic_launcher);
        holder.btnSua.setOnClickListener(v -> listener.onSua(ve));
        holder.btnXoa.setOnClickListener(v -> listener.onXoa(ve));
    }

    @Override
    public int getItemCount() {
        return danhSachHienThi.size();
    }

    private String layTenLoaiVe(Ve ve) {
        LoaiVe loaiVe = ve.getLoaiVe();
        if (loaiVe != null && loaiVe.getTenLoaiVe() != null) {
            return loaiVe.getTenLoaiVe();
        }
        return "Mã " + ve.getMaLoaiVe();
    }

    static class VeQuanLyViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgAnhVe;
        private final TextView lblTenVe;
        private final TextView lblLoaiVe;
        private final TextView lblGiaVe;
        private final TextView lblSoLuong;
        private final TextView lblTrangThai;
        private final Button btnSua;
        private final Button btnXoa;

        public VeQuanLyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAnhVe = itemView.findViewById(R.id.imgAnhVe);
            lblTenVe = itemView.findViewById(R.id.lblTenVe);
            lblLoaiVe = itemView.findViewById(R.id.lblLoaiVe);
            lblGiaVe = itemView.findViewById(R.id.lblGiaVe);
            lblSoLuong = itemView.findViewById(R.id.lblSoLuong);
            lblTrangThai = itemView.findViewById(R.id.lblTrangThai);
            btnSua = itemView.findViewById(R.id.btnSua);
            btnXoa = itemView.findViewById(R.id.btnXoa);
        }
    }
}
