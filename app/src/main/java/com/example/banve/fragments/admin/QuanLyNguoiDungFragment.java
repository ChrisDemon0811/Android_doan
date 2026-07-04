package com.example.banve.fragments.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.banve.R;
import com.example.banve.adapters.NguoiDungAdapter;
import com.example.banve.controllers.NguoiDungController;
import com.example.banve.models.NguoiDung;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.Session;
import com.example.banve.utils.TienIch;

import java.util.Arrays;
import java.util.List;

public class QuanLyNguoiDungFragment extends Fragment {
    private static final String TAT_CA_VAI_TRO = "Tất cả vai trò";

    private EditText edtTimKiem;
    private Spinner spnLocVaiTro;
    private LinearLayout layBoLoc;
    private Button btnBoLoc;
    private RecyclerView rcvDanhSachNguoiDung;
    private NguoiDungAdapter adapter;
    private NguoiDungController nguoiDungController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_quan_ly_nguoi_dung, container, false);
        anhXa(view);
        nguoiDungController = new NguoiDungController();
        khoiTaoBoLoc();
        khoiTaoRecyclerView();
        batSuKien();
        taiDanhSachNguoiDung();
        return view;
    }

    private void anhXa(View view) {
        edtTimKiem = view.findViewById(R.id.edtTimKiem);
        spnLocVaiTro = view.findViewById(R.id.spnLocVaiTro);
        layBoLoc = view.findViewById(R.id.layBoLoc);
        btnBoLoc = view.findViewById(R.id.btnBoLoc);
        rcvDanhSachNguoiDung = view.findViewById(R.id.rcvDanhSachNguoiDung);
    }

    private void khoiTaoBoLoc() {
        List<String> danhSachVaiTro = Arrays.asList(TAT_CA_VAI_TRO, "NguoiDung", "QuanLy");
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, danhSachVaiTro);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLocVaiTro.setAdapter(spinnerAdapter);
    }

    private void khoiTaoRecyclerView() {
        adapter = new NguoiDungAdapter(new NguoiDungAdapter.OnNguoiDungClickListener() {
            @Override
            public void onDoiTrangThai(NguoiDung nguoiDung, boolean khoa) {
                doiTrangThaiNguoiDung(nguoiDung, khoa);
            }

            @Override
            public void onResetMatKhau(NguoiDung nguoiDung) {
                moDialogDatLaiMatKhau(nguoiDung);
            }
        });
        rcvDanhSachNguoiDung.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvDanhSachNguoiDung.setAdapter(adapter);
    }

    private void batSuKien() {
        btnBoLoc.setOnClickListener(v -> doiTrangThaiBoLoc());
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

        spnLocVaiTro.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                apDungLoc();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
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

    private void taiDanhSachNguoiDung() {
        nguoiDungController.layDanhSachNguoiDung(new ApiCallback<List<NguoiDung>>() {
            @Override
            public void onSuccess(List<NguoiDung> data) {
                adapter.capNhatDuLieu(data);
                apDungLoc();
            }

            @Override
            public void onError(String thongBao) {
                baoLoi("Lỗi người dùng", thongBao);
            }
        });
    }

    private void apDungLoc() {
        if (adapter == null || edtTimKiem == null || spnLocVaiTro == null) {
            return;
        }

        String vaiTro = spnLocVaiTro.getSelectedItem() == null ? "" : spnLocVaiTro.getSelectedItem().toString();
        if (TAT_CA_VAI_TRO.equals(vaiTro)) {
            vaiTro = "";
        }
        adapter.locDuLieu(edtTimKiem.getText().toString(), vaiTro);
    }

    private void doiTrangThaiNguoiDung(NguoiDung nguoiDung, boolean khoa) {
        if (nguoiDung == null) {
            return;
        }

        if (khoa && Session.nguoiDungHienTai != null
                && Session.nguoiDungHienTai.getMaNguoiDung() == nguoiDung.getMaNguoiDung()) {
            Toast.makeText(requireContext(), "Không thể khóa tài khoản đang đăng nhập", Toast.LENGTH_SHORT).show();
            taiDanhSachNguoiDung();
            return;
        }

        String trangThaiMoi = khoa ? "Khoa" : "HoatDong";
        nguoiDungController.capNhatTrangThai(nguoiDung.getMaNguoiDung(), trangThaiMoi, new ApiCallback<NguoiDung>() {
            @Override
            public void onSuccess(NguoiDung data) {
                Toast.makeText(requireContext(), "Đã cập nhật trạng thái người dùng", Toast.LENGTH_SHORT).show();
                taiDanhSachNguoiDung();
            }

            @Override
            public void onError(String thongBao) {
                baoLoi("Lỗi cập nhật trạng thái", thongBao);
                taiDanhSachNguoiDung();
            }
        });
    }

    private void moDialogDatLaiMatKhau(NguoiDung nguoiDung) {
        View view = getLayoutInflater().inflate(R.layout.admin_dialog_dat_mat_khau_nguoi_dung, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Đặt lại mật khẩu")
                .setView(view)
                .create();

        TextView lblTaiKhoan = view.findViewById(R.id.lblTaiKhoan);
        EditText edtMatKhauMoi = view.findViewById(R.id.edtMatKhauMoi);
        Button btnLuu = view.findViewById(R.id.btnLuu);
        Button btnHuy = view.findViewById(R.id.btnHuy);

        lblTaiKhoan.setText("Tài khoản: " + nguoiDung.getTaiKhoan());
        TienIch.ganAnHienMatKhau(edtMatKhauMoi);
        btnLuu.setOnClickListener(v -> datLaiMatKhau(dialog, nguoiDung, edtMatKhauMoi));
        btnHuy.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void datLaiMatKhau(AlertDialog dialog, NguoiDung nguoiDung, EditText edtMatKhauMoi) {
        nguoiDungController.datLaiMatKhau(
                nguoiDung.getMaNguoiDung(),
                edtMatKhauMoi.getText().toString(),
                new ApiCallback<NguoiDung>() {
                    @Override
                    public void onSuccess(NguoiDung data) {
                        Toast.makeText(requireContext(), "Đã đặt lại mật khẩu", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(String thongBao) {
                        baoLoi("Lỗi đặt lại mật khẩu", thongBao);
                    }
                }
        );
    }

    private void baoLoi(String tieuDe, String noiDung) {
        new AlertDialog.Builder(requireContext())
                .setTitle(tieuDe)
                .setMessage(noiDung)
                .setPositiveButton("Đồng ý", null)
                .show();
    }
}
