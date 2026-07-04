import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

type UploadPayload = {
  tenFile?: string;
  kieuNoiDung?: string;
  duLieuBase64?: string;
};

const TEN_BUCKET = "anh-ve";
const KICH_THUOC_TOI_DA = 5 * 1024 * 1024;

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

const jsonHeaders = {
  ...corsHeaders,
  "Content-Type": "application/json; charset=utf-8",
};

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (request.method !== "POST") {
    return traVeJson({ loi: "Phương thức không hợp lệ" }, 405);
  }

  const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
  const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
  if (!supabaseUrl || !serviceRoleKey) {
    return traVeJson({ loi: "Thiếu cấu hình Supabase Edge Function" }, 500);
  }

  let payload: UploadPayload;
  try {
    payload = await request.json();
  } catch (_error) {
    return traVeJson({ loi: "Dữ liệu ảnh không hợp lệ" }, 400);
  }

  if (!payload.duLieuBase64) {
    return traVeJson({ loi: "Chưa có dữ liệu ảnh" }, 400);
  }

  const kieuNoiDung = chuanHoaKieuNoiDung(payload.kieuNoiDung);
  if (!kieuNoiDung.startsWith("image/")) {
    return traVeJson({ loi: "Chỉ cho phép tải ảnh" }, 400);
  }

  let duLieuAnh: Uint8Array;
  try {
    duLieuAnh = chuyenBase64ThanhByte(payload.duLieuBase64);
  } catch (_error) {
    return traVeJson({ loi: "Không đọc được dữ liệu ảnh" }, 400);
  }

  if (duLieuAnh.byteLength > KICH_THUOC_TOI_DA) {
    return traVeJson({ loi: "Ảnh vượt quá dung lượng 5MB" }, 400);
  }

  const supabase = createClient(supabaseUrl, serviceRoleKey, {
    auth: {
      persistSession: false,
      autoRefreshToken: false,
    },
  });

  await damBaoBucketTonTai(supabase);

  const duoiFile = layDuoiFile(payload.tenFile, kieuNoiDung);
  const duongDan = `ve/${Date.now()}_${crypto.randomUUID()}${duoiFile}`;
  const { error: loiTaiLen } = await supabase.storage
    .from(TEN_BUCKET)
    .upload(duongDan, duLieuAnh, {
      contentType: kieuNoiDung,
      cacheControl: "3600",
      upsert: false,
    });

  if (loiTaiLen) {
    return traVeJson({ loi: loiTaiLen.message }, 500);
  }

  const { data } = supabase.storage.from(TEN_BUCKET).getPublicUrl(duongDan);
  return traVeJson({
    thanhCong: true,
    duongDan,
    urlCongKhai: data.publicUrl,
  });
});

async function damBaoBucketTonTai(supabase: ReturnType<typeof createClient>) {
  const { data: buckets } = await supabase.storage.listBuckets();
  const daTonTai = (buckets ?? []).some((bucket) => bucket.name === TEN_BUCKET);
  if (daTonTai) {
    return;
  }

  await supabase.storage.createBucket(TEN_BUCKET, {
    public: true,
    fileSizeLimit: KICH_THUOC_TOI_DA,
    allowedMimeTypes: ["image/jpeg", "image/png", "image/webp", "image/gif"],
  });
}

function chuanHoaKieuNoiDung(kieuNoiDung?: string): string {
  if (!kieuNoiDung || !kieuNoiDung.trim()) {
    return "image/jpeg";
  }
  return kieuNoiDung.trim().toLowerCase();
}

function layDuoiFile(tenFile?: string, kieuNoiDung?: string): string {
  if (tenFile && tenFile.includes(".")) {
    const duoiFile = tenFile.substring(tenFile.lastIndexOf(".")).toLowerCase();
    if (/^\.(jpg|jpeg|png|webp|gif)$/.test(duoiFile)) {
      return duoiFile;
    }
  }

  if (kieuNoiDung === "image/png") return ".png";
  if (kieuNoiDung === "image/webp") return ".webp";
  if (kieuNoiDung === "image/gif") return ".gif";
  return ".jpg";
}

function chuyenBase64ThanhByte(base64: string): Uint8Array {
  const duLieuSach = base64.includes(",") ? base64.split(",").pop() ?? "" : base64;
  const chuoiNhiPhan = atob(duLieuSach);
  const bytes = new Uint8Array(chuoiNhiPhan.length);
  for (let i = 0; i < chuoiNhiPhan.length; i++) {
    bytes[i] = chuoiNhiPhan.charCodeAt(i);
  }
  return bytes;
}

function traVeJson(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: jsonHeaders,
  });
}
