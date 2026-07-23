package com.example.banve.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.banve.R;
import com.example.banve.models.KetQuaKiemTraVoucher;
import com.example.banve.models.Voucher;
import com.example.banve.utils.DinhDangTien;

import java.util.ArrayList;
import java.util.List;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {
    public interface OnVoucherClickListener {
        void onClick(KetQuaKiemTraVoucher ketQua);
    }

    private final List<KetQuaKiemTraVoucher> danhSachVoucher = new ArrayList<>();
    private final OnVoucherClickListener listener;
    private int maVoucherDangChon = -1;

    public VoucherAdapter(OnVoucherClickListener listener) {
        this.listener = listener;
    }

    public void capNhatDuLieu(List<KetQuaKiemTraVoucher> duLieuMoi) {
        danhSachVoucher.clear();
        if (duLieuMoi != null) {
            danhSachVoucher.addAll(duLieuMoi);
        }
        notifyDataSetChanged();
    }

    public void chonVoucher(Voucher voucher) {
        maVoucherDangChon = voucher == null ? -1 : voucher.getMaVoucher();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        KetQuaKiemTraVoucher ketQua = danhSachVoucher.get(position);
        Voucher voucher = ketQua.getVoucher();
        holder.lblTenVoucher.setText(voucher.getTenVoucher());
        holder.lblMaGiamGia.setText("Mã: " + voucher.getMaGiamGia());
        holder.lblMoTaGiam.setText(taoMoTaGiam(voucher));
        holder.lblDieuKienVoucher.setText(taoMoTaDieuKien(voucher, ketQua));
        holder.lblTienGiamThucTe.setText(ketQua.isHopLe()
                ? "Giảm thực tế: " + DinhDangTien.dinhDang(ketQua.getTienGiam())
                : "Chưa thể áp dụng cho giỏ hàng hiện tại");
        holder.lblLyDoVoucher.setText(ketQua.getLyDo());
        holder.lblTrangThaiVoucher.setText(ketQua.isHopLe() ? "Đủ điều kiện" : "Chưa đủ điều kiện");
        holder.lblTrangThaiVoucher.setBackgroundResource(
                ketQua.isHopLe() ? R.drawable.bg_badge_success : R.drawable.bg_badge_warning
        );
        holder.lblTrangThaiVoucher.setTextColor(ContextCompat.getColor(
                holder.itemView.getContext(),
                ketQua.isHopLe() ? R.color.mauThanhCong : R.color.mauNguyHiem
        ));
        holder.lblLyDoVoucher.setTextColor(ContextCompat.getColor(
                holder.itemView.getContext(),
                ketQua.isHopLe() ? R.color.mauThanhCong : R.color.mauNguyHiem
        ));
        holder.itemView.setBackgroundResource(voucher.getMaVoucher() == maVoucherDangChon
                ? R.drawable.bg_item_highlight : R.drawable.bg_card_accent);
        holder.itemView.setAlpha(ketQua.isHopLe() ? 1f : 0.58f);
        holder.itemView.setEnabled(ketQua.isHopLe());
        holder.itemView.setOnClickListener(null);
        if (ketQua.isHopLe()) {
            holder.itemView.setOnClickListener(view -> listener.onClick(ketQua));
        }
    }

    @Override
    public int getItemCount() {
        return danhSachVoucher.size();
    }

    private String taoMoTaGiam(Voucher voucher) {
        if ("PhanTram".equals(voucher.getKieuGiamGia())) {
            return "Giảm " + String.format("%.0f", voucher.getGiaTriGiam()) + "%";
        }
        return "Giảm " + DinhDangTien.dinhDang(voucher.getGiaTriGiam());
    }

    private String taoMoTaDieuKien(Voucher voucher, KetQuaKiemTraVoucher ketQua) {
        String donToiThieu = voucher.getDonToiThieu() > 0
                ? DinhDangTien.dinhDang(voucher.getDonToiThieu()) : "Không giới hạn";
        return "Đơn tối thiểu: " + donToiThieu
                + " • Tối thiểu " + Math.max(1, voucher.getSoLuongVeToiThieu()) + " vé"
                + "\nPhạm vi: " + ketQua.getPhamViApDung();
    }

    static class VoucherViewHolder extends RecyclerView.ViewHolder {
        private final TextView lblTenVoucher;
        private final TextView lblMaGiamGia;
        private final TextView lblMoTaGiam;
        private final TextView lblDieuKienVoucher;
        private final TextView lblTienGiamThucTe;
        private final TextView lblLyDoVoucher;
        private final TextView lblTrangThaiVoucher;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            lblTenVoucher = itemView.findViewById(R.id.lblTenVoucher);
            lblMaGiamGia = itemView.findViewById(R.id.lblMaGiamGia);
            lblMoTaGiam = itemView.findViewById(R.id.lblMoTaGiam);
            lblDieuKienVoucher = itemView.findViewById(R.id.lblDieuKienVoucher);
            lblTienGiamThucTe = itemView.findViewById(R.id.lblTienGiamThucTe);
            lblLyDoVoucher = itemView.findViewById(R.id.lblLyDoVoucher);
            lblTrangThaiVoucher = itemView.findViewById(R.id.lblTrangThaiVoucher);
        }
    }
}
