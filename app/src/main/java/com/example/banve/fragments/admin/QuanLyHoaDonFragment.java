package com.example.banve.fragments.admin;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.banve.R;
import com.example.banve.adapters.ChiTietHoaDonAdapter;
import com.example.banve.adapters.HoaDonQuanLyAdapter;
import com.example.banve.controllers.HoaDonController;
import com.example.banve.models.ChiTietHoaDon;
import com.example.banve.models.HoaDon;
import com.example.banve.models.NguoiDung;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.DinhDangTien;
import com.example.banve.utils.HienThi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QuanLyHoaDonFragment extends Fragment {
    private EditText edtTimKiem;
    private LinearLayout layBoLoc;
    private Button btnBoLoc;
    private Button btnTuNgay;
    private Button btnDenNgay;
    private Button btnLoc;
    private RecyclerView rcvDanhSachHoaDon;
    private HoaDonQuanLyAdapter adapter;
    private HoaDonController hoaDonController;
    private final List<HoaDon> danhSachGoc = new ArrayList<>();
    private final Calendar tuNgayCalendar = Calendar.getInstance();
    private final Calendar denNgayCalendar = Calendar.getInstance();
    private final SimpleDateFormat dinhDangNgay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_quan_ly_hoa_don, container, false);
        anhXa(view);
        hoaDonController = new HoaDonController();
        khoiTaoNgayLoc();
        khoiTaoRecyclerView();
        batSuKien();
        taiDanhSachHoaDon();
        return view;
    }

    private void anhXa(View view) {
        edtTimKiem = view.findViewById(R.id.edtTimKiem);
        layBoLoc = view.findViewById(R.id.layBoLoc);
        btnBoLoc = view.findViewById(R.id.btnBoLoc);
        btnTuNgay = view.findViewById(R.id.btnTuNgay);
        btnDenNgay = view.findViewById(R.id.btnDenNgay);
        btnLoc = view.findViewById(R.id.btnLoc);
        rcvDanhSachHoaDon = view.findViewById(R.id.rcvDanhSachHoaDon);
    }

    private void khoiTaoNgayLoc() {
        tuNgayCalendar.setTime(new Date());
        tuNgayCalendar.add(Calendar.MONTH, -1);
        duaVeDauNgay(tuNgayCalendar);

        denNgayCalendar.setTime(new Date());
        duaVeCuoiNgay(denNgayCalendar);
        capNhatNutNgay();
    }

    private void khoiTaoRecyclerView() {
        adapter = new HoaDonQuanLyAdapter(this::moDialogChiTietHoaDon);
        rcvDanhSachHoaDon.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvDanhSachHoaDon.setNestedScrollingEnabled(false);
        rcvDanhSachHoaDon.setAdapter(adapter);
    }

    private void batSuKien() {
        btnBoLoc.setOnClickListener(v -> doiTrangThaiBoLoc());
        btnTuNgay.setOnClickListener(v -> moDialogChonNgay(tuNgayCalendar, false));
        btnDenNgay.setOnClickListener(v -> moDialogChonNgay(denNgayCalendar, true));
        btnLoc.setOnClickListener(v -> locTheoKhoangNgay());
        edtTimKiem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                locTheoKhoangNgay();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void doiTrangThaiBoLoc() {
        if (layBoLoc.getVisibility() == View.VISIBLE) {
            layBoLoc.setVisibility(View.GONE);
        btnBoLoc.setText("Lọc");
        } else {
            layBoLoc.setVisibility(View.VISIBLE);
            btnBoLoc.setText("Ẩn lọc");
        }
    }

    private void taiDanhSachHoaDon() {
        hoaDonController.layDanhSachHoaDonQuanLy(new ApiCallback<List<HoaDon>>() {
            @Override
            public void onSuccess(List<HoaDon> data) {
                danhSachGoc.clear();
                if (data != null) {
                    danhSachGoc.addAll(data);
                }
                locTheoKhoangNgay();
            }

            @Override
            public void onError(String thongBao) {
                baoLoi("Lỗi hóa đơn", thongBao);
            }
        });
    }

    private void locTheoKhoangNgay() {
        Calendar tuNgay = (Calendar) tuNgayCalendar.clone();
        Calendar denNgay = (Calendar) denNgayCalendar.clone();

        if (tuNgay.after(denNgay)) {
            baoLoi("Lỗi lọc hóa đơn", "Từ ngày không được lớn hơn đến ngày");
            return;
        }

        String tuKhoa = edtTimKiem.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<HoaDon> danhSachLoc = new ArrayList<>();
        for (HoaDon hoaDon : danhSachGoc) {
            Date ngayLap = parseNgayGio(hoaDon.getNgayLap());
            if (ngayLap == null) {
                continue;
            }

            boolean dungNgay = !ngayLap.before(tuNgay.getTime()) && !ngayLap.after(denNgay.getTime());
            boolean dungTuKhoa = tuKhoa.isEmpty() || hoaDonKhopTuKhoa(hoaDon, tuKhoa);
            if (dungNgay && dungTuKhoa) {
                danhSachLoc.add(hoaDon);
            }
        }
        adapter.capNhatDuLieu(danhSachLoc);
    }

    private boolean hoaDonKhopTuKhoa(HoaDon hoaDon, String tuKhoa) {
        return String.valueOf(hoaDon.getMaHoaDon()).contains(tuKhoa)
                || chuaTuKhoa(layHoTenKhach(hoaDon), tuKhoa)
                || chuaTuKhoa(hoaDon.getThanhToan(), tuKhoa)
                || chuaTuKhoa(HienThi.hinhThucThanhToan(hoaDon.getThanhToan()), tuKhoa)
                || chuaTuKhoa(hoaDon.getTrangThai(), tuKhoa)
                || chuaTuKhoa(HienThi.trangThai(hoaDon.getTrangThai()), tuKhoa);
    }

    private boolean chuaTuKhoa(String giaTri, String tuKhoa) {
        return giaTri != null && giaTri.toLowerCase(Locale.ROOT).contains(tuKhoa);
    }

    private void moDialogChonNgay(Calendar ngayDangChon, boolean cuoiNgay) {
        new DatePickerDialog(
                requireContext(),
                (view, nam, thang, ngay) -> {
                    ngayDangChon.set(nam, thang, ngay);
                    if (cuoiNgay) {
                        duaVeCuoiNgay(ngayDangChon);
                    } else {
                        duaVeDauNgay(ngayDangChon);
                    }
                    capNhatNutNgay();
                },
                ngayDangChon.get(Calendar.YEAR),
                ngayDangChon.get(Calendar.MONTH),
                ngayDangChon.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void capNhatNutNgay() {
        btnTuNgay.setText("Từ: " + dinhDangNgay.format(tuNgayCalendar.getTime()));
        btnDenNgay.setText("Đến: " + dinhDangNgay.format(denNgayCalendar.getTime()));
    }

    private void duaVeDauNgay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void duaVeCuoiNgay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
    }

    private void moDialogChiTietHoaDon(HoaDon hoaDon) {
        if (hoaDon == null || hoaDon.getMaHoaDon() <= 0) {
            baoLoi("Lỗi chi tiết hóa đơn", "Hóa đơn không hợp lệ");
            return;
        }

        View view = getLayoutInflater().inflate(R.layout.admin_dialog_chi_tiet_hoa_don_quan_ly, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Chi tiết hóa đơn")
                .setView(view)
                .create();

        TextView lblMaHoaDon = view.findViewById(R.id.lblMaHoaDon);
        TextView lblHoTenKhach = view.findViewById(R.id.lblHoTenKhach);
        TextView lblNgayLap = view.findViewById(R.id.lblNgayLap);
        TextView lblTongTien = view.findViewById(R.id.lblTongTien);
        TextView lblTienGiam = view.findViewById(R.id.lblTienGiam);
        TextView lblPhaiTra = view.findViewById(R.id.lblPhaiTra);
        TextView lblHinhThuc = view.findViewById(R.id.lblHinhThuc);
        TextView lblTrangThai = view.findViewById(R.id.lblTrangThai);
        LinearLayout layDanhSachChiTietHoaDon = view.findViewById(R.id.layDanhSachChiTietHoaDon);
        Button btnDong = view.findViewById(R.id.btnDong);

        lblMaHoaDon.setText("Mã hóa đơn: " + hoaDon.getMaHoaDon());
        lblHoTenKhach.setText("Khách hàng: " + layHoTenKhach(hoaDon));
        lblNgayLap.setText("Ngày lập: " + dinhDangNgayGio(hoaDon.getNgayLap()));
        lblTongTien.setText("Tổng tiền: " + DinhDangTien.dinhDang(hoaDon.getTongTien()));
        lblTienGiam.setText("Tiền giảm: " + DinhDangTien.dinhDang(hoaDon.getTienGiam()));
        lblPhaiTra.setText("Phải trả: " + DinhDangTien.dinhDang(Math.max(0, hoaDon.getTongTien() - hoaDon.getTienGiam())));
        lblHinhThuc.setText("Hình thức thanh toán: " + HienThi.hinhThucThanhToan(hoaDon.getThanhToan()));
        lblTrangThai.setText("Trạng thái: " + HienThi.trangThai(hoaDon.getTrangThai()));

        btnDong.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        taiChiTietHoaDon(hoaDon.getMaHoaDon(), layDanhSachChiTietHoaDon);
    }

    private void taiChiTietHoaDon(int maHoaDon, LinearLayout layDanhSachChiTietHoaDon) {
        hoaDonController.layChiTietHoaDon(maHoaDon, new ApiCallback<List<ChiTietHoaDon>>() {
            @Override
            public void onSuccess(List<ChiTietHoaDon> data) {
                hienThiDanhSachChiTiet(data, layDanhSachChiTietHoaDon);
            }

            @Override
            public void onError(String thongBao) {
                baoLoi("Lỗi chi tiết hóa đơn", thongBao);
            }
        });
    }

    private void hienThiDanhSachChiTiet(List<ChiTietHoaDon> danhSachChiTiet, LinearLayout layDanhSachChiTietHoaDon) {
        layDanhSachChiTietHoaDon.removeAllViews();
        if (danhSachChiTiet == null || danhSachChiTiet.isEmpty()) {
            TextView lblTrong = new TextView(requireContext());
            lblTrong.setText("Chưa có chi tiết vé cho hóa đơn này.");
            lblTrong.setTextSize(16);
            layDanhSachChiTietHoaDon.addView(lblTrong);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (ChiTietHoaDon chiTiet : danhSachChiTiet) {
            View itemView = inflater.inflate(R.layout.user_item_chi_tiet_hoa_don, layDanhSachChiTietHoaDon, false);
            ChiTietHoaDonAdapter.hienThiChiTiet(itemView, chiTiet);
            layDanhSachChiTietHoaDon.addView(itemView);
        }
    }

    private String layHoTenKhach(HoaDon hoaDon) {
        NguoiDung nguoiDung = hoaDon.getNguoiDung();
        if (nguoiDung == null || nguoiDung.getHoTen() == null || nguoiDung.getHoTen().trim().isEmpty()) {
            return "Mã người dùng " + hoaDon.getMaNguoiDung();
        }
        return nguoiDung.getHoTen();
    }

    private String giaTri(String giaTri) {
        return giaTri == null ? "" : giaTri;
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
        return giaTri(thanhToan);
    }

    private String dinhDangNgayGio(String ngayGio) {
        Date ngay = parseNgayGio(ngayGio);
        if (ngay == null) {
            return ngayGio == null ? "" : ngayGio;
        }
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(ngay);
    }

    private Date parseNgayGio(String ngayGio) {
        if (ngayGio == null || ngayGio.trim().isEmpty()) {
            return null;
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
                return new SimpleDateFormat(dinhDang, Locale.getDefault()).parse(ngayGio);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private void baoLoi(String tieuDe, String noiDung) {
        new AlertDialog.Builder(requireContext())
                .setTitle(tieuDe)
                .setMessage(noiDung)
                .setPositiveButton("Đồng ý", null)
                .show();
    }
}
