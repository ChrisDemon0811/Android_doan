package com.example.banve.controllers;

import com.example.banve.dao.LichSuChatDAO;
import com.example.banve.models.DeXuatThemGioHang;
import com.example.banve.models.KetQuaChatAI;
import com.example.banve.models.KetQuaTuVanVe;
import com.example.banve.models.LichSuChat;
import com.example.banve.models.LuaChonVeTuVan;
import com.example.banve.models.NhomKhachTuVan;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.Session;
import com.example.banve.utils.TienIch;

import java.util.ArrayList;
import java.util.List;

public class ChatAIController {
    private static final int SO_LICH_SU_GUI_AI = 6;

    private final AIContextController aiContextController;
    private final AIServiceController aiServiceController;
    private final LichSuChatDAO lichSuChatDAO;
    private final TuVanVeController tuVanVeController;

    public ChatAIController() {
        aiContextController = new AIContextController();
        aiServiceController = new AIServiceController();
        lichSuChatDAO = new LichSuChatDAO();
        tuVanVeController = new TuVanVeController();
    }

    public void tuVanVeTheoNhom(NhomKhachTuVan nhomKhach, ApiCallback<KetQuaChatAI> callback) {
        if (!Session.dangDangNhap() || Session.nguoiDungHienTai == null) {
            callback.onError("Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại");
            return;
        }

        tuVanVeController.tuVan(nhomKhach, new ApiCallback<KetQuaTuVanVe>() {
            @Override
            public void onSuccess(KetQuaTuVanVe ketQuaTuVan) {
                String cauHoi = taoCauHoiTuVanNhom(nhomKhach);
                if (ketQuaTuVan.getDeXuatChinh() == null) {
                    KetQuaChatAI ketQuaChat = taoKetQuaChat(ketQuaTuVan.getNoiDungDuPhong(), null);
                    luuLichSuTuVan(cauHoi, ketQuaChat, callback);
                    return;
                }

                String prompt = "Bạn chỉ diễn giải đề xuất đã được Java tính sẵn thành câu trả lời tự nhiên bằng tiếng Việt. "
                        + "Không đổi vé đề xuất, không đổi giá, không đổi số lượng, không thêm mã vé hoặc dữ liệu mới.";
                aiServiceController.guiNoiDung(prompt, taoDuLieuTuVanGuiAI(ketQuaTuVan), new ApiCallback<String>() {
                    @Override
                    public void onSuccess(String traLoi) {
                        KetQuaChatAI ketQuaChat = taoKetQuaChat(traLoi, taoDeXuatThemGio(ketQuaTuVan.getDeXuatChinh()));
                        luuLichSuTuVan(cauHoi, ketQuaChat, callback);
                    }

                    @Override
                    public void onError(String thongBao) {
                        KetQuaChatAI ketQuaChat = taoKetQuaChat(
                                ketQuaTuVan.getNoiDungDuPhong(),
                                taoDeXuatThemGio(ketQuaTuVan.getDeXuatChinh())
                        );
                        luuLichSuTuVan(cauHoi, ketQuaChat, callback);
                    }
                });
            }

            @Override
            public void onError(String thongBao) {
                callback.onError(thongBao);
            }
        });
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

    private void luuLichSuTuVan(String cauHoi, KetQuaChatAI ketQua, ApiCallback<KetQuaChatAI> callback) {
        LichSuChat lichSuChat = new LichSuChat();
        lichSuChat.setMaNguoiDung(Session.nguoiDungHienTai.getMaNguoiDung());
        lichSuChat.setCauHoi(cauHoi);
        lichSuChat.setTraLoi(ketQua.getNoiDung());

        lichSuChatDAO.themLichSu(lichSuChat, new ApiCallback<LichSuChat>() {
            @Override
            public void onSuccess(LichSuChat data) {
                callback.onSuccess(ketQua);
            }

            @Override
            public void onError(String thongBao) {
                callback.onSuccess(ketQua);
            }
        });
    }

    private String taoCauHoiTuVanNhom(NhomKhachTuVan nhomKhach) {
        return "Tư vấn vé cho nhóm "
                + nhomKhach.getSoLuongNguoiLon() + " người lớn, "
                + nhomKhach.getSoLuongTreEm() + " trẻ em, "
                + nhomKhach.getSoLuongNguoiCaoTuoi() + " người cao tuổi, ngày "
                + TienIch.dinhDangNgay(nhomKhach.getNgaySuDung());
    }

    private String taoDuLieuTuVanGuiAI(KetQuaTuVanVe ketQuaTuVan) {
        StringBuilder builder = new StringBuilder();
        builder.append("KẾT QUẢ JAVA ĐÃ TÍNH VÀ CHỌN\n")
                .append(ketQuaTuVan.getNoiDungDuPhong())
                .append("\n\nDANH SÁCH SO SÁNH CÓ CẤU TRÚC\n");
        for (LuaChonVeTuVan luaChon : ketQuaTuVan.getDanhSachLuaChon()) {
            builder.append("- MaVe=").append(luaChon.getMaVe())
                    .append(", TenVe=").append(luaChon.getTenVe())
                    .append(", LoaiVe=").append(luaChon.getTenLoaiVe())
                    .append(", TongTien=").append(luaChon.getTongTienDuKien())
                    .append("\n");
        }
        builder.append("Đề xuất chính bắt buộc là vé đầu tiên. Chỉ viết lại cho dễ đọc.");
        return builder.toString();
    }

    private DeXuatThemGioHang taoDeXuatThemGio(LuaChonVeTuVan luaChon) {
        DeXuatThemGioHang deXuat = new DeXuatThemGioHang();
        deXuat.setMaVe(luaChon.getMaVe());
        deXuat.setNgaySuDung(luaChon.getNgaySuDung());
        deXuat.setSoLuongNguoiLon(luaChon.getSoLuongNguoiLon());
        deXuat.setSoLuongTreEm(luaChon.getSoLuongTreEm());
        deXuat.setSoLuongNguoiCaoTuoi(luaChon.getSoLuongNguoiCaoTuoi());
        return deXuat;
    }

    private KetQuaChatAI taoKetQuaChat(String noiDung, DeXuatThemGioHang deXuat) {
        KetQuaChatAI ketQua = new KetQuaChatAI();
        ketQua.setNoiDung(noiDung);
        ketQua.setDeXuatThemGioHang(deXuat);
        return ketQua;
    }

    private String chuoi(String giaTri) {
        return giaTri == null ? "" : giaTri.trim();
    }
}
