package com.example.cau2;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Giá vé (VND)
    private static final double PRICE_FIRST = 1_500_000;
    private static final double PRICE_BUSINESS = 1_300_000;
    private static final double PRICE_ECONOMY = 1_000_000;

    EditText etName, etPhone, etQuantity, etDiscount;
    RadioGroup rgTicketType;
    RatingBar ratingBar;
    Button btnBook;
    TextView tvRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etQuantity = findViewById(R.id.etQuantity);
        etDiscount = findViewById(R.id.etDiscount);
        rgTicketType = findViewById(R.id.rgTicketType);
        ratingBar = findViewById(R.id.ratingBar);
        btnBook = findViewById(R.id.btnBook);
        tvRecords = findViewById(R.id.tvRecords);

        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = etName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String qtyText = etQuantity.getText().toString().trim();
                String discountText = etDiscount.getText().toString().trim();

                if (name.isEmpty()) {
                    etName.setError("Nhập họ tên");
                    return;
                }
                if (phone.isEmpty()) {
                    etPhone.setError("Nhập số điện thoại");
                    return;
                }

                int quantity;
                try {
                    quantity = Integer.parseInt(qtyText);
                    if (quantity <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    etQuantity.setError("Số lượng không hợp lệ");
                    return;
                }

                double discountPercent = 0;
                if (!discountText.isEmpty()) {
                    try {
                        discountPercent = Double.parseDouble(discountText);
                        if (discountPercent < 0) discountPercent = 0;
                    } catch (NumberFormatException e) {
                        etDiscount.setError("Ưu đãi không hợp lệ");
                        return;
                    }
                }

                int selectedId = rgTicketType.getCheckedRadioButtonId();
                double pricePerTicket;
                String ticketType;
                if (selectedId == R.id.rbFirst) {
                    pricePerTicket = PRICE_FIRST;
                    ticketType = "Hạng nhất";
                } else if (selectedId == R.id.rbBusiness) {
                    pricePerTicket = PRICE_BUSINESS;
                    ticketType = "Thương gia";
                } else {
                    pricePerTicket = PRICE_ECONOMY;
                    ticketType = "Phổ thông";
                }

                float rating = ratingBar.getRating();
                if (rating == 5.0f) {
                    discountPercent += 5.0;
                }

                double subtotal = pricePerTicket * quantity;
                double discountAmount = subtotal * (discountPercent / 100);
                double total = subtotal - discountAmount;

                String totalStr = nf.format(total) + " VND";
                String priceStr = nf.format(pricePerTicket) + " VND";

                String record = "Họ tên: " + name +
                        "\nĐT: " + phone +
                        "\nLoại vé: " + ticketType +
                        "\nSố lượng: " + quantity +
                        "\nGiá 1 vé: " + priceStr +
                        "\nƯu đãi: " + discountPercent + " %" +
                        "\nTổng tiền: " + totalStr +
                        "\n---------------------------\n";

                tvRecords.append(record);

                Toast.makeText(MainActivity.this, "Đặt vé thành công!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
