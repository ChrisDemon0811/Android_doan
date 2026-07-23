package com.example.banve.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HoaDon {
    @SerializedName("MaHoaDon")
    private int maHoaDon;

    @SerializedName("MaXacThuc")
    private String maXacThuc;

    @SerializedName("MaNguoiDung")
    private int maNguoiDung;

    @SerializedName("NgayLap")
    private String ngayLap;

    @SerializedName("TongTien")
    private double tongTien;

    @SerializedName("MaVoucher")
    private Integer maVoucher;

    @SerializedName("TienGiam")
    private double tienGiam;

    @SerializedName("ThanhToan")
    private String thanhToan;

    @SerializedName("TrangThai")
    private String trangThai;

    @SerializedName("NoiDungChuyenKhoan")
    private String noiDungChuyenKhoan;

    @SerializedName("NguoiDung")
    private NguoiDung nguoiDung;

    private List<ChiTietHoaDon> danhSachChiTiet;

    public int getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(int maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getMaXacThuc() {
        return maXacThuc;
    }

    public void setMaXacThuc(String maXacThuc) {
        this.maXacThuc = maXacThuc;
    }

    public int getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(int maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public String getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(String ngayLap) {
        this.ngayLap = ngayLap;
    }

    public double getTongTien() {
        return tongTien;
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    public Integer getMaVoucher() {
        return maVoucher;
    }

    public void setMaVoucher(Integer maVoucher) {
        this.maVoucher = maVoucher;
    }

    public double getTienGiam() {
        return tienGiam;
    }

    public void setTienGiam(double tienGiam) {
        this.tienGiam = tienGiam;
    }

    public String getThanhToan() {
        return thanhToan;
    }

    public void setThanhToan(String thanhToan) {
        this.thanhToan = thanhToan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getNoiDungChuyenKhoan() {
        return noiDungChuyenKhoan;
    }

    public void setNoiDungChuyenKhoan(String noiDungChuyenKhoan) {
        this.noiDungChuyenKhoan = noiDungChuyenKhoan;
    }

    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }

    public void setNguoiDung(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    public List<ChiTietHoaDon> getDanhSachChiTiet() {
        return danhSachChiTiet;
    }

    public void setDanhSachChiTiet(List<ChiTietHoaDon> danhSachChiTiet) {
        this.danhSachChiTiet = danhSachChiTiet;
    }
}
