package com.example.banve.controllers;

import com.example.banve.dao.LichSuChatDAO;
import com.example.banve.models.LichSuChat;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.Session;

import java.util.ArrayList;
import java.util.List;

public class ChatAIController {
    private static final int SO_LICH_SU_GUI_AI = 6;

    private final AIContextController aiContextController;
    private final AIServiceController aiServiceController;
    private final LichSuChatDAO lichSuChatDAO;

    public ChatAIController() {
        aiContextController = new AIContextController();
        aiServiceController = new AIServiceController();
        lichSuChatDAO = new LichSuChatDAO();
    }

    public void guiCauHoi(String cauHoi, ApiCallback<String> callback) {
        if (cauHoi == null || cauHoi.trim().isEmpty()) {
            callback.onError("Vui lòng nhập câu hỏi");
            return;
        }

        if (!Session.dangDangNhap() || Session.nguoiDungHienTai == null) {
            callback.onError("Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại");
            return;
        }

        String cauHoiChuan = cauHoi.trim();
        aiContextController.taoContextChat(new ApiCallback<String>() {
            @Override
            public void onSuccess(String contextDuLieu) {
                taiLichSuGanDay(cauHoiChuan, contextDuLieu, callback);
            }

            @Override
            public void onError(String thongBao) {
                callback.onError(thongBao);
            }
        });
    }

    private void taiLichSuGanDay(String cauHoi, String contextDuLieu, ApiCallback<String> callback) {
        lichSuChatDAO.layTheoNguoiDung(Session.nguoiDungHienTai.getMaNguoiDung(), new ApiCallback<List<LichSuChat>>() {
            @Override
            public void onSuccess(List<LichSuChat> data) {
                goiAI(cauHoi, contextDuLieu, ghepLichSuChat(data), callback);
            }

            @Override
            public void onError(String thongBao) {
                goiAI(cauHoi, contextDuLieu, "Chưa tải được lịch sử chat gần đây.", callback);
            }
        });
    }

    private void goiAI(String cauHoi, String contextDuLieu, String lichSuGanDay, ApiCallback<String> callback) {
        String promptHeThong = taoQuyTacTraLoi();
        String noiDungNguoiDung = taoNoiDungNguoiDung(contextDuLieu, lichSuGanDay, cauHoi);

        aiServiceController.guiNoiDung(promptHeThong, noiDungNguoiDung, new ApiCallback<String>() {
            @Override
            public void onSuccess(String traLoi) {
                luuLichSu(cauHoi, traLoi, callback);
            }

            @Override
            public void onError(String thongBao) {
                callback.onError(thongBao);
            }
        });
    }

    private String taoQuyTacTraLoi() {
        return "Bạn là trợ lý tư vấn vé cho ứng dụng quản lý bán vé khu du lịch.\n"
                + "- Luôn trả lời bằng tiếng Việt có dấu.\n"
                + "- Chỉ tư vấn trong phạm vi vé, voucher, đơn hàng và dịch vụ của ứng dụng.\n"
                + "- Bắt buộc dựa trên dữ liệu thật được cung cấp trong prompt.\n"
                + "- Không bịa tên vé, giá vé, voucher, hóa đơn hoặc số liệu.\n"
                + "- Nếu dữ liệu không có trong database, hãy nói: “Hiện tại hệ thống chưa có dữ liệu này.”\n"
                + "- Nếu người dùng hỏi vé rẻ nhất, trẻ em, gia đình hoặc người cao tuổi, hãy so sánh trên giá thật.\n"
                + "- Nếu hỏi đơn hàng, chỉ dùng hóa đơn gần đây của chính người dùng hiện tại.\n"
                + "- Không tự đặt vé hoặc thanh toán thay người dùng nếu chưa có flow xác nhận rõ ràng.\n"
                + "- Nếu câu hỏi ngoài phạm vi app, trả lời ngắn gọn và điều hướng về tư vấn vé.";
    }

    private String taoNoiDungNguoiDung(String contextDuLieu, String lichSuGanDay, String cauHoi) {
        return contextDuLieu
                + "\n\nLỊCH SỬ CHAT GẦN ĐÂY\n"
                + lichSuGanDay
                + "\n\nCÂU HỎI HIỆN TẠI\n"
                + cauHoi;
    }

    private String ghepLichSuChat(List<LichSuChat> danhSachLichSu) {
        if (danhSachLichSu == null || danhSachLichSu.isEmpty()) {
            return "Chưa có lịch sử chat gần đây.";
        }

        List<LichSuChat> danhSachGanDay = new ArrayList<>();
        int batDau = Math.max(0, danhSachLichSu.size() - SO_LICH_SU_GUI_AI);
        for (int i = batDau; i < danhSachLichSu.size(); i++) {
            danhSachGanDay.add(danhSachLichSu.get(i));
        }

        StringBuilder builder = new StringBuilder();
        for (LichSuChat lichSuChat : danhSachGanDay) {
            builder.append("- Người dùng: ").append(chuoi(lichSuChat.getCauHoi())).append("\n");
            builder.append("  AI: ").append(chuoi(lichSuChat.getTraLoi())).append("\n");
        }
        return builder.toString();
    }

    private void luuLichSu(String cauHoi, String traLoi, ApiCallback<String> callback) {
        LichSuChat lichSuChat = new LichSuChat();
        lichSuChat.setMaNguoiDung(Session.nguoiDungHienTai.getMaNguoiDung());
        lichSuChat.setCauHoi(cauHoi);
        lichSuChat.setTraLoi(traLoi);

        lichSuChatDAO.themLichSu(lichSuChat, new ApiCallback<LichSuChat>() {
            @Override
            public void onSuccess(LichSuChat data) {
                callback.onSuccess(traLoi);
            }

            @Override
            public void onError(String thongBao) {
                callback.onSuccess(traLoi);
            }
        });
    }

    private String chuoi(String giaTri) {
        return giaTri == null ? "" : giaTri.trim();
    }
}
