package com.example.banve.network;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.webkit.MimeTypeMap;

import com.example.banve.config.SupabaseConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class SupabaseStorageClient {
    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private SupabaseStorageClient() {
    }

    public static void taiAnhVe(Context context, Uri uriAnh, ApiCallback<String> callback) {
        if (context == null || uriAnh == null) {
            callback.onError("Ảnh vé không hợp lệ");
            return;
        }

        String kieuNoiDung = layKieuNoiDung(context, uriAnh);
        byte[] duLieuAnh;
        try {
            duLieuAnh = docDuLieuAnh(context, uriAnh);
        } catch (IOException e) {
            callback.onError("Không thể đọc ảnh: " + e.getMessage());
            return;
        }

        JSONObject duLieuGui;
        try {
            duLieuGui = new JSONObject();
            duLieuGui.put("tenFile", "anh_ve_" + System.currentTimeMillis() + layDuoiFile(kieuNoiDung));
            duLieuGui.put("kieuNoiDung", kieuNoiDung);
            duLieuGui.put("duLieuBase64", Base64.encodeToString(duLieuAnh, Base64.NO_WRAP));
        } catch (JSONException e) {
            callback.onError("Không thể chuẩn bị dữ liệu ảnh");
            return;
        }

        RequestBody body = RequestBody.create(
                duLieuGui.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );
        String urlTaiLen = SupabaseConfig.SUPABASE_URL + "/functions/v1/upload-anh-ve";

        Request request = new Request.Builder()
                .url(urlTaiLen)
                .addHeader("apikey", SupabaseConfig.SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError("Lỗi tải ảnh lên Storage: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (Response phanHoi = response) {
                    if (!phanHoi.isSuccessful()) {
                        String noiDungLoi = phanHoi.body() == null ? "" : phanHoi.body().string();
                        mainHandler.post(() -> callback.onError("Không thể tải ảnh lên Supabase Storage: " + noiDungLoi));
                        return;
                    }

                    String noiDung = phanHoi.body() == null ? "{}" : phanHoi.body().string();
                    try {
                        JSONObject ketQua = new JSONObject(noiDung);
                        String urlCongKhai = ketQua.optString("urlCongKhai", "");
                        if (urlCongKhai.trim().isEmpty()) {
                            mainHandler.post(() -> callback.onError("Không nhận được URL ảnh sau khi tải lên"));
                            return;
                        }
                        mainHandler.post(() -> callback.onSuccess(urlCongKhai));
                    } catch (JSONException e) {
                        mainHandler.post(() -> callback.onError("Phản hồi tải ảnh không hợp lệ"));
                    }
                }
            }
        });
    }

    private static byte[] docDuLieuAnh(Context context, Uri uriAnh) throws IOException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uriAnh);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new IOException("Không mở được tệp ảnh");
            }

            byte[] boDem = new byte[8192];
            int soByte;
            while ((soByte = inputStream.read(boDem)) != -1) {
                outputStream.write(boDem, 0, soByte);
            }
            return outputStream.toByteArray();
        }
    }

    private static String layKieuNoiDung(Context context, Uri uriAnh) {
        String kieuNoiDung = context.getContentResolver().getType(uriAnh);
        if (kieuNoiDung == null || !kieuNoiDung.toLowerCase(Locale.US).startsWith("image/")) {
            return "image/jpeg";
        }
        return kieuNoiDung;
    }

    private static String layDuoiFile(String kieuNoiDung) {
        String duoiFile = MimeTypeMap.getSingleton().getExtensionFromMimeType(kieuNoiDung);
        if (duoiFile == null || duoiFile.trim().isEmpty()) {
            return ".jpg";
        }
        return "." + duoiFile;
    }
}
