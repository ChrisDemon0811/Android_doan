package com.example.banve.config;

public final class SupabaseConfig {
    // URL dự án Supabase dùng cho REST API.
    public static final String SUPABASE_URL = "https://zouvtylxvdbadkpbrpdb.supabase.co";

    // Anon key của Supabase, không dùng service role key trong ứng dụng Android.
    public static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InpvdXZ0eWx4dmRiYWRrcGJycGRiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODA2MjE4NDksImV4cCI6MjA5NjE5Nzg0OX0.Ar3pS9551athZz6vO-cpp1c4Grxcx7SfzQt84zfOOQI";

    // Bucket Supabase Storage dùng để lưu ảnh vé.
    public static final String STORAGE_BUCKET_ANH_VE = "anh-ve";

    private SupabaseConfig() {
    }
}
