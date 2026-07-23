package com.example.banve.controllers;

import com.example.banve.dao.VeDAO;
import com.example.banve.models.KetQuaTuVanVe;
import com.example.banve.models.LoaiVe;
import com.example.banve.models.LuaChonVeTuVan;
import com.example.banve.models.NhomKhachTuVan;
import com.example.banve.models.Ve;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.DinhDangTien;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TuVanVeController {
    private static final int SO_LUA_CHON_TOI_DA = 3;
    private final VeDAO veDAO;

    public TuVanVeController() {
        veDAO = new VeDAO();
    }

    public void tuVan(NhomKhachTuVan nhomKhach, ApiCallback<KetQuaTuVanVe> callback) {
        String loi = kiemTraNhomKhach(nhomKhach);
        if (loi != null) {
            callback.onError(loi);
            return;
        }

        veDAO.layDanhSachVeQuanLy(new ApiCallback<List<Ve>>() {
            @Override
            public void onSuccess(List<Ve> data) {
                callback.onSuccess(tinhKetQuaTuDanhSach(nhomKhach, data));
            }

            @Override
            public void onError(String thongBao) {
                callback.onError("Không thể tải dữ liệu vé để tư vấn");
            }
        });
    }

    public KetQuaTuVanVe tinhKetQuaTuDanhSach(NhomKhachTuVan nhomKhach, List<Ve> danhSachVe) {
        KetQuaTuVanVe ketQua = new KetQuaTuVanVe();
        String loi = kiemTraNhomKhach(nhomKhach);
        if (loi != null) {
            ketQua.setNoiDungDuPhong(loi);
            return ketQua;
        }

        List<LuaChonVeTuVan> danhSachPhuHop = new ArrayList<>();
        if (danhSachVe != null) {
            for (Ve ve : danhSachVe) {
                if (vePhuHop(ve, nhomKhach)) {
                    danhSachPhuHop.add(taoLuaChon(ve, nhomKhach));
                }
            }
        }
        Collections.sort(danhSachPhuHop, Comparator.comparingDouble(LuaChonVeTuVan::getTongTienDuKien));
        for (int i = 0; i < danhSachPhuHop.size() && i < SO_LUA_CHON_TOI_DA; i++) {
            ketQua.getDanhSachLuaChon().add(danhSachPhuHop.get(i));
        }

        if (ketQua.getDanhSachLuaChon().isEmpty()) {
            ketQua.setNoiDungDuPhong("Hiện tại hệ thống chưa có vé phù hợp với đầy đủ nhóm khách đã chọn.");
            return ketQua;
        }

        ketQua.setDeXuatChinh(ketQua.getDanhSachLuaChon().get(0));
        ketQua.setNoiDungDuPhong(taoNoiDungDuPhong(nhomKhach, ketQua));
        return ketQua;
    }

    public String kiemTraNhomKhach(NhomKhachTuVan nhomKhach) {
        if (nhomKhach == null) {
            return "Thông tin nhóm khách không hợp lệ";
        }
        if (nhomKhach.getSoLuongNguoiLon() < 0
                || nhomKhach.getSoLuongTreEm() < 0
                || nhomKhach.getSoLuongNguoiCaoTuoi() < 0) {
            return "Số lượng khách không được âm";
        }
        int tongSoLuong = nhomKhach.getSoLuongNguoiLon()
                + nhomKhach.getSoLuongTreEm()
                + nhomKhach.getSoLuongNguoiCaoTuoi();
        if (tongSoLuong <= 0) {
            return "Vui lòng nhập ít nhất một khách";
        }
        Date ngaySuDung = parseNgay(nhomKhach.getNgaySuDung());
        if (ngaySuDung == null) {
            return "Ngày sử dụng không hợp lệ";
        }
        Calendar homNay = Calendar.getInstance();
        datDauNgay(homNay);
        if (ngaySuDung.before(homNay.getTime())) {
            return "Ngày sử dụng không được nhỏ hơn ngày hiện tại";
        }
        return null;
    }

    private boolean vePhuHop(Ve ve, NhomKhachTuVan nhomKhach) {
        if (ve == null || ve.getMaVe() <= 0 || !"HoatDong".equals(ve.getTrangThai())) {
            return false;
        }
        if (nhomKhach.getSoLuongNguoiLon() > 0 && ve.getGiaNguoiLon() <= 0) {
            return false;
        }
        if (nhomKhach.getSoLuongTreEm() > 0 && ve.getGiaTreEm() <= 0) {
            return false;
        }
        return nhomKhach.getSoLuongNguoiCaoTuoi() <= 0 || ve.getGiaNguoiCaoTuoi() > 0;
    }

    private LuaChonVeTuVan taoLuaChon(Ve ve, NhomKhachTuVan nhomKhach) {
        LuaChonVeTuVan luaChon = new LuaChonVeTuVan();
        luaChon.setMaVe(ve.getMaVe());
        luaChon.setTenVe(ve.getTenVe());
        LoaiVe loaiVe = ve.getLoaiVe();
        luaChon.setTenLoaiVe(loaiVe == null ? "Chưa rõ loại vé" : loaiVe.getTenLoaiVe());
        luaChon.setGiaNguoiLon(ve.getGiaNguoiLon());
        luaChon.setGiaTreEm(ve.getGiaTreEm());
        luaChon.setGiaNguoiCaoTuoi(ve.getGiaNguoiCaoTuoi());
        luaChon.setSoLuongNguoiLon(nhomKhach.getSoLuongNguoiLon());
        luaChon.setSoLuongTreEm(nhomKhach.getSoLuongTreEm());
        luaChon.setSoLuongNguoiCaoTuoi(nhomKhach.getSoLuongNguoiCaoTuoi());
        luaChon.setNgaySuDung(nhomKhach.getNgaySuDung());
        luaChon.setTongTienDuKien(
                nhomKhach.getSoLuongNguoiLon() * ve.getGiaNguoiLon()
                        + nhomKhach.getSoLuongTreEm() * ve.getGiaTreEm()
                        + nhomKhach.getSoLuongNguoiCaoTuoi() * ve.getGiaNguoiCaoTuoi()
        );
        return luaChon;
    }

    private String taoNoiDungDuPhong(NhomKhachTuVan nhomKhach, KetQuaTuVanVe ketQua) {
        LuaChonVeTuVan deXuat = ketQua.getDeXuatChinh();
        StringBuilder builder = new StringBuilder();
        builder.append("Với nhóm ")
                .append(nhomKhach.getSoLuongNguoiLon()).append(" người lớn, ")
                .append(nhomKhach.getSoLuongTreEm()).append(" trẻ em và ")
                .append(nhomKhach.getSoLuongNguoiCaoTuoi()).append(" người cao tuổi, mình đề xuất ")
                .append(deXuat.getTenVe()).append(".\n\n")
                .append("Chi phí dự kiến:\n")
                .append("- ").append(nhomKhach.getSoLuongNguoiLon()).append(" người lớn: ")
                .append(DinhDangTien.dinhDang(nhomKhach.getSoLuongNguoiLon() * deXuat.getGiaNguoiLon())).append("\n")
                .append("- ").append(nhomKhach.getSoLuongTreEm()).append(" trẻ em: ")
                .append(DinhDangTien.dinhDang(nhomKhach.getSoLuongTreEm() * deXuat.getGiaTreEm())).append("\n")
                .append("- ").append(nhomKhach.getSoLuongNguoiCaoTuoi()).append(" người cao tuổi: ")
                .append(DinhDangTien.dinhDang(nhomKhach.getSoLuongNguoiCaoTuoi() * deXuat.getGiaNguoiCaoTuoi())).append("\n")
                .append("- Tổng cộng: ").append(DinhDangTien.dinhDang(deXuat.getTongTienDuKien())).append("\n\n")
                .append("Đây là lựa chọn có tổng chi phí thấp nhất trong các vé phù hợp hiện tại.");
        if (ketQua.getDanhSachLuaChon().size() > 1) {
            builder.append("\n\nCác lựa chọn khác để tham khảo:");
            for (int i = 1; i < ketQua.getDanhSachLuaChon().size(); i++) {
                LuaChonVeTuVan luaChon = ketQua.getDanhSachLuaChon().get(i);
                builder.append("\n- ").append(luaChon.getTenVe())
                        .append(": ").append(DinhDangTien.dinhDang(luaChon.getTongTienDuKien()));
            }
        }
        return builder.toString();
    }

    private Date parseNgay(String ngay) {
        if (ngay == null || ngay.trim().isEmpty()) {
            return null;
        }
        SimpleDateFormat dinhDang = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dinhDang.setLenient(false);
        try {
            return dinhDang.parse(ngay);
        } catch (ParseException e) {
            return null;
        }
    }

    private void datDauNgay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}
