package com.example.banve.models;

public class LuaChonDanhMuc {
    private final Integer ma;
    private final String ten;

    public LuaChonDanhMuc(Integer ma, String ten) {
        this.ma = ma;
        this.ten = ten;
    }

    public Integer getMa() {
        return ma;
    }

    public String getTen() {
        return ten;
    }

    @Override
    public String toString() {
        return ten;
    }
}
