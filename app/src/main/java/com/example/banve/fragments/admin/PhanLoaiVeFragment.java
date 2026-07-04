package com.example.banve.fragments.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.banve.R;
import com.example.banve.adapters.LoaiVeQuanLyAdapter;
import com.example.banve.controllers.LoaiVeController;
import com.example.banve.models.LoaiVe;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.TienIch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PhanLoaiVeFragment extends Fragment {
    private EditText edtTimKiem;
    private LinearLayout layBoLoc;
    private Button btnBoLoc;
    private Button btnThemLoaiVe;
    private RecyclerView rcvDanhSachLoaiVe;
    private LoaiVeQuanLyAdapter adapter;
    private LoaiVeController loaiVeController;
    private final List<LoaiVe> danhSachGoc = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_phan_loai_ve, container, false);
        anhXa(view);
        loaiVeController = new LoaiVeController();
        khoiTaoRecyclerView();
        batSuKien();
        taiDanhSachLoaiVe();
        return view;
    }

    private void anhXa(View view) {
        edtTimKiem = view.findViewById(R.id.edtTimKiem);
        layBoLoc = view.findViewById(R.id.layBoLoc);
        btnBoLoc = view.findViewById(R.id.btnBoLoc);
        btnThemLoaiVe = view.findViewById(R.id.btnThemLoaiVe);
        rcvDanhSachLoaiVe = view.findViewById(R.id.rcvDanhSachLoaiVe);
    }

    private void khoiTaoRecyclerView() {
        adapter = new LoaiVeQuanLyAdapter(new LoaiVeQuanLyAdapter.OnLoaiVeClickListener() {
            @Override
            public void onSua(LoaiVe loaiVe) {
                moDialogNhapLoaiVe(loaiVe);
            }

            @Override
            public void onXoa(LoaiVe loaiVe) {
                xacNhanXoaLoaiVe(loaiVe);
            }
        });
        rcvDanhSachLoaiVe.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvDanhSachLoaiVe.setAdapter(adapter);
    }

    private void batSuKien() {
        btnBoLoc.setOnClickListener(v -> doiTrangThaiBoLoc());
        btnThemLoaiVe.setOnClickListener(v -> moDialogNhapLoaiVe(null));
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
        if (layBoLoc.getVisibility() == View.VISIBLE) {
            layBoLoc.setVisibility(View.GONE);
        btnBoLoc.setText("Lọc");
        } else {
            layBoLoc.setVisibility(View.VISIBLE);
            btnBoLoc.setText("Ẩn lọc");
        }
    }

    private void taiDanhSachLoaiVe() {
        loaiVeController.layDanhSachQuanLy(new ApiCallback<List<LoaiVe>>() {
            @Override
            public void onSuccess(List<LoaiVe> data) {
                danhSachGoc.clear();
                if (data != null) {
                    danhSachGoc.addAll(data);
                }
                apDungLoc();
            }

            @Override
            public void onError(String thongBao) {
                baoLoi("Lỗi loại vé", thongBao);
            }
        });
    }

    private void apDungLoc() {
        String tuKhoa = edtTimKiem.getText().toString().trim().toLowerCase(Locale.ROOT);
        if (tuKhoa.isEmpty()) {
            adapter.capNhatDuLieu(danhSachGoc);
            return;
        }

        List<LoaiVe> danhSachLoc = new ArrayList<>();
        for (LoaiVe loaiVe : danhSachGoc) {
            if (chuaTuKhoa(loaiVe.getTenLoaiVe(), tuKhoa) || chuaTuKhoa(loaiVe.getMoTa(), tuKhoa)) {
                danhSachLoc.add(loaiVe);
            }
        }
        adapter.capNhatDuLieu(danhSachLoc);
    }

    private boolean chuaTuKhoa(String giaTri, String tuKhoa) {
        return giaTri != null && giaTri.toLowerCase(Locale.ROOT).contains(tuKhoa);
    }

    private void moDialogNhapLoaiVe(LoaiVe loaiVeCanSua) {
        View view = getLayoutInflater().inflate(R.layout.admin_dialog_nhap_loai_ve, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(loaiVeCanSua == null ? "Thêm loại vé" : "Sửa loại vé")
                .setView(view)
                .create();

        EditText edtTenLoaiVe = view.findViewById(R.id.edtTenLoaiVe);
        EditText edtMoTa = view.findViewById(R.id.edtMoTa);
        Switch swtTrangThai = view.findViewById(R.id.swtTrangThai);
        Button btnLuu = view.findViewById(R.id.btnLuu);
        Button btnHuy = view.findViewById(R.id.btnHuy);

        if (loaiVeCanSua != null) {
            edtTenLoaiVe.setText(loaiVeCanSua.getTenLoaiVe());
            edtMoTa.setText(loaiVeCanSua.getMoTa());
            swtTrangThai.setChecked("HoatDong".equals(loaiVeCanSua.getTrangThai()));
        }

        btnLuu.setOnClickListener(v -> luuLoaiVe(dialog, loaiVeCanSua, edtTenLoaiVe, edtMoTa, swtTrangThai));
        btnHuy.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void luuLoaiVe(
            AlertDialog dialog,
            LoaiVe loaiVeCanSua,
            EditText edtTenLoaiVe,
            EditText edtMoTa,
            Switch swtTrangThai
    ) {
        LoaiVe loaiVe = loaiVeCanSua == null ? new LoaiVe() : loaiVeCanSua;
        if (loaiVeCanSua != null) {
            loaiVe.setMaLoaiVe(loaiVeCanSua.getMaLoaiVe());
        }
        loaiVe.setTenLoaiVe(edtTenLoaiVe.getText().toString());
        loaiVe.setMoTa(edtMoTa.getText().toString());
        loaiVe.setTrangThai(swtTrangThai.isChecked() ? "HoatDong" : "Khoa");

        if (loaiVeCanSua == null) {
            themLoaiVe(dialog, loaiVe);
        } else {
            suaLoaiVe(dialog, loaiVe);
        }
    }

    private void themLoaiVe(AlertDialog dialog, LoaiVe loaiVe) {
        loaiVeController.themLoaiVe(loaiVe, new ApiCallback<LoaiVe>() {
            @Override
            public void onSuccess(LoaiVe data) {
                Toast.makeText(requireContext(), "Đã thêm loại vé", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                taiDanhSachLoaiVe();
            }

            @Override
            public void onError(String thongBao) {
                baoLoi("Lỗi thêm loại vé", thongBao);
            }
        });
    }

    private void suaLoaiVe(AlertDialog dialog, LoaiVe loaiVe) {
        loaiVeController.suaLoaiVe(loaiVe, new ApiCallback<LoaiVe>() {
            @Override
            public void onSuccess(LoaiVe data) {
                Toast.makeText(requireContext(), "Đã cập nhật loại vé", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                taiDanhSachLoaiVe();
            }

            @Override
            public void onError(String thongBao) {
                baoLoi("Lỗi cập nhật loại vé", thongBao);
            }
        });
    }

    private void xacNhanXoaLoaiVe(LoaiVe loaiVe) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa loại vé")
                .setMessage("Xóa loại vé này sẽ cascade xóa vé thuộc loại này. Tiếp tục?")
                .setPositiveButton("Đồng ý", (dialog, which) -> loaiVeController.xoaLoaiVe(loaiVe.getMaLoaiVe(), new ApiCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean data) {
                        Toast.makeText(requireContext(), "Đã xóa loại vé", Toast.LENGTH_SHORT).show();
                        taiDanhSachLoaiVe();
                    }

                    @Override
                    public void onError(String thongBao) {
                        baoLoi("Lỗi xóa loại vé", thongBao);
                    }
                }))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void baoLoi(String tieuDe, String thongBao) {
        if (getContext() != null) {
            TienIch.hienAlert(requireContext(), tieuDe, thongBao);
        }
    }
}
