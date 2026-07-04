package com.example.banve.activities.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.banve.R;
import com.example.banve.adapters.HoaDonAdapter;
import com.example.banve.controllers.HoaDonController;
import com.example.banve.models.HoaDon;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.Session;
import com.example.banve.utils.TienIch;

import java.util.List;

public class LichSuDonHangActivity extends AppCompatActivity {
    private RecyclerView rcvLichSuDonHang;
    private TextView lblTrong;
    private HoaDonAdapter hoaDonAdapter;
    private HoaDonController hoaDonController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Session.khoiPhuc(this);
        setContentView(R.layout.user_activity_lich_su_don_hang);

        anhXa();
        hoaDonController = new HoaDonController();
        khoiTaoRecyclerView();
        taiLichSuDonHang();
    }

    private void anhXa() {
        rcvLichSuDonHang = findViewById(R.id.rcvLichSuDonHang);
        lblTrong = findViewById(R.id.lblTrong);
        findViewById(R.id.btnQuayLai).setOnClickListener(v -> finish());
    }

    private void khoiTaoRecyclerView() {
        hoaDonAdapter = new HoaDonAdapter(this::moChiTietHoaDon);
        rcvLichSuDonHang.setLayoutManager(new LinearLayoutManager(this));
        rcvLichSuDonHang.setAdapter(hoaDonAdapter);
    }

    private void taiLichSuDonHang() {
        if (!Session.dangDangNhap()) {
            TienIch.hienAlert(this, "Thông báo", "Vui lòng đăng nhập lại");
            finish();
            return;
        }

        hoaDonController.layDanhSachDaThanhToan(Session.nguoiDungHienTai.getMaNguoiDung(), new ApiCallback<List<HoaDon>>() {
            @Override
            public void onSuccess(List<HoaDon> data) {
                hoaDonAdapter.capNhatDuLieu(data);
                boolean trong = data == null || data.isEmpty();
                lblTrong.setVisibility(trong ? View.VISIBLE : View.GONE);
                rcvLichSuDonHang.setVisibility(trong ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onError(String thongBao) {
                TienIch.hienAlert(LichSuDonHangActivity.this, "Lỗi lịch sử", thongBao);
            }
        });
    }

    private void moChiTietHoaDon(HoaDon hoaDon) {
        Intent intent = new Intent(this, ChiTietHoaDonActivity.class);
        intent.putExtra("maHoaDon", hoaDon.getMaHoaDon());
        intent.putExtra("ngayLap", hoaDon.getNgayLap());
        intent.putExtra("tongTien", hoaDon.getTongTien());
        intent.putExtra("tienGiam", hoaDon.getTienGiam());
        intent.putExtra("thanhToan", hoaDon.getThanhToan());
        startActivity(intent);
    }
}

