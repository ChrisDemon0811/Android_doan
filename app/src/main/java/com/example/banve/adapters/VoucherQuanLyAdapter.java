package com.example.banve.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.banve.R;
import com.example.banve.models.LoaiVe;
import com.example.banve.models.Ve;
import com.example.banve.models.Voucher;
import com.example.banve.utils.DinhDangTien;
import com.example.banve.utils.HienThi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VoucherQuanLyAdapter extends RecyclerView.Adapter<VoucherQuanLyAdapter.VoucherQuanLyViewHolder> {
    public interface OnVoucherQuanLyClickListener {
        void onSua(Voucher voucher);

        void onXoa(Voucher voucher);
    }

    private final List<Voucher> danhSachVoucher = new ArrayList<>();
    private final Map<Integer, String> tenLoaiVeTheoMa = new HashMap<>();
    private final Map<Integer, String> tenVeTheoMa = new HashMap<>();
    private final OnVoucherQuanLyClickListener listener;

    public VoucherQuanLyAdapter(OnVoucherQuanLyClickListener listener) {
        this.listener = listener;
    }

    public void capNhatDuLieu(List<Voucher> duLieuMoi) {
        danhSachVoucher.clear();
        if (duLieuMoi != null) {
            danhSachVoucher.addAll(duLieuMoi);
        }
        notifyDataSetChanged();
    }

    public void capNhatPhamVi(List<LoaiVe> danhSachLoaiVe, List<Ve> danhSachVe) {
        tenLoaiVeTheoMa.clear();
        tenVeTheoMa.clear();
        if (danhSachLoaiVe != null) {
            for (LoaiVe loaiVe : danhSachLoaiVe) {
                tenLoaiVeTheoMa.put(loaiVe.getMaLoaiVe(), loaiVe.getTenLoaiVe());
            }
        }
        if (danhSachVe != null) {
            for (Ve ve : danhSachVe) {
                tenVeTheoMa.put(ve.getMaVe(), ve.getTenVe());
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VoucherQuanLyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_item_voucher_quan_ly, parent, false);
        return new VoucherQuanLyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherQuanLyViewHolder holder, int position) {
        Voucher voucher = danhSachVoucher.get(position);
        holder.lblMaGiamGia.setText(voucher.getMaGiamGia());
        holder.lblTenVoucher.setText(voucher.getTenVoucher());
        holder.lblGiaTri.setText("Giá trị: " + hienThiGiaTri(voucher));
        holder.lblGiamToiDa.setText("Giảm tối đa: " + hienThiKhongGioiHan(voucher.getGiamToiDa()));
        holder.lblDonToiThieu.setText("Đơn tối thiểu: " + hienThiKhongGioiHan(voucher.getDonToiThieu()));
        holder.lblSoLuongVeToiThieu.setText("Số lượng vé tối thiểu: " + Math.max(1, voucher.getSoLuongVeToiThieu()));
        holder.lblPhamVi.setText("Phạm vi: " + hienThiPhamVi(voucher));
        holder.lblGioiHanMoiNguoi.setText("Giới hạn mỗi người: " + hienThiGioiHanMoiNguoi(voucher));
        holder.lblDoiTuong.setText("Đối tượng: " + (voucher.isChiApDungKhachMoi() ? "Khách hàng mới" : "Tất cả khách hàng"));
        holder.lblThoiHan.setText("Thời hạn: " + dinhDangNgay(voucher.getNgayBatDau()) + " - " + dinhDangNgay(voucher.getNgayKetThuc()));
        holder.lblSoLuong.setText("Số lượng còn lại: " + voucher.getSoLuong());
        capNhatTrangThai(holder, voucher);
        holder.btnSua.setOnClickListener(v -> listener.onSua(voucher));
        holder.btnXoa.setOnClickListener(v -> listener.onXoa(voucher));
    }

    @Override
    public int getItemCount() {
        return danhSachVoucher.size();
    }

    private void capNhatTrangThai(VoucherQuanLyViewHolder holder, Voucher voucher) {
        boolean hoatDong = "HoatDong".equals(voucher.getTrangThai());
        holder.lblTrangThai.setText(HienThi.trangThai(voucher.getTrangThai()));
        holder.lblTrangThai.setBackgroundResource(hoatDong ? R.drawable.bg_badge_success : R.drawable.bg_badge_warning);
        int mau = hoatDong ? R.color.mauThanhCong : R.color.mauCanhBao;
        holder.lblTrangThai.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), mau));
    }

    private String hienThiGiaTri(Voucher voucher) {
        if ("PhanTram".equals(voucher.getKieuGiamGia())) {
            return String.format(Locale.getDefault(), "%.0f%%", voucher.getGiaTriGiam());
        }
        return DinhDangTien.dinhDang(voucher.getGiaTriGiam());
    }

    private String hienThiKhongGioiHan(double giaTri) {
        return giaTri > 0 ? DinhDangTien.dinhDang(giaTri) : "Không giới hạn";
    }

    private String hienThiGioiHanMoiNguoi(Voucher voucher) {
        return voucher.getSoLanDungToiDaMoiNguoi() > 0
                ? voucher.getSoLanDungToiDaMoiNguoi() + " lần"
                : "Không giới hạn";
    }

    private String hienThiPhamVi(Voucher voucher) {
        if (voucher.getMaVeApDung() != null) {
            String tenVe = tenVeTheoMa.get(voucher.getMaVeApDung());
            return tenVe == null ? "Vé mã " + voucher.getMaVeApDung() : "Vé " + tenVe;
        }
        if (voucher.getMaLoaiVeApDung() != null) {
            String tenLoaiVe = tenLoaiVeTheoMa.get(voucher.getMaLoaiVeApDung());
            return tenLoaiVe == null ? "Loại vé mã " + voucher.getMaLoaiVeApDung() : "Loại vé " + tenLoaiVe;
        }
        return "Tất cả vé";
    }

    private String dinhDangNgay(String ngay) {
        if (ngay == null || ngay.trim().isEmpty()) {
            return "";
        }
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(ngay);
            return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
        } catch (ParseException e) {
            return ngay;
        }
    }

    static class VoucherQuanLyViewHolder extends RecyclerView.ViewHolder {
        private final TextView lblMaGiamGia;
        private final TextView lblTenVoucher;
        private final TextView lblGiaTri;
        private final TextView lblGiamToiDa;
        private final TextView lblDonToiThieu;
        private final TextView lblSoLuongVeToiThieu;
        private final TextView lblPhamVi;
        private final TextView lblGioiHanMoiNguoi;
        private final TextView lblDoiTuong;
        private final TextView lblThoiHan;
        private final TextView lblSoLuong;
        private final TextView lblTrangThai;
        private final Button btnSua;
        private final Button btnXoa;

        private VoucherQuanLyViewHolder(@NonNull View itemView) {
            super(itemView);
            lblMaGiamGia = itemView.findViewById(R.id.lblMaGiamGia);
            lblTenVoucher = itemView.findViewById(R.id.lblTenVoucher);
            lblGiaTri = itemView.findViewById(R.id.lblGiaTri);
            lblGiamToiDa = itemView.findViewById(R.id.lblGiamToiDa);
            lblDonToiThieu = itemView.findViewById(R.id.lblDonToiThieu);
            lblSoLuongVeToiThieu = itemView.findViewById(R.id.lblSoLuongVeToiThieu);
            lblPhamVi = itemView.findViewById(R.id.lblPhamVi);
            lblGioiHanMoiNguoi = itemView.findViewById(R.id.lblGioiHanMoiNguoi);
            lblDoiTuong = itemView.findViewById(R.id.lblDoiTuong);
            lblThoiHan = itemView.findViewById(R.id.lblThoiHan);
            lblSoLuong = itemView.findViewById(R.id.lblSoLuong);
            lblTrangThai = itemView.findViewById(R.id.lblTrangThai);
            btnSua = itemView.findViewById(R.id.btnSua);
            btnXoa = itemView.findViewById(R.id.btnXoa);
        }
    }
}
