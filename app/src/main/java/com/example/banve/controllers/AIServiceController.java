package com.example.banve.controllers;

import android.os.Handler;
import android.os.Looper;

import com.example.banve.dao.CauHinhAIDAO;
import com.example.banve.models.CauHinhAI;
import com.example.banve.network.ApiCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class AIServiceController {
    private final CauHinhAIDAO cauHinhAIDAO;
    private final Handler mainHandler;

    public AIServiceController() {
        cauHinhAIDAO = new CauHinhAIDAO();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void guiNoiDung(String promptHeThongBoSung, String noiDungNguoiDung, ApiCallback<String> callback) {
        if (rong(noiDungNguoiDung)) {
            callback.onError("Nội dung gửi AI không hợp lệ");
            return;
        }

        cauHinhAIDAO.layCauHinhHienTai(new ApiCallback<CauHinhAI>() {
            @Override
            public void onSuccess(CauHinhAI cauHinhAI) {
                goiApiAI(cauHinhAI, promptHeThongBoSung, noiDungNguoiDung, callback);
            }

            @Override
            public void onError(String thongBao) {
                callback.onError(thongBao);
            }
        });
    }

    private void goiApiAI(CauHinhAI cauHinhAI, String promptHeThongBoSung, String noiDungNguoiDung, ApiCallback<String> callback) {
        String loi = kiemTraCauHinh(cauHinhAI);
        if (loi != null) {
            callback.onError(loi);
            return;
        }

        new Thread(() -> {
            try {
                String traLoi = guiYeuCauHttp(cauHinhAI, promptHeThongBoSung, noiDungNguoiDung);
                mainHandler.post(() -> callback.onSuccess(traLoi));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(chuyenLoiThanThien(e)));
            }
        }).start();
    }

    private String kiemTraCauHinh(CauHinhAI cauHinhAI) {
        if (cauHinhAI == null) {
            return "Chưa có cấu hình AI";
        }
        if (rong(cauHinhAI.getNhaCungCap())) {
            return "Chưa cấu hình nhà cung cấp AI";
        }
        if (rong(cauHinhAI.getKhoaApi())) {
            return "Chưa cấu hình khóa API AI";
        }
        if (rong(cauHinhAI.getMoHinh())) {
            return "Chưa cấu hình mô hình AI";
        }
        if (!laCauHinhGemini(cauHinhAI.getNhaCungCap())) {
            return "Hiện tại ứng dụng chỉ hỗ trợ Gemini, vui lòng kiểm tra lại cấu hình AI";
        }
        return null;
    }

    private String guiYeuCauHttp(CauHinhAI cauHinhAI, String promptHeThongBoSung, String noiDungNguoiDung) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(taoUrlGemini(cauHinhAI)).openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(60000);
        connection.setDoOutput(true);
        connection.setRequestProperty("x-goog-api-key", cauHinhAI.getKhoaApi());
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        byte[] body = taoBody(cauHinhAI, promptHeThongBoSung, noiDungNguoiDung).toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(body);
        }

        int statusCode = connection.getResponseCode();
        String responseBody = docResponse(connection, statusCode);
        connection.disconnect();

        if (statusCode < 200 || statusCode >= 300) {
            throw new Exception("HTTP " + statusCode + " - " + responseBody);
        }

        return parseTraLoi(responseBody);
    }

    private JSONObject taoBody(CauHinhAI cauHinhAI, String promptHeThongBoSung, String noiDungNguoiDung) throws Exception {
        JSONObject body = new JSONObject();
        String promptHeThong = ghepPromptHeThong(cauHinhAI.getNhacLenh(), promptHeThongBoSung);
        if (!rong(promptHeThong)) {
            body.put("system_instruction", new JSONObject()
                    .put("parts", new JSONArray()
                            .put(new JSONObject().put("text", promptHeThong))));
        }

        body.put("contents", new JSONArray()
                .put(new JSONObject()
                        .put("role", "user")
                        .put("parts", new JSONArray()
                                .put(new JSONObject().put("text", noiDungNguoiDung)))));
        return body;
    }

    private String ghepPromptHeThong(String promptCauHinh, String promptBoSung) {
        StringBuilder builder = new StringBuilder();
        if (!rong(promptCauHinh)) {
            builder.append(promptCauHinh.trim());
        }
        if (!rong(promptBoSung)) {
            if (builder.length() > 0) {
                builder.append("\n\n");
            }
            builder.append(promptBoSung.trim());
        }
        return builder.toString();
    }

    private String taoUrlGemini(CauHinhAI cauHinhAI) {
        String nhaCungCap = cauHinhAI.getNhaCungCap().trim();
        String moHinh = chuanHoaMoHinhGemini(cauHinhAI.getMoHinh());

        if (nhaCungCap.startsWith("http://") || nhaCungCap.startsWith("https://")) {
            if (nhaCungCap.contains("{model}")) {
                return nhaCungCap.replace("{model}", moHinh);
            }
            if (nhaCungCap.contains(":generateContent")) {
                return nhaCungCap;
            }

            String url = nhaCungCap.endsWith("/")
                    ? nhaCungCap.substring(0, nhaCungCap.length() - 1)
                    : nhaCungCap;
            if (url.endsWith("/models")) {
                return url + "/" + moHinh + ":generateContent";
            }
            if (url.endsWith("/v1beta")) {
                return url + "/models/" + moHinh + ":generateContent";
            }
            return url + "/v1beta/models/" + moHinh + ":generateContent";
        }

        return "https://generativelanguage.googleapis.com/v1beta/models/"
                + moHinh
                + ":generateContent";
    }

    private String chuanHoaMoHinhGemini(String moHinh) {
        String giaTri = moHinh.trim();
        if (giaTri.startsWith("models/")) {
            giaTri = giaTri.substring("models/".length());
        }
        return giaTri.toLowerCase(Locale.US);
    }

    private String docResponse(HttpURLConnection connection, int statusCode) throws Exception {
        InputStream inputStream = statusCode >= 200 && statusCode < 300
                ? connection.getInputStream()
                : connection.getErrorStream();

        if (inputStream == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private String parseTraLoi(String responseBody) throws Exception {
        JSONObject jsonObject = new JSONObject(responseBody);
        JSONArray candidates = jsonObject.optJSONArray("candidates");
        if (candidates == null || candidates.length() == 0) {
            throw new Exception("AI không trả về nội dung");
        }

        JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
        if (content == null) {
            String lyDo = candidates.getJSONObject(0).optString("finishReason", "");
            throw new Exception(lyDo.isEmpty() ? "AI không trả về nội dung" : "AI dừng trả lời: " + lyDo);
        }

        JSONArray parts = content.optJSONArray("parts");
        if (parts == null || parts.length() == 0) {
            throw new Exception("AI không trả về nội dung");
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length(); i++) {
            String text = parts.getJSONObject(i).optString("text", "");
            if (!text.trim().isEmpty()) {
                builder.append(text);
            }
        }

        String traLoi = builder.toString().trim();
        if (traLoi.isEmpty()) {
            throw new Exception("AI trả lời rỗng");
        }
        return traLoi;
    }

    private String chuyenLoiThanThien(Exception e) {
        String thongBao = e.getMessage() == null ? "" : e.getMessage();
        if (thongBao.contains("HTTP 400")) {
            return "Không thể kết nối đến AI. Vui lòng kiểm tra mô hình hoặc nội dung cấu hình.";
        }
        if (thongBao.contains("HTTP 401") || thongBao.contains("HTTP 403")) {
            return "Không thể kết nối đến AI. Vui lòng kiểm tra khóa API.";
        }
        if (thongBao.contains("HTTP 404")) {
            return "Không thể kết nối đến AI. Vui lòng kiểm tra nhà cung cấp hoặc tên mô hình.";
        }
        if (thongBao.contains("AI không trả về") || thongBao.contains("AI trả lời rỗng") || thongBao.contains("AI dừng trả lời")) {
            return thongBao;
        }
        return "Không thể kết nối đến AI. Vui lòng kiểm tra cấu hình hoặc mạng.";
    }

    private boolean laCauHinhGemini(String nhaCungCap) {
        if (rong(nhaCungCap)) {
            return false;
        }
        String giaTri = nhaCungCap.toLowerCase(Locale.US);
        return giaTri.contains("gemini") || giaTri.contains("generativelanguage.googleapis.com");
    }

    private boolean rong(String chuoi) {
        return chuoi == null || chuoi.trim().isEmpty();
    }
}
