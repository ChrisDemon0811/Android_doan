package com.example.banve.activities.user;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.banve.R;
import com.example.banve.adapters.ChiTietHoaDonAdapter;
import com.example.banve.controllers.HoaDonController;
import com.example.banve.models.ChiTietHoaDon;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.DinhDangTien;
import com.example.banve.utils.HienThi;
import com.example.banve.utils.TienIch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChiTietHoaDonActivity extends AppCompatActivity {
    private TextView lblMaHoaDon;
    private TextView lblNgayLap;
    private TextView lblTongTien;
    private TextView lblTienGiam;
    private TextView lblThanhTien;
    private TextView lblHinhThuc;
    private RecyclerView rcvChiTietHoaDon;
    private Button btnDong;
    private ChiTietHoaDonAdapter chiTietHoaDonAdapter;
    private HoaDonController hoaDonController;
    private int maHoaDon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_chi_tiet_hoa_don);

        anhXa();
        hoaDonController = new HoaDonController();
        khoiTaoRecyclerView();
        hienThongTinHoaDon();
        batSuKien();
        taiChiTietHoaDon();
    }

    private void anhXa() {
        lblMaHoaDon = findViewById(R.id.lblMaHoaDon);
        lblNgayLap = findViewById(R.id.lblNgayLap);
        lblTongTien = findViewById(R.id.lblTongTien);
        lblTienGiam = findViewById(R.id.lblTienGiam);
        lblThanhTien = findViewById(R.id.lblThanhTien);
        lblHinhThuc = findViewById(R.id.lblHinhThuc);
        rcvChiTietHoaDon = findViewById(R.id.rcvChiTietHoaDon);
        btnDong = findViewById(R.id.btnDong);
        findViewById(R.id.btnQuayLai).setOnClickListener(v -> finish());
    }

    private void khoiTaoRecyclerView() {
        chiTietHoaDonAdapter = new ChiTietHoaDonAdapter();
        rcvChiTietHoaDon.setLayoutManager(new LinearLayoutManager(this));
        rcvChiTietHoaDon.setAdapter(chiTietHoaDonAdapter);
    }

    private void hienThongTinHoaDon() {
        maHoaDon = getIntent().getIntExtra("maHoaDon", 0);
        String ngayLap = getIntent().getStringExtra("ngayLap");
        double tongTien = getIntent().getDoubleExtra("tongTien", 0);
        double tienGiam = getIntent().getDoubleExtra("tienGiam", 0);
        String thanhToan = getIntent().getStringExtra("thanhToan");
        double thanhTien = Math.max(0, tongTien - tienGiam);

        lblMaHoaDon.setText("Mã hóa đơn: " + maHoaDon);
        lblNgayLap.setText("Ngày lập: " + dinhDangNgayGio(ngayLap));
        lblTongTien.setText("Tổng tiền: " + DinhDangTien.dinhDang(tongTien));
        lblTienGiam.setText("Tiền giảm: " + DinhDangTien.dinhDang(tienGiam));
        lblThanhTien.setText("Thành tiền: " + DinhDangTien.dinhDang(thanhTien));
        lblHinhThuc.setText("Hình thức thanh toán: " + HienThi.hinhThucThanhToan(thanhToan));
    }

    private void batSuKien() {
        btnDong.setOnClickListener(v -> finish());
    }

    private void taiChiTietHoaDon() {
        if (maHoaDon <= 0) {
            TienIch.hienAlert(this, "Lỗi chi tiết hóa đơn", "Mã hóa đơn không hợp lệ");
            return;
        }

        hoaDonController.layChiTietHoaDon(maHoaDon, new ApiCallback<List<ChiTietHoaDon>>() {
            @Override
            public void onSuccess(List<ChiTietHoaDon> data) {
                chiTietHoaDonAdapter.capNhatDuLieu(data);
            }

            @Override
            public void onError(String thongBao) {
                TienIch.hienAlert(ChiTietHoaDonActivity.this, "Lỗi chi tiết hóa đơn", thongBao);
            }
        });
    }

    private String hienThiHinhThucThanhToan(String thanhToan) {
        if ("ChuyenKhoan".equals(thanhToan)) {
            return "Chuyển khoản";
        }
        if ("VNPay".equals(thanhToan)) {
            return "VNPay";
        }
        if ("TheQuocTe".equals(thanhToan) || "TienMat".equals(thanhToan)) {
            return "Thẻ tín dụng/ghi nợ quốc tế";
        }
        return thanhToan == null ? "" : thanhToan;
    }

    private String dinhDangNgayGio(String ngayGio) {
        if (ngayGio == null || ngayGio.trim().isEmpty()) {
            return "";
        }

        String[] dinhDangNguon = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss"
        };

        for (String dinhDang : dinhDangNguon) {
            try {
                Date ngay = new SimpleDateFormat(dinhDang, Locale.getDefault()).parse(ngayGio);
                return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(ngay);
            } catch (ParseException ignored) {
            }
        }

        return ngayGio;
    }
}
