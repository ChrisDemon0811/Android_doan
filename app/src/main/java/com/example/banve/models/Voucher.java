package com.example.banve.models;

import com.google.gson.annotations.SerializedName;

public class Voucher {
    @SerializedName("MaVoucher")
    private int maVoucher;

    @SerializedName("MaGiamGia")
    private String maGiamGia;

    @SerializedName("TenVoucher")
    private String tenVoucher;

    @SerializedName("KieuGiamGia")
    private String kieuGiamGia;

    @SerializedName("GiaTriGiam")
    private double giaTriGiam;

    @SerializedName("NgayBatDau")
    private String ngayBatDau;

    @SerializedName("NgayKetThuc")
    private String ngayKetThuc;

    @SerializedName("SoLuong")
    private int soLuong;

    @SerializedName("TrangThai")
    private String trangThai;

    @SerializedName("DonToiThieu")
    private double donToiThieu;

    @SerializedName("GiamToiDa")
    private double giamToiDa;

    @SerializedName("SoLuongVeToiThieu")
    private int soLuongVeToiThieu = 1;

    @SerializedName("SoLanDungToiDaMoiNguoi")
    private int soLanDungToiDaMoiNguoi;

    @SerializedName("ChiApDungKhachMoi")
    private boolean chiApDungKhachMoi;

    @SerializedName("MaLoaiVeApDung")
    private Integer maLoaiVeApDung;

    @SerializedName("MaVeApDung")
    private Integer maVeApDung;

    @SerializedName("MucTieu")
    private String mucTieu;

    @SerializedName("MoTaDieuKien")
    private String moTaDieuKien;

    private transient String phamViApDung;

    public int getMaVoucher() {
        return maVoucher;
    }

    public void setMaVoucher(int maVoucher) {
        this.maVoucher = maVoucher;
    }

    public String getMaGiamGia() {
        return maGiamGia;
    }

    public void setMaGiamGia(String maGiamGia) {
        this.maGiamGia = maGiamGia;
    }

    public String getTenVoucher() {
        return tenVoucher;
    }

    public void setTenVoucher(String tenVoucher) {
        this.tenVoucher = tenVoucher;
    }

    public String getKieuGiamGia() {
        return kieuGiamGia;
    }

    public void setKieuGiamGia(String kieuGiamGia) {
        this.kieuGiamGia = kieuGiamGia;
    }

    public double getGiaTriGiam() {
        return giaTriGiam;
    }

    public void setGiaTriGiam(double giaTriGiam) {
        this.giaTriGiam = giaTriGiam;
    }

    public String getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(String ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public String getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(String ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public double getDonToiThieu() {
        return donToiThieu;
    }

    public void setDonToiThieu(double donToiThieu) {
        this.donToiThieu = donToiThieu;
    }

    public double getGiamToiDa() {
        return giamToiDa;
    }

    public void setGiamToiDa(double giamToiDa) {
        this.giamToiDa = giamToiDa;
    }

    public int getSoLuongVeToiThieu() {
        return soLuongVeToiThieu;
    }

    public void setSoLuongVeToiThieu(int soLuongVeToiThieu) {
        this.soLuongVeToiThieu = soLuongVeToiThieu;
    }

    public int getSoLanDungToiDaMoiNguoi() {
        return soLanDungToiDaMoiNguoi;
    }

    public void setSoLanDungToiDaMoiNguoi(int soLanDungToiDaMoiNguoi) {
        this.soLanDungToiDaMoiNguoi = soLanDungToiDaMoiNguoi;
    }

    public boolean isChiApDungKhachMoi() {
        return chiApDungKhachMoi;
    }

    public void setChiApDungKhachMoi(boolean chiApDungKhachMoi) {
        this.chiApDungKhachMoi = chiApDungKhachMoi;
    }

    public Integer getMaLoaiVeApDung() {
        return maLoaiVeApDung;
    }

    public void setMaLoaiVeApDung(Integer maLoaiVeApDung) {
        this.maLoaiVeApDung = maLoaiVeApDung;
    }

    public Integer getMaVeApDung() {
        return maVeApDung;
    }

    public void setMaVeApDung(Integer maVeApDung) {
        this.maVeApDung = maVeApDung;
    }

    public String getMucTieu() {
        return mucTieu;
    }

    public void setMucTieu(String mucTieu) {
        this.mucTieu = mucTieu;
    }

    public String getMoTaDieuKien() {
        return moTaDieuKien;
    }

    public void setMoTaDieuKien(String moTaDieuKien) {
        this.moTaDieuKien = moTaDieuKien;
    }

    public String getPhamViApDung() {
        return phamViApDung;
    }

    public void setPhamViApDung(String phamViApDung) {
        this.phamViApDung = phamViApDung;
    }
}
