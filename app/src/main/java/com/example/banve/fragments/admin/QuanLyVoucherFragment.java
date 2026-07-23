package com.example.banve.fragments.admin;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.banve.R;
import com.example.banve.adapters.VoucherQuanLyAdapter;
import com.example.banve.controllers.LoaiVeController;
import com.example.banve.controllers.PhanTichVoucherController;
import com.example.banve.controllers.VeController;
import com.example.banve.controllers.VoucherController;
import com.example.banve.models.LoaiVe;
import com.example.banve.models.LuaChonDanhMuc;
import com.example.banve.models.PhanTichVoucher;
import com.example.banve.models.Ve;
import com.example.banve.models.Voucher;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.TienIch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QuanLyVoucherFragment extends Fragment {
    private EditText edtTimKiem;
    private LinearLayout layBoLoc;
    private Button btnBoLoc;
    private Button btnThemVoucher;
    private RecyclerView rcvDanhSachVoucher;
    private VoucherQuanLyAdapter adapter;
    private VoucherController voucherController;
    private LoaiVeController loaiVeController;
    private VeController veController;
    private PhanTichVoucherController phanTichVoucherController;
    private final List<Voucher> danhSachGoc = new ArrayList<>();
    private final List<LoaiVe> danhSachLoaiVe = new ArrayList<>();
    private final List<Ve> danhSachVe = new ArrayList<>();
    private boolean daTaiLoaiVe;
    private boolean daTaiVe;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_quan_ly_voucher, container, false);
        anhXa(view);
        voucherController = new VoucherController();
        loaiVeController = new LoaiVeController();
        veController = new VeController();
        phanTichVoucherController = new PhanTichVoucherController();
        khoiTaoRecyclerView();
        batSuKien();
        taiDuLieuPhamViApDung();
        taiDanhSachVoucher();
        return view;
    }

    private void anhXa(View view) {
        edtTimKiem = view.findViewById(R.id.edtTimKiem);
        layBoLoc = view.findViewById(R.id.layBoLoc);
        btnBoLoc = view.findViewById(R.id.btnBoLoc);
        btnThemVoucher = view.findViewById(R.id.btnThemVoucher);
        rcvDanhSachVoucher = view.findViewById(R.id.rcvDanhSachVoucher);
        btnThemVoucher.setEnabled(false);
    }

    private void khoiTaoRecyclerView() {
        adapter = new VoucherQuanLyAdapter(new VoucherQuanLyAdapter.OnVoucherQuanLyClickListener() {
            @Override
            public void onSua(Voucher voucher) {
                moDialogNhapVoucher(voucher);
            }

            @Override
            public void onXoa(Voucher voucher) {
                xacNhanXoaVoucher(voucher);
            }
        });
        rcvDanhSachVoucher.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvDanhSachVoucher.setAdapter(adapter);
    }

    private void batSuKien() {
        btnBoLoc.setOnClickListener(v -> doiTrangThaiBoLoc());
        btnThemVoucher.setOnClickListener(v -> moDialogNhapVoucher(null));
        edtTimKiem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                apDungLoc();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void doiTrangThaiBoLoc() {
        boolean dangHien = layBoLoc.getVisibility() == View.VISIBLE;
        layBoLoc.setVisibility(dangHien ? View.GONE : View.VISIBLE);
        btnBoLoc.setText(dangHien ? "Lọc" : "Ẩn lọc");
    }

    private void taiDuLieuPhamViApDung() {
        daTaiLoaiVe = false;
        daTaiVe = false;
        loaiVeController.layDanhSachQuanLy(new ApiCallback<List<LoaiVe>>() {
            @Override
            public void onSuccess(List<LoaiVe> data) {
                danhSachLoaiVe.clear();
                if (data != null) {
                    danhSachLoaiVe.addAll(data);
                }
                daTaiLoaiVe = true;
                capNhatTrangThaiDuLieuPhamVi();
            }

            @Override
            public void onError(String thongBao) {
                baoLoi("Lỗi loại vé", "Không thể tải loại vé để cấu hình phạm vi áp dụng");
            }
        });

        veController.layDanhSachVeQuanLy(new ApiCallback<List<Ve>>() {
            @Override
            public void onSuccess(List<Ve> data) {
                danhSachVe.clear();
                if (data != null) {
                    danhSachVe.addAll(data);
                }
                daTaiVe = true;
                capNhatTrangThaiDuLieuPhamVi();
            }

            @Override
            public void onError(String thongBao) {
                baoLoi("Lỗi vé", "Không thể tải vé để cấu hình phạm vi áp dụng");
            }
        });
    }

    private void capNhatTrangThaiDuLieuPhamVi() {
        boolean daSanSang = daTaiLoaiVe && daTaiVe;
        btnThemVoucher.setEnabled(daSanSang);
        adapter.capNhatPhamVi(danhSachLoaiVe, danhSachVe);
    }

    private void taiDanhSachVoucher() {
        voucherController.layDanhSachVoucher(new ApiCallback<List<Voucher>>() {
            @Override
            public void onSuccess(List<Voucher> data) {
                danhSachGoc.clear();
                if (data != null) {
                    danhSachGoc.addAll(data);
                }
                apDungLoc();
            }

            @Override
            public void onError(String thongBao) {
                baoLoi("Lỗi voucher", thongBao);
            }
        });
    }

    private void apDungLoc() {
        String tuKhoa = edtTimKiem.getText().toString().trim().toLowerCase(Locale.ROOT);
        if (tuKhoa.isEmpty()) {
            adapter.capNhatDuLieu(danhSachGoc);
            return;
        }

        List<Voucher> danhSachLoc = new ArrayList<>();
        for (Voucher voucher : danhSachGoc) {
            if (chuaTuKhoa(voucher.getMaGiamGia(), tuKhoa)
                    || chuaTuKhoa(voucher.getTenVoucher(), tuKhoa)
                    || chuaTuKhoa(voucher.getKieuGiamGia(), tuKhoa)
                    || chuaTuKhoa(voucher.getMucTieu(), tuKhoa)) {
                danhSachLoc.add(voucher);
            }
        }
        adapter.capNhatDuLieu(danhSachLoc);
    }

    private boolean chuaTuKhoa(String giaTri, String tuKhoa) {
        return giaTri != null && giaTri.toLowerCase(Locale.ROOT).contains(tuKhoa);
    }

    private void moDialogNhapVoucher(Voucher voucherCanSua) {
        if (!daTaiLoaiVe || !daTaiVe) {
            TienIch.hienToast(requireContext(), "Dữ liệu vé đang được tải, vui lòng thử lại");
            return;
        }

        View view = getLayoutInflater().inflate(R.layout.admin_dialog_nhap_voucher, null);
        VoucherDialogViews views = new VoucherDialogViews(view);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();

        views.lblTieuDeDialog.setText(voucherCanSua == null ? "Thêm voucher" : "Sửa voucher");
        cauHinhSpinner(views.spnLoaiVeApDung, taoLuaChonLoaiVe());
        cauHinhSpinner(views.spnVeApDung, taoLuaChonVe());

        if (voucherCanSua == null) {
            cauHinhDuLieuMacDinh(views);
        } else {
            doDuLieuLenDialog(voucherCanSua, views);
        }
        capNhatTrangThaiKieuGiam(views);
        capNhatTrangThaiPhamVi(views);
        batSuKienDialog(views);
        capNhatPhanTich(views);

        views.btnLuu.setOnClickListener(v -> luuVoucherTuDialog(dialog, voucherCanSua, views));
        views.btnHuy.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private List<LuaChonDanhMuc> taoLuaChonLoaiVe() {
        List<LuaChonDanhMuc> danhSach = new ArrayList<>();
        danhSach.add(new LuaChonDanhMuc(null, "Chọn loại vé"));
        for (LoaiVe loaiVe : danhSachLoaiVe) {
            String hauTo = "HoatDong".equals(loaiVe.getTrangThai()) ? "" : " (Đã khóa)";
            danhSach.add(new LuaChonDanhMuc(loaiVe.getMaLoaiVe(), loaiVe.getTenLoaiVe() + hauTo));
        }
        return danhSach;
    }

    private List<LuaChonDanhMuc> taoLuaChonVe() {
        List<LuaChonDanhMuc> danhSach = new ArrayList<>();
        danhSach.add(new LuaChonDanhMuc(null, "Chọn vé"));
        for (Ve ve : danhSachVe) {
            String hauTo = "HoatDong".equals(ve.getTrangThai()) ? "" : " (Đã khóa)";
            danhSach.add(new LuaChonDanhMuc(ve.getMaVe(), ve.getTenVe() + hauTo));
        }
        return danhSach;
    }

    private void cauHinhSpinner(Spinner spinner, List<LuaChonDanhMuc> danhSach) {
        ArrayAdapter<LuaChonDanhMuc> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                danhSach
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
    }

    private void cauHinhDuLieuMacDinh(VoucherDialogViews views) {
        cauHinhNgayMacDinh(views.edtNgayBatDau, views.edtNgayKetThuc);
        views.edtDonToiThieu.setText("0");
        views.edtGiamToiDa.setText("0");
        views.edtSoLuongVeToiThieu.setText("1");
        views.edtSoLanDungToiDaMoiNguoi.setText("1");
        views.edtSoLuong.setText("0");
        views.radTatCaVe.setChecked(true);
        views.swtTrangThai.setChecked(true);
    }

    private void cauHinhNgayMacDinh(EditText edtNgayBatDau, EditText edtNgayKetThuc) {
        Calendar homNay = Calendar.getInstance();
        edtNgayBatDau.setText(dinhDangNgayHienThi(homNay.getTime()));
        homNay.add(Calendar.DAY_OF_MONTH, 30);
        edtNgayKetThuc.setText(dinhDangNgayHienThi(homNay.getTime()));
    }

    private void doDuLieuLenDialog(Voucher voucher, VoucherDialogViews views) {
        views.edtMaGiamGia.setText(voucher.getMaGiamGia());
        views.edtTenVoucher.setText(voucher.getTenVoucher());
        views.radPhanTram.setChecked("PhanTram".equals(voucher.getKieuGiamGia()));
        views.radTienMat.setChecked("TienMat".equals(voucher.getKieuGiamGia()));
        views.edtGiaTriGiam.setText(hienThiSo(voucher.getGiaTriGiam()));
        views.edtGiamToiDa.setText(hienThiSo(voucher.getGiamToiDa()));
        views.edtDonToiThieu.setText(hienThiSo(voucher.getDonToiThieu()));
        views.edtSoLuongVeToiThieu.setText(String.valueOf(Math.max(1, voucher.getSoLuongVeToiThieu())));
        views.edtSoLanDungToiDaMoiNguoi.setText(String.valueOf(voucher.getSoLanDungToiDaMoiNguoi()));
        views.edtSoLuong.setText(String.valueOf(voucher.getSoLuong()));
        views.swtChiApDungKhachMoi.setChecked(voucher.isChiApDungKhachMoi());
        views.swtTrangThai.setChecked("HoatDong".equals(voucher.getTrangThai()));
        views.edtMucTieu.setText(voucher.getMucTieu());
        views.edtMoTaDieuKien.setText(voucher.getMoTaDieuKien());
        views.edtNgayBatDau.setText(chuyenNgaySangHienThi(voucher.getNgayBatDau()));
        views.edtNgayKetThuc.setText(chuyenNgaySangHienThi(voucher.getNgayKetThuc()));

        if (voucher.getMaVeApDung() != null) {
            views.radTheoVe.setChecked(true);
            chonTheoMa(views.spnVeApDung, voucher.getMaVeApDung());
        } else if (voucher.getMaLoaiVeApDung() != null) {
            views.radTheoLoaiVe.setChecked(true);
            chonTheoMa(views.spnLoaiVeApDung, voucher.getMaLoaiVeApDung());
        } else {
            views.radTatCaVe.setChecked(true);
        }
    }

    private void chonTheoMa(Spinner spinner, Integer maCanChon) {
        if (maCanChon == null || spinner.getAdapter() == null) {
            return;
        }
        for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
            Object item = spinner.getAdapter().getItem(i);
            if (item instanceof LuaChonDanhMuc) {
                LuaChonDanhMuc luaChon = (LuaChonDanhMuc) item;
                if (luaChon.getMa() != null && luaChon.getMa().equals(maCanChon)) {
                    spinner.setSelection(i);
                    return;
                }
            }
        }
    }

    private void batSuKienDialog(VoucherDialogViews views) {
        views.edtNgayBatDau.setOnClickListener(v -> moChonNgay(views.edtNgayBatDau));
        views.edtNgayKetThuc.setOnClickListener(v -> moChonNgay(views.edtNgayKetThuc));
        views.rgKieuGiamGia.setOnCheckedChangeListener((group, checkedId) -> {
            capNhatTrangThaiKieuGiam(views);
            capNhatPhanTich(views);
        });
        views.rgPhamViApDung.setOnCheckedChangeListener((group, checkedId) -> {
            capNhatTrangThaiPhamVi(views);
            capNhatPhanTich(views);
        });
        views.swtChiApDungKhachMoi.setOnCheckedChangeListener((buttonView, isChecked) -> capNhatPhanTich(views));
        ganSuKienSpinner(views.spnLoaiVeApDung, views);
        ganSuKienSpinner(views.spnVeApDung, views);

        theoDoiThayDoi(views.edtGiaTriGiam, views);
        theoDoiThayDoi(views.edtGiamToiDa, views);
        theoDoiThayDoi(views.edtDonToiThieu, views);
        theoDoiThayDoi(views.edtSoLuongVeToiThieu, views);
        theoDoiThayDoi(views.edtSoLanDungToiDaMoiNguoi, views);
        theoDoiThayDoi(views.edtSoLuong, views);
        theoDoiThayDoi(views.edtMucTieu, views);
    }

    private void ganSuKienSpinner(Spinner spinner, VoucherDialogViews views) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                capNhatPhanTich(views);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void theoDoiThayDoi(EditText editText, VoucherDialogViews views) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                capNhatPhanTich(views);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void capNhatTrangThaiKieuGiam(VoucherDialogViews views) {
        boolean laPhanTram = views.radPhanTram.isChecked();
        views.lblGiaTriGiam.setText(laPhanTram ? "Giá trị giảm (%)" : "Số tiền giảm (VNĐ)");
        views.edtGiaTriGiam.setHint(laPhanTram ? "Ví dụ: 10" : "Ví dụ: 50000");
        views.layGiamToiDa.setVisibility(laPhanTram ? View.VISIBLE : View.GONE);
        views.lblCanhBaoGiamToiDa.setVisibility(
                laPhanTram && docDoubleAnToan(views.edtGiamToiDa) <= 0 ? View.VISIBLE : View.GONE
        );
    }

    private void capNhatTrangThaiPhamVi(VoucherDialogViews views) {
        views.layChonLoaiVe.setVisibility(views.radTheoLoaiVe.isChecked() ? View.VISIBLE : View.GONE);
        views.layChonVe.setVisibility(views.radTheoVe.isChecked() ? View.VISIBLE : View.GONE);
    }

    private void capNhatPhanTich(VoucherDialogViews views) {
        Voucher voucher = taoVoucherTuDialog(views, null, false);
        PhanTichVoucher phanTich = phanTichVoucherController.phanTich(voucher);
        views.lblSoLuotToiDa.setText("Số lượt voucher tối đa: " + phanTich.getSoLuotToiDa());
        views.lblDoanhThuToiThieu.setText("Doanh thu tối thiểu kích hoạt: " + phanTich.getDoanhThuToiThieu());
        views.lblTienGiamMoiDon.setText("Tiền giảm dự kiến mỗi đơn: " + phanTich.getTienGiamMoiDon());
        views.lblNganSachGiamGia.setText("Ngân sách giảm giá tối đa: " + phanTich.getNganSachGiamGia());
        views.lblDoanhThuSauGiam.setText("Doanh thu sau giảm ước tính: " + phanTich.getDoanhThuSauGiam());
        views.lblNguoiBanDuoc.setText("Người bán được gì:\n" + phanTich.getNguoiBanDuoc());
        views.lblRuiRo.setText("Rủi ro cần chú ý:\n" + phanTich.getRuiRo());
        views.lblDanhGiaAnToan.setText("Đánh giá: " + phanTich.getDanhGiaAnToan());
        capNhatMauDanhGia(views.lblDanhGiaAnToan, phanTich.getDanhGiaAnToan());
        capNhatTrangThaiKieuGiam(views);
    }

    private void capNhatMauDanhGia(TextView textView, String danhGia) {
        if ("An toàn".equals(danhGia)) {
            textView.setBackgroundResource(R.drawable.bg_badge_success);
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.mauThanhCong));
            return;
        }
        textView.setBackgroundResource(R.drawable.bg_badge_warning);
        int mau = "Rủi ro cao".equals(danhGia) ? R.color.mauNguyHiem : R.color.mauCanhBao;
        textView.setTextColor(ContextCompat.getColor(requireContext(), mau));
    }

    private void luuVoucherTuDialog(AlertDialog dialog, Voucher voucherCanSua, VoucherDialogViews views) {
        if (views.radTheoLoaiVe.isChecked() && layLuaChon(views.spnLoaiVeApDung) == null) {
            TienIch.hienToast(requireContext(), "Vui lòng chọn loại vé áp dụng");
            return;
        }
        if (views.radTheoVe.isChecked() && layLuaChon(views.spnVeApDung) == null) {
            TienIch.hienToast(requireContext(), "Vui lòng chọn vé áp dụng");
            return;
        }

        try {
            Voucher voucher = taoVoucherTuDialog(views, voucherCanSua, true);
            views.btnLuu.setEnabled(false);
            if (voucherCanSua == null) {
                themVoucher(dialog, voucher, views.btnLuu);
            } else {
                suaVoucher(dialog, voucher, views.btnLuu);
            }
        } catch (NumberFormatException e) {
            TienIch.hienToast(requireContext(), "Vui lòng nhập đúng định dạng số");
        }
    }

    private Voucher taoVoucherTuDialog(VoucherDialogViews views, Voucher voucherCanSua, boolean docNghiemNgat) {
        Voucher voucher = new Voucher();
        if (voucherCanSua != null) {
            voucher.setMaVoucher(voucherCanSua.getMaVoucher());
        }
        voucher.setMaGiamGia(views.edtMaGiamGia.getText().toString());
        voucher.setTenVoucher(views.edtTenVoucher.getText().toString());
        voucher.setKieuGiamGia(views.radPhanTram.isChecked() ? "PhanTram" : "TienMat");
        voucher.setGiaTriGiam(docSoThuc(views.edtGiaTriGiam, docNghiemNgat));
        voucher.setGiamToiDa(views.radPhanTram.isChecked() ? docSoThuc(views.edtGiamToiDa, docNghiemNgat) : 0);
        voucher.setDonToiThieu(docSoThuc(views.edtDonToiThieu, docNghiemNgat));
        voucher.setSoLuongVeToiThieu(docSoNguyen(views.edtSoLuongVeToiThieu, docNghiemNgat));
        voucher.setSoLanDungToiDaMoiNguoi(docSoNguyen(views.edtSoLanDungToiDaMoiNguoi, docNghiemNgat));
        voucher.setNgayBatDau(chuyenNgaySangLuu(views.edtNgayBatDau.getText().toString()));
        voucher.setNgayKetThuc(chuyenNgaySangLuu(views.edtNgayKetThuc.getText().toString()));
        voucher.setSoLuong(docSoNguyen(views.edtSoLuong, docNghiemNgat));
        voucher.setChiApDungKhachMoi(views.swtChiApDungKhachMoi.isChecked());
        voucher.setTrangThai(views.swtTrangThai.isChecked() ? "HoatDong" : "Khoa");
        voucher.setMucTieu(views.edtMucTieu.getText().toString());
        voucher.setMoTaDieuKien(views.edtMoTaDieuKien.getText().toString());

        if (views.radTheoLoaiVe.isChecked()) {
            voucher.setPhamViApDung("LoaiVe");
            voucher.setMaLoaiVeApDung(layLuaChon(views.spnLoaiVeApDung));
        } else if (views.radTheoVe.isChecked()) {
            voucher.setPhamViApDung("Ve");
            voucher.setMaVeApDung(layLuaChon(views.spnVeApDung));
        } else {
            voucher.setPhamViApDung("TatCa");
        }
        return voucher;
    }

    private Integer layLuaChon(Spinner spinner) {
        Object item = spinner.getSelectedItem();
        return item instanceof LuaChonDanhMuc ? ((LuaChonDanhMuc) item).getMa() : null;
    }

    private double docSoThuc(EditText editText, boolean docNghiemNgat) {
        return docNghiemNgat ? docDouble(editText) : docDoubleAnToan(editText);
    }

    private int docSoNguyen(EditText editText, boolean docNghiemNgat) {
        return docNghiemNgat ? docInt(editText) : docIntAnToan(editText);
    }

    private void themVoucher(AlertDialog dialog, Voucher voucher, Button btnLuu) {
        voucherController.themVoucher(voucher, new ApiCallback<Voucher>() {
            @Override
            public void onSuccess(Voucher data) {
                Toast.makeText(requireContext(), "Đã thêm voucher", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                taiDanhSachVoucher();
            }

            @Override
            public void onError(String thongBao) {
                btnLuu.setEnabled(true);
                baoLoi("Lỗi thêm voucher", thongBao);
            }
        });
    }

    private void suaVoucher(AlertDialog dialog, Voucher voucher, Button btnLuu) {
        voucherController.suaVoucher(voucher, new ApiCallback<Voucher>() {
            @Override
            public void onSuccess(Voucher data) {
                Toast.makeText(requireContext(), "Đã cập nhật voucher", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                taiDanhSachVoucher();
            }

            @Override
            public void onError(String thongBao) {
                btnLuu.setEnabled(true);
                baoLoi("Lỗi cập nhật voucher", thongBao);
            }
        });
    }

    private void xacNhanXoaVoucher(Voucher voucher) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa voucher")
                .setMessage("Bạn có chắc muốn xóa voucher này?")
                .setPositiveButton("Đồng ý", (dialog, which) -> voucherController.xoaVoucher(voucher.getMaVoucher(), new ApiCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean data) {
                        Toast.makeText(requireContext(), "Đã xóa voucher", Toast.LENGTH_SHORT).show();
                        taiDanhSachVoucher();
                    }

                    @Override
                    public void onError(String thongBao) {
                        baoLoi("Lỗi xóa voucher", thongBao);
                    }
                }))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void moChonNgay(EditText editText) {
        Calendar calendar = taoCalendarTuNgayHienThi(editText.getText().toString());
        new DatePickerDialog(
                requireContext(),
                (view, nam, thang, ngay) -> {
                    calendar.set(nam, thang, ngay);
                    editText.setText(dinhDangNgayHienThi(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private Calendar taoCalendarTuNgayHienThi(String ngay) {
        Calendar calendar = Calendar.getInstance();
        try {
            Date date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(ngay);
            if (date != null) {
                calendar.setTime(date);
            }
        } catch (ParseException ignored) {
        }
        return calendar;
    }

    private String chuyenNgaySangLuu(String ngayHienThi) {
        try {
            Date date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(ngayHienThi);
            if (date != null) {
                return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
            }
        } catch (ParseException ignored) {
        }
        return "";
    }

    private String chuyenNgaySangHienThi(String ngayLuu) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(ngayLuu);
            if (date != null) {
                return dinhDangNgayHienThi(date);
            }
        } catch (ParseException ignored) {
        }
        return "";
    }

    private String dinhDangNgayHienThi(Date ngay) {
        if (ngay == null) {
            return "";
        }
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(ngay);
    }

    private String hienThiSo(double giaTri) {
        if (giaTri == Math.rint(giaTri)) {
            return String.valueOf((long) giaTri);
        }
        return String.valueOf(giaTri);
    }

    private double docDouble(EditText editText) {
        String chuoi = editText.getText().toString().trim();
        return chuoi.isEmpty() ? 0 : Double.parseDouble(chuoi);
    }

    private double docDoubleAnToan(EditText editText) {
        try {
            return docDouble(editText);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int docInt(EditText editText) {
        String chuoi = editText.getText().toString().trim();
        return chuoi.isEmpty() ? 0 : Integer.parseInt(chuoi);
    }

    private int docIntAnToan(EditText editText) {
        try {
            return docInt(editText);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void baoLoi(String tieuDe, String thongBao) {
        if (getContext() != null) {
            TienIch.hienAlert(requireContext(), tieuDe, thongBao);
        }
    }

    private static class VoucherDialogViews {
        private final TextView lblTieuDeDialog;
        private final EditText edtMaGiamGia;
        private final EditText edtTenVoucher;
        private final RadioGroup rgKieuGiamGia;
        private final RadioButton radPhanTram;
        private final RadioButton radTienMat;
        private final TextView lblGiaTriGiam;
        private final EditText edtGiaTriGiam;
        private final LinearLayout layGiamToiDa;
        private final EditText edtGiamToiDa;
        private final TextView lblCanhBaoGiamToiDa;
        private final EditText edtDonToiThieu;
        private final EditText edtSoLuongVeToiThieu;
        private final EditText edtNgayBatDau;
        private final EditText edtNgayKetThuc;
        private final EditText edtSoLuong;
        private final EditText edtSoLanDungToiDaMoiNguoi;
        private final Switch swtChiApDungKhachMoi;
        private final RadioGroup rgPhamViApDung;
        private final RadioButton radTatCaVe;
        private final RadioButton radTheoLoaiVe;
        private final RadioButton radTheoVe;
        private final LinearLayout layChonLoaiVe;
        private final Spinner spnLoaiVeApDung;
        private final LinearLayout layChonVe;
        private final Spinner spnVeApDung;
        private final EditText edtMucTieu;
        private final EditText edtMoTaDieuKien;
        private final Switch swtTrangThai;
        private final TextView lblSoLuotToiDa;
        private final TextView lblDoanhThuToiThieu;
        private final TextView lblTienGiamMoiDon;
        private final TextView lblNganSachGiamGia;
        private final TextView lblDoanhThuSauGiam;
        private final TextView lblNguoiBanDuoc;
        private final TextView lblRuiRo;
        private final TextView lblDanhGiaAnToan;
        private final Button btnLuu;
        private final Button btnHuy;

        private VoucherDialogViews(View view) {
            lblTieuDeDialog = view.findViewById(R.id.lblTieuDeDialog);
            edtMaGiamGia = view.findViewById(R.id.edtMaGiamGia);
            edtTenVoucher = view.findViewById(R.id.edtTenVoucher);
            rgKieuGiamGia = view.findViewById(R.id.rgKieuGiamGia);
            radPhanTram = view.findViewById(R.id.radPhanTram);
            radTienMat = view.findViewById(R.id.radTienMat);
            lblGiaTriGiam = view.findViewById(R.id.lblGiaTriGiam);
            edtGiaTriGiam = view.findViewById(R.id.edtGiaTriGiam);
            layGiamToiDa = view.findViewById(R.id.layGiamToiDa);
            edtGiamToiDa = view.findViewById(R.id.edtGiamToiDa);
            lblCanhBaoGiamToiDa = view.findViewById(R.id.lblCanhBaoGiamToiDa);
            edtDonToiThieu = view.findViewById(R.id.edtDonToiThieu);
            edtSoLuongVeToiThieu = view.findViewById(R.id.edtSoLuongVeToiThieu);
            edtNgayBatDau = view.findViewById(R.id.edtNgayBatDau);
            edtNgayKetThuc = view.findViewById(R.id.edtNgayKetThuc);
            edtSoLuong = view.findViewById(R.id.edtSoLuong);
            edtSoLanDungToiDaMoiNguoi = view.findViewById(R.id.edtSoLanDungToiDaMoiNguoi);
            swtChiApDungKhachMoi = view.findViewById(R.id.swtChiApDungKhachMoi);
            rgPhamViApDung = view.findViewById(R.id.rgPhamViApDung);
            radTatCaVe = view.findViewById(R.id.radTatCaVe);
            radTheoLoaiVe = view.findViewById(R.id.radTheoLoaiVe);
            radTheoVe = view.findViewById(R.id.radTheoVe);
            layChonLoaiVe = view.findViewById(R.id.layChonLoaiVe);
            spnLoaiVeApDung = view.findViewById(R.id.spnLoaiVeApDung);
            layChonVe = view.findViewById(R.id.layChonVe);
            spnVeApDung = view.findViewById(R.id.spnVeApDung);
            edtMucTieu = view.findViewById(R.id.edtMucTieu);
            edtMoTaDieuKien = view.findViewById(R.id.edtMoTaDieuKien);
            swtTrangThai = view.findViewById(R.id.swtTrangThai);
            lblSoLuotToiDa = view.findViewById(R.id.lblSoLuotToiDa);
            lblDoanhThuToiThieu = view.findViewById(R.id.lblDoanhThuToiThieu);
            lblTienGiamMoiDon = view.findViewById(R.id.lblTienGiamMoiDon);
            lblNganSachGiamGia = view.findViewById(R.id.lblNganSachGiamGia);
            lblDoanhThuSauGiam = view.findViewById(R.id.lblDoanhThuSauGiam);
            lblNguoiBanDuoc = view.findViewById(R.id.lblNguoiBanDuoc);
            lblRuiRo = view.findViewById(R.id.lblRuiRo);
            lblDanhGiaAnToan = view.findViewById(R.id.lblDanhGiaAnToan);
            btnLuu = view.findViewById(R.id.btnLuu);
            btnHuy = view.findViewById(R.id.btnHuy);
        }
    }
}
