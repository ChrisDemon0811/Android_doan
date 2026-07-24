package com.example.banve.controllers;

import com.example.banve.models.KetQuaThongKe;
import com.example.banve.models.ThongKeTheoLoaiVe;
import com.example.banve.models.ThongKeTheoNgay;
import com.example.banve.models.ThongKeTheoThang;
import com.example.banve.models.ThongKeTongQuan;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.DinhDangTien;
import com.example.banve.utils.Session;
import com.example.banve.utils.TienIch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AIThongKeController {
    private final ThongKeController thongKeController;
    private final AIServiceController aiServiceController;
    private final SimpleDateFormat dinhDangNgay;

    public AIThongKeController() {
        thongKeController = new ThongKeController();
        aiServiceController = new AIServiceController();
        dinhDangNgay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    public void phanTichThongKe(Date tuNgay, Date denNgay, ApiCallback<String> callback) {
        if (!Session.laQuanLy()) {
            callback.onError("Bạn không có quyền dùng chức năng AI phân tích thống kê");
            return;
        }
        if (tuNgay == null || denNgay == null) {
            callback.onError("Ngày thống kê không hợp lệ");
            return;
        }
        if (tuNgay.after(denNgay)) {
            callback.onError("Từ ngày không được lớn hơn đến ngày");
            return;
        }

        thongKeController.layThongKeTongQuan(tuNgay, denNgay, new ApiCallback<KetQuaThongKe>() {
            @Override
            public void onSuccess(KetQuaThongKe data) {
                if (khongCoDuLieu(data)) {
                    callback.onError("Chưa có dữ liệu thống kê trong khoảng thời gian đã chọn.");
                    return;
                }
                goiAIPhanTich(tuNgay, denNgay, data, callback);
            }

            @Override
            public void onError(String thongBao) {
                callback.onError(thongBao);
            }
        });
    }

    private void goiAIPhanTich(Date tuNgay, Date denNgay, KetQuaThongKe ketQuaThongKe, ApiCallback<String> callback) {
        String promptHeThong = "Bạn là trợ lý phân tích doanh thu cho quản lý khu du lịch.\n"
                + "- Trả lời bằng tiếng Việt có dấu.\n"
                + "- Chỉ dựa vào số liệu thống kê được cung cấp.\n"
                + "- Không bịa số liệu, không suy diễn ngoài dữ liệu.\n"
                + "- Nếu dữ liệu ít, hãy cảnh báo chưa đủ cơ sở kết luận.\n"
                + "- Phân tích đơn giản, dễ hiểu, phù hợp project sinh viên.\n"
                + "- Định dạng kết quả gồm: 1. Tổng quan, 2. Điểm nổi bật, 3. Vé/loại vé bán chạy, 4. Vấn đề cần chú ý, 5. Gợi ý cải thiện doanh thu, 6. Kết luận.";

        String noiDung = "Khoảng thời gian: "
                + dinhDangNgay.format(tuNgay)
                + " - "
                + dinhDangNgay.format(denNgay)
                + "\n\n"
                + taoContextThongKe(ketQuaThongKe);

        aiServiceController.guiNoiDung(promptHeThong, noiDung, callback);
    }

    private String taoContextThongKe(KetQuaThongKe ketQuaThongKe) {
        StringBuilder builder = new StringBuilder();
        ThongKeTongQuan tongQuan = ketQuaThongKe.getThongKeTongQuan();
        builder.append("SỐ LIỆU THẬT TỪ HỆ THỐNG\n");
        builder.append("- Tổng số hóa đơn đã thanh toán: ").append(tongQuan.getTongHoaDon()).append("\n");
        builder.append("- Tổng doanh thu trước giảm: ").append(DinhDangTien.dinhDang(tongQuan.getTongDoanhThu())).append("\n");
        builder.append("- Tổng tiền giảm: ").append(DinhDangTien.dinhDang(tongQuan.getTongTienGiam())).append("\n");
        builder.append("- Doanh thu thực nhận: ").append(DinhDangTien.dinhDang(tongQuan.getTongThanhTien())).append("\n");
        builder.append("- Tổng số vé đã bán: ").append(tongQuan.getTongVeBan()).append("\n\n");

        builder.append("Doanh thu theo loại vé:\n");
        if (rong(ketQuaThongKe.getDanhSachTheoLoaiVe())) {
            builder.append("- Chưa có dữ liệu theo loại vé.\n");
        } else {
            for (ThongKeTheoLoaiVe item : ketQuaThongKe.getDanhSachTheoLoaiVe()) {
                builder.append("- ").append(item.getTenLoaiVe())
                        .append(": ").append(item.getSoVeBan()).append(" vé, ")
                        .append(DinhDangTien.dinhDang(item.getDoanhThu()))
                        .append("\n");
            }
        }

        builder.append("\nDoanh thu theo ngày:\n");
        if (rong(ketQuaThongKe.getDanhSachTheoNgay())) {
            builder.append("- Chưa có dữ liệu theo ngày.\n");
        } else {
            for (ThongKeTheoNgay item : ketQuaThongKe.getDanhSachTheoNgay()) {
                builder.append("- ").append(TienIch.dinhDangNgay(item.getNgay()))
                        .append(": ").append(DinhDangTien.dinhDang(item.getDoanhThu()))
                        .append("\n");
            }
        }

        builder.append("\nDoanh thu theo tháng:\n");
        if (rong(ketQuaThongKe.getDanhSachTheoThang())) {
            builder.append("- Chưa có dữ liệu theo tháng.\n");
        } else {
            for (ThongKeTheoThang item : ketQuaThongKe.getDanhSachTheoThang()) {
                builder.append("- ").append(item.getThang())
                        .append(": ").append(DinhDangTien.dinhDang(item.getDoanhThu()))
                        .append("\n");
            }
        }
        return builder.toString();
    }

    private boolean khongCoDuLieu(KetQuaThongKe ketQuaThongKe) {
        if (ketQuaThongKe == null || ketQuaThongKe.getThongKeTongQuan() == null) {
            return true;
        }
        ThongKeTongQuan tongQuan = ketQuaThongKe.getThongKeTongQuan();
        return tongQuan.getTongHoaDon() <= 0 && tongQuan.getTongVeBan() <= 0 && tongQuan.getTongThanhTien() <= 0;
    }

    private boolean rong(List<?> danhSach) {
        return danhSach == null || danhSach.isEmpty();
    }
}
