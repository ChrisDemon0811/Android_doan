-- =========================================================
-- Setup database PostgreSQL cho ứng dụng Quản lý Bán Vé Khu Du Lịch
-- Chạy được trên Supabase PostgreSQL
-- =========================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DROP TABLE IF EXISTS "LichSuChat", "CauHinhAI", "ChiTietHoaDon", "ChiTietThanhToanTam", "ThanhToanTam", "HoaDon", "ChiTietGioHang", "GioHang", "Voucher", "Ve", "LoaiVe", "NguoiDung" CASCADE;

-- =========================================================
-- Bảng NguoiDung
-- =========================================================
CREATE TABLE "NguoiDung" (
    "MaNguoiDung" BIGSERIAL PRIMARY KEY,
    "TaiKhoan" TEXT UNIQUE NOT NULL,
    "MatKhau" TEXT NOT NULL,
    "HoTen" TEXT NOT NULL,
    "Email" TEXT,
    "SoDienThoai" TEXT,
    "VaiTro" TEXT NOT NULL DEFAULT 'NguoiDung',
    "NgayDangKy" DATE DEFAULT CURRENT_DATE,
    "TrangThai" TEXT NOT NULL DEFAULT 'HoatDong'
);

ALTER TABLE "NguoiDung" ENABLE ROW LEVEL SECURITY;

-- =========================================================
-- Bảng LoaiVe
-- =========================================================
CREATE TABLE "LoaiVe" (
    "MaLoaiVe" BIGSERIAL PRIMARY KEY,
    "TenLoaiVe" TEXT NOT NULL,
    "MoTa" TEXT,
    "TrangThai" TEXT NOT NULL DEFAULT 'HoatDong'
);

ALTER TABLE "LoaiVe" ENABLE ROW LEVEL SECURITY;

-- =========================================================
-- Bảng Ve
-- =========================================================
CREATE TABLE "Ve" (
    "MaVe" BIGSERIAL PRIMARY KEY,
    "MaLoaiVe" BIGINT NOT NULL REFERENCES "LoaiVe"("MaLoaiVe") ON DELETE CASCADE,
    "TenVe" TEXT NOT NULL,
    "GiaVe" NUMERIC(18,2) DEFAULT 0,
    "GiaNguoiLon" NUMERIC(18,2) DEFAULT 0,
    "GiaTreEm" NUMERIC(18,2) DEFAULT 0,
    "GiaNguoiCaoTuoi" NUMERIC(18,2) DEFAULT 0,
    "SoLuong" INT DEFAULT 0,
    "MoTa" TEXT,
    "ThongTinVe" TEXT,
    "AnhVe" TEXT,
    "TrangThai" TEXT NOT NULL DEFAULT 'HoatDong'
);

ALTER TABLE "Ve" ENABLE ROW LEVEL SECURITY;

-- =========================================================
-- Bảng Voucher
-- =========================================================
CREATE TABLE "Voucher" (
    "MaVoucher" BIGSERIAL PRIMARY KEY,
    "MaGiamGia" TEXT UNIQUE NOT NULL,
    "TenVoucher" TEXT NOT NULL,
    "KieuGiamGia" TEXT NOT NULL,
    "GiaTriGiam" NUMERIC(18,2) NOT NULL,
    "NgayBatDau" DATE NOT NULL,
    "NgayKetThuc" DATE NOT NULL,
    "SoLuong" INT DEFAULT 0,
    "TrangThai" TEXT NOT NULL DEFAULT 'HoatDong',
    "DonToiThieu" NUMERIC(18,2) NOT NULL DEFAULT 0,
    "GiamToiDa" NUMERIC(18,2) NOT NULL DEFAULT 0,
    "SoLuongVeToiThieu" INT NOT NULL DEFAULT 1,
    "SoLanDungToiDaMoiNguoi" INT NOT NULL DEFAULT 0,
    "ChiApDungKhachMoi" BOOLEAN NOT NULL DEFAULT FALSE,
    "MaLoaiVeApDung" BIGINT REFERENCES "LoaiVe"("MaLoaiVe") ON DELETE SET NULL,
    "MaVeApDung" BIGINT REFERENCES "Ve"("MaVe") ON DELETE SET NULL,
    "MucTieu" TEXT,
    "MoTaDieuKien" TEXT,
    CONSTRAINT "Voucher_DonToiThieu_KhongAm" CHECK ("DonToiThieu" >= 0),
    CONSTRAINT "Voucher_GiamToiDa_KhongAm" CHECK ("GiamToiDa" >= 0),
    CONSTRAINT "Voucher_SoLuongVeToiThieu_HopLe" CHECK ("SoLuongVeToiThieu" >= 1),
    CONSTRAINT "Voucher_SoLanDungToiDaMoiNguoi_KhongAm" CHECK ("SoLanDungToiDaMoiNguoi" >= 0),
    CONSTRAINT "Voucher_PhamViApDung_HopLe"
        CHECK (NOT ("MaLoaiVeApDung" IS NOT NULL AND "MaVeApDung" IS NOT NULL))
);

ALTER TABLE "Voucher" ENABLE ROW LEVEL SECURITY;

CREATE INDEX "idx_Voucher_MaLoaiVeApDung"
ON "Voucher"("MaLoaiVeApDung")
WHERE "MaLoaiVeApDung" IS NOT NULL;

CREATE INDEX "idx_Voucher_MaVeApDung"
ON "Voucher"("MaVeApDung")
WHERE "MaVeApDung" IS NOT NULL;

-- =========================================================
-- Bảng GioHang
-- =========================================================
CREATE TABLE "GioHang" (
    "MaGioHang" BIGSERIAL PRIMARY KEY,
    "MaNguoiDung" BIGINT UNIQUE NOT NULL REFERENCES "NguoiDung"("MaNguoiDung") ON DELETE CASCADE,
    "NgayTao" TIMESTAMP DEFAULT NOW()
);

ALTER TABLE "GioHang" ENABLE ROW LEVEL SECURITY;

-- =========================================================
-- Bảng ChiTietGioHang
-- =========================================================
CREATE TABLE "ChiTietGioHang" (
    "MaChiTietGioHang" BIGSERIAL PRIMARY KEY,
    "MaGioHang" BIGINT NOT NULL REFERENCES "GioHang"("MaGioHang") ON DELETE CASCADE,
    "MaVe" BIGINT NOT NULL REFERENCES "Ve"("MaVe") ON DELETE CASCADE,
    "NgaySuDung" DATE NOT NULL,
    "SoLuongNguoiLon" INT DEFAULT 0,
    "SoLuongTreEm" INT DEFAULT 0,
    "SoLuongNguoiCaoTuoi" INT DEFAULT 0,
    "DonGiaNguoiLon" NUMERIC(18,2) DEFAULT 0,
    "DonGiaTreEm" NUMERIC(18,2) DEFAULT 0,
    "DonGiaNguoiCaoTuoi" NUMERIC(18,2) DEFAULT 0
);

ALTER TABLE "ChiTietGioHang" ENABLE ROW LEVEL SECURITY;

-- =========================================================
-- Bảng HoaDon
-- =========================================================
CREATE TABLE "HoaDon" (
    "MaHoaDon" BIGSERIAL PRIMARY KEY,
    "MaXacThuc" UUID NOT NULL DEFAULT gen_random_uuid(),
    "MaNguoiDung" BIGINT NOT NULL REFERENCES "NguoiDung"("MaNguoiDung") ON DELETE CASCADE,
    "NgayLap" TIMESTAMP DEFAULT NOW(),
    "TongTien" NUMERIC(18,2) DEFAULT 0,
    "MaVoucher" BIGINT REFERENCES "Voucher"("MaVoucher") ON DELETE SET NULL,
    "TienGiam" NUMERIC(18,2) DEFAULT 0,
    "ThanhToan" TEXT,
    "TrangThai" TEXT NOT NULL DEFAULT 'ChuaThanhToan',
    "NoiDungChuyenKhoan" TEXT
);

ALTER TABLE "HoaDon" ENABLE ROW LEVEL SECURITY;

CREATE UNIQUE INDEX "HoaDon_MaXacThuc_unique"
ON "HoaDon" ("MaXacThuc");

-- =========================================================
-- Bảng ThanhToanTam
-- Lưu phiên QR SePay tạm, chưa phải hóa đơn thật.
-- =========================================================
CREATE TABLE "ThanhToanTam" (
    "MaHoaDon" BIGINT PRIMARY KEY DEFAULT nextval('"HoaDon_MaHoaDon_seq"'::regclass),
    "MaNguoiDung" BIGINT NOT NULL REFERENCES "NguoiDung"("MaNguoiDung") ON DELETE CASCADE,
    "NgayTao" TIMESTAMP DEFAULT NOW(),
    "TongTien" NUMERIC(18,2) DEFAULT 0,
    "MaVoucher" BIGINT REFERENCES "Voucher"("MaVoucher") ON DELETE SET NULL,
    "TienGiam" NUMERIC(18,2) DEFAULT 0,
    "ThanhToan" TEXT,
    "TrangThai" TEXT NOT NULL DEFAULT 'ChoThanhToan',
    "NoiDungChuyenKhoan" TEXT UNIQUE,
    "ThongBaoLoi" TEXT
);

ALTER TABLE "ThanhToanTam" ENABLE ROW LEVEL SECURITY;

-- =========================================================
-- Bảng ChiTietThanhToanTam
-- Chụp lại chi tiết giỏ hàng cho phiên QR SePay.
-- =========================================================
CREATE TABLE "ChiTietThanhToanTam" (
    "MaChiTietThanhToanTam" BIGSERIAL PRIMARY KEY,
    "MaHoaDon" BIGINT NOT NULL REFERENCES "ThanhToanTam"("MaHoaDon") ON DELETE CASCADE,
    "MaChiTietGioHang" BIGINT NOT NULL,
    "MaVe" BIGINT NOT NULL REFERENCES "Ve"("MaVe") ON DELETE CASCADE,
    "NgaySuDung" DATE NOT NULL,
    "SoLuongNguoiLon" INT DEFAULT 0,
    "SoLuongTreEm" INT DEFAULT 0,
    "SoLuongNguoiCaoTuoi" INT DEFAULT 0,
    "DonGiaNguoiLon" NUMERIC(18,2) DEFAULT 0,
    "DonGiaTreEm" NUMERIC(18,2) DEFAULT 0,
    "DonGiaNguoiCaoTuoi" NUMERIC(18,2) DEFAULT 0,
    "ThanhTien" NUMERIC(18,2) DEFAULT 0
);

ALTER TABLE "ChiTietThanhToanTam" ENABLE ROW LEVEL SECURITY;

-- =========================================================
-- Bảng ChiTietHoaDon
-- =========================================================
CREATE TABLE "ChiTietHoaDon" (
    "MaChiTietHoaDon" BIGSERIAL PRIMARY KEY,
    "MaHoaDon" BIGINT NOT NULL REFERENCES "HoaDon"("MaHoaDon") ON DELETE CASCADE,
    "MaVe" BIGINT NOT NULL REFERENCES "Ve"("MaVe") ON DELETE CASCADE,
    "NgaySuDung" DATE NOT NULL,
    "SoLuongNguoiLon" INT DEFAULT 0,
    "SoLuongTreEm" INT DEFAULT 0,
    "SoLuongNguoiCaoTuoi" INT DEFAULT 0,
    "DonGiaNguoiLon" NUMERIC(18,2) DEFAULT 0,
    "DonGiaTreEm" NUMERIC(18,2) DEFAULT 0,
    "DonGiaNguoiCaoTuoi" NUMERIC(18,2) DEFAULT 0,
    "ThanhTien" NUMERIC(18,2) DEFAULT 0
);

ALTER TABLE "ChiTietHoaDon" ENABLE ROW LEVEL SECURITY;

-- =========================================================
-- Bảng CauHinhAI
-- =========================================================
CREATE TABLE "CauHinhAI" (
    "MaCauHinhAI" BIGSERIAL PRIMARY KEY,
    "NhaCungCap" TEXT NOT NULL,
    "KhoaApi" TEXT NOT NULL,
    "MoHinh" TEXT NOT NULL,
    "NhacLenh" TEXT
);

ALTER TABLE "CauHinhAI" ENABLE ROW LEVEL SECURITY;

-- =========================================================
-- Bảng LichSuChat
-- =========================================================
CREATE TABLE "LichSuChat" (
    "MaLichSuChat" BIGSERIAL PRIMARY KEY,
    "MaNguoiDung" BIGINT NOT NULL REFERENCES "NguoiDung"("MaNguoiDung") ON DELETE CASCADE,
    "CauHoi" TEXT NOT NULL,
    "TraLoi" TEXT,
    "NgayTao" TIMESTAMP DEFAULT NOW()
);

ALTER TABLE "LichSuChat" ENABLE ROW LEVEL SECURITY;

-- =========================================================
-- Dữ liệu mẫu bảng NguoiDung
-- =========================================================
-- Bảo mật Supabase: bật RLS và cấp policy cho REST API
-- Lưu ý: Project đang dùng custom login trong Android thay vì Supabase Auth.
-- Các policy bên dưới giúp hết cảnh báo "RLS Disabled in Public" và giữ app hiện tại chạy được.
-- Với sản phẩm thật, nên chuyển sang Supabase Auth/JWT để giới hạn theo từng người dùng.
-- =========================================================

GRANT USAGE ON SCHEMA public TO anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE "NguoiDung", "LoaiVe", "Ve", "Voucher", "GioHang", "ChiTietGioHang", "HoaDon", "ThanhToanTam", "ChiTietThanhToanTam", "ChiTietHoaDon", "CauHinhAI", "LichSuChat" TO anon, authenticated;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO anon, authenticated;

DROP POLICY IF EXISTS "NguoiDung_doc" ON "NguoiDung";
DROP POLICY IF EXISTS "NguoiDung_them" ON "NguoiDung";
DROP POLICY IF EXISTS "NguoiDung_sua" ON "NguoiDung";
DROP POLICY IF EXISTS "NguoiDung_xoa" ON "NguoiDung";

CREATE POLICY "NguoiDung_doc" ON "NguoiDung"
    FOR SELECT TO anon, authenticated
    USING (true);

CREATE POLICY "NguoiDung_them" ON "NguoiDung"
    FOR INSERT TO anon, authenticated
    WITH CHECK (true);

CREATE POLICY "NguoiDung_sua" ON "NguoiDung"
    FOR UPDATE TO anon, authenticated
    USING (true)
    WITH CHECK (true);

CREATE POLICY "NguoiDung_xoa" ON "NguoiDung"
    FOR DELETE TO anon, authenticated
    USING (true);

DROP POLICY IF EXISTS "LoaiVe_doc" ON "LoaiVe";
DROP POLICY IF EXISTS "LoaiVe_them" ON "LoaiVe";
DROP POLICY IF EXISTS "LoaiVe_sua" ON "LoaiVe";
DROP POLICY IF EXISTS "LoaiVe_xoa" ON "LoaiVe";

CREATE POLICY "LoaiVe_doc" ON "LoaiVe"
    FOR SELECT TO anon, authenticated
    USING (true);

CREATE POLICY "LoaiVe_them" ON "LoaiVe"
    FOR INSERT TO anon, authenticated
    WITH CHECK (true);

CREATE POLICY "LoaiVe_sua" ON "LoaiVe"
    FOR UPDATE TO anon, authenticated
    USING (true)
    WITH CHECK (true);

CREATE POLICY "LoaiVe_xoa" ON "LoaiVe"
    FOR DELETE TO anon, authenticated
    USING (true);

DROP POLICY IF EXISTS "Ve_doc" ON "Ve";
DROP POLICY IF EXISTS "Ve_them" ON "Ve";
DROP POLICY IF EXISTS "Ve_sua" ON "Ve";
DROP POLICY IF EXISTS "Ve_xoa" ON "Ve";

CREATE POLICY "Ve_doc" ON "Ve"
    FOR SELECT TO anon, authenticated
    USING (true);

CREATE POLICY "Ve_them" ON "Ve"
    FOR INSERT TO anon, authenticated
    WITH CHECK (true);

CREATE POLICY "Ve_sua" ON "Ve"
    FOR UPDATE TO anon, authenticated
    USING (true)
    WITH CHECK (true);

CREATE POLICY "Ve_xoa" ON "Ve"
    FOR DELETE TO anon, authenticated
    USING (true);

DROP POLICY IF EXISTS "Voucher_doc" ON "Voucher";
DROP POLICY IF EXISTS "Voucher_them" ON "Voucher";
DROP POLICY IF EXISTS "Voucher_sua" ON "Voucher";
DROP POLICY IF EXISTS "Voucher_xoa" ON "Voucher";

CREATE POLICY "Voucher_doc" ON "Voucher"
    FOR SELECT TO anon, authenticated
    USING (true);

CREATE POLICY "Voucher_them" ON "Voucher"
    FOR INSERT TO anon, authenticated
    WITH CHECK (true);

CREATE POLICY "Voucher_sua" ON "Voucher"
    FOR UPDATE TO anon, authenticated
    USING (true)
    WITH CHECK (true);

CREATE POLICY "Voucher_xoa" ON "Voucher"
    FOR DELETE TO anon, authenticated
    USING (true);

DROP POLICY IF EXISTS "GioHang_doc" ON "GioHang";
DROP POLICY IF EXISTS "GioHang_them" ON "GioHang";
DROP POLICY IF EXISTS "GioHang_sua" ON "GioHang";
DROP POLICY IF EXISTS "GioHang_xoa" ON "GioHang";

CREATE POLICY "GioHang_doc" ON "GioHang"
    FOR SELECT TO anon, authenticated
    USING (true);

CREATE POLICY "GioHang_them" ON "GioHang"
    FOR INSERT TO anon, authenticated
    WITH CHECK (true);

CREATE POLICY "GioHang_sua" ON "GioHang"
    FOR UPDATE TO anon, authenticated
    USING (true)
    WITH CHECK (true);

CREATE POLICY "GioHang_xoa" ON "GioHang"
    FOR DELETE TO anon, authenticated
    USING (true);

DROP POLICY IF EXISTS "ChiTietGioHang_doc" ON "ChiTietGioHang";
DROP POLICY IF EXISTS "ChiTietGioHang_them" ON "ChiTietGioHang";
DROP POLICY IF EXISTS "ChiTietGioHang_sua" ON "ChiTietGioHang";
DROP POLICY IF EXISTS "ChiTietGioHang_xoa" ON "ChiTietGioHang";

CREATE POLICY "ChiTietGioHang_doc" ON "ChiTietGioHang"
    FOR SELECT TO anon, authenticated
    USING (true);

CREATE POLICY "ChiTietGioHang_them" ON "ChiTietGioHang"
    FOR INSERT TO anon, authenticated
    WITH CHECK (true);

CREATE POLICY "ChiTietGioHang_sua" ON "ChiTietGioHang"
    FOR UPDATE TO anon, authenticated
    USING (true)
    WITH CHECK (true);

CREATE POLICY "ChiTietGioHang_xoa" ON "ChiTietGioHang"
    FOR DELETE TO anon, authenticated
    USING (true);

DROP POLICY IF EXISTS "HoaDon_doc" ON "HoaDon";
DROP POLICY IF EXISTS "HoaDon_them" ON "HoaDon";
DROP POLICY IF EXISTS "HoaDon_sua" ON "HoaDon";
DROP POLICY IF EXISTS "HoaDon_xoa" ON "HoaDon";

CREATE POLICY "HoaDon_doc" ON "HoaDon"
    FOR SELECT TO anon, authenticated
    USING (true);

CREATE POLICY "HoaDon_them" ON "HoaDon"
    FOR INSERT TO anon, authenticated
    WITH CHECK (true);

CREATE POLICY "HoaDon_sua" ON "HoaDon"
    FOR UPDATE TO anon, authenticated
    USING (true)
    WITH CHECK (true);

CREATE POLICY "HoaDon_xoa" ON "HoaDon"
    FOR DELETE TO anon, authenticated
    USING (true);

DROP POLICY IF EXISTS "ThanhToanTam_doc" ON "ThanhToanTam";
DROP POLICY IF EXISTS "ThanhToanTam_them" ON "ThanhToanTam";
DROP POLICY IF EXISTS "ThanhToanTam_sua" ON "ThanhToanTam";
DROP POLICY IF EXISTS "ThanhToanTam_xoa" ON "ThanhToanTam";

CREATE POLICY "ThanhToanTam_doc" ON "ThanhToanTam"
    FOR SELECT TO anon, authenticated
    USING (true);

CREATE POLICY "ThanhToanTam_them" ON "ThanhToanTam"
    FOR INSERT TO anon, authenticated
    WITH CHECK (true);

CREATE POLICY "ThanhToanTam_sua" ON "ThanhToanTam"
    FOR UPDATE TO anon, authenticated
    USING (true)
    WITH CHECK (true);

CREATE POLICY "ThanhToanTam_xoa" ON "ThanhToanTam"
    FOR DELETE TO anon, authenticated
    USING (true);

DROP POLICY IF EXISTS "ChiTietThanhToanTam_doc" ON "ChiTietThanhToanTam";
DROP POLICY IF EXISTS "ChiTietThanhToanTam_them" ON "ChiTietThanhToanTam";
DROP POLICY IF EXISTS "ChiTietThanhToanTam_sua" ON "ChiTietThanhToanTam";
DROP POLICY IF EXISTS "ChiTietThanhToanTam_xoa" ON "ChiTietThanhToanTam";

CREATE POLICY "ChiTietThanhToanTam_doc" ON "ChiTietThanhToanTam"
    FOR SELECT TO anon, authenticated
    USING (true);

CREATE POLICY "ChiTietThanhToanTam_them" ON "ChiTietThanhToanTam"
    FOR INSERT TO anon, authenticated
    WITH CHECK (true);

CREATE POLICY "ChiTietThanhToanTam_sua" ON "ChiTietThanhToanTam"
    FOR UPDATE TO anon, authenticated
    USING (true)
    WITH CHECK (true);

CREATE POLICY "ChiTietThanhToanTam_xoa" ON "ChiTietThanhToanTam"
    FOR DELETE TO anon, authenticated
    USING (true);

DROP POLICY IF EXISTS "ChiTietHoaDon_doc" ON "ChiTietHoaDon";
DROP POLICY IF EXISTS "ChiTietHoaDon_them" ON "ChiTietHoaDon";
DROP POLICY IF EXISTS "ChiTietHoaDon_sua" ON "ChiTietHoaDon";
DROP POLICY IF EXISTS "ChiTietHoaDon_xoa" ON "ChiTietHoaDon";

CREATE POLICY "ChiTietHoaDon_doc" ON "ChiTietHoaDon"
    FOR SELECT TO anon, authenticated
    USING (true);

CREATE POLICY "ChiTietHoaDon_them" ON "ChiTietHoaDon"
    FOR INSERT TO anon, authenticated
    WITH CHECK (true);

CREATE POLICY "ChiTietHoaDon_sua" ON "ChiTietHoaDon"
    FOR UPDATE TO anon, authenticated
    USING (true)
    WITH CHECK (true);

CREATE POLICY "ChiTietHoaDon_xoa" ON "ChiTietHoaDon"
    FOR DELETE TO anon, authenticated
    USING (true);

DROP POLICY IF EXISTS "CauHinhAI_doc" ON "CauHinhAI";
DROP POLICY IF EXISTS "CauHinhAI_them" ON "CauHinhAI";
DROP POLICY IF EXISTS "CauHinhAI_sua" ON "CauHinhAI";
DROP POLICY IF EXISTS "CauHinhAI_xoa" ON "CauHinhAI";

CREATE POLICY "CauHinhAI_doc" ON "CauHinhAI"
    FOR SELECT TO anon, authenticated
    USING (true);

CREATE POLICY "CauHinhAI_them" ON "CauHinhAI"
    FOR INSERT TO anon, authenticated
    WITH CHECK (true);

CREATE POLICY "CauHinhAI_sua" ON "CauHinhAI"
    FOR UPDATE TO anon, authenticated
    USING (true)
    WITH CHECK (true);

CREATE POLICY "CauHinhAI_xoa" ON "CauHinhAI"
    FOR DELETE TO anon, authenticated
    USING (true);

DROP POLICY IF EXISTS "LichSuChat_doc" ON "LichSuChat";
DROP POLICY IF EXISTS "LichSuChat_them" ON "LichSuChat";
DROP POLICY IF EXISTS "LichSuChat_sua" ON "LichSuChat";
DROP POLICY IF EXISTS "LichSuChat_xoa" ON "LichSuChat";

CREATE POLICY "LichSuChat_doc" ON "LichSuChat"
    FOR SELECT TO anon, authenticated
    USING (true);

CREATE POLICY "LichSuChat_them" ON "LichSuChat"
    FOR INSERT TO anon, authenticated
    WITH CHECK (true);

CREATE POLICY "LichSuChat_sua" ON "LichSuChat"
    FOR UPDATE TO anon, authenticated
    USING (true)
    WITH CHECK (true);

CREATE POLICY "LichSuChat_xoa" ON "LichSuChat"
    FOR DELETE TO anon, authenticated
    USING (true);

CREATE INDEX IF NOT EXISTS "idx_ThanhToanTam_MaNguoiDung"
ON "ThanhToanTam"("MaNguoiDung");

CREATE INDEX IF NOT EXISTS "idx_ThanhToanTam_NoiDungChuyenKhoan"
ON "ThanhToanTam"("NoiDungChuyenKhoan");

CREATE INDEX IF NOT EXISTS "idx_ChiTietThanhToanTam_MaHoaDon"
ON "ChiTietThanhToanTam"("MaHoaDon");

CREATE INDEX IF NOT EXISTS "idx_HoaDon_NoiDungChuyenKhoan"
ON "HoaDon"("NoiDungChuyenKhoan");

CREATE INDEX IF NOT EXISTS "idx_HoaDon_NguoiDung_Voucher_ThanhToan"
ON "HoaDon"("MaNguoiDung", "MaVoucher")
WHERE "TrangThai" = 'DaThanhToan' AND "MaVoucher" IS NOT NULL;

-- =========================================================
-- Tự động tiêu thụ hoặc hoàn lại voucher theo hóa đơn thật.
-- =========================================================
CREATE SCHEMA IF NOT EXISTS private;
REVOKE ALL ON SCHEMA private FROM PUBLIC;
REVOKE ALL ON SCHEMA private FROM anon, authenticated;
GRANT USAGE ON SCHEMA private TO service_role;

CREATE OR REPLACE FUNCTION private.dieu_chinh_so_luong_voucher_hoa_don()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
DECLARE
    v_voucher public."Voucher"%ROWTYPE;
    v_la_luot_su_dung_moi BOOLEAN;
    v_so_hoa_don_thanh_cong INT;
    v_so_lan_da_dung INT;
    v_tong_so_ve INT;
    v_tong_chi_tiet NUMERIC(18,2);
    v_tam_tinh_ap_dung NUMERIC(18,2);
    v_tien_giam_cho_phep NUMERIC(18,2);
BEGIN
    IF TG_OP = 'DELETE' THEN
        IF OLD."TrangThai" = 'DaThanhToan' AND OLD."MaVoucher" IS NOT NULL THEN
            UPDATE public."Voucher"
            SET "SoLuong" = COALESCE("SoLuong", 0) + 1
            WHERE "MaVoucher" = OLD."MaVoucher";
        END IF;
        RETURN OLD;
    END IF;

    IF TG_OP = 'UPDATE'
       AND OLD."TrangThai" = 'DaThanhToan'
       AND OLD."MaVoucher" IS NOT NULL
       AND (
           NEW."TrangThai" IS DISTINCT FROM 'DaThanhToan'
           OR NEW."MaVoucher" IS DISTINCT FROM OLD."MaVoucher"
       ) THEN
        UPDATE public."Voucher"
        SET "SoLuong" = COALESCE("SoLuong", 0) + 1
        WHERE "MaVoucher" = OLD."MaVoucher";
    END IF;

    IF TG_OP = 'INSERT' THEN
        v_la_luot_su_dung_moi := NEW."TrangThai" = 'DaThanhToan'
            AND NEW."MaVoucher" IS NOT NULL;
    ELSE
        v_la_luot_su_dung_moi := NEW."TrangThai" = 'DaThanhToan'
            AND NEW."MaVoucher" IS NOT NULL
            AND (
                OLD."TrangThai" IS DISTINCT FROM 'DaThanhToan'
                OR OLD."MaVoucher" IS DISTINCT FROM NEW."MaVoucher"
            );
    END IF;

    IF NOT v_la_luot_su_dung_moi THEN
        RETURN NEW;
    END IF;

    PERFORM pg_catalog.pg_advisory_xact_lock(NEW."MaNguoiDung");

    SELECT *
    INTO v_voucher
    FROM public."Voucher"
    WHERE "MaVoucher" = NEW."MaVoucher"
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Voucher không tồn tại';
    END IF;
    IF v_voucher."TrangThai" <> 'HoatDong' THEN
        RAISE EXCEPTION 'Voucher không còn hoạt động';
    END IF;
    IF CURRENT_DATE < v_voucher."NgayBatDau" THEN
        RAISE EXCEPTION 'Voucher chưa đến ngày áp dụng';
    END IF;
    IF CURRENT_DATE > v_voucher."NgayKetThuc" THEN
        RAISE EXCEPTION 'Voucher đã hết hạn';
    END IF;
    IF COALESCE(v_voucher."SoLuong", 0) <= 0 THEN
        RAISE EXCEPTION 'Voucher đã hết lượt sử dụng';
    END IF;
    IF COALESCE(NEW."TongTien", 0) < COALESCE(v_voucher."DonToiThieu", 0) THEN
        RAISE EXCEPTION 'Hóa đơn chưa đạt giá trị tối thiểu của voucher';
    END IF;

    SELECT COUNT(*)
    INTO v_so_hoa_don_thanh_cong
    FROM public."HoaDon"
    WHERE "MaNguoiDung" = NEW."MaNguoiDung"
      AND "TrangThai" = 'DaThanhToan'
      AND "MaHoaDon" <> COALESCE(NEW."MaHoaDon", 0);

    IF v_voucher."ChiApDungKhachMoi" AND v_so_hoa_don_thanh_cong > 0 THEN
        RAISE EXCEPTION 'Voucher chỉ dành cho khách hàng mới';
    END IF;

    SELECT COUNT(*)
    INTO v_so_lan_da_dung
    FROM public."HoaDon"
    WHERE "MaNguoiDung" = NEW."MaNguoiDung"
      AND "MaVoucher" = NEW."MaVoucher"
      AND "TrangThai" = 'DaThanhToan'
      AND "MaHoaDon" <> COALESCE(NEW."MaHoaDon", 0);

    IF COALESCE(v_voucher."SoLanDungToiDaMoiNguoi", 0) > 0
       AND v_so_lan_da_dung >= v_voucher."SoLanDungToiDaMoiNguoi" THEN
        RAISE EXCEPTION 'Người dùng đã sử dụng hết số lần cho phép';
    END IF;

    IF TG_OP = 'UPDATE' THEN
        SELECT
            COALESCE(SUM(
                COALESCE("SoLuongNguoiLon", 0)
                + COALESCE("SoLuongTreEm", 0)
                + COALESCE("SoLuongNguoiCaoTuoi", 0)
            ), 0),
            COALESCE(SUM("ThanhTien"), 0)
        INTO v_tong_so_ve, v_tong_chi_tiet
        FROM public."ChiTietHoaDon"
        WHERE "MaHoaDon" = NEW."MaHoaDon";

        IF v_tong_so_ve <= 0 OR v_tong_chi_tiet <= 0 THEN
            RAISE EXCEPTION 'Hóa đơn chưa có chi tiết vé hợp lệ';
        END IF;
        IF ABS(v_tong_chi_tiet - COALESCE(NEW."TongTien", 0)) > 0.01 THEN
            RAISE EXCEPTION 'Tổng tiền hóa đơn không khớp chi tiết vé';
        END IF;
        IF v_tong_so_ve < GREATEST(1, COALESCE(v_voucher."SoLuongVeToiThieu", 1)) THEN
            RAISE EXCEPTION 'Hóa đơn chưa đạt số lượng vé tối thiểu của voucher';
        END IF;

        IF v_voucher."MaVeApDung" IS NOT NULL THEN
            SELECT COALESCE(SUM("ThanhTien"), 0)
            INTO v_tam_tinh_ap_dung
            FROM public."ChiTietHoaDon"
            WHERE "MaHoaDon" = NEW."MaHoaDon"
              AND "MaVe" = v_voucher."MaVeApDung";
        ELSIF v_voucher."MaLoaiVeApDung" IS NOT NULL THEN
            SELECT COALESCE(SUM(chi_tiet."ThanhTien"), 0)
            INTO v_tam_tinh_ap_dung
            FROM public."ChiTietHoaDon" AS chi_tiet
            JOIN public."Ve" AS ve ON ve."MaVe" = chi_tiet."MaVe"
            WHERE chi_tiet."MaHoaDon" = NEW."MaHoaDon"
              AND ve."MaLoaiVe" = v_voucher."MaLoaiVeApDung";
        ELSE
            v_tam_tinh_ap_dung := v_tong_chi_tiet;
        END IF;

        IF v_tam_tinh_ap_dung <= 0 THEN
            RAISE EXCEPTION 'Hóa đơn không có vé thuộc phạm vi áp dụng';
        END IF;
    ELSE
        v_tam_tinh_ap_dung := COALESCE(NEW."TongTien", 0);
    END IF;

    v_tien_giam_cho_phep := CASE
        WHEN v_voucher."KieuGiamGia" = 'PhanTram'
            THEN v_tam_tinh_ap_dung * COALESCE(v_voucher."GiaTriGiam", 0) / 100
        ELSE COALESCE(v_voucher."GiaTriGiam", 0)
    END;
    IF COALESCE(v_voucher."GiamToiDa", 0) > 0 THEN
        v_tien_giam_cho_phep := LEAST(v_tien_giam_cho_phep, v_voucher."GiamToiDa");
    END IF;
    v_tien_giam_cho_phep := LEAST(
        v_tien_giam_cho_phep,
        v_tam_tinh_ap_dung,
        COALESCE(NEW."TongTien", 0)
    );

    IF COALESCE(NEW."TienGiam", 0) < 0
       OR (TG_OP = 'UPDATE' AND ABS(COALESCE(NEW."TienGiam", 0) - v_tien_giam_cho_phep) > 0.01)
       OR (TG_OP = 'INSERT' AND COALESCE(NEW."TienGiam", 0) > v_tien_giam_cho_phep + 0.01) THEN
        RAISE EXCEPTION 'Số tiền giảm của voucher không hợp lệ';
    END IF;

    UPDATE public."Voucher"
    SET "SoLuong" = "SoLuong" - 1
    WHERE "MaVoucher" = NEW."MaVoucher"
      AND "SoLuong" > 0;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Voucher đã hết lượt sử dụng';
    END IF;
    RETURN NEW;
END;
$$;

REVOKE ALL ON FUNCTION private.dieu_chinh_so_luong_voucher_hoa_don() FROM PUBLIC;
REVOKE ALL ON FUNCTION private.dieu_chinh_so_luong_voucher_hoa_don() FROM anon, authenticated;

DROP TRIGGER IF EXISTS "trg_HoaDon_DieuChinhSoLuongVoucher" ON public."HoaDon";
CREATE TRIGGER "trg_HoaDon_DieuChinhSoLuongVoucher"
BEFORE INSERT OR UPDATE OR DELETE ON public."HoaDon"
FOR EACH ROW
EXECUTE FUNCTION private.dieu_chinh_so_luong_voucher_hoa_don();

CREATE OR REPLACE FUNCTION private.tinh_tien_giam_voucher_sepay(
    p_ma_hoa_don BIGINT,
    p_ma_nguoi_dung BIGINT,
    p_ma_voucher BIGINT,
    p_tong_tien NUMERIC
)
RETURNS NUMERIC
LANGUAGE plpgsql
SET search_path = ''
AS $$
DECLARE
    v_voucher public."Voucher"%ROWTYPE;
    v_tam_tinh_ap_dung NUMERIC(18,2);
    v_tien_giam NUMERIC(18,2);
    v_tong_so_ve INT;
    v_so_hoa_don_thanh_cong INT;
    v_so_lan_da_dung INT;
BEGIN
    IF p_ma_voucher IS NULL THEN
        RETURN 0;
    END IF;

    SELECT *
    INTO v_voucher
    FROM public."Voucher"
    WHERE "MaVoucher" = p_ma_voucher
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Voucher không tồn tại';
    END IF;
    IF v_voucher."TrangThai" <> 'HoatDong' THEN
        RAISE EXCEPTION 'Voucher không còn hoạt động';
    END IF;
    IF CURRENT_DATE < v_voucher."NgayBatDau" THEN
        RAISE EXCEPTION 'Voucher chưa đến ngày áp dụng';
    END IF;
    IF CURRENT_DATE > v_voucher."NgayKetThuc" THEN
        RAISE EXCEPTION 'Voucher đã hết hạn';
    END IF;
    IF COALESCE(v_voucher."SoLuong", 0) <= 0 THEN
        RAISE EXCEPTION 'Voucher đã hết lượt sử dụng';
    END IF;
    IF p_tong_tien < COALESCE(v_voucher."DonToiThieu", 0) THEN
        RAISE EXCEPTION 'Đơn hàng chưa đạt giá trị tối thiểu của voucher';
    END IF;

    SELECT COALESCE(SUM(
        COALESCE("SoLuongNguoiLon", 0)
        + COALESCE("SoLuongTreEm", 0)
        + COALESCE("SoLuongNguoiCaoTuoi", 0)
    ), 0)
    INTO v_tong_so_ve
    FROM public."ChiTietThanhToanTam"
    WHERE "MaHoaDon" = p_ma_hoa_don;

    IF v_tong_so_ve < GREATEST(1, COALESCE(v_voucher."SoLuongVeToiThieu", 1)) THEN
        RAISE EXCEPTION 'Đơn hàng chưa đạt số lượng vé tối thiểu của voucher';
    END IF;

    SELECT COUNT(*)
    INTO v_so_hoa_don_thanh_cong
    FROM public."HoaDon"
    WHERE "MaNguoiDung" = p_ma_nguoi_dung
      AND "TrangThai" = 'DaThanhToan';

    IF v_voucher."ChiApDungKhachMoi" AND v_so_hoa_don_thanh_cong > 0 THEN
        RAISE EXCEPTION 'Voucher chỉ dành cho khách hàng mới';
    END IF;

    SELECT COUNT(*)
    INTO v_so_lan_da_dung
    FROM public."HoaDon"
    WHERE "MaNguoiDung" = p_ma_nguoi_dung
      AND "MaVoucher" = p_ma_voucher
      AND "TrangThai" = 'DaThanhToan';

    IF COALESCE(v_voucher."SoLanDungToiDaMoiNguoi", 0) > 0
       AND v_so_lan_da_dung >= v_voucher."SoLanDungToiDaMoiNguoi" THEN
        RAISE EXCEPTION 'Người dùng đã sử dụng hết số lần cho phép';
    END IF;

    IF v_voucher."MaVeApDung" IS NOT NULL THEN
        SELECT COALESCE(SUM("ThanhTien"), 0)
        INTO v_tam_tinh_ap_dung
        FROM public."ChiTietThanhToanTam"
        WHERE "MaHoaDon" = p_ma_hoa_don
          AND "MaVe" = v_voucher."MaVeApDung";
    ELSIF v_voucher."MaLoaiVeApDung" IS NOT NULL THEN
        SELECT COALESCE(SUM(chi_tiet."ThanhTien"), 0)
        INTO v_tam_tinh_ap_dung
        FROM public."ChiTietThanhToanTam" AS chi_tiet
        JOIN public."Ve" AS ve ON ve."MaVe" = chi_tiet."MaVe"
        WHERE chi_tiet."MaHoaDon" = p_ma_hoa_don
          AND ve."MaLoaiVe" = v_voucher."MaLoaiVeApDung";
    ELSE
        v_tam_tinh_ap_dung := p_tong_tien;
    END IF;

    IF v_tam_tinh_ap_dung <= 0 THEN
        RAISE EXCEPTION 'Giỏ hàng không có vé thuộc phạm vi áp dụng';
    END IF;

    v_tien_giam := CASE
        WHEN v_voucher."KieuGiamGia" = 'PhanTram'
            THEN v_tam_tinh_ap_dung * COALESCE(v_voucher."GiaTriGiam", 0) / 100
        ELSE COALESCE(v_voucher."GiaTriGiam", 0)
    END;
    IF COALESCE(v_voucher."GiamToiDa", 0) > 0 THEN
        v_tien_giam := LEAST(v_tien_giam, v_voucher."GiamToiDa");
    END IF;
    RETURN GREATEST(0, LEAST(v_tien_giam, v_tam_tinh_ap_dung, p_tong_tien));
END;
$$;

REVOKE ALL ON FUNCTION private.tinh_tien_giam_voucher_sepay(BIGINT, BIGINT, BIGINT, NUMERIC) FROM PUBLIC;
REVOKE ALL ON FUNCTION private.tinh_tien_giam_voucher_sepay(BIGINT, BIGINT, BIGINT, NUMERIC) FROM anon, authenticated;
GRANT EXECUTE ON FUNCTION private.tinh_tien_giam_voucher_sepay(BIGINT, BIGINT, BIGINT, NUMERIC) TO service_role;

-- =========================================================
-- Hàm hoàn tất thanh toán SePay
-- Chỉ webhook dùng hàm này sau khi ngân hàng xác nhận nhận tiền.
-- =========================================================
CREATE OR REPLACE FUNCTION public.hoan_tat_thanh_toan_sepay(
    p_ma_hoa_don BIGINT,
    p_so_tien_nhan NUMERIC DEFAULT NULL
)
RETURNS TABLE (
    thanh_cong BOOLEAN,
    ma_hoa_don BIGINT,
    thong_bao TEXT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_thanh_toan_tam RECORD;
    v_chi_tiet RECORD;
    v_so_tien_can_tra NUMERIC(18,2);
    v_so_luong_goc INT;
    v_so_luong_da_ban INT;
    v_so_luong_mua INT;
    v_da_co_hoa_don BOOLEAN;
    v_tien_giam NUMERIC(18,2) := 0;
BEGIN
    SELECT EXISTS (
        SELECT 1
        FROM "HoaDon"
        WHERE "MaHoaDon" = p_ma_hoa_don
          AND "TrangThai" = 'DaThanhToan'
    )
    INTO v_da_co_hoa_don;

    IF v_da_co_hoa_don THEN
        RETURN QUERY SELECT true, p_ma_hoa_don, 'Hóa đơn đã được thanh toán';
        RETURN;
    END IF;

    SELECT *
    INTO v_thanh_toan_tam
    FROM "ThanhToanTam"
    WHERE "MaHoaDon" = p_ma_hoa_don
    FOR UPDATE;

    IF NOT FOUND THEN
        RETURN QUERY SELECT false, p_ma_hoa_don, 'Không tìm thấy thanh toán tạm';
        RETURN;
    END IF;

    BEGIN
        v_tien_giam := private.tinh_tien_giam_voucher_sepay(
            p_ma_hoa_don,
            v_thanh_toan_tam."MaNguoiDung",
            v_thanh_toan_tam."MaVoucher",
            COALESCE(v_thanh_toan_tam."TongTien", 0)
        );
    EXCEPTION WHEN OTHERS THEN
        UPDATE "ThanhToanTam"
        SET "TrangThai" = 'LoiThanhToan',
            "ThongBaoLoi" = SQLERRM
        WHERE "MaHoaDon" = p_ma_hoa_don;

        RETURN QUERY SELECT false, p_ma_hoa_don, SQLERRM;
        RETURN;
    END;

    v_thanh_toan_tam."TienGiam" := v_tien_giam;
    UPDATE "ThanhToanTam"
    SET "TienGiam" = v_tien_giam
    WHERE "MaHoaDon" = p_ma_hoa_don;

    v_so_tien_can_tra := GREATEST(0, COALESCE(v_thanh_toan_tam."TongTien", 0) - v_tien_giam);
    IF p_so_tien_nhan IS NOT NULL AND p_so_tien_nhan < v_so_tien_can_tra THEN
        UPDATE "ThanhToanTam"
        SET "TrangThai" = 'LoiThanhToan',
            "ThongBaoLoi" = 'Số tiền chuyển khoản chưa đủ'
        WHERE "MaHoaDon" = p_ma_hoa_don;

        RETURN QUERY SELECT false, p_ma_hoa_don, 'Số tiền chuyển khoản chưa đủ';
        RETURN;
    END IF;

    FOR v_chi_tiet IN
        SELECT *
        FROM "ChiTietThanhToanTam"
        WHERE "MaHoaDon" = p_ma_hoa_don
    LOOP
        SELECT COALESCE("SoLuong", 0)
        INTO v_so_luong_goc
        FROM "Ve"
        WHERE "MaVe" = v_chi_tiet."MaVe";

        SELECT COALESCE(SUM(
            COALESCE("SoLuongNguoiLon", 0)
            + COALESCE("SoLuongTreEm", 0)
            + COALESCE("SoLuongNguoiCaoTuoi", 0)
        ), 0)
        INTO v_so_luong_da_ban
        FROM "ChiTietHoaDon"
        WHERE "MaVe" = v_chi_tiet."MaVe"
          AND "NgaySuDung" = v_chi_tiet."NgaySuDung";

        v_so_luong_mua :=
            COALESCE(v_chi_tiet."SoLuongNguoiLon", 0)
            + COALESCE(v_chi_tiet."SoLuongTreEm", 0)
            + COALESCE(v_chi_tiet."SoLuongNguoiCaoTuoi", 0);

        IF v_so_luong_da_ban + v_so_luong_mua > COALESCE(v_so_luong_goc, 0) THEN
            UPDATE "ThanhToanTam"
            SET "TrangThai" = 'LoiThanhToan',
                "ThongBaoLoi" = 'Vé không đủ số lượng'
            WHERE "MaHoaDon" = p_ma_hoa_don;

            RETURN QUERY SELECT false, p_ma_hoa_don, 'Vé không đủ số lượng';
            RETURN;
        END IF;
    END LOOP;

    INSERT INTO "HoaDon" (
        "MaHoaDon",
        "MaNguoiDung",
        "TongTien",
        "MaVoucher",
        "TienGiam",
        "ThanhToan",
        "TrangThai",
        "NoiDungChuyenKhoan"
    )
    VALUES (
        v_thanh_toan_tam."MaHoaDon",
        v_thanh_toan_tam."MaNguoiDung",
        v_thanh_toan_tam."TongTien",
        v_thanh_toan_tam."MaVoucher",
        v_thanh_toan_tam."TienGiam",
        v_thanh_toan_tam."ThanhToan",
        'DangXuLy',
        v_thanh_toan_tam."NoiDungChuyenKhoan"
    );

    INSERT INTO "ChiTietHoaDon" (
        "MaHoaDon",
        "MaVe",
        "NgaySuDung",
        "SoLuongNguoiLon",
        "SoLuongTreEm",
        "SoLuongNguoiCaoTuoi",
        "DonGiaNguoiLon",
        "DonGiaTreEm",
        "DonGiaNguoiCaoTuoi",
        "ThanhTien"
    )
    SELECT
        "MaHoaDon",
        "MaVe",
        "NgaySuDung",
        "SoLuongNguoiLon",
        "SoLuongTreEm",
        "SoLuongNguoiCaoTuoi",
        "DonGiaNguoiLon",
        "DonGiaTreEm",
        "DonGiaNguoiCaoTuoi",
        "ThanhTien"
    FROM "ChiTietThanhToanTam"
    WHERE "MaHoaDon" = p_ma_hoa_don;

    UPDATE "HoaDon"
    SET "TrangThai" = 'DaThanhToan'
    WHERE "MaHoaDon" = p_ma_hoa_don;

    DELETE FROM "ChiTietGioHang"
    WHERE "MaChiTietGioHang" IN (
        SELECT "MaChiTietGioHang"
        FROM "ChiTietThanhToanTam"
        WHERE "MaHoaDon" = p_ma_hoa_don
    );

    DELETE FROM "ThanhToanTam"
    WHERE "MaHoaDon" = p_ma_hoa_don;

    RETURN QUERY SELECT true, p_ma_hoa_don, 'Thanh toán thành công';
END;
$$;

REVOKE ALL ON FUNCTION public.hoan_tat_thanh_toan_sepay(BIGINT, NUMERIC) FROM PUBLIC;
REVOKE ALL ON FUNCTION public.hoan_tat_thanh_toan_sepay(BIGINT, NUMERIC) FROM anon;
REVOKE ALL ON FUNCTION public.hoan_tat_thanh_toan_sepay(BIGINT, NUMERIC) FROM authenticated;
GRANT EXECUTE ON FUNCTION public.hoan_tat_thanh_toan_sepay(BIGINT, NUMERIC) TO service_role;

-- =========================================================
INSERT INTO "NguoiDung" ("TaiKhoan", "MatKhau", "HoTen", "Email", "SoDienThoai", "VaiTro", "TrangThai")
VALUES
    ('admin', '0192023a7bbd73250516f069df18b500', 'Quản trị viên', 'admin@example.com', '0900000000', 'QuanLy', 'HoatDong'),
    ('user1', 'e10adc3949ba59abbe56e057f20f883e', 'Người dùng 1', 'user1@example.com', '0911111111', 'NguoiDung', 'HoatDong'),
    ('user2', 'e10adc3949ba59abbe56e057f20f883e', 'Người dùng 2', 'user2@example.com', '0922222222', 'NguoiDung', 'HoatDong');

-- =========================================================
-- Dữ liệu mẫu bảng LoaiVe
-- =========================================================
INSERT INTO "LoaiVe" ("TenLoaiVe", "MoTa", "TrangThai")
VALUES
    ('Vé tham quan', 'Vé dành cho khách tham quan khu du lịch', 'HoatDong'),
    ('Vé vui chơi', 'Vé dành cho các khu trò chơi giải trí', 'HoatDong'),
    ('Vé Combo', 'Vé kết hợp tham quan và vui chơi', 'HoatDong');

-- =========================================================
-- Dữ liệu mẫu bảng Ve
-- =========================================================
INSERT INTO "Ve" (
    "MaLoaiVe",
    "TenVe",
    "GiaVe",
    "GiaNguoiLon",
    "GiaTreEm",
    "GiaNguoiCaoTuoi",
    "SoLuong",
    "MoTa",
    "ThongTinVe",
    "AnhVe",
    "TrangThai"
)
VALUES
    (1, 'Vé tham quan trong ngày', 120000, 120000, 80000, 90000, 100, 'Tham quan toàn khu trong ngày', 'Áp dụng cho một lượt vào cổng trong ngày sử dụng.', 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee', 'HoatDong'),
    (1, 'Vé tham quan cuối tuần', 150000, 150000, 100000, 110000, 100, 'Tham quan khu du lịch vào cuối tuần', 'Áp dụng thứ Bảy, Chủ Nhật và ngày lễ.', 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e', 'HoatDong'),
    (2, 'Vé khu trò chơi ngoài trời', 180000, 180000, 120000, 140000, 100, 'Vui chơi tại khu trò chơi ngoài trời', 'Bao gồm các trò chơi phổ thông trong khu ngoài trời.', 'https://images.unsplash.com/photo-1513889961551-628c1e5e2ee9', 'HoatDong'),
    (2, 'Vé khu trò chơi nước', 220000, 220000, 150000, 170000, 100, 'Vui chơi tại công viên nước', 'Áp dụng cho các trò chơi nước trong ngày.', 'https://images.unsplash.com/photo-1530549387789-4c1017266635', 'HoatDong'),
    (3, 'Vé combo tham quan và vui chơi', 300000, 300000, 210000, 240000, 100, 'Combo tham quan và vui chơi cơ bản', 'Bao gồm vé vào cổng và khu trò chơi phổ thông.', 'https://images.unsplash.com/photo-1526772662000-3f88f10405ff', 'HoatDong'),
    (3, 'Vé combo gia đình', 500000, 500000, 350000, 400000, 100, 'Combo phù hợp cho gia đình', 'Bao gồm nhiều quyền lợi tham quan, vui chơi và ưu đãi dịch vụ.', 'https://images.unsplash.com/photo-1501785888041-af3ef285b470', 'HoatDong');

-- =========================================================
-- Dữ liệu mẫu bảng Voucher
-- =========================================================
INSERT INTO "Voucher" ("MaGiamGia", "TenVoucher", "KieuGiamGia", "GiaTriGiam", "NgayBatDau", "NgayKetThuc", "SoLuong", "TrangThai")
VALUES
    ('GIAM10', 'Giảm 10% tổng hóa đơn', 'PhanTram', 10, CURRENT_DATE, CURRENT_DATE + INTERVAL '30 days', 50, 'HoatDong'),
    ('TANG50K', 'Giảm 50,000 VNĐ', 'TienMat', 50000, CURRENT_DATE, CURRENT_DATE + INTERVAL '30 days', 50, 'HoatDong');

-- =========================================================
-- Dữ liệu mẫu bảng CauHinhAI
-- =========================================================
INSERT INTO "CauHinhAI" ("NhaCungCap", "KhoaApi", "MoHinh", "NhacLenh")
VALUES
    ('Gemini', '', 'gemini-3.5-flash', 'Bạn là trợ lý chăm sóc khách hàng của khu du lịch. Trả lời ngắn gọn, lịch sự và đúng chính sách.');

SELECT 'Tạo database thành công!' AS "ThongBao";
