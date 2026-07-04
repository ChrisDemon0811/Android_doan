package com.example.banve.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BoNhoAnh {
    private static final int KICH_THUOC_CACHE = (int) (Runtime.getRuntime().maxMemory() / 8);
    private static final LruCache<String, Bitmap> boNhoAnh = new LruCache<String, Bitmap>(KICH_THUOC_CACHE) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return bitmap.getByteCount();
        }
    };
    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final Map<String, List<WeakReference<ImageView>>> danhSachDangCho = new ConcurrentHashMap<>();

    private BoNhoAnh() {
    }

    public static void taiAnh(String duongDanAnh, ImageView imageView, int anhMacDinh) {
        String khoaAnh = duongDanAnh == null ? "" : duongDanAnh.trim();
        imageView.setTag(khoaAnh);

        if (khoaAnh.isEmpty()) {
            imageView.setImageResource(anhMacDinh);
            return;
        }

        Bitmap bitmapTrongCache = boNhoAnh.get(khoaAnh);
        if (bitmapTrongCache != null) {
            imageView.setImageBitmap(bitmapTrongCache);
            return;
        }

        imageView.setImageResource(anhMacDinh);
        List<WeakReference<ImageView>> danhSachChoMoi = Collections.synchronizedList(new ArrayList<>());
        List<WeakReference<ImageView>> danhSachChoCu = danhSachDangCho.putIfAbsent(khoaAnh, danhSachChoMoi);
        List<WeakReference<ImageView>> danhSachCho = danhSachChoCu == null ? danhSachChoMoi : danhSachChoCu;
        danhSachCho.add(new WeakReference<>(imageView));

        if (danhSachChoCu != null) {
            return;
        }

        executorService.execute(() -> {
            Bitmap bitmap = taiBitmap(khoaAnh);
            if (bitmap != null) {
                boNhoAnh.put(khoaAnh, bitmap);
            }
            mainHandler.post(() -> capNhatAnhDangCho(khoaAnh, bitmap, anhMacDinh));
        });
    }

    public static void xoaBoNho() {
        boNhoAnh.evictAll();
        danhSachDangCho.clear();
    }

    private static Bitmap taiBitmap(String duongDanAnh) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(duongDanAnh);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setUseCaches(true);
            try (InputStream inputStream = connection.getInputStream()) {
                return BitmapFactory.decodeStream(inputStream);
            }
        } catch (Exception ignored) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static void capNhatAnhDangCho(String khoaAnh, Bitmap bitmap, int anhMacDinh) {
        List<WeakReference<ImageView>> danhSachCho = danhSachDangCho.remove(khoaAnh);
        if (danhSachCho == null) {
            return;
        }

        synchronized (danhSachCho) {
            for (WeakReference<ImageView> thamChieuAnh : danhSachCho) {
                ImageView imageView = thamChieuAnh.get();
                if (imageView == null || !khoaAnh.equals(imageView.getTag())) {
                    continue;
                }
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(anhMacDinh);
                }
            }
        }
    }
}
