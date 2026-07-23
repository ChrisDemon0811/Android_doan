-- =========================================================
-- Bổ sung mã xác thực UUID cố định cho từng hóa đơn
-- Migration an toàn cho bảng HoaDon đã có dữ liệu.
-- =========================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE public."HoaDon"
ADD COLUMN IF NOT EXISTS "MaXacThuc" UUID;

UPDATE public."HoaDon"
SET "MaXacThuc" = gen_random_uuid()
WHERE "MaXacThuc" IS NULL;

ALTER TABLE public."HoaDon"
ALTER COLUMN "MaXacThuc" SET DEFAULT gen_random_uuid();

ALTER TABLE public."HoaDon"
ALTER COLUMN "MaXacThuc" SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS "HoaDon_MaXacThuc_unique"
ON public."HoaDon" ("MaXacThuc");
