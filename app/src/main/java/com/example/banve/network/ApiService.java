package com.example.banve.network;

import com.example.banve.models.NguoiDung;
import com.example.banve.models.LoaiVe;
import com.example.banve.models.Ve;
import com.example.banve.models.GioHang;
import com.example.banve.models.ChiTietGioHang;
import com.example.banve.models.Voucher;
import com.example.banve.models.HoaDon;
import com.example.banve.models.ChiTietHoaDon;
import com.example.banve.models.ThanhToanTam;
import com.example.banve.models.ChiTietThanhToanTam;
import com.example.banve.models.CauHinhAI;
import com.example.banve.models.LichSuChat;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface ApiService {
    // Thêm người dùng mới vào bảng NguoiDung.
    @POST("rest/v1/NguoiDung")
    Call<List<NguoiDung>> themNguoiDung(@Body Map<String, Object> nguoiDung);

    // Tìm người dùng theo tài khoản để kiểm tra trùng trước khi đăng ký.
    @GET("rest/v1/NguoiDung")
    Call<List<NguoiDung>> layNguoiDungTheoTaiKhoan(@Query("TaiKhoan") String dieuKienTaiKhoan);

    // Tìm người dùng bằng các bộ lọc Supabase REST API.
    @GET("rest/v1/NguoiDung")
    Call<List<NguoiDung>> timNguoiDung(@QueryMap Map<String, String> filter);

    // Cập nhật thông tin người dùng theo mã người dùng.
    @PATCH("rest/v1/NguoiDung")
    Call<List<NguoiDung>> capNhatNguoiDung(
            @Query("MaNguoiDung") String dieuKienMaNguoiDung,
            @Body Map<String, String> duLieuCapNhat
    );

    // Lấy danh sách loại vé đang hoạt động.
    @GET("rest/v1/LoaiVe")
    Call<List<LoaiVe>> layDanhSachLoaiVe(@Query("TrangThai") String trangThai);

    // Tìm loại vé theo bộ lọc Supabase REST API.
    @GET("rest/v1/LoaiVe")
    Call<List<LoaiVe>> timLoaiVe(@QueryMap Map<String, String> filter);

    // Thêm loại vé mới.
    @POST("rest/v1/LoaiVe")
    Call<List<LoaiVe>> themLoaiVe(@Body Map<String, Object> loaiVe);

    // Cập nhật loại vé.
    @PATCH("rest/v1/LoaiVe")
    Call<List<LoaiVe>> capNhatLoaiVe(
            @Query("MaLoaiVe") String dieuKienMaLoaiVe,
            @Body Map<String, Object> duLieuCapNhat
    );

    // Xóa loại vé theo mã loại vé.
    @DELETE("rest/v1/LoaiVe")
    Call<Void> xoaLoaiVe(@Query("MaLoaiVe") String dieuKienMaLoaiVe);

    // Lấy danh sách vé theo bộ lọc Supabase REST API.
    @GET("rest/v1/Ve")
    Call<List<Ve>> layDanhSachVe(@QueryMap Map<String, String> filter);

    // Thêm vé mới.
    @POST("rest/v1/Ve")
    Call<List<Ve>> themVe(@Body Map<String, Object> ve);

    // Tìm giỏ hàng theo bộ lọc Supabase REST API.
    @GET("rest/v1/GioHang")
    Call<List<GioHang>> timGioHang(@QueryMap Map<String, String> filter);

    // Tạo giỏ hàng mới cho người dùng.
    @POST("rest/v1/GioHang")
    Call<List<GioHang>> taoGioHang(@Body Map<String, Object> gioHang);

    // Tìm chi tiết giỏ hàng theo bộ lọc Supabase REST API.
    @GET("rest/v1/ChiTietGioHang")
    Call<List<ChiTietGioHang>> timChiTietGioHang(@QueryMap Map<String, String> filter);

    // Thêm một mục vào giỏ hàng.
    @POST("rest/v1/ChiTietGioHang")
    Call<List<ChiTietGioHang>> themChiTietGioHang(@Body Map<String, Object> chiTietGioHang);

    // Cập nhật một mục trong giỏ hàng.
    @PATCH("rest/v1/ChiTietGioHang")
    Call<List<ChiTietGioHang>> capNhatChiTietGioHang(
            @Query("MaChiTietGioHang") String dieuKienMaChiTietGioHang,
            @Body Map<String, Object> chiTietGioHang
    );

    // Xóa một mục trong giỏ hàng.
    @DELETE("rest/v1/ChiTietGioHang")
    Call<Void> xoaChiTietGioHang(@Query("MaChiTietGioHang") String dieuKienMaChiTietGioHang);

    // Tìm voucher theo bộ lọc Supabase REST API.
    @GET("rest/v1/Voucher")
    Call<List<Voucher>> timVoucher(@QueryMap Map<String, String> filter);

    // Thêm voucher mới.
    @POST("rest/v1/Voucher")
    Call<List<Voucher>> themVoucher(@Body Map<String, Object> voucher);

    // Cập nhật voucher.
    @PATCH("rest/v1/Voucher")
    Call<List<Voucher>> capNhatVoucher(
            @Query("MaVoucher") String dieuKienMaVoucher,
            @Body Map<String, Object> duLieuCapNhat
    );

    // Xóa voucher theo mã voucher.
    @DELETE("rest/v1/Voucher")
    Call<Void> xoaVoucher(@Query("MaVoucher") String dieuKienMaVoucher);

    // Cập nhật vé.
    @PATCH("rest/v1/Ve")
    Call<List<Ve>> capNhatVe(
            @Query("MaVe") String dieuKienMaVe,
            @Body Map<String, Object> duLieuCapNhat
    );

    // Xóa vé theo mã vé.
    @DELETE("rest/v1/Ve")
    Call<Void> xoaVe(@Query("MaVe") String dieuKienMaVe);

    // Tạo hóa đơn.
    @POST("rest/v1/HoaDon")
    Call<List<HoaDon>> taoHoaDon(@Body Map<String, Object> hoaDon);

    // Tìm hóa đơn theo bộ lọc Supabase REST API.
    @GET("rest/v1/HoaDon")
    Call<List<HoaDon>> timHoaDon(@QueryMap Map<String, String> filter);

    // Cập nhật hóa đơn theo mã hóa đơn.
    @PATCH("rest/v1/HoaDon")
    Call<List<HoaDon>> capNhatHoaDon(
            @Query("MaHoaDon") String dieuKienMaHoaDon,
            @Body Map<String, Object> duLieuCapNhat
    );

    // Xóa hóa đơn chưa hoàn chỉnh để hoàn tác lỗi tạo chi tiết.
    @DELETE("rest/v1/HoaDon")
    Call<Void> xoaHoaDon(@Query("MaHoaDon") String dieuKienMaHoaDon);

    // Tạo thanh toán tạm SePay, chưa tạo hóa đơn thật.
    @POST("rest/v1/ThanhToanTam")
    Call<List<ThanhToanTam>> taoThanhToanTam(@Body Map<String, Object> thanhToanTam);

    // Tìm thanh toán tạm theo bộ lọc Supabase REST API.
    @GET("rest/v1/ThanhToanTam")
    Call<List<ThanhToanTam>> timThanhToanTam(@QueryMap Map<String, String> filter);

    // Cập nhật thanh toán tạm theo mã hóa đơn dự kiến.
    @PATCH("rest/v1/ThanhToanTam")
    Call<List<ThanhToanTam>> capNhatThanhToanTam(
            @Query("MaHoaDon") String dieuKienMaHoaDon,
            @Body Map<String, Object> duLieuCapNhat
    );

    // Xóa thanh toán tạm theo bộ lọc.
    @DELETE("rest/v1/ThanhToanTam")
    Call<Void> xoaThanhToanTam(@QueryMap Map<String, String> filter);

    // Tạo chi tiết thanh toán tạm SePay.
    @POST("rest/v1/ChiTietThanhToanTam")
    Call<List<ChiTietThanhToanTam>> taoChiTietThanhToanTam(@Body Map<String, Object> chiTietThanhToanTam);

    // Tạo chi tiết hóa đơn.
    @POST("rest/v1/ChiTietHoaDon")
    Call<List<ChiTietHoaDon>> taoChiTietHoaDon(@Body Map<String, Object> chiTietHoaDon);

    // Tìm chi tiết hóa đơn và dữ liệu vé liên quan.
    @GET("rest/v1/ChiTietHoaDon")
    Call<List<ChiTietHoaDon>> timChiTietHoaDon(@QueryMap Map<String, String> filter);

    // Lấy cấu hình AI hiện tại.
    @GET("rest/v1/CauHinhAI")
    Call<List<CauHinhAI>> layCauHinhAI(@QueryMap Map<String, String> filter);

    // Thêm cấu hình AI mới.
    @POST("rest/v1/CauHinhAI")
    Call<List<CauHinhAI>> themCauHinhAI(@Body Map<String, Object> cauHinhAI);

    // Cập nhật cấu hình AI theo mã cấu hình.
    @PATCH("rest/v1/CauHinhAI")
    Call<List<CauHinhAI>> capNhatCauHinhAI(
            @Query("MaCauHinhAI") String dieuKienMaCauHinhAI,
            @Body Map<String, Object> cauHinhAI
    );

    // Tìm lịch sử chat theo bộ lọc Supabase REST API.
    @GET("rest/v1/LichSuChat")
    Call<List<LichSuChat>> timLichSuChat(@QueryMap Map<String, String> filter);

    // Thêm lịch sử chat mới.
    @POST("rest/v1/LichSuChat")
    Call<List<LichSuChat>> themLichSuChat(@Body Map<String, Object> lichSuChat);
}
