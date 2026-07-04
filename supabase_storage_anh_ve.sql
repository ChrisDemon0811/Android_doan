-- Cấu hình bucket Storage cho ảnh vé.
-- Bản này KHÔNG ALTER TABLE storage.objects để tránh lỗi:
-- ERROR 42501: must be owner of table objects

INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
    'anh-ve',
    'anh-ve',
    TRUE,
    5242880,
    ARRAY['image/jpeg', 'image/png', 'image/webp', 'image/gif']
)
ON CONFLICT (id) DO UPDATE
SET
    public = TRUE,
    file_size_limit = 5242880,
    allowed_mime_types = ARRAY['image/jpeg', 'image/png', 'image/webp', 'image/gif'];

SELECT 'Da tao/cap nhat bucket anh-ve. Hay tao Storage policies trong giao dien Supabase.' AS ThongBao;
