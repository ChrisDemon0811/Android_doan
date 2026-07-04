package com.example.banve.activities.user;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.banve.R;
import com.example.banve.adapters.TinNhanAdapter;
import com.example.banve.controllers.ChatAIController;
import com.example.banve.dao.LichSuChatDAO;
import com.example.banve.models.LichSuChat;
import com.example.banve.network.ApiCallback;
import com.example.banve.utils.Session;
import com.example.banve.utils.TienIch;

import java.util.List;

public class ChatAIActivity extends AppCompatActivity {
    private RecyclerView rcvLichSuChat;
    private ProgressBar pgbDangTra;
    private EditText edtCauHoi;
    private Button btnGui;
    private TinNhanAdapter tinNhanAdapter;
    private LichSuChatDAO lichSuChatDAO;
    private ChatAIController chatAIController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Session.khoiPhuc(this);
        setContentView(R.layout.user_activity_chat_ai);

        anhXa();
        lichSuChatDAO = new LichSuChatDAO();
        chatAIController = new ChatAIController();
        khoiTaoRecyclerView();
        batSuKien();
        taiLichSuChat();
    }

    private void anhXa() {
        rcvLichSuChat = findViewById(R.id.rcvLichSuChat);
        pgbDangTra = findViewById(R.id.pgbDangTra);
        edtCauHoi = findViewById(R.id.edtCauHoi);
        btnGui = findViewById(R.id.btnGui);
        findViewById(R.id.btnQuayLai).setOnClickListener(v -> finish());
    }

    private void khoiTaoRecyclerView() {
        tinNhanAdapter = new TinNhanAdapter();
        rcvLichSuChat.setLayoutManager(new LinearLayoutManager(this));
        rcvLichSuChat.setAdapter(tinNhanAdapter);
    }

    private void batSuKien() {
        btnGui.setOnClickListener(v -> guiCauHoi());
    }

    private void taiLichSuChat() {
        if (!Session.dangDangNhap()) {
            TienIch.hienAlert(this, "Thông báo", "Vui lòng đăng nhập lại");
            finish();
            return;
        }

        lichSuChatDAO.layTheoNguoiDung(Session.nguoiDungHienTai.getMaNguoiDung(), new ApiCallback<List<LichSuChat>>() {
            @Override
            public void onSuccess(List<LichSuChat> data) {
                tinNhanAdapter.capNhatTuLichSu(data);
                cuonXuongCuoi();
            }

            @Override
            public void onError(String thongBao) {
                TienIch.hienAlert(ChatAIActivity.this, "Lỗi chat AI", thongBao);
            }
        });
    }

    private void guiCauHoi() {
        String cauHoi = edtCauHoi.getText().toString().trim();
        if (cauHoi.isEmpty()) {
            TienIch.hienToast(this, "Vui lòng nhập câu hỏi");
            return;
        }

        tinNhanAdapter.themTinNhanUser(cauHoi);
        edtCauHoi.setText("");
        cuonXuongCuoi();
        capNhatDangTraLoi(true);

        chatAIController.guiCauHoi(cauHoi, new ApiCallback<String>() {
            @Override
            public void onSuccess(String data) {
                capNhatDangTraLoi(false);
                tinNhanAdapter.themTinNhanAI(data);
                cuonXuongCuoi();
            }

            @Override
            public void onError(String thongBao) {
                capNhatDangTraLoi(false);
                TienIch.hienAlert(ChatAIActivity.this, "Lỗi chat AI", thongBao);
            }
        });
    }

    private void capNhatDangTraLoi(boolean dangTraLoi) {
        pgbDangTra.setVisibility(dangTraLoi ? View.VISIBLE : View.GONE);
        btnGui.setEnabled(!dangTraLoi);
        edtCauHoi.setEnabled(!dangTraLoi);
    }

    private void cuonXuongCuoi() {
        rcvLichSuChat.post(() -> {
            int soTinNhan = tinNhanAdapter.getItemCount();
            if (soTinNhan > 0) {
                rcvLichSuChat.scrollToPosition(soTinNhan - 1);
            }
        });
    }
}

