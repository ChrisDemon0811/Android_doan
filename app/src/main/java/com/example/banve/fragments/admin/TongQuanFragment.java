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

import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TongQuanFragment extends Fragment {
    private Button btnTuNgay;
    private Button btnDenNgay;
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
    private ThongKeLoaiVeAdapter thongKeLoaiVeAdapter;
    private ThongKeNgayAdapter thongKeNgayAdapter;
    private ThongKeThangAdapter thongKeThangAdapter;
    private ThongKeController thongKeController;
    private AIThongKeController aiThongKeController;
    private final Calendar tuNgayCalendar = Calendar.getInstance();
    private final Calendar denNgayCalendar = Calendar.getInstance();
    private final SimpleDateFormat dinhDangNgay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_thong_ke, container, false);
        anhXa(view);
        thongKeController = new ThongKeController();
        aiThongKeController = new AIThongKeController();
        khoiTaoNgayMacDinh();
        khoiTaoRecyclerView();
        batSuKien();
        taiThongKe();
        return view;
    }

    private void anhXa(View view) {
        btnTuNgay = view.findViewById(R.id.btnTuNgay);
        btnDenNgay = view.findViewById(R.id.btnDenNgay);
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

    private void khoiTaoNgayMacDinh() {
        tuNgayCalendar.setTime(new Date());
        tuNgayCalendar.add(Calendar.DAY_OF_MONTH, -30);
        duaVeDauNgay(tuNgayCalendar);

        denNgayCalendar.setTime(new Date());
        duaVeDauNgay(denNgayCalendar);
        capNhatNutNgay();
    }

    private void khoiTaoRecyclerView() {
        thongKeLoaiVeAdapter = new ThongKeLoaiVeAdapter();
        thongKeNgayAdapter = new ThongKeNgayAdapter();
        thongKeThangAdapter = new ThongKeThangAdapter();

        rcvThongKeLoaiVe.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvThongKeLoaiVe.setAdapter(thongKeLoaiVeAdapter);
        rcvDoanhThuNgay.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvDoanhThuNgay.setAdapter(thongKeNgayAdapter);
        rcvDoanhThuThang.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvDoanhThuThang.setAdapter(thongKeThangAdapter);
    }

    private void batSuKien() {
        btnTuNgay.setOnClickListener(v -> moDialogChonNgay(tuNgayCalendar, this::capNhatNutNgay));
        btnDenNgay.setOnClickListener(v -> moDialogChonNgay(denNgayCalendar, this::capNhatNutNgay));
        btnLoc.setOnClickListener(v -> taiThongKe());
        btnPhanTichAI.setOnClickListener(v -> phanTichBangAI());
    }

    private void taiThongKe() {
        Date tuNgay = tuNgayCalendar.getTime();
        Date denNgay = denNgayCalendar.getTime();

        if (tuNgay.after(denNgay)) {
            baoLoi("Lỗi thống kê", "Từ ngày không được lớn hơn đến ngày");
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
                baoLoi("Lỗi thống kê", thongBao);
            }
        });
    }

    private void phanTichBangAI() {
        Date tuNgay = tuNgayCalendar.getTime();
        Date denNgay = denNgayCalendar.getTime();

        if (tuNgay.after(denNgay)) {
            baoLoi("Lỗi phân tích AI", "Từ ngày không được lớn hơn đến ngày");
            return;
        }

        hienDangTai(true);
        aiThongKeController.phanTichThongKe(tuNgay, denNgay, new ApiCallback<String>() {
            @Override
            public void onSuccess(String data) {
                hienDangTai(false);
                hienDialogPhanTichAI(data);
            }

            @Override
            public void onError(String thongBao) {
                hienDangTai(false);
                baoLoi("Lỗi phân tích AI", thongBao);
            }
        });
    }

    private void hienDialogPhanTichAI(String noiDung) {
        if (getContext() == null) {
            return;
        }

        TextView lblNoiDung = new TextView(requireContext());
        lblNoiDung.setText(noiDung == null || noiDung.trim().isEmpty()
                ? "AI chưa trả về nội dung phân tích."
                : noiDung.trim());
        lblNoiDung.setTextSize(15);
        lblNoiDung.setLineSpacing(6, 1.05f);
        int padding = getResources().getDimensionPixelSize(R.dimen.card_padding);
        lblNoiDung.setPadding(padding, padding, padding, padding);

        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.addView(lblNoiDung);

        new AlertDialog.Builder(requireContext())
                .setTitle("🤖 AI phân tích doanh thu")
                .setView(scrollView)
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void moDialogChonNgay(Calendar ngayDangChon, Runnable sauKhiChon) {
        new DatePickerDialog(
                requireContext(),
                (view, nam, thang, ngay) -> {
                    ngayDangChon.set(nam, thang, ngay);
                    duaVeDauNgay(ngayDangChon);
                    sauKhiChon.run();
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

    private void hienThongKe(KetQuaThongKe ketQua) {
        if (ketQua == null || ketQua.getThongKeTongQuan() == null) {
            return;
        }

        ThongKeTongQuan tongQuan = ketQua.getThongKeTongQuan();
        lblTongHoaDon.setText("Tổng hóa đơn: " + tongQuan.getTongHoaDon());
        lblTongDoanhThu.setText("Tổng doanh thu: " + DinhDangTien.dinhDang(tongQuan.getTongDoanhThu()));
        lblTongTienGiam.setText("Tổng tiền giảm: " + DinhDangTien.dinhDang(tongQuan.getTongTienGiam()));
        lblTongThanhTien.setText("Tổng thành tiền: " + DinhDangTien.dinhDang(tongQuan.getTongThanhTien()));
        lblTongVeBan.setText("Tổng vé bán: " + tongQuan.getTongVeBan());

        thongKeLoaiVeAdapter.capNhatDuLieu(ketQua.getDanhSachTheoLoaiVe());
        thongKeNgayAdapter.capNhatDuLieu(ketQua.getDanhSachTheoNgay());
        thongKeThangAdapter.capNhatDuLieu(ketQua.getDanhSachTheoThang());
    }

    private void hienDangTai(boolean dangTai) {
        pgbDangTai.setVisibility(dangTai ? View.VISIBLE : View.GONE);
        btnLoc.setEnabled(!dangTai);
        btnPhanTichAI.setEnabled(!dangTai);
        btnTuNgay.setEnabled(!dangTai);
        btnDenNgay.setEnabled(!dangTai);
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
