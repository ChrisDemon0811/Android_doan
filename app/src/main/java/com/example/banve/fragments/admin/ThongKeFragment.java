package com.example.banve.fragments.admin;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.banve.R;
import com.example.banve.adapters.ThongKeLoaiVeAdapter;
import com.example.banve.adapters.ThongKeNgayAdapter;
import com.example.banve.adapters.ThongKeThangAdapter;
import com.example.banve.controllers.AIThongKeController;
import com.example.banve.controllers.ThongKeController;
import com.example.banve.models.KetQuaThongKe;
import com.example.banve.models.ThongKeTongQuan;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.DinhDangTien;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ThongKeFragment extends Fragment {
    private Button btnTuNgay;
    private Button btnDenNgay;
    private Button btnHomNay;
    private Button btnThangNay;
    private Button btnThangTruoc;
    private Button btnLoc;
    private Button btnPhanTichAI;
    private ProgressBar pgbDangTai;
    private TextView lblTongHoaDon;
    private TextView lblTongDoanhThu;
    private TextView lblTongTienGiam;
    private TextView lblTongThanhTien;
    private TextView lblTongVeBan;
    private RecyclerView rcvThongKeLoaiVe;
    private RecyclerView rcvDoanhThuNgay;
    private RecyclerView rcvDoanhThuThang;

    private ThongKeController thongKeController;
    private AIThongKeController aiThongKeController;
    private ThongKeLoaiVeAdapter thongKeLoaiVeAdapter;
    private ThongKeNgayAdapter thongKeNgayAdapter;
    private ThongKeThangAdapter thongKeThangAdapter;
    private final SimpleDateFormat dinhDangNgay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private Date tuNgay;
    private Date denNgay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_thong_ke, container, false);
        anhXa(view);
        khoiTao();
        batSuKien();
        chonThangNay();
        taiThongKe();
        return view;
    }

    private void anhXa(View view) {
        btnTuNgay = view.findViewById(R.id.btnTuNgay);
        btnDenNgay = view.findViewById(R.id.btnDenNgay);
        btnHomNay = view.findViewById(R.id.btnHomNay);
        btnThangNay = view.findViewById(R.id.btnThangNay);
        btnThangTruoc = view.findViewById(R.id.btnThangTruoc);
        btnLoc = view.findViewById(R.id.btnLoc);
        btnPhanTichAI = view.findViewById(R.id.btnPhanTichAI);
        pgbDangTai = view.findViewById(R.id.pgbDangTai);
        lblTongHoaDon = view.findViewById(R.id.lblTongHoaDon);
        lblTongDoanhThu = view.findViewById(R.id.lblTongDoanhThu);
        lblTongTienGiam = view.findViewById(R.id.lblTongTienGiam);
        lblTongThanhTien = view.findViewById(R.id.lblTongThanhTien);
        lblTongVeBan = view.findViewById(R.id.lblTongVeBan);
        rcvThongKeLoaiVe = view.findViewById(R.id.rcvThongKeLoaiVe);
        rcvDoanhThuNgay = view.findViewById(R.id.rcvDoanhThuNgay);
        rcvDoanhThuThang = view.findViewById(R.id.rcvDoanhThuThang);
    }

    private void khoiTao() {
        thongKeController = new ThongKeController();
        aiThongKeController = new AIThongKeController();
        thongKeLoaiVeAdapter = new ThongKeLoaiVeAdapter();
        thongKeNgayAdapter = new ThongKeNgayAdapter();
        thongKeThangAdapter = new ThongKeThangAdapter();

        rcvThongKeLoaiVe.setLayoutManager(new LinearLayoutManager(requireContext()));
        rcvDoanhThuNgay.setLayoutManager(new LinearLayoutManager(requireContext()));
        rcvDoanhThuThang.setLayoutManager(new LinearLayoutManager(requireContext()));
        rcvThongKeLoaiVe.setAdapter(thongKeLoaiVeAdapter);
        rcvDoanhThuNgay.setAdapter(thongKeNgayAdapter);
        rcvDoanhThuThang.setAdapter(thongKeThangAdapter);
    }

    private void batSuKien() {
        btnTuNgay.setOnClickListener(v -> moChonNgay(true));
        btnDenNgay.setOnClickListener(v -> moChonNgay(false));
        btnHomNay.setOnClickListener(v -> {
            chonHomNay();
            taiThongKe();
        });
        btnThangNay.setOnClickListener(v -> {
            chonThangNay();
            taiThongKe();
        });
        btnThangTruoc.setOnClickListener(v -> {
            chonThangTruoc();
            taiThongKe();
        });
        btnLoc.setOnClickListener(v -> taiThongKe());
        btnPhanTichAI.setOnClickListener(v -> phanTichAI());
    }

    private void chonHomNay() {
        Calendar calendar = Calendar.getInstance();
        tuNgay = calendar.getTime();
        denNgay = calendar.getTime();
        capNhatNutNgay();
    }

    private void chonThangNay() {
        Calendar calendar = Calendar.getInstance();
        denNgay = calendar.getTime();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        tuNgay = calendar.getTime();
        capNhatNutNgay();
    }

    private void chonThangTruoc() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        tuNgay = calendar.getTime();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        denNgay = calendar.getTime();
        capNhatNutNgay();
    }

    private void moChonNgay(boolean chonTuNgay) {
        Calendar calendar = Calendar.getInstance();
        Date ngayDangChon = chonTuNgay ? tuNgay : denNgay;
        if (ngayDangChon != null) {
            calendar.setTime(ngayDangChon);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, nam, thang, ngay) -> {
                    Calendar ngayMoi = Calendar.getInstance();
                    ngayMoi.set(nam, thang, ngay);
                    if (chonTuNgay) {
                        tuNgay = ngayMoi.getTime();
                    } else {
                        denNgay = ngayMoi.getTime();
                    }
                    capNhatNutNgay();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void capNhatNutNgay() {
        btnTuNgay.setText("Từ: " + dinhDangNgay.format(tuNgay));
        btnDenNgay.setText("Đến: " + dinhDangNgay.format(denNgay));
    }

    private void taiThongKe() {
        if (!kiemTraNgay()) {
            return;
        }

        hienDangTai(true);
        thongKeController.layThongKeTongQuan(tuNgay, denNgay, new ApiCallback<KetQuaThongKe>() {
            @Override
            public void onSuccess(KetQuaThongKe data) {
                hienDangTai(false);
                hienThongKe(data);
            }

            @Override
            public void onError(String thongBao) {
                hienDangTai(false);
                hienThongKeRong();
                baoLoi("Lỗi thống kê", thongBao);
            }
        });
    }

    private void phanTichAI() {
        if (!kiemTraNgay()) {
            return;
        }

        hienDangTai(true);
        btnPhanTichAI.setEnabled(false);
        aiThongKeController.phanTichThongKe(tuNgay, denNgay, new ApiCallback<String>() {
            @Override
            public void onSuccess(String data) {
                hienDangTai(false);
                btnPhanTichAI.setEnabled(true);
                hienDialogPhanTichAI(data);
            }

            @Override
            public void onError(String thongBao) {
                hienDangTai(false);
                btnPhanTichAI.setEnabled(true);
                baoLoi("AI phân tích", thongBao);
            }
        });
    }

    private boolean kiemTraNgay() {
        if (tuNgay == null || denNgay == null) {
            Toast.makeText(requireContext(), "Vui lòng chọn khoảng thời gian thống kê", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (tuNgay.after(denNgay)) {
            Toast.makeText(requireContext(), "Từ ngày không được lớn hơn đến ngày", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void hienThongKe(KetQuaThongKe ketQua) {
        if (ketQua == null || ketQua.getThongKeTongQuan() == null) {
            hienThongKeRong();
            return;
        }

        ThongKeTongQuan tongQuan = ketQua.getThongKeTongQuan();
        lblTongHoaDon.setText("Tổng hóa đơn đã thanh toán: " + tongQuan.getTongHoaDon());
        lblTongDoanhThu.setText("Doanh thu gộp: " + DinhDangTien.dinhDang(tongQuan.getTongDoanhThu()));
        lblTongTienGiam.setText("Tổng giảm giá: " + DinhDangTien.dinhDang(tongQuan.getTongTienGiam()));
        lblTongThanhTien.setText("Doanh thu thực nhận: " + DinhDangTien.dinhDang(tongQuan.getTongThanhTien()));
        lblTongVeBan.setText("Tổng vé bán: " + tongQuan.getTongVeBan());

        thongKeLoaiVeAdapter.capNhatDuLieu(ketQua.getDanhSachTheoLoaiVe());
        thongKeNgayAdapter.capNhatDuLieu(ketQua.getDanhSachTheoNgay());
        thongKeThangAdapter.capNhatDuLieu(ketQua.getDanhSachTheoThang());
    }

    private void hienThongKeRong() {
        lblTongHoaDon.setText("Tổng hóa đơn đã thanh toán: 0");
        lblTongDoanhThu.setText("Doanh thu gộp: 0 VNĐ");
        lblTongTienGiam.setText("Tổng giảm giá: 0 VNĐ");
        lblTongThanhTien.setText("Doanh thu thực nhận: 0 VNĐ");
        lblTongVeBan.setText("Tổng vé bán: 0");
        thongKeLoaiVeAdapter.capNhatDuLieu(null);
        thongKeNgayAdapter.capNhatDuLieu(null);
        thongKeThangAdapter.capNhatDuLieu(null);
    }

    private void hienDialogPhanTichAI(String noiDung) {
        if (getContext() == null) {
            return;
        }

        TextView lblNoiDung = new TextView(requireContext());
        lblNoiDung.setText(noiDung == null || noiDung.trim().isEmpty() ? "AI chưa trả về nội dung phân tích." : noiDung);
        lblNoiDung.setTextSize(15);
        lblNoiDung.setTextColor(getResources().getColor(R.color.mauChuChinh));
        lblNoiDung.setPadding(32, 24, 32, 24);

        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.addView(lblNoiDung);

        new AlertDialog.Builder(requireContext())
                .setTitle("?? AI phân tích doanh thu")
                .setView(scrollView)
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void hienDangTai(boolean dangTai) {
        pgbDangTai.setVisibility(dangTai ? View.VISIBLE : View.GONE);
    }

    private void baoLoi(String tieuDe, String noiDung) {
        if (getContext() == null) {
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(tieuDe)
                .setMessage(noiDung)
                .setPositiveButton("Đồng ý", null)
                .show();
    }
}
