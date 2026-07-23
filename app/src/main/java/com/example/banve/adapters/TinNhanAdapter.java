package com.example.banve.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.banve.R;
import com.example.banve.models.DeXuatThemGioHang;
import com.example.banve.models.LichSuChat;

import java.util.ArrayList;
import java.util.List;

public class TinNhanAdapter extends RecyclerView.Adapter<TinNhanAdapter.TinNhanViewHolder> {
    public interface OnThemVaoGioListener {
        void onThemVaoGio(int viTri, DeXuatThemGioHang deXuat);
    }

    private static final int LOAI_USER = 1;
    private static final int LOAI_AI = 2;
    private final List<TinNhan> danhSachTinNhan = new ArrayList<>();
    private final OnThemVaoGioListener listener;

    public TinNhanAdapter(OnThemVaoGioListener listener) {
        this.listener = listener;
    }

    public void capNhatTuLichSu(List<LichSuChat> danhSachLichSu) {
        danhSachTinNhan.clear();
        if (danhSachLichSu != null) {
            for (LichSuChat lichSuChat : danhSachLichSu) {
                if (!rong(lichSuChat.getCauHoi())) {
                    danhSachTinNhan.add(new TinNhan(lichSuChat.getCauHoi(), LOAI_USER));
                }
                if (!rong(lichSuChat.getTraLoi())) {
                    danhSachTinNhan.add(new TinNhan(lichSuChat.getTraLoi(), LOAI_AI));
                }
            }
        }
        notifyDataSetChanged();
    }

    public void themTinNhanUser(String noiDung) {
        themTinNhan(noiDung, LOAI_USER);
    }

    public void themTinNhanAI(String noiDung) {
        themTinNhanAI(noiDung, null);
    }

    public void themTinNhanAI(String noiDung, DeXuatThemGioHang deXuatThemGioHang) {
        danhSachTinNhan.add(new TinNhan(noiDung, LOAI_AI, deXuatThemGioHang));
        notifyItemInserted(danhSachTinNhan.size() - 1);
    }

    public void capNhatTrangThaiThemGio(int viTri, boolean dangXuLy, boolean daThem) {
        if (viTri < 0 || viTri >= danhSachTinNhan.size()) {
            return;
        }
        TinNhan tinNhan = danhSachTinNhan.get(viTri);
        tinNhan.dangXuLy = dangXuLy;
        tinNhan.daThem = daThem;
        notifyItemChanged(viTri);
    }

    private void themTinNhan(String noiDung, int loaiTinNhan) {
        danhSachTinNhan.add(new TinNhan(noiDung, loaiTinNhan));
        notifyItemInserted(danhSachTinNhan.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        return danhSachTinNhan.get(position).loaiTinNhan;
    }

    @NonNull
    @Override
    public TinNhanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = viewType == LOAI_USER ? R.layout.user_item_tin_nhan_user : R.layout.user_item_tin_nhan_ai;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new TinNhanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TinNhanViewHolder holder, int position) {
        TinNhan tinNhan = danhSachTinNhan.get(position);
        holder.lblNoiDungTinNhan.setText(tinNhan.noiDung);
        if (holder.btnThemVaoGio == null) {
            return;
        }

        boolean coDeXuat = tinNhan.loaiTinNhan == LOAI_AI && tinNhan.deXuatThemGioHang != null;
        holder.btnThemVaoGio.setVisibility(coDeXuat ? View.VISIBLE : View.GONE);
        holder.btnThemVaoGio.setEnabled(coDeXuat && !tinNhan.dangXuLy && !tinNhan.daThem);
        holder.btnThemVaoGio.setText(tinNhan.daThem
                ? "Đã thêm vào giỏ"
                : tinNhan.dangXuLy ? "Đang thêm..." : "Thêm vào giỏ");
        holder.btnThemVaoGio.setOnClickListener(null);
        if (coDeXuat && !tinNhan.daThem) {
            holder.btnThemVaoGio.setOnClickListener(view -> {
                int viTriHienTai = holder.getBindingAdapterPosition();
                if (viTriHienTai != RecyclerView.NO_POSITION && listener != null) {
                    listener.onThemVaoGio(viTriHienTai, danhSachTinNhan.get(viTriHienTai).deXuatThemGioHang);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return danhSachTinNhan.size();
    }

    private boolean rong(String chuoi) {
        return chuoi == null || chuoi.trim().isEmpty();
    }

    static class TinNhanViewHolder extends RecyclerView.ViewHolder {
        private final TextView lblNoiDungTinNhan;
        private final Button btnThemVaoGio;

        public TinNhanViewHolder(@NonNull View itemView) {
            super(itemView);
            lblNoiDungTinNhan = itemView.findViewById(R.id.lblNoiDungTinNhan);
            btnThemVaoGio = itemView.findViewById(R.id.btnThemVaoGio);
        }
    }

    static class TinNhan {
        private final String noiDung;
        private final int loaiTinNhan;
        private final DeXuatThemGioHang deXuatThemGioHang;
        private boolean daThem;
        private boolean dangXuLy;

        public TinNhan(String noiDung, int loaiTinNhan) {
            this(noiDung, loaiTinNhan, null);
        }

        public TinNhan(String noiDung, int loaiTinNhan, DeXuatThemGioHang deXuatThemGioHang) {
            this.noiDung = noiDung;
            this.loaiTinNhan = loaiTinNhan;
            this.deXuatThemGioHang = deXuatThemGioHang;
        }
    }
}

