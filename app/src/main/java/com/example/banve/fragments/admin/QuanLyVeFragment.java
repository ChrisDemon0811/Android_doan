package com.example.banve.fragments.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.banve.R;
import com.example.banve.adapters.VeQuanLyAdapter;
import com.example.banve.controllers.VeController;
import com.example.banve.dao.LoaiVeDAO;
import com.example.banve.models.LoaiVe;
import com.example.banve.models.Ve;
import com.example.banve.network.ApiCallback;
import com.example.banve.network.SupabaseStorageClient;
import com.example.banve.utils.TienIch;

import java.util.ArrayList;
import java.util.List;

public class QuanLyVeFragment extends Fragment {
    private static final int MA_CHON_ANH_VE = 4101;

    private EditText edtTimKiem;
    private Spinner spnLocLoaiVe;
    private LinearLayout layBoLoc;
    private Button btnBoLoc;
    private Button btnThemVe;
    private RecyclerView rcvDanhSachVe;
    private VeQuanLyAdapter adapter;
    private VeController veController;
    private LoaiVeDAO loaiVeDAO;
    private final List<LoaiVe> danhSachLoaiVe = new ArrayList<>();
    private ArrayAdapter<String> locLoaiVeAdapter;
    private Uri anhVeUriDangChon;
    private ImageView imgAnhVeXemTruocDangMo;
    private TextView lblAnhVeDaChonDangMo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_quan_ly_ve, container, false);
        anhXa(view);
        veController = new VeController();
        loaiVeDAO = new LoaiVeDAO();
        khoiTaoRecyclerView();
        khoiTaoLocLoaiVe();
        batSuKien();
        taiLoaiVe();
        taiDanhSachVe();
        return view;
    }

    private void anhXa(View view) {
        edtTimKiem = view.findViewById(R.id.edtTimKiem);
        spnLocLoaiVe = view.findViewById(R.id.spnLocLoaiVe);
        layBoLoc = view.findViewById(R.id.layBoLoc);
        btnBoLoc = view.findViewById(R.id.btnBoLoc);
        btnThemVe = view.findViewById(R.id.btnThemVe);
        rcvDanhSachVe = view.findViewById(R.id.rcvDanhSachVe);
    }

    private void khoiTaoRecyclerView() {
        adapter = new VeQuanLyAdapter(new VeQuanLyAdapter.OnVeQuanLyClickListener() {
            @Override
            public void onSua(Ve ve) {
                moDialogNhapVe(ve);
            }

            @Override
            public void onXoa(Ve ve) {
                xacNhanXoaVe(ve);
            }
        });
        rcvDanhSachVe.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvDanhSachVe.setAdapter(adapter);
    }

    private void khoiTaoLocLoaiVe() {
        locLoaiVeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        locLoaiVeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLocLoaiVe.setAdapter(locLoaiVeAdapter);
    }

    private void batSuKien() {
        btnBoLoc.setOnClickListener(v -> doiTrangThaiBoLoc());
        btnThemVe.setOnClickListener(v -> moDialogNhapVe(null));
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
        spnLocLoaiVe.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
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
            btnBoLoc.setText("🔎 Lọc");
        } else {
            layBoLoc.setVisibility(View.VISIBLE);
            btnBoLoc.setText("Ẩn lọc");
        }
    }

    private void taiLoaiVe() {
        loaiVeDAO.layDanhSachLoaiVe(new ApiCallback<List<LoaiVe>>() {
            @Override
            public void onSuccess(List<LoaiVe> data) {
                danhSachLoaiVe.clear();
                if (data != null) {
                    danhSachLoaiVe.addAll(data);
                }
                capNhatSpinnerLoc();
            }

            @Override
            public void onError(String thongBao) {
                baoLoi("Lỗi loại vé", thongBao);
            }
        });
    }

    private void capNhatSpinnerLoc() {
        locLoaiVeAdapter.clear();
        locLoaiVeAdapter.add("Tất cả loại vé");
        for (LoaiVe loaiVe : danhSachLoaiVe) {
            locLoaiVeAdapter.add(loaiVe.getTenLoaiVe());
        }
        locLoaiVeAdapter.notifyDataSetChanged();
    }

    private void taiDanhSachVe() {
        veController.layDanhSachVeQuanLy(new ApiCallback<List<Ve>>() {
            @Override
            public void onSuccess(List<Ve> data) {
                adapter.capNhatDuLieu(data);
                apDungLoc();
            }

            @Override
            public void onError(String thongBao) {
                baoLoi("Lỗi tải vé", thongBao);
            }
        });
    }

    private void apDungLoc() {
        Integer maLoaiVe = layMaLoaiVeDangLoc();
        adapter.locDuLieu(edtTimKiem.getText().toString(), maLoaiVe);
    }

    private Integer layMaLoaiVeDangLoc() {
        int viTri = spnLocLoaiVe.getSelectedItemPosition();
        if (viTri <= 0 || viTri - 1 >= danhSachLoaiVe.size()) {
            return null;
        }
        return danhSachLoaiVe.get(viTri - 1).getMaLoaiVe();
    }

    private void moDialogNhapVe(Ve veCanSua) {
        if (danhSachLoaiVe.isEmpty()) {
            TienIch.hienToast(requireContext(), "Chưa có loại vé để chọn");
            return;
        }

        View view = getLayoutInflater().inflate(R.layout.admin_dialog_nhap_ve, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(veCanSua == null ? "Thêm vé" : "Sửa vé")
                .setView(view)
                .create();

        EditText edtTenVe = view.findViewById(R.id.edtTenVe);
        Spinner spnLoaiVe = view.findViewById(R.id.spnLoaiVe);
        EditText edtGiaVe = view.findViewById(R.id.edtGiaVe);
        EditText edtGiaNguoiLon = view.findViewById(R.id.edtGiaNguoiLon);
        EditText edtGiaTreEm = view.findViewById(R.id.edtGiaTreEm);
        EditText edtGiaCaoTuoi = view.findViewById(R.id.edtGiaCaoTuoi);
        EditText edtSoLuong = view.findViewById(R.id.edtSoLuong);
        EditText edtMoTa = view.findViewById(R.id.edtMoTa);
        EditText edtThongTinVe = view.findViewById(R.id.edtThongTinVe);
        TextView lblAnhVeDaChon = view.findViewById(R.id.lblAnhVeDaChon);
        ImageView imgAnhVeXemTruoc = view.findViewById(R.id.imgAnhVeXemTruoc);
        Button btnChonAnhVe = view.findViewById(R.id.btnChonAnhVe);
        Switch swtTrangThai = view.findViewById(R.id.swtTrangThai);
        Button btnLuu = view.findViewById(R.id.btnLuu);
        Button btnHuy = view.findViewById(R.id.btnHuy);

        anhVeUriDangChon = null;
        imgAnhVeXemTruocDangMo = imgAnhVeXemTruoc;
        lblAnhVeDaChonDangMo = lblAnhVeDaChon;

        ArrayAdapter<String> loaiVeAdapter = taoAdapterLoaiVe();
        spnLoaiVe.setAdapter(loaiVeAdapter);
        if (veCanSua != null) {
            doDuLieuLenDialog(veCanSua, edtTenVe, spnLoaiVe, edtGiaVe, edtGiaNguoiLon, edtGiaTreEm, edtGiaCaoTuoi, edtSoLuong, edtMoTa, edtThongTinVe, lblAnhVeDaChon, imgAnhVeXemTruoc, swtTrangThai);
        }

        btnChonAnhVe.setOnClickListener(v -> moManHinhChonAnh());
        btnLuu.setOnClickListener(v -> luuVeTuDialog(
                dialog,
                veCanSua,
                edtTenVe,
                spnLoaiVe,
                edtGiaVe,
                edtGiaNguoiLon,
                edtGiaTreEm,
                edtGiaCaoTuoi,
                edtSoLuong,
                edtMoTa,
                edtThongTinVe,
                swtTrangThai,
                btnLuu
        ));
        btnHuy.setOnClickListener(v -> dialog.dismiss());
        dialog.setOnDismissListener(d -> {
            anhVeUriDangChon = null;
            imgAnhVeXemTruocDangMo = null;
            lblAnhVeDaChonDangMo = null;
        });
        dialog.show();
    }

    private ArrayAdapter<String> taoAdapterLoaiVe() {
        List<String> tenLoaiVe = new ArrayList<>();
        for (LoaiVe loaiVe : danhSachLoaiVe) {
            tenLoaiVe.add(loaiVe.getTenLoaiVe());
        }
        ArrayAdapter<String> adapterLoaiVe = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, tenLoaiVe);
        adapterLoaiVe.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapterLoaiVe;
    }

    private void doDuLieuLenDialog(
            Ve ve,
            EditText edtTenVe,
            Spinner spnLoaiVe,
            EditText edtGiaVe,
            EditText edtGiaNguoiLon,
            EditText edtGiaTreEm,
            EditText edtGiaCaoTuoi,
            EditText edtSoLuong,
            EditText edtMoTa,
            EditText edtThongTinVe,
            TextView lblAnhVeDaChon,
            ImageView imgAnhVeXemTruoc,
            Switch swtTrangThai
    ) {
        edtTenVe.setText(ve.getTenVe());
        spnLoaiVe.setSelection(timViTriLoaiVe(ve.getMaLoaiVe()));
        edtGiaVe.setText(String.valueOf(ve.getGiaVe()));
        edtGiaNguoiLon.setText(String.valueOf(ve.getGiaNguoiLon()));
        edtGiaTreEm.setText(String.valueOf(ve.getGiaTreEm()));
        edtGiaCaoTuoi.setText(String.valueOf(ve.getGiaNguoiCaoTuoi()));
        edtSoLuong.setText(String.valueOf(ve.getSoLuong()));
        edtMoTa.setText(ve.getMoTa());
        edtThongTinVe.setText(ve.getThongTinVe());
        if (coNoiDung(ve.getAnhVe())) {
            lblAnhVeDaChon.setText("Đang dùng ảnh đã lưu trên Supabase Storage");
            Glide.with(this)
                    .load(ve.getAnhVe())
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(imgAnhVeXemTruoc);
        } else {
            lblAnhVeDaChon.setText("Chưa chọn ảnh vé");
            imgAnhVeXemTruoc.setImageResource(R.mipmap.ic_launcher);
        }
        swtTrangThai.setChecked("HoatDong".equals(ve.getTrangThai()));
    }

    private int timViTriLoaiVe(int maLoaiVe) {
        for (int i = 0; i < danhSachLoaiVe.size(); i++) {
            if (danhSachLoaiVe.get(i).getMaLoaiVe() == maLoaiVe) {
                return i;
            }
        }
        return 0;
    }

    private void luuVeTuDialog(
            AlertDialog dialog,
            Ve veCanSua,
            EditText edtTenVe,
            Spinner spnLoaiVe,
            EditText edtGiaVe,
            EditText edtGiaNguoiLon,
            EditText edtGiaTreEm,
            EditText edtGiaCaoTuoi,
            EditText edtSoLuong,
            EditText edtMoTa,
            EditText edtThongTinVe,
            Switch swtTrangThai,
            Button btnLuu
    ) {
        try {
            Ve ve = veCanSua == null ? new Ve() : veCanSua;
            if (veCanSua != null) {
                ve.setMaVe(veCanSua.getMaVe());
            }
            LoaiVe loaiVe = danhSachLoaiVe.get(spnLoaiVe.getSelectedItemPosition());
            ve.setMaLoaiVe(loaiVe.getMaLoaiVe());
            ve.setTenVe(layChuoi(edtTenVe));
            ve.setGiaVe(docDouble(edtGiaVe));
            ve.setGiaNguoiLon(docDouble(edtGiaNguoiLon));
            ve.setGiaTreEm(docDouble(edtGiaTreEm));
            ve.setGiaNguoiCaoTuoi(docDouble(edtGiaCaoTuoi));
            ve.setSoLuong(docInt(edtSoLuong));
            ve.setMoTa(layChuoi(edtMoTa));
            ve.setThongTinVe(layChuoi(edtThongTinVe));
            ve.setAnhVe(veCanSua == null ? "" : veCanSua.getAnhVe());
            ve.setTrangThai(swtTrangThai.isChecked() ? "HoatDong" : "Khoa");

            if (anhVeUriDangChon != null) {
                btnLuu.setEnabled(false);
                btnLuu.setText("Đang tải ảnh...");
                SupabaseStorageClient.taiAnhVe(requireContext(), anhVeUriDangChon, new ApiCallback<String>() {
                    @Override
                    public void onSuccess(String data) {
                        ve.setAnhVe(data);
                        luuVeSauKhiCoAnh(dialog, veCanSua, ve);
                    }

                    @Override
                    public void onError(String thongBao) {
                        btnLuu.setEnabled(true);
                        btnLuu.setText("💾 Lưu");
                        baoLoi("Lỗi tải ảnh", thongBao);
                    }
                });
            } else {
                luuVeSauKhiCoAnh(dialog, veCanSua, ve);
            }
        } catch (NumberFormatException e) {
            TienIch.hienToast(requireContext(), "Vui lòng nhập đúng định dạng số");
        }
    }

    private void luuVeSauKhiCoAnh(AlertDialog dialog, Ve veCanSua, Ve ve) {
        if (veCanSua == null) {
            themVe(dialog, ve);
        } else {
            suaVe(dialog, ve);
        }
    }

    private void themVe(AlertDialog dialog, Ve ve) {
        veController.themVe(ve, new ApiCallback<Ve>() {
            @Override
            public void onSuccess(Ve data) {
                Toast.makeText(requireContext(), "Đã thêm vé", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                taiDanhSachVe();
            }

            @Override
            public void onError(String thongBao) {
                baoLoi("Lỗi thêm vé", thongBao);
            }
        });
    }

    private void suaVe(AlertDialog dialog, Ve ve) {
        veController.suaVe(ve, new ApiCallback<Ve>() {
            @Override
            public void onSuccess(Ve data) {
                Toast.makeText(requireContext(), "Đã cập nhật vé", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                taiDanhSachVe();
            }

            @Override
            public void onError(String thongBao) {
                baoLoi("Lỗi cập nhật vé", thongBao);
            }
        });
    }

    private void xacNhanXoaVe(Ve ve) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa vé")
                .setMessage("Xóa vé này sẽ xóa toàn bộ lịch sử liên quan. Tiếp tục?")
                .setPositiveButton("Đồng ý", (dialog, which) -> veController.xoaVe(ve.getMaVe(), new ApiCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean data) {
                        Toast.makeText(requireContext(), "Đã xóa vé", Toast.LENGTH_SHORT).show();
                        taiDanhSachVe();
                    }

                    @Override
                    public void onError(String thongBao) {
                        baoLoi("Lỗi xóa vé", thongBao);
                    }
                }))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private String layChuoi(EditText editText) {
        return editText.getText().toString();
    }

    private double docDouble(EditText editText) {
        String chuoi = layChuoi(editText).trim();
        return chuoi.isEmpty() ? 0 : Double.parseDouble(chuoi);
    }

    private int docInt(EditText editText) {
        String chuoi = layChuoi(editText).trim();
        return chuoi.isEmpty() ? 0 : Integer.parseInt(chuoi);
    }

    private void moManHinhChonAnh() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh vé"), MA_CHON_ANH_VE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != MA_CHON_ANH_VE || resultCode != android.app.Activity.RESULT_OK || data == null || data.getData() == null) {
            return;
        }

        anhVeUriDangChon = data.getData();
        if (lblAnhVeDaChonDangMo != null) {
            lblAnhVeDaChonDangMo.setText("Đã chọn ảnh mới, ảnh sẽ được tải lên khi lưu");
        }
        if (imgAnhVeXemTruocDangMo != null) {
            Glide.with(this)
                    .load(anhVeUriDangChon)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(imgAnhVeXemTruocDangMo);
        }
    }

    private boolean coNoiDung(String chuoi) {
        return chuoi != null && !chuoi.trim().isEmpty();
    }

    private void baoLoi(String tieuDe, String thongBao) {
        if (getContext() != null) {
            TienIch.hienAlert(requireContext(), tieuDe, thongBao);
        }
    }
}
