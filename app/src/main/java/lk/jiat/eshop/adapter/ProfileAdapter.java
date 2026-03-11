package lk.jiat.eshop.adapter;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.content.Context;
import java.util.Arrays;
import java.util.List;

public class ProfileAdapter {

    public static void setupCitySpinner(Context context, AutoCompleteTextView citySpinner) {
        List<String> cities = Arrays.asList("Colombo", "Gampaha", "Kalutara", "Kandy", "Matale", "Nuwara Eliya", "Galle", "Matara", "Hambantota", "Jaffna", "Kilinochchi", "Mannar", "Vavuniya", "Mullaitivu", "Batticaloa", "Ampara", "Trincomalee", "Kurunegala", "Puttalam", "Anuradhapura", "Polonnaruwa", "Badulla", "Moneragala", "Ratnapura", "Kegalle");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, cities);
        citySpinner.setAdapter(adapter);
    }
}
