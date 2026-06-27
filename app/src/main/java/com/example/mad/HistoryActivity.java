package com.example.mad;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";
    private RecyclerView rvAttendanceHistory, rvStepHistory;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        rvAttendanceHistory = findViewById(R.id.rvAttendanceHistory);
        rvStepHistory = findViewById(R.id.rvStepHistory);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            finish();
            return;
        }

        setupRecyclerViews();
        fetchAttendanceHistory();
        fetchStepHistory();
    }

    private void setupRecyclerViews() {
        rvAttendanceHistory.setLayoutManager(new LinearLayoutManager(this));
        rvStepHistory.setLayoutManager(new LinearLayoutManager(this));
    }

    private void fetchAttendanceHistory() {
        db.collection("attendance")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<HistoryItemModel> attendanceList = new ArrayList<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String title = document.getString("class") + " - " + document.getString("room");
                        String subtitle = "Status: " + document.getString("status") + " on " + sdf.format(document.getDate("timestamp"));
                        attendanceList.add(new HistoryItemModel(title, subtitle));
                    }
                    rvAttendanceHistory.setAdapter(new HistoryAdapter(attendanceList));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching attendance history", e));
    }

    private void fetchStepHistory() {
        db.collection("daily_steps")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<HistoryItemModel> stepList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String title = document.get("steps") + " steps";
                        String subtitle = "On: " + document.getString("date");
                        stepList.add(new HistoryItemModel(title, subtitle));
                    }
                    rvStepHistory.setAdapter(new HistoryAdapter(stepList));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching step history", e));
    }

    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private final List<HistoryItemModel> historyItems;

        public HistoryAdapter(List<HistoryItemModel> historyItems) {
            this.historyItems = historyItems;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HistoryItemModel item = historyItems.get(position);
            holder.tvTitle.setText(item.getTitle());
            holder.tvSubtitle.setText(item.getSubtitle());
        }

        @Override
        public int getItemCount() {
            return historyItems.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView tvTitle;
            public TextView tvSubtitle;

            public ViewHolder(View view) {
                super(view);
                tvTitle = view.findViewById(R.id.tvHistoryTitle);
                tvSubtitle = view.findViewById(R.id.tvHistorySubtitle);
            }
        }
    }
}
