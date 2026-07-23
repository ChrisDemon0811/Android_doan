package com.example.banve.controllers;

import com.example.banve.models.PhanTichVoucher;
import com.example.banve.models.Voucher;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PhanTichVoucherControllerTest {
    @Test
    public void phanTramKhongCoTranKhongBiaNganSachToiDa() {
        Voucher voucher = new Voucher();
        voucher.setKieuGiamGia("PhanTram");
        voucher.setGiaTriGiam(10);
        voucher.setGiamToiDa(0);
        voucher.setDonToiThieu(800000);
        voucher.setSoLuong(50);

        PhanTichVoucher ketQua = new PhanTichVoucherController().phanTich(voucher);

        assertEquals(
                "Không thể xác định ngân sách tối đa vì voucher chưa giới hạn số tiền giảm trên mỗi đơn.",
                ketQua.getNganSachGiamGia()
        );
    }
}
