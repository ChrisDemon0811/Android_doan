package com.example.banve.activities.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.banve.R;
import com.example.banve.controllers.CauHinhAIController;
import com.example.banve.models.CauHinhAI;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.Session;
import com.example.banve.utils.TienIch;

public class QuanLyAIActivity extends AppCompatActivity {
    private EditText edtNhaCungCap;
    private EditText edtKhoaApi;
    private EditText edtMoHinh;
    private EditText edtNhacLenh;
    private Button btnLuu;
    private Button btnKiemTra;
    private ProgressBar pgbDangTai;
    private CauHinhAIController cauHinhAIController;
    private int maCauHinhAI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Session.khoiPhuc(this);

        if (!Session.laQuanLy()) {
            Toast.makeText(this, "Bạn không có quyền", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.admin_activity_quan_ly_ai);
        anhXa();
        cauHinhAIController = new CauHinhAIController();
        ganGiaTriMacDinh();
        batSuKien();
        taiCauHinhHienTai();
    }

    private void anhXa() {
        edtNhaCungCap = findViewById(R.id.edtNhaCungCap);
        edtKhoaApi = findViewById(R.id.edtKhoaApi);
        TienIch.ganAnHienMatKhau(edtKhoaApi);
        edtMoHinh = findViewById(R.id.edtMoHinh);
        edtNhacLenh = findViewById(R.id.edtNhacLenh);
        btnLuu = findViewById(R.id.btnLuu);
        btnKiemTra = findViewById(R.id.btnKiemTra);
        pgbDangTai = findViewById(R.id.pgbDangTai);
        findViewById(R.id.btnQuayLai).setOnClickListener(v -> finish());
    }

    private void ganGiaTriMacDinh() {
        edtNhaCungCap.setText("https://generativelanguage.googleapis.com");
        edtMoHinh.setText("gemini-1.5-flash");
        edtNhacLenh.setText("Bạn là trợ lý ảo cho khu du lịch, trả lời ngắn gọn bằng tiếng Việt.");
    }

    private void batSuKien() {
        btnLuu.setOnClickListener(v -> luuCauHinh());
        btnKiemTra.setOnClickListener(v -> kiemTraAI());
    }

    private void taiCauHinhHienTai() {
        hienDangTai(true);
        cauHinhAIController.layCauHinhHienTai(new ApiCallback<CauHinhAI>() {
            @Override
            public void onSuccess(CauHinhAI data) {
                hienDangTai(false);
                doDuLieuLenForm(data);
            }

            @Override
            public void onError(String thongBao) {
                hienDangTai(false);
                if (!"Chưa có cấu hình AI".equals(thongBao)) {
                    hienAlert("Lỗi cấu hình AI", thongBao);
                }
            }
        });
    }

    private void doDuLieuLenForm(CauHinhAI cauHinhAI) {
        if (cauHinhAI == null) {
            return;
        }

        maCauHinhAI = cauHinhAI.getMaCauHinhAI();
        edtNhaCungCap.setText(giaTri(cauHinhAI.getNhaCungCap()));
        edtKhoaApi.setText(giaTri(cauHinhAI.getKhoaApi()));
        edtMoHinh.setText(giaTri(cauHinhAI.getMoHinh()));
        edtNhacLenh.setText(giaTri(cauHinhAI.getNhacLenh()));
    }

    private void luuCauHinh() {
        CauHinhAI cauHinhAI = layCauHinhTuForm();
        hienDangTai(true);
        cauHinhAIController.luuCauHinh(cauHinhAI, new ApiCallback<CauHinhAI>() {
            @Override
            public void onSuccess(CauHinhAI data) {
                hienDangTai(false);
                if (data != null) {
                    maCauHinhAI = data.getMaCauHinhAI();
                }
                Toast.makeText(QuanLyAIActivity.this, "Đã lưu cấu hình AI", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String thongBao) {
                hienDangTai(false);
                hienAlert("Lỗi lưu cấu hình AI", thongBao);
            }
        });
    }

    private void kiemTraAI() {
        CauHinhAI cauHinhAI = layCauHinhTuForm();
        hienDangTai(true);
        cauHinhAIController.kiemTraCauHinh(cauHinhAI, new ApiCallback<String>() {
            @Override
            public void onSuccess(String data) {
                hienDangTai(false);
                hienAlert("Kiểm tra AI thành công", data);
            }

            @Override
            public void onError(String thongBao) {
                hienDangTai(false);
                hienAlert("Kiểm tra AI thất bại", thongBao);
            }
        });
    }

    private CauHinhAI layCauHinhTuForm() {
        CauHinhAI cauHinhAI = new CauHinhAI();
        cauHinhAI.setMaCauHinhAI(maCauHinhAI);
        cauHinhAI.setNhaCungCap(edtNhaCungCap.getText().toString());
        cauHinhAI.setKhoaApi(edtKhoaApi.getText().toString());
        cauHinhAI.setMoHinh(edtMoHinh.getText().toString());
        cauHinhAI.setNhacLenh(edtNhacLenh.getText().toString());
        return cauHinhAI;
    }

    private void hienDangTai(boolean dangTai) {
        pgbDangTai.setVisibility(dangTai ? View.VISIBLE : View.GONE);
        btnLuu.setEnabled(!dangTai);
        btnKiemTra.setEnabled(!dangTai);
    }

    private void hienAlert(String tieuDe, String noiDung) {
        new AlertDialog.Builder(this)
                .setTitle(tieuDe)
                .setMessage(noiDung)
                .setPositiveButton("Đồng ý", null)
                .show();
    }

    private String giaTri(String giaTri) {
        return giaTri == null ? "" : giaTri;
    }
}
