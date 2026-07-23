-- =========================================================
-- Bổ sung điều kiện áp dụng voucher
-- Migration chỉ mở rộng cấu trúc, không xóa dữ liệu cũ.
-- =========================================================

ALTER TABLE public."Voucher"
    ADD COLUMN IF NOT EXISTS "DonToiThieu" NUMERIC(18,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS "GiamToiDa" NUMERIC(18,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS "SoLuongVeToiThieu" INT NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS "SoLanDungToiDaMoiNguoi" INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS "ChiApDungKhachMoi" BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS "MaLoaiVeApDung" BIGINT NULL REFERENCES public."LoaiVe"("MaLoaiVe") ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS "MaVeApDung" BIGINT NULL REFERENCES public."Ve"("MaVe") ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS "MucTieu" TEXT,
    ADD COLUMN IF NOT EXISTS "MoTaDieuKien" TEXT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'Voucher_DonToiThieu_KhongAm'
          AND conrelid = 'public."Voucher"'::regclass
    ) THEN
        ALTER TABLE public."Voucher"
            ADD CONSTRAINT "Voucher_DonToiThieu_KhongAm"
            CHECK ("DonToiThieu" >= 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'Voucher_GiamToiDa_KhongAm'
          AND conrelid = 'public."Voucher"'::regclass
    ) THEN
        ALTER TABLE public."Voucher"
            ADD CONSTRAINT "Voucher_GiamToiDa_KhongAm"
            CHECK ("GiamToiDa" >= 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'Voucher_SoLuongVeToiThieu_HopLe'
          AND conrelid = 'public."Voucher"'::regclass
    ) THEN
        ALTER TABLE public."Voucher"
            ADD CONSTRAINT "Voucher_SoLuongVeToiThieu_HopLe"
            CHECK ("SoLuongVeToiThieu" >= 1);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'Voucher_SoLanDungToiDaMoiNguoi_KhongAm'
          AND conrelid = 'public."Voucher"'::regclass
    ) THEN
        ALTER TABLE public."Voucher"
            ADD CONSTRAINT "Voucher_SoLanDungToiDaMoiNguoi_KhongAm"
            CHECK ("SoLanDungToiDaMoiNguoi" >= 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'Voucher_PhamViApDung_HopLe'
          AND conrelid = 'public."Voucher"'::regclass
    ) THEN
        ALTER TABLE public."Voucher"
            ADD CONSTRAINT "Voucher_PhamViApDung_HopLe"
            CHECK (NOT ("MaLoaiVeApDung" IS NOT NULL AND "MaVeApDung" IS NOT NULL));
    END IF;
END;
$$;

CREATE INDEX IF NOT EXISTS "idx_Voucher_MaLoaiVeApDung"
ON public."Voucher"("MaLoaiVeApDung")
WHERE "MaLoaiVeApDung" IS NOT NULL;

CREATE INDEX IF NOT EXISTS "idx_Voucher_MaVeApDung"
ON public."Voucher"("MaVeApDung")
WHERE "MaVeApDung" IS NOT NULL;

CREATE INDEX IF NOT EXISTS "idx_HoaDon_NguoiDung_Voucher_ThanhToan"
ON public."HoaDon"("MaNguoiDung", "MaVoucher")
WHERE "TrangThai" = 'DaThanhToan' AND "MaVoucher" IS NOT NULL;

-- =========================================================
-- Đồng bộ số lượng voucher theo hóa đơn đã thanh toán.
-- Trigger giúp thanh toán thường và SePay không trừ hai lần.
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

-- =========================================================
-- Hoàn tất SePay bằng dữ liệu mới nhất trong một transaction.
-- Phiên tạm không tiêu thụ voucher; hóa đơn chính thức mới tiêu thụ.
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
SET search_path = ''
AS $$
DECLARE
    v_thanh_toan_tam public."ThanhToanTam"%ROWTYPE;
    v_voucher public."Voucher"%ROWTYPE;
    v_chi_tiet RECORD;
    v_so_tien_can_tra NUMERIC(18,2);
    v_tong_tien NUMERIC(18,2);
    v_tam_tinh_ap_dung NUMERIC(18,2);
    v_tien_giam NUMERIC(18,2) := 0;
    v_so_luong_goc INT;
    v_so_luong_da_ban INT;
    v_so_luong_mua INT;
    v_tong_so_ve INT;
    v_so_chi_tiet INT;
    v_so_hoa_don_thanh_cong INT;
    v_so_lan_da_dung INT;
    v_da_co_hoa_don BOOLEAN;
    v_loi_voucher TEXT;
BEGIN
    SELECT EXISTS (
        SELECT 1
        FROM public."HoaDon"
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
    FROM public."ThanhToanTam"
    WHERE "MaHoaDon" = p_ma_hoa_don
    FOR UPDATE;

    IF NOT FOUND THEN
        RETURN QUERY SELECT false, p_ma_hoa_don, 'Không tìm thấy thanh toán tạm';
        RETURN;
    END IF;

    SELECT
        COUNT(*),
        COALESCE(SUM("ThanhTien"), 0),
        COALESCE(SUM(
            COALESCE("SoLuongNguoiLon", 0)
            + COALESCE("SoLuongTreEm", 0)
            + COALESCE("SoLuongNguoiCaoTuoi", 0)
        ), 0)
    INTO v_so_chi_tiet, v_tong_tien, v_tong_so_ve
    FROM public."ChiTietThanhToanTam"
    WHERE "MaHoaDon" = p_ma_hoa_don;

    IF v_so_chi_tiet <= 0 OR v_tong_tien <= 0 OR v_tong_so_ve <= 0 THEN
        UPDATE public."ThanhToanTam"
        SET "TrangThai" = 'LoiThanhToan',
            "ThongBaoLoi" = 'Thanh toán tạm không có chi tiết vé hợp lệ'
        WHERE "MaHoaDon" = p_ma_hoa_don;
        RETURN QUERY SELECT false, p_ma_hoa_don, 'Thanh toán tạm không có chi tiết vé hợp lệ';
        RETURN;
    END IF;

    FOR v_chi_tiet IN
        SELECT *
        FROM public."ChiTietThanhToanTam"
        WHERE "MaHoaDon" = p_ma_hoa_don
    LOOP
        IF v_chi_tiet."NgaySuDung" < CURRENT_DATE THEN
            UPDATE public."ThanhToanTam"
            SET "TrangThai" = 'LoiThanhToan',
                "ThongBaoLoi" = 'Ngày sử dụng vé đã ở trong quá khứ'
            WHERE "MaHoaDon" = p_ma_hoa_don;
            RETURN QUERY SELECT false, p_ma_hoa_don, 'Ngày sử dụng vé đã ở trong quá khứ';
            RETURN;
        END IF;

        SELECT COALESCE("SoLuong", 0)
        INTO v_so_luong_goc
        FROM public."Ve"
        WHERE "MaVe" = v_chi_tiet."MaVe"
          AND "TrangThai" = 'HoatDong';

        IF NOT FOUND THEN
            UPDATE public."ThanhToanTam"
            SET "TrangThai" = 'LoiThanhToan',
                "ThongBaoLoi" = 'Vé không còn hoạt động'
            WHERE "MaHoaDon" = p_ma_hoa_don;
            RETURN QUERY SELECT false, p_ma_hoa_don, 'Vé không còn hoạt động';
            RETURN;
        END IF;

        SELECT COALESCE(SUM(
            COALESCE("SoLuongNguoiLon", 0)
            + COALESCE("SoLuongTreEm", 0)
            + COALESCE("SoLuongNguoiCaoTuoi", 0)
        ), 0)
        INTO v_so_luong_da_ban
        FROM public."ChiTietHoaDon"
        WHERE "MaVe" = v_chi_tiet."MaVe"
          AND "NgaySuDung" = v_chi_tiet."NgaySuDung";

        v_so_luong_mua :=
            COALESCE(v_chi_tiet."SoLuongNguoiLon", 0)
            + COALESCE(v_chi_tiet."SoLuongTreEm", 0)
            + COALESCE(v_chi_tiet."SoLuongNguoiCaoTuoi", 0);

        IF v_so_luong_da_ban + v_so_luong_mua > COALESCE(v_so_luong_goc, 0) THEN
            UPDATE public."ThanhToanTam"
            SET "TrangThai" = 'LoiThanhToan',
                "ThongBaoLoi" = 'Vé không đủ số lượng'
            WHERE "MaHoaDon" = p_ma_hoa_don;
            RETURN QUERY SELECT false, p_ma_hoa_don, 'Vé không đủ số lượng';
            RETURN;
        END IF;
    END LOOP;

    IF v_thanh_toan_tam."MaVoucher" IS NOT NULL THEN
        SELECT *
        INTO v_voucher
        FROM public."Voucher"
        WHERE "MaVoucher" = v_thanh_toan_tam."MaVoucher"
        FOR UPDATE;

        IF NOT FOUND THEN
            v_loi_voucher := 'Voucher không tồn tại';
        ELSIF v_voucher."TrangThai" <> 'HoatDong' THEN
            v_loi_voucher := 'Voucher không còn hoạt động';
        ELSIF CURRENT_DATE < v_voucher."NgayBatDau" THEN
            v_loi_voucher := 'Voucher chưa đến ngày áp dụng';
        ELSIF CURRENT_DATE > v_voucher."NgayKetThuc" THEN
            v_loi_voucher := 'Voucher đã hết hạn';
        ELSIF COALESCE(v_voucher."SoLuong", 0) <= 0 THEN
            v_loi_voucher := 'Voucher đã hết lượt sử dụng';
        ELSIF v_tong_tien < COALESCE(v_voucher."DonToiThieu", 0) THEN
            v_loi_voucher := 'Đơn hàng chưa đạt giá trị tối thiểu của voucher';
        ELSIF v_tong_so_ve < GREATEST(1, COALESCE(v_voucher."SoLuongVeToiThieu", 1)) THEN
            v_loi_voucher := 'Đơn hàng chưa đạt số lượng vé tối thiểu của voucher';
        ELSIF v_voucher."MaLoaiVeApDung" IS NOT NULL AND v_voucher."MaVeApDung" IS NOT NULL THEN
            v_loi_voucher := 'Phạm vi áp dụng voucher không hợp lệ';
        END IF;

        IF v_loi_voucher IS NULL THEN
            SELECT COUNT(*)
            INTO v_so_hoa_don_thanh_cong
            FROM public."HoaDon"
            WHERE "MaNguoiDung" = v_thanh_toan_tam."MaNguoiDung"
              AND "TrangThai" = 'DaThanhToan';

            IF v_voucher."ChiApDungKhachMoi" AND v_so_hoa_don_thanh_cong > 0 THEN
                v_loi_voucher := 'Voucher chỉ dành cho khách hàng mới';
            END IF;
        END IF;

        IF v_loi_voucher IS NULL THEN
            SELECT COUNT(*)
            INTO v_so_lan_da_dung
            FROM public."HoaDon"
            WHERE "MaNguoiDung" = v_thanh_toan_tam."MaNguoiDung"
              AND "MaVoucher" = v_thanh_toan_tam."MaVoucher"
              AND "TrangThai" = 'DaThanhToan';

            IF COALESCE(v_voucher."SoLanDungToiDaMoiNguoi", 0) > 0
               AND v_so_lan_da_dung >= v_voucher."SoLanDungToiDaMoiNguoi" THEN
                v_loi_voucher := 'Người dùng đã sử dụng hết số lần cho phép';
            END IF;
        END IF;

        IF v_loi_voucher IS NULL THEN
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
                v_tam_tinh_ap_dung := v_tong_tien;
            END IF;

            IF v_tam_tinh_ap_dung <= 0 THEN
                v_loi_voucher := 'Giỏ hàng không có vé thuộc phạm vi áp dụng';
            END IF;
        END IF;

        IF v_loi_voucher IS NULL THEN
            v_tien_giam := CASE
                WHEN v_voucher."KieuGiamGia" = 'PhanTram'
                    THEN v_tam_tinh_ap_dung * COALESCE(v_voucher."GiaTriGiam", 0) / 100
                ELSE COALESCE(v_voucher."GiaTriGiam", 0)
            END;
            IF COALESCE(v_voucher."GiamToiDa", 0) > 0 THEN
                v_tien_giam := LEAST(v_tien_giam, v_voucher."GiamToiDa");
            END IF;
            v_tien_giam := GREATEST(0, LEAST(v_tien_giam, v_tam_tinh_ap_dung, v_tong_tien));
            IF v_tien_giam <= 0 THEN
                v_loi_voucher := 'Voucher không tạo ra giá trị giảm cho đơn hàng này';
            END IF;
        END IF;

        IF v_loi_voucher IS NOT NULL THEN
            UPDATE public."ThanhToanTam"
            SET "TrangThai" = 'LoiThanhToan',
                "ThongBaoLoi" = v_loi_voucher
            WHERE "MaHoaDon" = p_ma_hoa_don;
            RETURN QUERY SELECT false, p_ma_hoa_don, v_loi_voucher;
            RETURN;
        END IF;
    END IF;

    v_so_tien_can_tra := GREATEST(0, v_tong_tien - v_tien_giam);
    IF p_so_tien_nhan IS NOT NULL AND p_so_tien_nhan < v_so_tien_can_tra THEN
        UPDATE public."ThanhToanTam"
        SET "TrangThai" = 'LoiThanhToan',
            "ThongBaoLoi" = 'Số tiền chuyển khoản chưa đủ'
        WHERE "MaHoaDon" = p_ma_hoa_don;
        RETURN QUERY SELECT false, p_ma_hoa_don, 'Số tiền chuyển khoản chưa đủ';
        RETURN;
    END IF;

    UPDATE public."ThanhToanTam"
    SET "TongTien" = v_tong_tien,
        "TienGiam" = v_tien_giam,
        "TrangThai" = 'DangXuLy',
        "ThongBaoLoi" = NULL
    WHERE "MaHoaDon" = p_ma_hoa_don;

    INSERT INTO public."HoaDon" (
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
        v_tong_tien,
        v_thanh_toan_tam."MaVoucher",
        v_tien_giam,
        v_thanh_toan_tam."ThanhToan",
        'DangXuLy',
        v_thanh_toan_tam."NoiDungChuyenKhoan"
    );

    INSERT INTO public."ChiTietHoaDon" (
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
    FROM public."ChiTietThanhToanTam"
    WHERE "MaHoaDon" = p_ma_hoa_don;

    UPDATE public."HoaDon"
    SET "TrangThai" = 'DaThanhToan'
    WHERE "MaHoaDon" = p_ma_hoa_don;

    DELETE FROM public."ChiTietGioHang"
    WHERE "MaChiTietGioHang" IN (
        SELECT "MaChiTietGioHang"
        FROM public."ChiTietThanhToanTam"
        WHERE "MaHoaDon" = p_ma_hoa_don
          AND "MaChiTietGioHang" IS NOT NULL
    );

    DELETE FROM public."ThanhToanTam"
    WHERE "MaHoaDon" = p_ma_hoa_don;

    RETURN QUERY SELECT true, p_ma_hoa_don, 'Thanh toán thành công';
END;
$$;

REVOKE ALL ON FUNCTION public.hoan_tat_thanh_toan_sepay(BIGINT, NUMERIC) FROM PUBLIC;
REVOKE ALL ON FUNCTION public.hoan_tat_thanh_toan_sepay(BIGINT, NUMERIC) FROM anon;
REVOKE ALL ON FUNCTION public.hoan_tat_thanh_toan_sepay(BIGINT, NUMERIC) FROM authenticated;
GRANT EXECUTE ON FUNCTION public.hoan_tat_thanh_toan_sepay(BIGINT, NUMERIC) TO service_role;
