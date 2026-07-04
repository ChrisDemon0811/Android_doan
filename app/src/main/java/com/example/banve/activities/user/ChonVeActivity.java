package com.example.banve.activities.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.banve.R;
import com.example.banve.controllers.GioHangController;
import com.example.banve.controllers.NguoiDungController;
import com.example.banve.controllers.VeController;
import com.example.banve.models.ChiTietGioHang;
import com.example.banve.models.NguoiDung;
import com.example.banve.models.Ve;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.DinhDangTien;
import com.example.banve.utils.Session;
import com.example.banve.utils.TienIch;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ChonVeActivity extends AppCompatActivity {
    public static final String EXTRA_MUA_NGAY = "muaNgay";
    public static final String EXTRA_NGAY_SU_DUNG = "ngaySuDung";
    public static final String EXTRA_SO_LUONG_NGUOI_LON = "soLuongNguoiLon";
    public static final String EXTRA_SO_LUONG_TRE_EM = "soLuongTreEm";
    public static final String EXTRA_SO_LUONG_NGUOI_CAO_TUOI = "soLuongNguoiCaoTuoi";
    public static final String EXTRA_DON_GIA_NGUOI_LON = "donGiaNguoiLon";
    public static final String EXTRA_DON_GIA_TRE_EM = "donGiaTreEm";
    public static final String EXTRA_DON_GIA_NGUOI_CAO_TUOI = "donGiaNguoiCaoTuoi";

    private TextView lblTenVe;
    private DatePicker dtpNgaySuDung;
    private TextView lblSoLuongNL;
    private TextView lblSoLuongTE;
    private TextView lblSoLuongCT;
    private TextView lblTongTamTinh;
    private Button btnThemVaoGio;
    private VeController veController;
    private GioHangController gioHangController;
    private NguoiDungController nguoiDungController;
    private Ve veHienTai;
    private boolean muaNgay;
    private int soLuongNguoiLon;
    private int soLuongTreEm;
    private int soLuongNguoiCaoTuoi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Session.khoiPhuc(this);
        setContentView(R.layout.user_activity_chon_ve);

        muaNgay = getIntent().getBooleanExtra(EXTRA_MUA_NGAY, false);
        anhXa();
        cauHinhNgaySuDung();
        veController = new VeController();
        gioHangController = new GioHangController();
        nguoiDungController = new NguoiDungController();
        batSuKienSoLuong();
        taiThongTinVe();
        btnThemVaoGio.setText(muaNgay ? "Thanh toán ngay" : "Thêm vào giỏ");
        btnThemVaoGio.setOnClickListener(v -> {
            if (muaNgay) {
                muaNgay();
            } else {
                themVaoGio();
            }
        });
    }

    private void anhXa() {
        lblTenVe = findViewById(R.id.lblTenVe);
        dtpNgaySuDung = findViewById(R.id.dtpNgaySuDung);
        lblSoLuongNL = findViewById(R.id.lblSoLuongNL);
        lblSoLuongTE = findViewById(R.id.lblSoLuongTE);
        lblSoLuongCT = findViewById(R.id.lblSoLuongCT);
        lblTongTamTinh = findViewById(R.id.lblTongTamTinh);
        btnThemVaoGio = findViewById(R.id.btnThemVaoGio);
        findViewById(R.id.btnQuayLai).setOnClickListener(v -> finish());
    }

    private void cauHinhNgaySuDung() {
        Calendar homNay = Calendar.getInstance();
        homNay.set(Calendar.HOUR_OF_DAY, 0);
        homNay.set(Calendar.MINUTE, 0);
        homNay.set(Calendar.SECOND, 0);
        homNay.set(Calendar.MILLISECOND, 0);
        dtpNgaySuDung.setMinDate(homNay.getTimeInMillis());
    }

    private void batSuKienSoLuong() {
        findViewById(R.id.btnGiamNL).setOnClickListener(v -> thayDoiSoLuong("NL", -1));
        findViewById(R.id.btnTangNL).setOnClickListener(v -> thayDoiSoLuong("NL", 1));
        findViewById(R.id.btnGiamTE).setOnClickListener(v -> thayDoiSoLuong("TE", -1));
        findViewById(R.id.btnTangTE).setOnClickListener(v -> thayDoiSoLuong("TE", 1));
        findViewById(R.id.btnGiamCT).setOnClickListener(v -> thayDoiSoLuong("CT", -1));
        findViewById(R.id.btnTangCT).setOnClickListener(v -> thayDoiSoLuong("CT", 1));
    }

    private void thayDoiSoLuong(String loai, int thayDoi) {
        if ("NL".equals(loai)) {
            soLuongNguoiLon = Math.max(0, soLuongNguoiLon + thayDoi);
        } else if ("TE".equals(loai)) {
            soLuongTreEm = Math.max(0, soLuongTreEm + thayDoi);
        } else {
            soLuongNguoiCaoTuoi = Math.max(0, soLuongNguoiCaoTuoi + thayDoi);
        }

        capNhatSoLuongVaTongTien();
    }

    private void taiThongTinVe() {
        int maVe = getIntent().getIntExtra("maVe", 0);
        veController.layTheoMa(maVe, new ApiCallback<Ve>() {
            @Override
            public void onSuccess(Ve data) {
                veHienTai = data;
                lblTenVe.setText(data.getTenVe());
                capNhatSoLuongVaTongTien();
            }

            @Override
            public void onError(String thongBao) {
                TienIch.hienAlert(ChonVeActivity.this, "Lỗi tải vé", thongBao);
            }
        });
    }

    private void capNhatSoLuongVaTongTien() {
        lblSoLuongNL.setText(String.valueOf(soLuongNguoiLon));
        lblSoLuongTE.setText(String.valueOf(soLuongTreEm));
        lblSoLuongCT.setText(String.valueOf(soLuongNguoiCaoTuoi));

        double tongTamTinh = 0;
        if (veHienTai != null) {
            tongTamTinh += soLuongNguoiLon * veHienTai.getGiaNguoiLon();
            tongTamTinh += soLuongTreEm * veHienTai.getGiaTreEm();
            tongTamTinh += soLuongNguoiCaoTuoi * veHienTai.getGiaNguoiCaoTuoi();
        }
        lblTongTamTinh.setText("Tạm tính: " + DinhDangTien.dinhDang(tongTamTinh));
    }

    private void themVaoGio() {
        if (!kiemTraDuLieuChonVe()) {
            return;
        }

        damBaoNguoiDungHopLe(new ApiCallback<NguoiDung>() {
            @Override
            public void onSuccess(NguoiDung nguoiDung) {
                btnThemVaoGio.setEnabled(false);
                gioHangController.themHoacGopMuc(
                        nguoiDung.getMaNguoiDung(),
                        taoMucGioHang(),
                        new ApiCallback<ChiTietGioHang>() {
                            @Override
                            public void onSuccess(ChiTietGioHang data) {
                                btnThemVaoGio.setEnabled(true);
                                Toast.makeText(ChonVeActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String thongBao) {
                                btnThemVaoGio.setEnabled(true);
                                TienIch.hienAlert(ChonVeActivity.this, "Lỗi giỏ hàng", thongBao);
                            }
                        }
                );
            }

            @Override
            public void onError(String thongBao) {
                TienIch.hienAlert(ChonVeActivity.this, "Lỗi giỏ hàng", thongBao);
            }
        });
    }

    private void muaNgay() {
        if (!kiemTraDuLieuChonVe()) {
            return;
        }

        damBaoNguoiDungHopLe(new ApiCallback<NguoiDung>() {
            @Override
            public void onSuccess(NguoiDung nguoiDung) {
                ChiTietGioHang mucMuaNgay = taoMucGioHang();
                Intent intent = new Intent(ChonVeActivity.this, ThanhToanActivity.class);
                intent.putExtra(EXTRA_MUA_NGAY, true);
                intent.putExtra("maVe", mucMuaNgay.getMaVe());
                intent.putExtra(EXTRA_NGAY_SU_DUNG, mucMuaNgay.getNgaySuDung());
                intent.putExtra(EXTRA_SO_LUONG_NGUOI_LON, mucMuaNgay.getSoLuongNguoiLon());
                intent.putExtra(EXTRA_SO_LUONG_TRE_EM, mucMuaNgay.getSoLuongTreEm());
                intent.putExtra(EXTRA_SO_LUONG_NGUOI_CAO_TUOI, mucMuaNgay.getSoLuongNguoiCaoTuoi());
                intent.putExtra(EXTRA_DON_GIA_NGUOI_LON, mucMuaNgay.getDonGiaNguoiLon());
                intent.putExtra(EXTRA_DON_GIA_TRE_EM, mucMuaNgay.getDonGiaTreEm());
                intent.putExtra(EXTRA_DON_GIA_NGUOI_CAO_TUOI, mucMuaNgay.getDonGiaNguoiCaoTuoi());
                startActivity(intent);
            }

            @Override
            public void onError(String thongBao) {
                TienIch.hienAlert(ChonVeActivity.this, "Lỗi mua ngay", thongBao);
            }
        });
    }

    private boolean kiemTraDuLieuChonVe() {
        if (Session.nguoiDungHienTai == null || Session.nguoiDungHienTai.getTaiKhoan() == null) {
            yeuCauDangNhapLai();
            return false;
        }
        if (veHienTai == null) {
            TienIch.hienAlert(this, "Thông báo", "Chưa tải xong thông tin vé");
            return false;
        }
        if (soLuongNguoiLon <= 0 && soLuongTreEm <= 0 && soLuongNguoiCaoTuoi <= 0) {
            TienIch.hienAlert(this, "Thông báo", "Vui lòng chọn ít nhất một vé");
            return false;
        }
        return true;
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
                Session.luuLocal(ChonVeActivity.this);
                callback.onSuccess(data);
            }

            @Override
            public void onError(String thongBao) {
                callback.onError(thongBao);
            }
        });
    }

    private void yeuCauDangNhapLai() {
        Session.dangXuat();
        Session.luuLocal(this);
        Toast.makeText(this, "Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, DangNhapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private ChiTietGioHang taoMucGioHang() {
        ChiTietGioHang chiTietGioHang = new ChiTietGioHang();
        chiTietGioHang.setMaVe(veHienTai.getMaVe());
        chiTietGioHang.setNgaySuDung(layNgaySuDung());
        chiTietGioHang.setSoLuongNguoiLon(soLuongNguoiLon);
        chiTietGioHang.setSoLuongTreEm(soLuongTreEm);
        chiTietGioHang.setSoLuongNguoiCaoTuoi(soLuongNguoiCaoTuoi);
        chiTietGioHang.setDonGiaNguoiLon(veHienTai.getGiaNguoiLon());
        chiTietGioHang.setDonGiaTreEm(veHienTai.getGiaTreEm());
        chiTietGioHang.setDonGiaNguoiCaoTuoi(veHienTai.getGiaNguoiCaoTuoi());
        return chiTietGioHang;
    }

    private String layNgaySuDung() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(dtpNgaySuDung.getYear(), dtpNgaySuDung.getMonth(), dtpNgaySuDung.getDayOfMonth());
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
    }
}
