package com.example.banve.activities.user;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.banve.R;
import com.example.banve.adapters.ChiTietGioHangAdapter;
import com.example.banve.controllers.GioHangController;
import com.example.banve.controllers.NguoiDungController;
import com.example.banve.controllers.VeController;
import com.example.banve.models.ChiTietGioHang;
import com.example.banve.models.MucGioHang;
import com.example.banve.models.NguoiDung;
import com.example.banve.models.Ve;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.DinhDangTien;
import com.example.banve.utils.Session;
import com.example.banve.utils.TienIch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GioHangActivity extends AppCompatActivity {
    private RecyclerView rcvDanhSachGioHang;
    private TextView lblTrong;
    private TextView lblTongTien;
    private Button btnThanhToan;
    private Button btnTiepTucMua;
    private ChiTietGioHangAdapter adapter;
    private GioHangController gioHangController;
    private NguoiDungController nguoiDungController;
    private VeController veController;
    private final List<MucGioHang> danhSachMuc = new ArrayList<>();
    private double tongTien;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Session.khoiPhuc(this);
        setContentView(R.layout.user_activity_gio_hang);

        anhXa();
        gioHangController = new GioHangController();
        nguoiDungController = new NguoiDungController();
        veController = new VeController();
        khoiTaoRecyclerView();
        batSuKien();
    }

    @Override
    protected void onResume() {
        super.onResume();
        taiGioHang();
    }

    private void anhXa() {
        rcvDanhSachGioHang = findViewById(R.id.rcvDanhSachGioHang);
        lblTrong = findViewById(R.id.lblTrong);
        lblTongTien = findViewById(R.id.lblTongTien);
        btnThanhToan = findViewById(R.id.btnThanhToan);
        btnTiepTucMua = findViewById(R.id.btnTiepTucMua);
        findViewById(R.id.btnQuayLai).setOnClickListener(v -> finish());
    }

    private void khoiTaoRecyclerView() {
        adapter = new ChiTietGioHangAdapter(new ChiTietGioHangAdapter.OnMucGioHangClickListener() {
            @Override
            public void onSua(MucGioHang mucGioHang) {
                moDialogSuaMuc(mucGioHang);
            }

            @Override
            public void onXoa(MucGioHang mucGioHang) {
                xacNhanXoaMuc(mucGioHang);
            }
        });
        rcvDanhSachGioHang.setLayoutManager(new LinearLayoutManager(this));
        rcvDanhSachGioHang.setAdapter(adapter);
    }

    private void batSuKien() {
        btnTiepTucMua.setOnClickListener(v -> finish());
        btnThanhToan.setOnClickListener(v -> {
            if (danhSachMuc.isEmpty()) {
                TienIch.hienAlert(this, "Thông báo", "Giỏ hàng đang trống");
                return;
            }

            String loiNgaySuDung = kiemTraNgaySuDungHopLe();
            if (loiNgaySuDung != null) {
                TienIch.hienAlert(this, "Lỗi thanh toán", loiNgaySuDung);
                return;
            }

            Intent intent = new Intent(this, ThanhToanActivity.class);
            intent.putExtra("tongTien", tongTien);
            startActivity(intent);
        });
    }

    private void taiGioHang() {
        if (Session.nguoiDungHienTai == null || Session.nguoiDungHienTai.getTaiKhoan() == null) {
            yeuCauDangNhapLai();
            return;
        }

        damBaoNguoiDungHopLe(new ApiCallback<NguoiDung>() {
            @Override
            public void onSuccess(NguoiDung nguoiDung) {
                taiGioHangTheoNguoiDung(nguoiDung);
            }

            @Override
            public void onError(String thongBao) {
                TienIch.hienAlert(GioHangActivity.this, "Lỗi giỏ hàng", thongBao);
            }
        });
    }

    private void taiGioHangTheoNguoiDung(NguoiDung nguoiDung) {
        danhSachMuc.clear();
        capNhatDanhSach();
        gioHangController.layDanhSachMuc(nguoiDung.getMaNguoiDung(), new ApiCallback<List<ChiTietGioHang>>() {
            @Override
            public void onSuccess(List<ChiTietGioHang> data) {
                if (data == null || data.isEmpty()) {
                    capNhatDanhSach();
                    return;
                }

                taiThongTinVeChoMuc(data, 0);
            }

            @Override
            public void onError(String thongBao) {
                TienIch.hienAlert(GioHangActivity.this, "Lỗi giỏ hàng", thongBao);
            }
        });
    }

    private void damBaoNguoiDungHopLe(ApiCallback<NguoiDung> callback) {
        if (Session.nguoiDungHienTai != null && Session.nguoiDungHienTai.getMaNguoiDung() > 0) {
            callback.onSuccess(Session.nguoiDungHienTai);
            return;
        }

        if (Session.nguoiDungHienTai == null || Session.nguoiDungHienTai.getTaiKhoan() == null) {
            yeuCauDangNhapLai();
            return;
        }

        nguoiDungController.layTheoTaiKhoan(Session.nguoiDungHienTai.getTaiKhoan(), new ApiCallback<NguoiDung>() {
            @Override
            public void onSuccess(NguoiDung data) {
                if (data.getMaNguoiDung() <= 0) {
                    yeuCauDangNhapLai();
                    return;
                }

                Session.dangNhap(data);
                Session.luuLocal(GioHangActivity.this);
                callback.onSuccess(data);
            }

            @Override
            public void onError(String thongBao) {
                callback.onError(thongBao);
            }
        });
    }

    private void taiThongTinVeChoMuc(List<ChiTietGioHang> danhSachChiTiet, int viTri) {
        if (viTri >= danhSachChiTiet.size()) {
            capNhatDanhSach();
            return;
        }

        ChiTietGioHang chiTiet = danhSachChiTiet.get(viTri);
        veController.layTheoMa(chiTiet.getMaVe(), new ApiCallback<Ve>() {
            @Override
            public void onSuccess(Ve data) {
                danhSachMuc.add(new MucGioHang(chiTiet, data));
                taiThongTinVeChoMuc(danhSachChiTiet, viTri + 1);
            }

            @Override
            public void onError(String thongBao) {
                taiThongTinVeChoMuc(danhSachChiTiet, viTri + 1);
            }
        });
    }

    private void capNhatDanhSach() {
        tongTien = 0;
        for (MucGioHang muc : danhSachMuc) {
            tongTien += muc.tinhThanhTien();
        }

        adapter.capNhatDuLieu(danhSachMuc);
        lblTongTien.setText("Tổng tiền: " + DinhDangTien.dinhDang(tongTien));
        lblTrong.setVisibility(danhSachMuc.isEmpty() ? View.VISIBLE : View.GONE);
        rcvDanhSachGioHang.setVisibility(danhSachMuc.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void moDialogSuaMuc(MucGioHang mucGioHang) {
        View view = getLayoutInflater().inflate(R.layout.user_dialog_sua_muc_gio_hang, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Sửa mục giỏ hàng")
                .setView(view)
                .create();

        TextView lblTenVe = view.findViewById(R.id.lblTenVe);
        DatePicker dtpNgaySuDung = view.findViewById(R.id.dtpNgaySuDung);
        TextView lblSoLuongNL = view.findViewById(R.id.lblSoLuongNL);
        TextView lblSoLuongTE = view.findViewById(R.id.lblSoLuongTE);
        TextView lblSoLuongCT = view.findViewById(R.id.lblSoLuongCT);
        TextView lblTongTamTinh = view.findViewById(R.id.lblTongTamTinh);
        Button btnLuu = view.findViewById(R.id.btnLuu);
        Button btnHuy = view.findViewById(R.id.btnHuy);

        ChiTietGioHang chiTiet = mucGioHang.getChiTietGioHang();
        int[] soLuong = {
                chiTiet.getSoLuongNguoiLon(),
                chiTiet.getSoLuongTreEm(),
                chiTiet.getSoLuongNguoiCaoTuoi()
        };

        lblTenVe.setText(mucGioHang.getVe().getTenVe());
        cauHinhNgayDialog(dtpNgaySuDung, chiTiet.getNgaySuDung());
        Runnable capNhatTong = () -> {
            lblSoLuongNL.setText(String.valueOf(soLuong[0]));
            lblSoLuongTE.setText(String.valueOf(soLuong[1]));
            lblSoLuongCT.setText(String.valueOf(soLuong[2]));
            double tamTinh = soLuong[0] * chiTiet.getDonGiaNguoiLon()
                    + soLuong[1] * chiTiet.getDonGiaTreEm()
                    + soLuong[2] * chiTiet.getDonGiaNguoiCaoTuoi();
            lblTongTamTinh.setText("Tạm tính: " + DinhDangTien.dinhDang(tamTinh));
        };
        capNhatTong.run();

        view.findViewById(R.id.btnGiamNL).setOnClickListener(v -> thayDoiSoLuong(soLuong, 0, -1, capNhatTong));
        view.findViewById(R.id.btnTangNL).setOnClickListener(v -> thayDoiSoLuong(soLuong, 0, 1, capNhatTong));
        view.findViewById(R.id.btnGiamTE).setOnClickListener(v -> thayDoiSoLuong(soLuong, 1, -1, capNhatTong));
        view.findViewById(R.id.btnTangTE).setOnClickListener(v -> thayDoiSoLuong(soLuong, 1, 1, capNhatTong));
        view.findViewById(R.id.btnGiamCT).setOnClickListener(v -> thayDoiSoLuong(soLuong, 2, -1, capNhatTong));
        view.findViewById(R.id.btnTangCT).setOnClickListener(v -> thayDoiSoLuong(soLuong, 2, 1, capNhatTong));

        btnLuu.setOnClickListener(v -> {
            chiTiet.setNgaySuDung(layNgayTuDatePicker(dtpNgaySuDung));
            chiTiet.setSoLuongNguoiLon(soLuong[0]);
            chiTiet.setSoLuongTreEm(soLuong[1]);
            chiTiet.setSoLuongNguoiCaoTuoi(soLuong[2]);
            gioHangController.capNhatMuc(chiTiet, new ApiCallback<ChiTietGioHang>() {
                @Override
                public void onSuccess(ChiTietGioHang data) {
                    dialog.dismiss();
                    Toast.makeText(GioHangActivity.this, "Đã cập nhật giỏ hàng", Toast.LENGTH_SHORT).show();
                    taiGioHang();
                }

                @Override
                public void onError(String thongBao) {
                    TienIch.hienAlert(GioHangActivity.this, "Lỗi cập nhật", thongBao);
                }
            });
        });
        btnHuy.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void xacNhanXoaMuc(MucGioHang mucGioHang) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa vé")
                .setMessage("Bạn có chắc muốn xóa vé này khỏi giỏ?")
                .setPositiveButton("Đồng ý", (dialog, which) -> gioHangController.xoaMuc(
                        mucGioHang.getChiTietGioHang().getMaChiTietGioHang(),
                        new ApiCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean data) {
                                Toast.makeText(GioHangActivity.this, "Đã xóa khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                                taiGioHang();
                            }

                            @Override
                            public void onError(String thongBao) {
                                TienIch.hienAlert(GioHangActivity.this, "Lỗi xóa", thongBao);
                            }
                        }
                ))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void thayDoiSoLuong(int[] soLuong, int viTri, int thayDoi, Runnable capNhatTong) {
        soLuong[viTri] = Math.max(0, soLuong[viTri] + thayDoi);
        capNhatTong.run();
    }

    private void cauHinhNgayDialog(DatePicker datePicker, String ngaySuDung) {
        Calendar homNay = Calendar.getInstance();
        homNay.set(Calendar.HOUR_OF_DAY, 0);
        homNay.set(Calendar.MINUTE, 0);
        homNay.set(Calendar.SECOND, 0);
        homNay.set(Calendar.MILLISECOND, 0);
        datePicker.setMinDate(homNay.getTimeInMillis());

        Calendar ngay = Calendar.getInstance();
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(ngaySuDung);
            if (date != null) {
                ngay.setTime(date);
            }
        } catch (ParseException ignored) {
        }
        datePicker.updateDate(ngay.get(Calendar.YEAR), ngay.get(Calendar.MONTH), ngay.get(Calendar.DAY_OF_MONTH));
    }

    private String layNgayTuDatePicker(DatePicker datePicker) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
    }

    private String kiemTraNgaySuDungHopLe() {
        Calendar homNay = Calendar.getInstance();
        homNay.set(Calendar.HOUR_OF_DAY, 0);
        homNay.set(Calendar.MINUTE, 0);
        homNay.set(Calendar.SECOND, 0);
        homNay.set(Calendar.MILLISECOND, 0);

        for (MucGioHang muc : danhSachMuc) {
            Date ngaySuDung = parseNgaySuDung(muc.getChiTietGioHang().getNgaySuDung());
            if (ngaySuDung != null && ngaySuDung.before(homNay.getTime())) {
                String tenVe = muc.getVe() == null ? "vé" : muc.getVe().getTenVe();
                return "Vé \"" + tenVe + "\" có ngày sử dụng trong quá khứ. Vui lòng sửa ngày trước khi thanh toán.";
            }
        }
        return null;
    }

    private Date parseNgaySuDung(String ngaySuDung) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(ngaySuDung);
        } catch (ParseException e) {
            return null;
        }
    }

    private void moDangNhap() {
        Intent intent = new Intent(this, DangNhapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void yeuCauDangNhapLai() {
        Session.dangXuat();
        Session.luuLocal(this);
        Toast.makeText(this, "Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
        moDangNhap();
    }
}
