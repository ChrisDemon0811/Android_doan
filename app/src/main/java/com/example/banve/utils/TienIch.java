package com.example.banve.utils;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.Toast;

import com.example.banve.R;
import com.example.banve.activities.user.DangNhapActivity;

import java.text.ParsePosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class TienIch {
    private static final String DINH_DANG_NGAY = "dd/MM/yyyy";
    private static final String DINH_DANG_NGAY_GIO = "dd/MM/yyyy HH:mm";
    private static final String[] DINH_DANG_NGAY_NGUON = {
            "dd/MM/yyyy",
            "yyyy-MM-dd"
    };
    private static final String[] DINH_DANG_NGAY_GIO_NGUON = {
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "yyyy-MM-dd'T'HH:mm:ssX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "dd/MM/yyyy HH:mm",
            "yyyy-MM-dd",
            "dd/MM/yyyy"
    };

    private TienIch() {
    }

    public static String dinhDangNgay(Date ngay) {
        if (ngay == null) {
            return "";
        }
        return new SimpleDateFormat(DINH_DANG_NGAY, Locale.getDefault()).format(ngay);
    }

    public static String dinhDangNgay(String chuoiNgay) {
        if (chuoiNgay == null || chuoiNgay.trim().isEmpty()) {
            return "";
        }

        String giaTri = chuoiNgay.trim();
        Date ngay = parseTheoDinhDang(giaTri, DINH_DANG_NGAY_NGUON);
        if (ngay == null && giaTri.length() >= 10) {
            ngay = parseTheoDinhDang(giaTri.substring(0, 10), DINH_DANG_NGAY_NGUON);
        }
        return ngay == null
                ? giaTri
                : new SimpleDateFormat(DINH_DANG_NGAY, Locale.getDefault()).format(ngay);
    }

    public static String dinhDangNgayGio(String chuoiNgayGio) {
        if (chuoiNgayGio == null || chuoiNgayGio.trim().isEmpty()) {
            return "";
        }

        String giaTri = chuoiNgayGio.trim();
        Date ngay = parseTheoDinhDang(giaTri, DINH_DANG_NGAY_GIO_NGUON);
        if (ngay == null) {
            return dinhDangNgay(giaTri);
        }

        boolean coGio = giaTri.contains("T")
                || giaTri.matches(".*\\d{2}:\\d{2}.*");
        return new SimpleDateFormat(
                coGio ? DINH_DANG_NGAY_GIO : DINH_DANG_NGAY,
                Locale.getDefault()
        ).format(ngay);
    }

    public static Date parseNgay(String chuoiNgay) {
        try {
            SimpleDateFormat dinhDang = new SimpleDateFormat(DINH_DANG_NGAY, Locale.getDefault());
            dinhDang.setLenient(false);
            return dinhDang.parse(chuoiNgay);
        } catch (ParseException e) {
            return null;
        }
    }

    private static Date parseTheoDinhDang(String giaTri, String[] danhSachDinhDang) {
        for (String mau : danhSachDinhDang) {
            SimpleDateFormat dinhDang = new SimpleDateFormat(mau, Locale.getDefault());
            dinhDang.setLenient(false);
            ParsePosition viTri = new ParsePosition(0);
            Date ngay = dinhDang.parse(giaTri, viTri);
            if (ngay != null && viTri.getIndex() == giaTri.length()) {
                return ngay;
            }
        }
        return null;
    }

    public static void hienToast(Context context, String thongBao) {
        Toast.makeText(context, thongBao, Toast.LENGTH_SHORT).show();
    }

    public static void hienAlert(Context context, String tieuDe, String noiDung) {
        new AlertDialog.Builder(context)
                .setTitle(tieuDe)
                .setMessage(noiDung)
                .setPositiveButton("Đồng ý", null)
                .show();
    }

    public static void ganAnHienMatKhau(EditText editText) {
        if (editText == null) {
            return;
        }

        final boolean[] dangHien = {false};
        capNhatIconMatKhau(editText, dangHien[0]);
        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        editText.setOnTouchListener((view, event) -> {
            if (event.getAction() != MotionEvent.ACTION_UP) {
                return false;
            }

            Drawable drawablePhai = editText.getCompoundDrawables()[2];
            if (drawablePhai == null) {
                return false;
            }

            float viTriBatDauIcon = editText.getWidth()
                    - editText.getPaddingRight()
                    - drawablePhai.getBounds().width();
            if (event.getX() < viTriBatDauIcon) {
                return false;
            }

            dangHien[0] = !dangHien[0];
            editText.setTransformationMethod(dangHien[0]
                    ? null
                    : PasswordTransformationMethod.getInstance());
            capNhatIconMatKhau(editText, dangHien[0]);
            editText.setSelection(editText.getText().length());
            view.performClick();
            return true;
        });
    }

    private static void capNhatIconMatKhau(EditText editText, boolean dangHien) {
        editText.setCompoundDrawablePadding(12);
        editText.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                dangHien ? R.drawable.ic_mat_khau_an : R.drawable.ic_mat_khau_hien,
                0
        );
    }

    public static void dangXuat(Activity hoatDong) {
        new AlertDialog.Builder(hoatDong)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất?")
                .setPositiveButton("Có", (dialog, which) -> {
                    Session.dangXuat();
                    Session.xoaLocal(hoatDong);
                    Intent intent = new Intent(hoatDong, DangNhapActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    hoatDong.startActivity(intent);
                    hoatDong.finish();
                })
                .setNegativeButton("Không", null)
                .show();
    }
}
