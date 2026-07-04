package com.example.banve.activities.user;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.banve.R;
import com.example.banve.controllers.NguoiDungController;
import com.example.banve.models.NguoiDung;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.HienThi;
import com.example.banve.utils.Session;
import com.example.banve.utils.TienIch;

public class ThongTinNguoiDungActivity extends AppCompatActivity {
    private TextView lblTaiKhoan;
    private TextView lblHoTen;
    private TextView lblEmail;
    private TextView lblSoDienThoai;
    private TextView lblVaiTro;
    private TextView lblNgayDangKy;
    private TextView lblTrangThai;
    private Button btnCapNhat;
    private Button btnDoiMatKhau;
    private Button btnDangXuat;
    private NguoiDungController nguoiDungController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Session.khoiPhuc(this);

        if (!Session.dangDangNhap()) {
            quayVeDangNhap();
            return;
        }

        setContentView(R.layout.user_activity_thong_tin_nguoi_dung);
        anhXa();
        nguoiDungController = new NguoiDungController();
        hienThongTin();
        batSuKien();
    }

    private void anhXa() {
        lblTaiKhoan = findViewById(R.id.lblTaiKhoan);
        lblHoTen = findViewById(R.id.lblHoTen);
        lblEmail = findViewById(R.id.lblEmail);
        lblSoDienThoai = findViewById(R.id.lblSoDienThoai);
        lblVaiTro = findViewById(R.id.lblVaiTro);
        lblNgayDangKy = findViewById(R.id.lblNgayDangKy);
        lblTrangThai = findViewById(R.id.lblTrangThai);
        btnCapNhat = findViewById(R.id.btnCapNhat);
        btnDoiMatKhau = findViewById(R.id.btnDoiMatKhau);
        btnDangXuat = findViewById(R.id.btnDangXuat);
        findViewById(R.id.btnQuayLai).setOnClickListener(v -> finish());
    }

    private void batSuKien() {
        btnCapNhat.setOnClickListener(v -> moDialogCapNhatThongTin());
        btnDoiMatKhau.setOnClickListener(v -> moDialogDoiMatKhau());
        btnDangXuat.setOnClickListener(v -> TienIch.dangXuat(this));
    }

    private void hienThongTin() {
        NguoiDung nguoiDung = Session.nguoiDungHienTai;
        lblTaiKhoan.setText("Tài khoản: " + giaTri(nguoiDung.getTaiKhoan()));
        lblHoTen.setText("Họ tên: " + giaTri(nguoiDung.getHoTen()));
        lblEmail.setText("Email: " + giaTri(nguoiDung.getEmail()));
        lblSoDienThoai.setText("Số điện thoại: " + giaTri(nguoiDung.getSoDienThoai()));
        lblVaiTro.setText("Vai trò: " + HienThi.vaiTro(nguoiDung.getVaiTro()));
        lblNgayDangKy.setText("Ngày đăng ký: " + giaTri(nguoiDung.getNgayDangKy()));
        lblTrangThai.setText("Trạng thái: " + HienThi.trangThai(nguoiDung.getTrangThai()));
    }

    private void moDialogCapNhatThongTin() {
        View view = getLayoutInflater().inflate(R.layout.user_dialog_cap_nhat_thong_tin, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Cập nhật thông tin")
                .setView(view)
                .create();

        EditText edtHoTen = view.findViewById(R.id.edtHoTen);
        EditText edtEmail = view.findViewById(R.id.edtEmail);
        EditText edtSoDienThoai = view.findViewById(R.id.edtSoDienThoai);
        Button btnLuu = view.findViewById(R.id.btnLuu);
        Button btnHuy = view.findViewById(R.id.btnHuy);

        NguoiDung nguoiDung = Session.nguoiDungHienTai;
        edtHoTen.setText(nguoiDung.getHoTen());
        edtEmail.setText(nguoiDung.getEmail());
        edtSoDienThoai.setText(nguoiDung.getSoDienThoai());

        btnLuu.setOnClickListener(v -> nguoiDungController.capNhatThongTin(
                nguoiDung.getMaNguoiDung(),
                layChuoi(edtHoTen),
                layChuoi(edtEmail),
                layChuoi(edtSoDienThoai),
                new ApiCallback<NguoiDung>() {
                    @Override
                    public void onSuccess(NguoiDung data) {
                        Session.dangNhap(data);
                        Session.luuLocal(ThongTinNguoiDungActivity.this);
                        hienThongTin();
                        dialog.dismiss();
                        Toast.makeText(ThongTinNguoiDungActivity.this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String thongBao) {
                        TienIch.hienAlert(ThongTinNguoiDungActivity.this, "Lỗi cập nhật", thongBao);
                    }
                }
        ));
        btnHuy.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void moDialogDoiMatKhau() {
        View view = getLayoutInflater().inflate(R.layout.user_dialog_doi_mat_khau, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Đổi mật khẩu")
                .setView(view)
                .create();

        EditText edtMatKhauCu = view.findViewById(R.id.edtMatKhauCu);
        EditText edtMatKhauMoi = view.findViewById(R.id.edtMatKhauMoi);
        EditText edtNhapLaiMatKhauMoi = view.findViewById(R.id.edtNhapLaiMatKhauMoi);
        TienIch.ganAnHienMatKhau(edtMatKhauCu);
        TienIch.ganAnHienMatKhau(edtMatKhauMoi);
        TienIch.ganAnHienMatKhau(edtNhapLaiMatKhauMoi);
        Button btnLuu = view.findViewById(R.id.btnLuu);
        Button btnHuy = view.findViewById(R.id.btnHuy);

        btnLuu.setOnClickListener(v -> nguoiDungController.doiMatKhau(
                Session.nguoiDungHienTai.getMaNguoiDung(),
                layChuoi(edtMatKhauCu),
                layChuoi(edtMatKhauMoi),
                layChuoi(edtNhapLaiMatKhauMoi),
                new ApiCallback<NguoiDung>() {
                    @Override
                    public void onSuccess(NguoiDung data) {
                        Session.dangNhap(data);
                        Session.luuLocal(ThongTinNguoiDungActivity.this);
                        dialog.dismiss();
                        Toast.makeText(ThongTinNguoiDungActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String thongBao) {
                        TienIch.hienAlert(ThongTinNguoiDungActivity.this, "Lỗi đổi mật khẩu", thongBao);
                    }
                }
        ));
        btnHuy.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void quayVeDangNhap() {
        Intent intent = new Intent(this, DangNhapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String layChuoi(EditText editText) {
        return editText.getText().toString();
    }

    private String giaTri(String giaTri) {
        return giaTri == null ? "" : giaTri;
    }
}
