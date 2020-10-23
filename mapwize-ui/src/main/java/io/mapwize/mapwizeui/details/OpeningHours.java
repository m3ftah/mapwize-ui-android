package io.mapwize.mapwizeui.details;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.mapwize.mapwizeui.R;

import static java.text.DateFormat.SHORT;
import static java.text.DateFormat.getTimeInstance;


public class OpeningHours extends Row {
    LinearLayout openingHoursButton;
    ImageView arrowDownOpeningHours;
    private RecyclerView recyclerView;

    public OpeningHours(Context context, List<Map<String, Object>> openingHours, OnClickListener clickListener) {
        super(context, getLabel(context, openingHours), R.drawable.mapwize_details_ic_baseline_access_time_24, openingHours != null && openingHours.size() != 0, OPENING_TIME_ROW, clickListener);
        showOpeningHours(openingHours);
    }

    static String getLabel(Context context, List<Map<String, Object>> openingHours) {
        if (openingHours == null) {
            return "";
        }
        String label = "";
        Calendar calendar = Calendar.getInstance();
        int day = (calendar.get(Calendar.DAY_OF_WEEK) + 6) % 7;
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        boolean isOpen = OpeningHoursFormat.isOpen(openingHours, day, hour, minute);
        Map<String, Object> closingOrOpening = isOpen ?
                OpeningHoursFormat.closesAt(openingHours, day, hour, minute) :
                OpeningHoursFormat.opensAt(openingHours, day, hour, minute);
        if (closingOrOpening != null) {
            label = closingOrOpening.containsKey("soon") ?
                    (isOpen ? context.getString(R.string.mapwize_details_closing_soon) : context.getString(R.string.mapwize_details_opening_soon)) :
                    (isOpen ? context.getString(R.string.mapwize_details_open) : context.getString(R.string.mapwize_details_closed));
            label += ", ";
            String closesAt = isOpen ? (String) closingOrOpening.get("close") : (String) closingOrOpening.get("open");
            if (closesAt != null) {
                String date = closesAt.equals("2359") ? context.getString(R.string.mapwize_details_midnight) :
                        (closesAt.equals("1200") ? context.getString(R.string.mapwize_details_midday) : formatHour(closesAt));
                if (closingOrOpening.containsKey("tomorrow")) {
                    label+= context.getString( isOpen ? R.string.mapwize_details_closes_tomorrow : R.string.mapwize_details_opens_tomorrow, date);
                } else if (closingOrOpening.containsKey("today")) {
                    label+= context.getString( isOpen ? R.string.mapwize_details_closes_today : R.string.mapwize_details_opens_today, date);
                } else {
                    int closingDay = (int) closingOrOpening.get("day");
                    String weekday = new DateFormatSymbols().getWeekdays()[(closingDay + 1) % 8];//days start from SUNDAY=1 to SATURDAY=7.
                    label+= context.getString( isOpen ? R.string.mapwize_details_closes_weekday : R.string.mapwize_details_opens_weekday, weekday, date);
                }
            }
        } else {
            label = context.getString(R.string.mapwize_details_open24_7);
        }
        return label;
    }

    static String formatHour(String HHmm) {
        String res = HHmm;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HHmm", Locale.forLanguageTag("fr"));
            Date dt = sdf.parse(HHmm);
            if (dt != null) {
                res = getTimeInstance(SHORT).format(dt);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public int getRowType() {
        return rowType;
    }

    @Override
    void initLayout(final Context context, String label, int iconId, boolean highlighted, int rowType, OnClickListener clickListener) {
        inflate(context, R.layout.mapwize_details_opening_hours, this);
        this.context = context;
        this.rowType = rowType;
        rowLabel = findViewById(R.id.rowLabel);
        iconImageView = findViewById(R.id.rowIcon);
        recyclerView = findViewById(R.id.daysRecyclerView);
        arrowDownOpeningHours = findViewById(R.id.arrowDownOpeningHours);
        openingHoursButton = findViewById(R.id.openingHoursButton);
        iconImageView.setImageResource(iconId);
        if (clickListener != null) {
            setOnClickListener(clickListener);
        }
        rowLabel.setText(label);
        this.iconId = iconId;
        setAvailability(highlighted);
    }

    private void showOpeningHours(List<Map<String, Object>> openingHours) {
        if (openingHours != null && openingHours.size() != 0) {
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(layoutManager);
            Calendar calendar = Calendar.getInstance();
            int day = (calendar.get(Calendar.DAY_OF_WEEK) + 6) % 7;
            DaysAdapter daysAdapter = new DaysAdapter(context, openingHours, day);
            recyclerView.setAdapter(daysAdapter);
            this.openingHoursButton.setOnClickListener(view -> {
                recyclerView.setVisibility(recyclerView.getVisibility() == GONE ? VISIBLE : GONE);
                arrowDownOpeningHours.setImageResource(
                        recyclerView.getVisibility() == GONE ?
                                R.drawable.mapwize_details_ic_baseline_keyboard_arrow_down_24 :
                                R.drawable.mapwize_details_ic_baseline_keyboard_arrow_up_24);
            });
        } else {
            arrowDownOpeningHours.setVisibility(GONE);
        }
    }
}
