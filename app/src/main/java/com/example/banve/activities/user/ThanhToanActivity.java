package com.example.banve.activities.user;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.banve.R;
import com.example.banve.adapters.VeThanhToanAdapter;
import com.example.banve.adapters.VoucherAdapter;
import com.example.banve.controllers.GioHangController;
import com.example.banve.controllers.ThanhToanController;
import com.example.banve.controllers.VeController;
import com.example.banve.dao.VoucherDAO;
import com.example.banve.models.ChiTietGioHang;
import com.example.banve.models.HoaDon;
import com.example.banve.models.MucGioHang;
import com.example.banve.models.ThanhToanTam;
import com.example.banve.models.Ve;
import com.example.banve.models.Voucher;
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

public class ThanhToanActivity extends AppCompatActivity {
    private RecyclerView rcvDanhSachVeThanhToan;
    private TextView lblVoucherDangChon;
    private TextView lblTongTien;
    private TextView lblTienGiam;
    private TextView lblPhaiTra;
    private RadioGroup rgThanhToan;
    private Button btnChonVoucher;
    private Button btnXacNhanThanhToan;
    private Button btnHuy;
    private VeThanhToanAdapter veThanhToanAdapter;
    private GioHangController gioHangController;
    private VeController veController;
    private VoucherDAO voucherDAO;
    private ThanhToanController thanhToanController;
    private final List<MucGioHang> danhSachMuc = new ArrayList<>();
    private final List<Voucher> danhSachVoucher = new ArrayList<>();
    private Voucher voucherDangChon;
    private double tongTien;
    private double tienGiam;
    private double phaiTra;
    private boolean muaNgay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Session.khoiPhuc(this);
        muaNgay = getIntent().getBooleanExtra(ChonVeActivity.EXTRA_MUA_NGAY, false);
        setContentView(R.layout.user_activity_thanh_toan);

        anhXa();
        gioHangController = new GioHangController();
        veController = new VeController();
        voucherDAO = new VoucherDAO();
        thanhToanController = new ThanhToanController();
        khoiTaoRecyclerView();
        batSuKien();
        taiDuLieuThanhToan();
        taiVoucher();
    }

    private void anhXa() {
        rcvDanhSachVeThanhToan = findViewById(R.id.rcvDanhSachVeThanhToan);
        lblVoucherDangChon = findViewById(R.id.lblVoucherDangChon);
        lblTongTien = findViewById(R.id.lblTongTien);
        lblTienGiam = findViewById(R.id.lblTienGiam);
        lblPhaiTra = findViewById(R.id.lblPhaiTra);
        rgThanhToan = findViewById(R.id.rgThanhToan);
        btnChonVoucher = findViewById(R.id.btnChonVoucher);
        btnXacNhanThanhToan = findViewById(R.id.btnXacNhanThanhToan);
        btnHuy = findViewById(R.id.btnHuy);
        findViewById(R.id.btnQuayLai).setOnClickListener(v -> finish());
    }

    private void khoiTaoRecyclerView() {
        veThanhToanAdapter = new VeThanhToanAdapter();
        rcvDanhSachVeThanhToan.setLayoutManager(new LinearLayoutManager(this));
        rcvDanhSachVeThanhToan.setNestedScrollingEnabled(false);
        rcvDanhSachVeThanhToan.setAdapter(veThanhToanAdapter);
    }

    private void batSuKien() {
        btnHuy.setOnClickListener(v -> finish());
        btnChonVoucher.setOnClickListener(v -> moDialogChonVoucher());
        btnXacNhanThanhToan.setOnClickListener(v -> xacNhanThanhToan());
    }

    private void taiDuLieuThanhToan() {
        if (!Session.dangDangNhap()) {
            TienIch.hienAlert(this, "Thông báo", "Vui lòng đăng nhập lại");
            finish();
            return;
        }

        danhSachMuc.clear();
        veThanhToanAdapter.capNhatDuLieu(danhSachMuc);
        if (muaNgay) {
            taiDuLieuMuaNgay();
            return;
        }

        gioHangController.layDanhSachMuc(Session.nguoiDungHienTai.getMaNguoiDung(), new ApiCallback<List<ChiTietGioHang>>() {
            @Override
            public void onSuccess(List<ChiTietGioHang> data) {
                if (data == null || data.isEmpty()) {
                    TienIch.hienAlert(ThanhToanActivity.this, "Thông báo", "Giỏ hàng đang trống");
                    tinhLaiTongTien();
                    return;
                }

                taiThongTinVeChoMuc(data, 0);
            }

            @Override
            public void onError(String thongBao) {
                TienIch.hienAlert(ThanhToanActivity.this, "Lỗi thanh toán", thongBao);
            }
        });
    }

    private void taiDuLieuMuaNgay() {
        ChiTietGioHang chiTietMuaNgay = taoChiTietMuaNgayTuIntent();
        if (chiTietMuaNgay == null) {
            TienIch.hienAlert(this, "Thông báo", "Thông tin mua ngay không hợp lệ");
            tinhLaiTongTien();
            return;
        }

        veController.layTheoMa(chiTietMuaNgay.getMaVe(), new ApiCallback<Ve>() {
            @Override
            public void onSuccess(Ve data) {
                danhSachMuc.add(new MucGioHang(chiTietMuaNgay, data));
                veThanhToanAdapter.capNhatDuLieu(danhSachMuc);
                tinhLaiTongTien();
                String loiNgaySuDung = kiemTraNgaySuDungHopLe();
                if (loiNgaySuDung != null) {
                    btnXacNhanThanhToan.setEnabled(false);
                    TienIch.hienAlert(ThanhToanActivity.this, "Lỗi thanh toán", loiNgaySuDung);
                }
            }

            @Override
            public void onError(String thongBao) {
                TienIch.hienAlert(ThanhToanActivity.this, "Lỗi thanh toán", thongBao);
            }
        });
    }

    private ChiTietGioHang taoChiTietMuaNgayTuIntent() {
        int maVe = getIntent().getIntExtra("maVe", 0);
        String ngaySuDung = getIntent().getStringExtra(ChonVeActivity.EXTRA_NGAY_SU_DUNG);
        int soLuongNguoiLon = getIntent().getIntExtra(ChonVeActivity.EXTRA_SO_LUONG_NGUOI_LON, 0);
        int soLuongTreEm = getIntent().getIntExtra(ChonVeActivity.EXTRA_SO_LUONG_TRE_EM, 0);
        int soLuongNguoiCaoTuoi = getIntent().getIntExtra(ChonVeActivity.EXTRA_SO_LUONG_NGUOI_CAO_TUOI, 0);

        if (maVe <= 0 || ngaySuDung == null || ngaySuDung.trim().isEmpty()) {
            return null;
        }
        if (soLuongNguoiLon <= 0 && soLuongTreEm <= 0 && soLuongNguoiCaoTuoi <= 0) {
            return null;
        }

        ChiTietGioHang chiTiet = new ChiTietGioHang();
        chiTiet.setMaVe(maVe);
        chiTiet.setNgaySuDung(ngaySuDung);
        chiTiet.setSoLuongNguoiLon(soLuongNguoiLon);
        chiTiet.setSoLuongTreEm(soLuongTreEm);
        chiTiet.setSoLuongNguoiCaoTuoi(soLuongNguoiCaoTuoi);
        chiTiet.setDonGiaNguoiLon(getIntent().getDoubleExtra(ChonVeActivity.EXTRA_DON_GIA_NGUOI_LON, 0));
        chiTiet.setDonGiaTreEm(getIntent().getDoubleExtra(ChonVeActivity.EXTRA_DON_GIA_TRE_EM, 0));
        chiTiet.setDonGiaNguoiCaoTuoi(getIntent().getDoubleExtra(ChonVeActivity.EXTRA_DON_GIA_NGUOI_CAO_TUOI, 0));
        return chiTiet;
    }

    private void taiThongTinVeChoMuc(List<ChiTietGioHang> danhSachChiTiet, int viTri) {
        if (viTri >= danhSachChiTiet.size()) {
            veThanhToanAdapter.capNhatDuLieu(danhSachMuc);
            tinhLaiTongTien();
            String loiNgaySuDung = kiemTraNgaySuDungHopLe();
            if (loiNgaySuDung != null) {
                btnXacNhanThanhToan.setEnabled(false);
                TienIch.hienAlert(ThanhToanActivity.this, "Lỗi thanh toán", loiNgaySuDung);
            }
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

    private void taiVoucher() {
        voucherDAO.layDanhSachVoucherKhaDung(new ApiCallback<List<Voucher>>() {
            @Override
            public void onSuccess(List<Voucher> data) {
                danhSachVoucher.clear();
                if (data != null) {
                    danhSachVoucher.addAll(data);
                }
                capNhatHienThiVoucher();
            }

            @Override
            public void onError(String thongBao) {
                TienIch.hienAlert(ThanhToanActivity.this, "Lỗi voucher", thongBao);
            }
        });
    }

    private void tinhLaiTongTien() {
        tongTien = thanhToanController.tinhTongTien(danhSachMuc);
        tienGiam = thanhToanController.apDungVoucher(voucherDangChon, tongTien);
        phaiTra = tongTien - tienGiam;

        lblTongTien.setText("Tổng tiền: " + DinhDangTien.dinhDang(tongTien));
        lblTienGiam.setText("Tiền giảm: " + DinhDangTien.dinhDang(tienGiam));
        lblPhaiTra.setText("Phải trả: " + DinhDangTien.dinhDang(phaiTra));
        capNhatHienThiVoucher();
    }

    private void moDialogChonVoucher() {
        View view = getLayoutInflater().inflate(R.layout.user_dialog_chon_voucher, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        RecyclerView rcvDanhSachVoucher = view.findViewById(R.id.rcvVoucher);
        Button btnBoChonVoucher = view.findViewById(R.id.btnBoChonVoucher);
        Button btnDong = view.findViewById(R.id.btnDong);

        VoucherAdapter dialogVoucherAdapter = new VoucherAdapter(voucher -> {
            if (voucherDangChon != null && voucherDangChon.getMaVoucher() == voucher.getMaVoucher()) {
                voucherDangChon = null;
            } else {
                voucherDangChon = voucher;
            }
            tinhLaiTongTien();
            dialog.dismiss();
        });
        dialogVoucherAdapter.capNhatDuLieu(danhSachVoucher);
        dialogVoucherAdapter.chonVoucher(voucherDangChon);
        rcvDanhSachVoucher.setLayoutManager(new LinearLayoutManager(this));
        rcvDanhSachVoucher.setAdapter(dialogVoucherAdapter);

        btnBoChonVoucher.setOnClickListener(v -> {
            voucherDangChon = null;
            tinhLaiTongTien();
            dialog.dismiss();
        });
        if (btnDong != null) {
            btnDong.setOnClickListener(v -> dialog.dismiss());
        }
        dialog.show();
    }

    private void capNhatHienThiVoucher() {
        if (voucherDangChon == null) {
            lblVoucherDangChon.setText("Chưa chọn voucher");
            return;
        }

        lblVoucherDangChon.setText(voucherDangChon.getTenVoucher() + " - giảm " + DinhDangTien.dinhDang(tienGiam));
    }

    private void xacNhanThanhToan() {
        if (danhSachMuc.isEmpty()) {
            TienIch.hienAlert(this, "Thông báo", "Giỏ hàng đang trống");
            return;
        }

        String loiNgaySuDung = kiemTraNgaySuDungHopLe();
        if (loiNgaySuDung != null) {
            TienIch.hienAlert(this, "Lỗi thanh toán", loiNgaySuDung);
            return;
        }

        String hinhThucThanhToan = layHinhThucThanhToan();
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận thanh toán")
                .setMessage("Xác nhận thanh toán " + DinhDangTien.dinhDang(phaiTra) + "?")
                .setPositiveButton("Đồng ý", (dialog, which) -> hoanTatThanhToan(hinhThucThanhToan))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void hoanTatThanhToan(String hinhThucThanhToan) {
        btnXacNhanThanhToan.setEnabled(false);
        if ("ChuyenKhoan".equals(hinhThucThanhToan)) {
            taoThanhToanSePay();
            return;
        }

        thanhToanController.hoanTatThanhToan(
                Session.nguoiDungHienTai.getMaNguoiDung(),
                danhSachMuc,
                voucherDangChon,
                hinhThucThanhToan,
                new ApiCallback<HoaDon>() {
                    @Override
                    public void onSuccess(HoaDon data) {
                        btnXacNhanThanhToan.setEnabled(true);
                        moThanhToanThanhCong(data);
                    }

                    @Override
                    public void onError(String thongBao) {
                        btnXacNhanThanhToan.setEnabled(true);
                        TienIch.hienAlert(ThanhToanActivity.this, "Lỗi thanh toán", thongBao);
                    }
                }
        );
    }

    private void taoThanhToanSePay() {
        thanhToanController.taoThanhToanSePay(
                Session.nguoiDungHienTai.getMaNguoiDung(),
                danhSachMuc,
                voucherDangChon,
                new ApiCallback<ThanhToanTam>() {
                    @Override
                    public void onSuccess(ThanhToanTam data) {
                        btnXacNhanThanhToan.setEnabled(true);
                        moManHinhQrSePay(data);
                    }

                    @Override
                    public void onError(String thongBao) {
                        btnXacNhanThanhToan.setEnabled(true);
                        TienIch.hienAlert(ThanhToanActivity.this, "Lỗi SePay", thongBao);
                    }
                }
        );
    }

    private void moManHinhQrSePay(ThanhToanTam thanhToanTam) {
        Intent intent = new Intent(this, ThanhToanSePayActivity.class);
        intent.putExtra("maHoaDon", thanhToanTam.getMaHoaDon());
        intent.putExtra("tongTien", thanhToanTam.getTongTien());
        intent.putExtra("tienGiam", thanhToanTam.getTienGiam());
        intent.putExtra("noiDungChuyenKhoan", thanhToanTam.getNoiDungChuyenKhoan());
        startActivity(intent);
    }

    private String layHinhThucThanhToan() {
        int id = rgThanhToan.getCheckedRadioButtonId();
        if (id == R.id.radChuyenKhoan) {
            return "ChuyenKhoan";
        }
        if (id == R.id.radVNPay) {
            return "VNPay";
        }
        return "TheQuocTe";
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
                return "Vé \"" + tenVe + "\" có ngày sử dụng trong quá khứ. Vui lòng quay lại giỏ hàng để sửa ngày.";
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

    private void moThanhToanThanhCong(HoaDon hoaDon) {
        Intent intent = new Intent(this, ThanhToanThanhCongActivity.class);
        intent.putExtra("maHoaDon", hoaDon.getMaHoaDon());
        intent.putExtra("noiDungChuyenKhoan", hoaDon.getNoiDungChuyenKhoan());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
