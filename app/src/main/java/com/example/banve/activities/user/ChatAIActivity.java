package com.example.banve.activities.user;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.banve.R;
import com.example.banve.adapters.TinNhanAdapter;
import com.example.banve.controllers.ChatAIController;
import com.example.banve.controllers.GioHangController;
import com.example.banve.controllers.VeController;
import com.example.banve.dao.LichSuChatDAO;
import com.example.banve.models.ChiTietGioHang;
import com.example.banve.models.DeXuatThemGioHang;
import com.example.banve.models.KetQuaChatAI;
import com.example.banve.models.LichSuChat;
import com.example.banve.models.NhomKhachTuVan;
import com.example.banve.models.Ve;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.Session;
import com.example.banve.utils.TienIch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAIActivity extends AppCompatActivity {
    private RecyclerView rcvLichSuChat;
    private ProgressBar pgbDangTra;
    private EditText edtCauHoi;
    private Button btnGui;
    private Button btnTuVanTheoNhom;
    private Button btnVeReNhat;
    private Button btnHoiVoucher;
    private Button btnDonGanNhat;
    private TinNhanAdapter tinNhanAdapter;
    private LichSuChatDAO lichSuChatDAO;
    private ChatAIController chatAIController;
    private GioHangController gioHangController;
    private VeController veController;
    private boolean dangXuLy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Session.khoiPhuc(this);
        setContentView(R.layout.user_activity_chat_ai);

        anhXa();
        lichSuChatDAO = new LichSuChatDAO();
        chatAIController = new ChatAIController();
        gioHangController = new GioHangController();
        veController = new VeController();
        khoiTaoRecyclerView();
        batSuKien();
        taiLichSuChat();
    }

    private void anhXa() {
        rcvLichSuChat = findViewById(R.id.rcvLichSuChat);
        pgbDangTra = findViewById(R.id.pgbDangTra);
        edtCauHoi = findViewById(R.id.edtCauHoi);
        btnGui = findViewById(R.id.btnGui);
        btnTuVanTheoNhom = findViewById(R.id.btnTuVanTheoNhom);
        btnVeReNhat = findViewById(R.id.btnVeReNhat);
        btnHoiVoucher = findViewById(R.id.btnHoiVoucher);
        btnDonGanNhat = findViewById(R.id.btnDonGanNhat);
        findViewById(R.id.btnQuayLai).setOnClickListener(v -> finish());
    }

    private void khoiTaoRecyclerView() {
        tinNhanAdapter = new TinNhanAdapter(this::themVaoGioTuTinNhan);
        rcvLichSuChat.setLayoutManager(new LinearLayoutManager(this));
        rcvLichSuChat.setAdapter(tinNhanAdapter);
    }

    private void batSuKien() {
        btnGui.setOnClickListener(v -> guiCauHoi());
        btnTuVanTheoNhom.setOnClickListener(v -> moDialogTuVanTheoNhom());
        btnVeReNhat.setOnClickListener(v -> guiCauHoi("Vé nào rẻ nhất?"));
        btnHoiVoucher.setOnClickListener(v -> guiCauHoi("Voucher đang có điều kiện gì?"));
        btnDonGanNhat.setOnClickListener(v -> guiCauHoi("Đơn gần nhất của tôi"));
    }

    private void taiLichSuChat() {
        if (!Session.dangDangNhap()) {
            TienIch.hienAlert(this, "Thông báo", "Vui lòng đăng nhập lại");
            finish();
            return;
        }

        lichSuChatDAO.layTheoNguoiDung(Session.nguoiDungHienTai.getMaNguoiDung(), new ApiCallback<List<LichSuChat>>() {
            @Override
            public void onSuccess(List<LichSuChat> data) {
                tinNhanAdapter.capNhatTuLichSu(data);
                cuonXuongCuoi();
            }

            @Override
            public void onError(String thongBao) {
                TienIch.hienAlert(ChatAIActivity.this, "Lỗi chat AI", thongBao);
            }
        });
    }

    private void guiCauHoi() {
        String cauHoi = edtCauHoi.getText().toString().trim();
        guiCauHoi(cauHoi);
    }

    private void guiCauHoi(String cauHoi) {
        if (dangXuLy) {
            return;
        }
        if (cauHoi.isEmpty()) {
            TienIch.hienToast(this, "Vui lòng nhập câu hỏi");
            return;
        }

        tinNhanAdapter.themTinNhanUser(cauHoi);
        edtCauHoi.setText("");
        cuonXuongCuoi();
        capNhatDangTraLoi(true);

        chatAIController.guiCauHoi(cauHoi, new ApiCallback<String>() {
            @Override
            public void onSuccess(String data) {
                capNhatDangTraLoi(false);
                tinNhanAdapter.themTinNhanAI(data);
                cuonXuongCuoi();
            }

            @Override
            public void onError(String thongBao) {
                capNhatDangTraLoi(false);
                TienIch.hienAlert(ChatAIActivity.this, "Lỗi chat AI", thongBao);
            }
        });
    }

    private void moDialogTuVanTheoNhom() {
        if (dangXuLy) {
            return;
        }

        View view = getLayoutInflater().inflate(R.layout.user_dialog_tu_van_ve_theo_nhom, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();
        TextView lblSoLuongNguoiLon = view.findViewById(R.id.lblSoLuongNL);
        TextView lblSoLuongTreEm = view.findViewById(R.id.lblSoLuongTE);
        TextView lblSoLuongNguoiCaoTuoi = view.findViewById(R.id.lblSoLuongCT);
        EditText edtNgaySuDung = view.findViewById(R.id.edtNgaySuDung);
        Button btnNhanTuVan = view.findViewById(R.id.btnNhanTuVan);
        Button btnHuy = view.findViewById(R.id.btnHuy);
        int[] soLuongKhach = {0, 0, 0};

        Calendar ngayDaChon = Calendar.getInstance();
        edtNgaySuDung.setText(dinhDangNgayHienThi(ngayDaChon.getTime()));
        edtNgaySuDung.setOnClickListener(v -> moChonNgay(ngayDaChon, edtNgaySuDung));
        view.findViewById(R.id.btnGiamNL).setOnClickListener(
                v -> thayDoiSoLuongTuVan(soLuongKhach, 0, -1, lblSoLuongNguoiLon)
        );
        view.findViewById(R.id.btnTangNL).setOnClickListener(
                v -> thayDoiSoLuongTuVan(soLuongKhach, 0, 1, lblSoLuongNguoiLon)
        );
        view.findViewById(R.id.btnGiamTE).setOnClickListener(
                v -> thayDoiSoLuongTuVan(soLuongKhach, 1, -1, lblSoLuongTreEm)
        );
        view.findViewById(R.id.btnTangTE).setOnClickListener(
                v -> thayDoiSoLuongTuVan(soLuongKhach, 1, 1, lblSoLuongTreEm)
        );
        view.findViewById(R.id.btnGiamCT).setOnClickListener(
                v -> thayDoiSoLuongTuVan(soLuongKhach, 2, -1, lblSoLuongNguoiCaoTuoi)
        );
        view.findViewById(R.id.btnTangCT).setOnClickListener(
                v -> thayDoiSoLuongTuVan(soLuongKhach, 2, 1, lblSoLuongNguoiCaoTuoi)
        );
        btnHuy.setOnClickListener(v -> dialog.dismiss());
        btnNhanTuVan.setOnClickListener(v -> {
            NhomKhachTuVan nhomKhach = new NhomKhachTuVan();
            nhomKhach.setSoLuongNguoiLon(soLuongKhach[0]);
            nhomKhach.setSoLuongTreEm(soLuongKhach[1]);
            nhomKhach.setSoLuongNguoiCaoTuoi(soLuongKhach[2]);
            nhomKhach.setNgaySuDung(dinhDangNgayLuu(ngayDaChon.getTime()));
            int tongSoKhach = nhomKhach.getSoLuongNguoiLon()
                    + nhomKhach.getSoLuongTreEm()
                    + nhomKhach.getSoLuongNguoiCaoTuoi();
            if (tongSoKhach <= 0) {
                TienIch.hienToast(this, "Vui lòng chọn ít nhất một khách");
                return;
            }
            if (laNgayQuaKhu(nhomKhach.getNgaySuDung())) {
                TienIch.hienToast(this, "Ngày sử dụng không được nhỏ hơn ngày hiện tại");
                return;
            }
            dialog.dismiss();
            guiTuVanTheoNhom(nhomKhach);
        });
        dialog.show();
    }

    private void thayDoiSoLuongTuVan(int[] soLuongKhach, int viTri, int thayDoi, TextView lblSoLuong) {
        soLuongKhach[viTri] = Math.max(0, soLuongKhach[viTri] + thayDoi);
        lblSoLuong.setText(String.valueOf(soLuongKhach[viTri]));
    }

    private void guiTuVanTheoNhom(NhomKhachTuVan nhomKhach) {
        if (dangXuLy) {
            return;
        }
        String cauHoiHienThi = "Tư vấn vé cho nhóm "
                + nhomKhach.getSoLuongNguoiLon() + " người lớn, "
                + nhomKhach.getSoLuongTreEm() + " trẻ em, "
                + nhomKhach.getSoLuongNguoiCaoTuoi() + " người cao tuổi, ngày "
                + chuyenNgaySangHienThi(nhomKhach.getNgaySuDung());
        tinNhanAdapter.themTinNhanUser(cauHoiHienThi);
        cuonXuongCuoi();
        capNhatDangTraLoi(true);

        chatAIController.tuVanVeTheoNhom(nhomKhach, new ApiCallback<KetQuaChatAI>() {
            @Override
            public void onSuccess(KetQuaChatAI data) {
                capNhatDangTraLoi(false);
                tinNhanAdapter.themTinNhanAI(data.getNoiDung(), data.getDeXuatThemGioHang());
                cuonXuongCuoi();
            }

            @Override
            public void onError(String thongBao) {
                capNhatDangTraLoi(false);
                TienIch.hienAlert(ChatAIActivity.this, "Lỗi tư vấn vé", thongBao);
            }
        });
    }

    private void themVaoGioTuTinNhan(int viTri, DeXuatThemGioHang deXuat) {
        if (deXuat == null || deXuat.getMaVe() <= 0) {
            TienIch.hienAlert(this, "Lỗi giỏ hàng", "Đề xuất vé không hợp lệ");
            return;
        }
        if (!Session.dangDangNhap() || Session.nguoiDungHienTai == null) {
            TienIch.hienAlert(this, "Lỗi giỏ hàng", "Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại");
            return;
        }
        if (laNgayQuaKhu(deXuat.getNgaySuDung())) {
            TienIch.hienAlert(this, "Lỗi giỏ hàng", "Ngày sử dụng không được nhỏ hơn ngày hiện tại");
            return;
        }
        int tongSoLuong = deXuat.getSoLuongNguoiLon()
                + deXuat.getSoLuongTreEm()
                + deXuat.getSoLuongNguoiCaoTuoi();
        if (tongSoLuong <= 0) {
            TienIch.hienAlert(this, "Lỗi giỏ hàng", "Vui lòng chọn ít nhất một vé");
            return;
        }

        tinNhanAdapter.capNhatTrangThaiThemGio(viTri, true, false);
        veController.layTheoMa(deXuat.getMaVe(), new ApiCallback<Ve>() {
            @Override
            public void onSuccess(Ve ve) {
                if (!"HoatDong".equals(ve.getTrangThai())) {
                    tinNhanAdapter.capNhatTrangThaiThemGio(viTri, false, false);
                    TienIch.hienAlert(ChatAIActivity.this, "Lỗi giỏ hàng", "Vé này không còn hoạt động");
                    return;
                }
                String loiGia = kiemTraGiaMoiNhat(deXuat, ve);
                if (loiGia != null) {
                    tinNhanAdapter.capNhatTrangThaiThemGio(viTri, false, false);
                    TienIch.hienAlert(ChatAIActivity.this, "Lỗi giỏ hàng", loiGia);
                    return;
                }

                ChiTietGioHang chiTiet = new ChiTietGioHang();
                chiTiet.setMaVe(ve.getMaVe());
                chiTiet.setNgaySuDung(deXuat.getNgaySuDung());
                chiTiet.setSoLuongNguoiLon(deXuat.getSoLuongNguoiLon());
                chiTiet.setSoLuongTreEm(deXuat.getSoLuongTreEm());
                chiTiet.setSoLuongNguoiCaoTuoi(deXuat.getSoLuongNguoiCaoTuoi());
                chiTiet.setDonGiaNguoiLon(ve.getGiaNguoiLon());
                chiTiet.setDonGiaTreEm(ve.getGiaTreEm());
                chiTiet.setDonGiaNguoiCaoTuoi(ve.getGiaNguoiCaoTuoi());
                gioHangController.themHoacGopMuc(
                        Session.nguoiDungHienTai.getMaNguoiDung(),
                        chiTiet,
                        new ApiCallback<ChiTietGioHang>() {
                            @Override
                            public void onSuccess(ChiTietGioHang data) {
                                tinNhanAdapter.capNhatTrangThaiThemGio(viTri, false, true);
                                TienIch.hienToast(ChatAIActivity.this, "Đã thêm vé vào giỏ hàng");
                            }

                            @Override
                            public void onError(String thongBao) {
                                tinNhanAdapter.capNhatTrangThaiThemGio(viTri, false, false);
                                TienIch.hienAlert(ChatAIActivity.this, "Lỗi giỏ hàng", thongBao);
                            }
                        }
                );
            }

            @Override
            public void onError(String thongBao) {
                tinNhanAdapter.capNhatTrangThaiThemGio(viTri, false, false);
                TienIch.hienAlert(ChatAIActivity.this, "Lỗi giỏ hàng", thongBao);
            }
        });
    }

    private void capNhatDangTraLoi(boolean dangTraLoi) {
        dangXuLy = dangTraLoi;
        pgbDangTra.setVisibility(dangTraLoi ? View.VISIBLE : View.GONE);
        btnGui.setEnabled(!dangTraLoi);
        edtCauHoi.setEnabled(!dangTraLoi);
        btnTuVanTheoNhom.setEnabled(!dangTraLoi);
        btnVeReNhat.setEnabled(!dangTraLoi);
        btnHoiVoucher.setEnabled(!dangTraLoi);
        btnDonGanNhat.setEnabled(!dangTraLoi);
    }

    private void cuonXuongCuoi() {
        rcvLichSuChat.post(() -> {
            int soTinNhan = tinNhanAdapter.getItemCount();
            if (soTinNhan > 0) {
                rcvLichSuChat.scrollToPosition(soTinNhan - 1);
            }
        });
    }

    private void moChonNgay(Calendar ngayDaChon, EditText edtNgaySuDung) {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (datePicker, nam, thang, ngay) -> {
                    ngayDaChon.set(nam, thang, ngay);
                    edtNgaySuDung.setText(dinhDangNgayHienThi(ngayDaChon.getTime()));
                },
                ngayDaChon.get(Calendar.YEAR),
                ngayDaChon.get(Calendar.MONTH),
                ngayDaChon.get(Calendar.DAY_OF_MONTH)
        );
        Calendar homNay = Calendar.getInstance();
        datDauNgay(homNay);
        dialog.getDatePicker().setMinDate(homNay.getTimeInMillis());
        dialog.show();
    }

    private String kiemTraGiaMoiNhat(DeXuatThemGioHang deXuat, Ve ve) {
        if (deXuat.getSoLuongNguoiLon() > 0 && ve.getGiaNguoiLon() <= 0) {
            return "Vé hiện không có giá hợp lệ cho người lớn";
        }
        if (deXuat.getSoLuongTreEm() > 0 && ve.getGiaTreEm() <= 0) {
            return "Vé hiện không có giá hợp lệ cho trẻ em";
        }
        if (deXuat.getSoLuongNguoiCaoTuoi() > 0 && ve.getGiaNguoiCaoTuoi() <= 0) {
            return "Vé hiện không có giá hợp lệ cho người cao tuổi";
        }
        return null;
    }

    private boolean laNgayQuaKhu(String ngayLuu) {
        Date ngay = parseNgay(ngayLuu, "yyyy-MM-dd");
        if (ngay == null) {
            return true;
        }
        Calendar homNay = Calendar.getInstance();
        datDauNgay(homNay);
        return ngay.before(homNay.getTime());
    }

    private String chuyenNgaySangHienThi(String ngayLuu) {
        Date ngay = parseNgay(ngayLuu, "yyyy-MM-dd");
        return ngay == null ? ngayLuu : dinhDangNgayHienThi(ngay);
    }

    private Date parseNgay(String ngay, String mau) {
        if (ngay == null || ngay.trim().isEmpty()) {
            return null;
        }
        SimpleDateFormat dinhDang = new SimpleDateFormat(mau, Locale.getDefault());
        dinhDang.setLenient(false);
        try {
            return dinhDang.parse(ngay);
        } catch (ParseException exception) {
            return null;
        }
    }

    private String dinhDangNgayHienThi(Date ngay) {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(ngay);
    }

    private String dinhDangNgayLuu(Date ngay) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(ngay);
    }

    private void datDauNgay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}
