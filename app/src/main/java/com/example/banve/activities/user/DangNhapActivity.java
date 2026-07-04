package com.example.banve.activities.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.banve.R;
import com.example.banve.activities.admin.DashboardQuanLyActivity;
import com.example.banve.controllers.NguoiDungController;
import com.example.banve.models.NguoiDung;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.Session;
import com.example.banve.utils.TienIch;

public class DangNhapActivity extends AppCompatActivity {
    private EditText edtTaiKhoan;
    private EditText edtMatKhau;
    private CheckBox chkGhiNhoMatKhau;
    private Button btnDangNhap;
    private Button btnDangKy;
    private ProgressBar pgbDangTai;
    private NguoiDungController nguoiDungController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Session.khoiPhuc(this);
        if (Session.dangDangNhap()) {
            chuyenDashboard();
            return;
        }

        setContentView(R.layout.user_activity_dang_nhap);
        anhXa();
        nguoiDungController = new NguoiDungController();
        batSuKien();
    }

    private void anhXa() {
        edtTaiKhoan = findViewById(R.id.edtTaiKhoan);
        edtMatKhau = findViewById(R.id.edtMatKhau);
        TienIch.ganAnHienMatKhau(edtMatKhau);
        chkGhiNhoMatKhau = findViewById(R.id.chkGhiNhoMatKhau);
        btnDangNhap = findViewById(R.id.btnDangNhap);
        btnDangKy = findViewById(R.id.btnDangKy);
        pgbDangTai = findViewById(R.id.pgbDangTai);
    }

    private void batSuKien() {
        btnDangNhap.setOnClickListener(v -> dangNhap());
        btnDangKy.setOnClickListener(v -> startActivity(new Intent(this, DangKyActivity.class)));
    }

    private void dangNhap() {
        hienDangTai(true);
        nguoiDungController.kiemTraDangNhap(
                layChuoi(edtTaiKhoan),
                layChuoi(edtMatKhau),
                new ApiCallback<NguoiDung>() {
                    @Override
                    public void onSuccess(NguoiDung data) {
                        hienDangTai(false);
                        Session.dangNhap(data);
                        if (chkGhiNhoMatKhau.isChecked()) {
                            Session.luuLocal(DangNhapActivity.this);
                        } else {
                            Session.xoaLocal(DangNhapActivity.this);
                        }
                        chuyenDashboard();
                    }

                    @Override
                    public void onError(String thongBao) {
                        hienDangTai(false);
                        TienIch.hienAlert(DangNhapActivity.this, "Lỗi đăng nhập", thongBao);
                    }
                }
        );
    }

    private void chuyenDashboard() {
        Intent intent;
        if (Session.laQuanLy()) {
            intent = new Intent(this, DashboardQuanLyActivity.class);
        } else {
            intent = new Intent(this, DashboardNguoiDungActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void hienDangTai(boolean dangTai) {
        pgbDangTai.setVisibility(dangTai ? View.VISIBLE : View.GONE);
        btnDangNhap.setEnabled(!dangTai);
        btnDangKy.setEnabled(!dangTai);
    }

    private String layChuoi(EditText editText) {
        return editText.getText().toString();
    }
}
