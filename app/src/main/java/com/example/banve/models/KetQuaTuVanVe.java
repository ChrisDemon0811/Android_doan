package com.example.banve.models;

import java.util.ArrayList;
import java.util.List;

public class KetQuaTuVanVe {
    private final List<LuaChonVeTuVan> danhSachLuaChon = new ArrayList<>();
    private LuaChonVeTuVan deXuatChinh;
    private String noiDungDuPhong;

    public List<LuaChonVeTuVan> getDanhSachLuaChon() {
        return danhSachLuaChon;
    }

    public LuaChonVeTuVan getDeXuatChinh() {
        return deXuatChinh;
    }

    public void setDeXuatChinh(LuaChonVeTuVan deXuatChinh) {
        this.deXuatChinh = deXuatChinh;
    }

    public String getNoiDungDuPhong() {
        return noiDungDuPhong;
    }

    public void setNoiDungDuPhong(String noiDungDuPhong) {
        this.noiDungDuPhong = noiDungDuPhong;
    }
}
