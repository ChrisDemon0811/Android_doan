package com.example.banve.activities.admin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.banve.R;
import com.example.banve.fragments.admin.PhanLoaiVeFragment;
import com.example.banve.fragments.admin.QuanLyHoaDonFragment;
import com.example.banve.fragments.admin.QuanLyNguoiDungFragment;
import com.example.banve.fragments.admin.QuanLyVeFragment;
import com.example.banve.fragments.admin.QuanLyVoucherFragment;
import com.example.banve.fragments.admin.ThongKeFragment;
import com.example.banve.fragments.admin.TongQuanFragment;
import com.example.banve.utils.Session;
import com.example.banve.utils.TienIch;

public class DashboardQuanLyActivity extends AppCompatActivity {
    private TextView lblXinChaoAdmin;
    private TextView lblManHinhDangChon;
    private FrameLayout layMenuOverlay;
    private LinearLayout layMenuQuanLy;
    private Button btnMoMenu;
    private Button btnDangXuat;
    private Button btnTongQuan;
    private Button btnThongKe;
    private Button btnQuanLyVe;
    private Button btnLoaiVe;
    private Button btnVoucher;
    private Button btnNguoiDung;
    private Button btnHoaDon;
    private Button btnCauHinhAI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Session.khoiPhuc(this);

        if (!Session.laQuanLy()) {
            Toast.makeText(this, "Bạn không có quyền", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.admin_activity_dashboard_quan_ly);
        anhXa();
        hienXinChao();
        batSuKien();

        if (savedInstanceState == null) {
            chonFragment("Tổng quan", new TongQuanFragment(), btnTongQuan);
        }
    }

    private void anhXa() {
        lblXinChaoAdmin = findViewById(R.id.lblXinChaoAdmin);
        lblManHinhDangChon = findViewById(R.id.lblManHinhDangChon);
        layMenuOverlay = findViewById(R.id.layMenuOverlay);
        layMenuQuanLy = findViewById(R.id.layMenuQuanLy);
        btnMoMenu = findViewById(R.id.btnMoMenu);
        btnDangXuat = findViewById(R.id.btnDangXuat);
        btnTongQuan = findViewById(R.id.btnTongQuan);
        btnThongKe = findViewById(R.id.btnThongKe);
        btnQuanLyVe = findViewById(R.id.btnQuanLyVe);
        btnLoaiVe = findViewById(R.id.btnLoaiVe);
        btnVoucher = findViewById(R.id.btnVoucher);
        btnNguoiDung = findViewById(R.id.btnNguoiDung);
        btnHoaDon = findViewById(R.id.btnHoaDon);
        btnCauHinhAI = findViewById(R.id.btnCauHinhAI);
    }

    private void hienXinChao() {
        if (Session.nguoiDungHienTai != null && Session.nguoiDungHienTai.getHoTen() != null) {
            lblXinChaoAdmin.setText("Xin chào quản lý, " + Session.nguoiDungHienTai.getHoTen());
        }
    }

    private void batSuKien() {
        btnDangXuat.setOnClickListener(v -> TienIch.dangXuat(this));
        layMenuOverlay.setOnClickListener(v -> dongMenu());
        layMenuQuanLy.setOnClickListener(v -> {
        });
        btnMoMenu.setOnClickListener(v -> doiTrangThaiMenu());
        btnTongQuan.setOnClickListener(v -> chonFragment("Tổng quan", new TongQuanFragment(), btnTongQuan));
        btnThongKe.setOnClickListener(v -> chonFragment("Thống kê", new ThongKeFragment(), btnThongKe));
        btnQuanLyVe.setOnClickListener(v -> chonFragment("Quản lý vé", new QuanLyVeFragment(), btnQuanLyVe));
        btnLoaiVe.setOnClickListener(v -> chonFragment("Loại vé", new PhanLoaiVeFragment(), btnLoaiVe));
        btnVoucher.setOnClickListener(v -> chonFragment("Voucher", new QuanLyVoucherFragment(), btnVoucher));
        btnNguoiDung.setOnClickListener(v -> chonFragment("Người dùng", new QuanLyNguoiDungFragment(), btnNguoiDung));
        btnHoaDon.setOnClickListener(v -> chonFragment("Hóa đơn", new QuanLyHoaDonFragment(), btnHoaDon));
        btnCauHinhAI.setOnClickListener(v -> moCauHinhAI());
    }

    private void doiTrangThaiMenu() {
        if (layMenuOverlay.getVisibility() == View.VISIBLE) {
            dongMenu();
        } else {
            layMenuOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void dongMenu() {
        layMenuOverlay.setVisibility(View.GONE);
    }

    private void chonFragment(String tenManHinh, Fragment fragment, Button nutDangChon) {
        lblManHinhDangChon.setText("Đang xem: " + tenManHinh);
        capNhatMenuDangChon(nutDangChon);
        dongMenu();
        moFragment(fragment);
    }

    private void moCauHinhAI() {
        lblManHinhDangChon.setText("Đang xem: Cấu hình AI");
        capNhatMenuDangChon(btnCauHinhAI);
        dongMenu();
        startActivity(new Intent(this, QuanLyAIActivity.class));
    }

    private void capNhatMenuDangChon(Button nutDangChon) {
        Button[] danhSachNut = {
                btnTongQuan,
                btnThongKe,
                btnQuanLyVe,
                btnLoaiVe,
                btnVoucher,
                btnNguoiDung,
                btnHoaDon,
                btnCauHinhAI
        };

        for (Button nut : danhSachNut) {
            if (nut == nutDangChon) {
                nut.setBackgroundResource(R.drawable.bg_bottom_nav_active);
                nut.setTextColor(getResources().getColor(R.color.white));
            } else {
                nut.setBackgroundColor(Color.TRANSPARENT);
                nut.setTextColor(getResources().getColor(R.color.mauChuTrenGradientPhu));
            }
        }
    }

    private void moFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.layNoiDungQuanLy, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (layMenuOverlay != null && layMenuOverlay.getVisibility() == View.VISIBLE) {
            dongMenu();
            return;
        }

        super.onBackPressed();
    }
}
