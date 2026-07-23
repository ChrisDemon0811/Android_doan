package com.example.banve.models;

public class KetQuaKiemTraVoucher {
    private Voucher voucher;
    private boolean hopLe;
    private String lyDo;
    private double tienGiam;
    private double tamTinhDuocApDung;
    private double soTienConThieu;
    private int soVeConThieu;
    private String phamViApDung;

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public boolean isHopLe() {
        return hopLe;
    }

    public void setHopLe(boolean hopLe) {
        this.hopLe = hopLe;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public double getTienGiam() {
        return tienGiam;
    }

    public void setTienGiam(double tienGiam) {
        this.tienGiam = tienGiam;
    }

    public double getTamTinhDuocApDung() {
        return tamTinhDuocApDung;
    }

    public void setTamTinhDuocApDung(double tamTinhDuocApDung) {
        this.tamTinhDuocApDung = tamTinhDuocApDung;
    }

    public double getSoTienConThieu() {
        return soTienConThieu;
    }

    public void setSoTienConThieu(double soTienConThieu) {
        this.soTienConThieu = soTienConThieu;
    }

    public int getSoVeConThieu() {
        return soVeConThieu;
    }

    public void setSoVeConThieu(int soVeConThieu) {
        this.soVeConThieu = soVeConThieu;
    }

    public String getPhamViApDung() {
        return phamViApDung;
    }

    public void setPhamViApDung(String phamViApDung) {
        this.phamViApDung = phamViApDung;
    }
}
