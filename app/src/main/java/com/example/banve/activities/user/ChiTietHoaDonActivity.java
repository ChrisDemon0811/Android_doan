package com.example.banve.activities.user;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.banve.R;
import com.example.banve.adapters.ChiTietHoaDonAdapter;
import com.example.banve.controllers.HoaDonController;
import com.example.banve.controllers.HoaDonPdfController;
import com.example.banve.models.ChiTietHoaDon;
import com.example.banve.models.DuLieuChiTietHoaDon;
import com.example.banve.models.HoaDon;
import com.example.banve.models.NguoiDung;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.DinhDangTien;
import com.example.banve.utils.HienThi;
import com.example.banve.utils.MaQrHoaDonUtil;
import com.example.banve.utils.Session;
import com.example.banve.utils.TienIch;

import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChiTietHoaDonActivity extends AppCompatActivity {
    private View layNoiDung;
    private ProgressBar pgbDangTai;
    private TextView lblMaHoaDon;
    private TextView lblNgayLap;
    private TextView lblTrangThai;
    private TextView lblTongTien;
    private TextView lblTienGiam;
    private TextView lblThanhTien;
    private TextView lblHinhThuc;
    private TextView lblNguoiMua;
    private TextView lblVoucher;
    private LinearLayout layQrHoaDon;
    private ImageView imgQrHoaDon;
    private ProgressBar pgbTaoQr;
    private TextView lblTrangThaiQr;
    private TextView lblMaXacThuc;
    private LinearLayout layDanhSachChiTietHoaDon;
    private ProgressBar pgbXuatPdf;
    private Button btnXuatPdf;
    private Button btnDong;

    private final ExecutorService boXuLyNen = Executors.newSingleThreadExecutor();
    private final ActivityResultLauncher<String> trinhTaoTepPdf = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/pdf"),
            this::xuLyNoiLuuPdf
    );

    private HoaDonController hoaDonController;
    private HoaDon hoaDonHienTai;
    private List<ChiTietHoaDon> danhSachChiTietHienTai = Collections.emptyList();
    private Bitmap qrBitmapHienTai;
    private int maHoaDon;
    private boolean dangChonNoiLuu;
    private boolean dangXuatPdf;
    private boolean daBiHuy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Session.khoiPhuc(this);
        setContentView(R.layout.user_activity_chi_tiet_hoa_don);

        anhXa();
        hoaDonController = new HoaDonController();
        batSuKien();

        if (!Session.dangDangNhap()) {
            hienLoiVaDong("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
            return;
        }

        maHoaDon = getIntent().getIntExtra("maHoaDon", 0);
        if (maHoaDon <= 0) {
            hienLoiVaDong("Mã hóa đơn không hợp lệ");
            return;
        }

        taiHoaDonDayDu();
    }

    private void anhXa() {
        layNoiDung = findViewById(R.id.layNoiDung);
        pgbDangTai = findViewById(R.id.pgbDangTai);
        lblMaHoaDon = findViewById(R.id.lblMaHoaDon);
        lblNgayLap = findViewById(R.id.lblNgayLap);
        lblTrangThai = findViewById(R.id.lblTrangThai);
        lblTongTien = findViewById(R.id.lblTongTien);
        lblTienGiam = findViewById(R.id.lblTienGiam);
        lblThanhTien = findViewById(R.id.lblThanhTien);
        lblHinhThuc = findViewById(R.id.lblHinhThuc);
        lblNguoiMua = findViewById(R.id.lblNguoiMua);
        lblVoucher = findViewById(R.id.lblVoucher);
        layQrHoaDon = findViewById(R.id.layQrHoaDon);
        imgQrHoaDon = findViewById(R.id.imgQrHoaDon);
        pgbTaoQr = findViewById(R.id.pgbTaoQr);
        lblTrangThaiQr = findViewById(R.id.lblTrangThaiQr);
        lblMaXacThuc = findViewById(R.id.lblMaXacThuc);
        layDanhSachChiTietHoaDon = findViewById(R.id.layDanhSachChiTietHoaDon);
        pgbXuatPdf = findViewById(R.id.pgbXuatPdf);
        btnXuatPdf = findViewById(R.id.btnXuatPdf);
        btnDong = findViewById(R.id.btnDong);
    }

    private void batSuKien() {
        findViewById(R.id.btnQuayLai).setOnClickListener(v -> finish());
        btnDong.setOnClickListener(v -> finish());
        btnXuatPdf.setOnClickListener(v -> moTrinhChonNoiLuu());
    }

    private void taiHoaDonDayDu() {
        hienDangTai(true);
        int maNguoiDung = Session.nguoiDungHienTai.getMaNguoiDung();
        hoaDonController.layHoaDonDayDu(
                maHoaDon,
                maNguoiDung,
                new ApiCallback<DuLieuChiTietHoaDon>() {
                    @Override
                    public void onSuccess(DuLieuChiTietHoaDon data) {
                        if (!conCoTheCapNhatGiaoDien()) {
                            return;
                        }
                        if (data == null || data.getHoaDon() == null) {
                            hienLoiVaDong("Không nhận được dữ liệu hóa đơn");
                            return;
                        }

                        HoaDon hoaDon = data.getHoaDon();
                        if (!Session.laQuanLy()
                                && hoaDon.getMaNguoiDung()
                                != Session.nguoiDungHienTai.getMaNguoiDung()) {
                            hienLoiVaDong("Bạn không có quyền xem hóa đơn này");
                            return;
                        }

                        hoaDonHienTai = hoaDon;
                        danhSachChiTietHienTai = new ArrayList<>(data.getDanhSachChiTiet());
                        hoaDonHienTai.setDanhSachChiTiet(danhSachChiTietHienTai);
                        hienThiHoaDon();
                        hienThiDanhSachChiTiet(danhSachChiTietHienTai);
                        hienDangTai(false);
                        xuLyMaQrHoaDon();
                    }

                    @Override
                    public void onError(String thongBao) {
                        if (!conCoTheCapNhatGiaoDien()) {
                            return;
                        }
                        hienDangTai(false);
                        hienLoiVaDong(thongBao);
                    }
                }
        );
    }

    private void hienThiHoaDon() {
        double thanhTien = Math.max(
                0,
                hoaDonHienTai.getTongTien() - hoaDonHienTai.getTienGiam()
        );
        lblMaHoaDon.setText("Mã hóa đơn: #" + hoaDonHienTai.getMaHoaDon());
        lblNgayLap.setText("Ngày lập: " + dinhDangNgayGio(hoaDonHienTai.getNgayLap()));
        lblTrangThai.setText("Trạng thái: " + HienThi.trangThai(hoaDonHienTai.getTrangThai()));
        lblHinhThuc.setText(
                "Hình thức thanh toán: "
                        + HienThi.hinhThucThanhToan(hoaDonHienTai.getThanhToan())
        );
        lblTongTien.setText(
                "Tổng tiền trước giảm: " + DinhDangTien.dinhDang(hoaDonHienTai.getTongTien())
        );
        lblTienGiam.setText(
                "Tổng giảm giá: " + DinhDangTien.dinhDang(hoaDonHienTai.getTienGiam())
        );
        lblThanhTien.setText("Thành tiền: " + DinhDangTien.dinhDang(thanhTien));

        NguoiDung nguoiDung = hoaDonHienTai.getNguoiDung();
        hienThiDongNeuCo(
                lblNguoiMua,
                nguoiDung == null ? null : nguoiDung.getHoTen(),
                "Người mua: "
        );
        if (hoaDonHienTai.getMaVoucher() == null) {
            lblVoucher.setVisibility(View.GONE);
        } else {
            lblVoucher.setVisibility(View.VISIBLE);
            lblVoucher.setText("Mã voucher: #" + hoaDonHienTai.getMaVoucher());
        }
    }

    private void hienThiDongNeuCo(TextView textView, String giaTri, String tienTo) {
        if (giaTri == null || giaTri.trim().isEmpty()) {
            textView.setVisibility(View.GONE);
            return;
        }
        textView.setText(tienTo + giaTri.trim());
        textView.setVisibility(View.VISIBLE);
    }

    private void xuLyMaQrHoaDon() {
        layQrHoaDon.setVisibility(View.VISIBLE);
        qrBitmapHienTai = null;
        capNhatNutXuatPdf();

        if (!"DaThanhToan".equals(hoaDonHienTai.getTrangThai())) {
            hienCanhBaoQr("Mã QR chỉ được cấp cho hóa đơn đã thanh toán thành công");
            return;
        }
        if (hoaDonHienTai.getMaXacThuc() == null
                || hoaDonHienTai.getMaXacThuc().trim().isEmpty()) {
            hienCanhBaoQr(
                    "Hóa đơn chưa có mã xác thực. Vui lòng chạy migration mới trên Supabase."
            );
            return;
        }

        pgbTaoQr.setVisibility(View.VISIBLE);
        imgQrHoaDon.setVisibility(View.INVISIBLE);
        lblMaXacThuc.setVisibility(View.GONE);
        lblTrangThaiQr.setText("Đang tạo mã QR hóa đơn");
        HoaDon hoaDonCanTaoQr = hoaDonHienTai;

        boXuLyNen.execute(() -> {
            try {
                String noiDungQr = MaQrHoaDonUtil.taoNoiDungQr(hoaDonCanTaoQr);
                Bitmap bitmap = MaQrHoaDonUtil.taoBitmapQr(noiDungQr, 720);
                runOnUiThread(() -> {
                    if (!conCoTheCapNhatGiaoDien() || hoaDonHienTai != hoaDonCanTaoQr) {
                        return;
                    }
                    qrBitmapHienTai = bitmap;
                    imgQrHoaDon.setImageBitmap(bitmap);
                    imgQrHoaDon.setVisibility(View.VISIBLE);
                    pgbTaoQr.setVisibility(View.GONE);
                    lblTrangThaiQr.setText("Xuất trình mã QR này khi kiểm tra vé");
                    lblMaXacThuc.setText("Mã xác thực: " + hoaDonHienTai.getMaXacThuc());
                    lblMaXacThuc.setVisibility(View.VISIBLE);
                    capNhatNutXuatPdf();
                });
            } catch (IllegalArgumentException | OutOfMemoryError exception) {
                runOnUiThread(() -> {
                    if (conCoTheCapNhatGiaoDien()) {
                        hienCanhBaoQr("Không thể tạo mã QR hóa đơn");
                    }
                });
            }
        });
    }

    private void hienCanhBaoQr(String thongBao) {
        pgbTaoQr.setVisibility(View.GONE);
        imgQrHoaDon.setImageDrawable(null);
        imgQrHoaDon.setVisibility(View.INVISIBLE);
        lblMaXacThuc.setVisibility(View.GONE);
        lblTrangThaiQr.setText(thongBao);
        lblTrangThaiQr.setTextColor(ContextCompat.getColor(this, R.color.mauNguyHiem));
        capNhatNutXuatPdf();
    }

    private void hienThiDanhSachChiTiet(List<ChiTietHoaDon> danhSachChiTiet) {
        layDanhSachChiTietHoaDon.removeAllViews();
        if (danhSachChiTiet == null || danhSachChiTiet.isEmpty()) {
            TextView lblTrong = new TextView(this);
            lblTrong.setText("Hóa đơn này chưa có dữ liệu chi tiết vé.");
            lblTrong.setTextColor(ContextCompat.getColor(this, R.color.mauChuPhu));
            lblTrong.setTextSize(15);
            lblTrong.setPadding(0, 16, 0, 16);
            layDanhSachChiTietHoaDon.addView(lblTrong);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (ChiTietHoaDon chiTiet : danhSachChiTiet) {
            View itemView = inflater.inflate(
                    R.layout.user_item_chi_tiet_hoa_don,
                    layDanhSachChiTietHoaDon,
                    false
            );
            ChiTietHoaDonAdapter.hienThiChiTiet(itemView, chiTiet);
            layDanhSachChiTietHoaDon.addView(itemView);
        }
    }

    private void moTrinhChonNoiLuu() {
        if (!duDieuKienXuatPdf()) {
            TienIch.hienToast(this, "Hóa đơn chưa sẵn sàng để xuất PDF");
            return;
        }

        dangChonNoiLuu = true;
        capNhatNutXuatPdf();
        try {
            trinhTaoTepPdf.launch(taoTenTepPdf());
        } catch (ActivityNotFoundException exception) {
            dangChonNoiLuu = false;
            capNhatNutXuatPdf();
            TienIch.hienAlert(
                    this,
                    "Không thể chọn nơi lưu",
                    "Thiết bị không có ứng dụng hỗ trợ tạo file PDF."
            );
        }
    }

    private void xuLyNoiLuuPdf(Uri uri) {
        dangChonNoiLuu = false;
        if (uri == null) {
            capNhatNutXuatPdf();
            return;
        }
        if (!duDieuKienXuatPdf()) {
            capNhatNutXuatPdf();
            return;
        }

        dangXuatPdf = true;
        pgbXuatPdf.setVisibility(View.VISIBLE);
        capNhatNutXuatPdf();

        HoaDon hoaDonCanXuat = hoaDonHienTai;
        List<ChiTietHoaDon> chiTietCanXuat = new ArrayList<>(danhSachChiTietHienTai);
        Bitmap qrCanXuat = qrBitmapHienTai;

        boXuLyNen.execute(() -> {
            try {
                OutputStream outputStream = getApplicationContext()
                        .getContentResolver()
                        .openOutputStream(uri, "w");
                if (outputStream == null) {
                    throw new IOException("Không mở được nơi lưu file PDF");
                }
                new HoaDonPdfController(getApplicationContext()).xuatPdf(
                        hoaDonCanXuat,
                        chiTietCanXuat,
                        qrCanXuat,
                        outputStream
                );
                hoanTatXuatPdf(null);
            } catch (OutOfMemoryError error) {
                hoanTatXuatPdf("Thiết bị không đủ bộ nhớ để xuất PDF hóa đơn.");
            } catch (Exception exception) {
                String thongBao = exception.getMessage();
                hoanTatXuatPdf(
                        thongBao == null || thongBao.trim().isEmpty()
                                ? "Không thể ghi file PDF hóa đơn."
                                : thongBao
                );
            }
        });
    }

    private void hoanTatXuatPdf(String thongBaoLoi) {
        runOnUiThread(() -> {
            if (!conCoTheCapNhatGiaoDien()) {
                return;
            }
            dangXuatPdf = false;
            pgbXuatPdf.setVisibility(View.GONE);
            capNhatNutXuatPdf();
            if (thongBaoLoi == null) {
                TienIch.hienToast(this, "Đã xuất PDF hóa đơn thành công");
            } else {
                TienIch.hienAlert(this, "Lỗi xuất PDF", thongBaoLoi);
            }
        });
    }

    private boolean duDieuKienXuatPdf() {
        return hoaDonHienTai != null
                && "DaThanhToan".equals(hoaDonHienTai.getTrangThai())
                && qrBitmapHienTai != null
                && !qrBitmapHienTai.isRecycled()
                && !dangXuatPdf;
    }

    private void capNhatNutXuatPdf() {
        if (btnXuatPdf == null) {
            return;
        }
        btnXuatPdf.setEnabled(duDieuKienXuatPdf() && !dangChonNoiLuu);
        if (dangXuatPdf) {
            btnXuatPdf.setText("Đang xuất PDF...");
        } else if (dangChonNoiLuu) {
            btnXuatPdf.setText("Chọn nơi lưu...");
        } else {
            btnXuatPdf.setText("Xuất PDF hóa đơn");
        }
    }

    private String taoTenTepPdf() {
        String ngay = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        return "HoaDon_" + hoaDonHienTai.getMaHoaDon() + "_" + ngay + ".pdf";
    }

    private void hienDangTai(boolean dangTai) {
        pgbDangTai.setVisibility(dangTai ? View.VISIBLE : View.GONE);
        layNoiDung.setVisibility(dangTai ? View.GONE : View.VISIBLE);
        btnXuatPdf.setEnabled(false);
    }

    private void hienLoiVaDong(String thongBao) {
        if (!conCoTheCapNhatGiaoDien()) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Không thể mở hóa đơn")
                .setMessage(
                        thongBao == null || thongBao.trim().isEmpty()
                                ? "Đã xảy ra lỗi khi tải hóa đơn."
                                : thongBao
                )
                .setCancelable(false)
                .setPositiveButton("Đồng ý", (dialog, which) -> finish())
                .show();
    }

    private String dinhDangNgayGio(String ngayGio) {
        if (ngayGio == null || ngayGio.trim().isEmpty()) {
            return "";
        }
        String[] dinhDangNguon = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                "yyyy-MM-dd'T'HH:mm:ssX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
        };

        for (String dinhDang : dinhDangNguon) {
            try {
                Date ngay = new SimpleDateFormat(dinhDang, Locale.getDefault()).parse(ngayGio);
                if (ngay != null) {
                    return new SimpleDateFormat(
                            "dd/MM/yyyy HH:mm",
                            Locale.getDefault()
                    ).format(ngay);
                }
            } catch (ParseException ignored) {
            }
        }
        return ngayGio;
    }

    private boolean conCoTheCapNhatGiaoDien() {
        return !daBiHuy && !isFinishing() && !isDestroyed();
    }

    @Override
    protected void onDestroy() {
        daBiHuy = true;
        boXuLyNen.shutdown();
        super.onDestroy();
    }
}
