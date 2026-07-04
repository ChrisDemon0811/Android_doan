package com.example.banve.activities.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.banve.R;
import com.example.banve.controllers.VeController;
import com.example.banve.models.Ve;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.BoNhoAnh;
import com.example.banve.utils.DinhDangTien;
import com.example.banve.utils.TienIch;

public class ThongTinVeActivity extends AppCompatActivity {
    private ImageView imgAnhVe;
    private TextView lblTenVe;
    private TextView lblMoTa;
    private TextView lblThongTinVe;
    private TextView lblGiaNguoiLon;
    private TextView lblGiaTreEm;
    private TextView lblGiaCaoTuoi;
    private Button btnChonVe;
    private Button btnMuaNgay;
    private VeController veController;
    private Ve veHienTai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_thong_tin_ve);

        anhXa();
        veController = new VeController();
        int maVe = getIntent().getIntExtra("maVe", 0);
        taiThongTinVe(maVe);
        btnChonVe.setOnClickListener(v -> moChonVe());
        btnMuaNgay.setOnClickListener(v -> moMuaNgay());
    }

    private void anhXa() {
        imgAnhVe = findViewById(R.id.imgAnhVe);
        lblTenVe = findViewById(R.id.lblTenVe);
        lblMoTa = findViewById(R.id.lblMoTa);
        lblThongTinVe = findViewById(R.id.lblThongTinVe);
        lblGiaNguoiLon = findViewById(R.id.lblGiaNguoiLon);
        lblGiaTreEm = findViewById(R.id.lblGiaTreEm);
        lblGiaCaoTuoi = findViewById(R.id.lblGiaCaoTuoi);
        btnChonVe = findViewById(R.id.btnChonVe);
        btnMuaNgay = findViewById(R.id.btnMuaNgay);
        findViewById(R.id.btnQuayLai).setOnClickListener(v -> finish());
    }

    private void taiThongTinVe(int maVe) {
        veController.layTheoMa(maVe, new ApiCallback<Ve>() {
            @Override
            public void onSuccess(Ve data) {
                veHienTai = data;
                hienThongTinVe(data);
            }

            @Override
            public void onError(String thongBao) {
                TienIch.hienAlert(ThongTinVeActivity.this, "Lỗi tải thông tin vé", thongBao);
            }
        });
    }

    private void hienThongTinVe(Ve ve) {
        lblTenVe.setText(ve.getTenVe());
        lblMoTa.setText(ve.getMoTa());
        lblThongTinVe.setText(ve.getThongTinVe());
        lblGiaNguoiLon.setText("Người lớn: " + DinhDangTien.dinhDang(ve.getGiaNguoiLon()));
        lblGiaTreEm.setText("Trẻ em: " + DinhDangTien.dinhDang(ve.getGiaTreEm()));
        lblGiaCaoTuoi.setText("Người cao tuổi: " + DinhDangTien.dinhDang(ve.getGiaNguoiCaoTuoi()));
        BoNhoAnh.taiAnh(ve.getAnhVe(), imgAnhVe, R.mipmap.ic_launcher);
    }

    private void moChonVe() {
        if (veHienTai == null) {
            TienIch.hienAlert(this, "Thông báo", "Chưa tải xong thông tin vé");
            return;
        }

        Intent intent = new Intent(this, ChonVeActivity.class);
        intent.putExtra("maVe", veHienTai.getMaVe());
        startActivity(intent);
    }

    private void moMuaNgay() {
        if (veHienTai == null) {
            TienIch.hienAlert(this, "Thông báo", "Chưa tải xong thông tin vé");
            return;
        }

        Intent intent = new Intent(this, ChonVeActivity.class);
        intent.putExtra("maVe", veHienTai.getMaVe());
        intent.putExtra(ChonVeActivity.EXTRA_MUA_NGAY, true);
        startActivity(intent);
    }
}
