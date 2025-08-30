package com.easydokan.activities;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.easydokan.R;
import com.easydokan.databinding.ActivityReportBinding;
import com.easydokan.models.ExpenseModel;
import com.easydokan.models.SaleModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    private ActivityReportBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference salesRef, expensesRef;
    private enum TimeFilter { DAILY, WEEKLY, MONTHLY }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFirebase();
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupChipGroupListener();
        loadReportData(TimeFilter.DAILY); // Initial load
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        salesRef = db.collection("users").document(userId).collection("sales");
        expensesRef = db.collection("users").document(userId).collection("expenses");
    }

    private void setupChipGroupListener() {
        binding.filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_daily) {
                loadReportData(TimeFilter.DAILY);
            } else if (checkedId == R.id.chip_weekly) {
                loadReportData(TimeFilter.WEEKLY);
            } else if (checkedId == R.id.chip_monthly) {
                loadReportData(TimeFilter.MONTHLY);
            }
        });
    }

    private void loadReportData(TimeFilter filter) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        switch (filter) {
            case DAILY:
                // Start of today is already set
                break;
            case WEEKLY:
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                break;
            case MONTHLY:
                cal.set(Calendar.DAY_OF_MONTH, 1);
                break;
        }
        Date startDate = cal.getTime();
        Date endDate = new Date(); // Now

        Query salesQuery = salesRef.whereGreaterThanOrEqualTo("saleDate", startDate).whereLessThanOrEqualTo("saleDate", endDate);
        Query expensesQuery = expensesRef.whereGreaterThanOrEqualTo("date", startDate).whereLessThanOrEqualTo("date", endDate);

        Tasks.whenAllSuccess(salesQuery.get(), expensesQuery.get()).addOnSuccessListener(results -> {
            QuerySnapshot salesSnapshot = (QuerySnapshot) results.get(0);
            QuerySnapshot expensesSnapshot = (QuerySnapshot) results.get(1);

            processData(salesSnapshot, expensesSnapshot);
        });
    }

    private void processData(QuerySnapshot salesSnapshot, QuerySnapshot expensesSnapshot) {
        double totalSales = 0;
        for (SaleModel sale : salesSnapshot.toObjects(SaleModel.class)) {
            totalSales += sale.getTotalAmount();
        }

        double totalExpenses = 0;
        Map<String, Double> expenseCategories = new HashMap<>();
        for (ExpenseModel expense : expensesSnapshot.toObjects(ExpenseModel.class)) {
            totalExpenses += expense.getAmount();
            String category = expense.getCategory() != null ? expense.getCategory() : "Uncategorized";
            expenseCategories.put(category, expenseCategories.getOrDefault(category, 0.0) + expense.getAmount());
        }

        double totalProfit = totalSales - totalExpenses;

        binding.totalSalesText.setText(String.format(Locale.getDefault(), "$%.2f", totalSales));
        binding.totalExpensesText.setText(String.format(Locale.getDefault(), "$%.2f", totalExpenses));
        binding.totalProfitText.setText(String.format(Locale.getDefault(), "$%.2f", totalProfit));

        setupBarChart(totalSales, totalExpenses);
        setupPieChart(expenseCategories);
    }

    private void setupBarChart(double totalSales, double totalExpenses) {
        BarChart barChart = binding.barChart;
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, (float) totalSales));
        entries.add(new BarEntry(1, (float) totalExpenses));

        BarDataSet dataSet = new BarDataSet(entries, "Sales vs Expenses");
        dataSet.setColors(Color.GREEN, Color.RED);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(16f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.invalidate(); // refresh
    }

    private void setupPieChart(Map<String, Double> expenseCategories) {
        PieChart pieChart = binding.pieChart;
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : expenseCategories.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expense Breakdown");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Expenses");
        pieChart.animate();
    }
}
