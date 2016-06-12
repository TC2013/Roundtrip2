package com.gxwtech.roundtrip2;

import android.os.Bundle;

import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records.Record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by geoff on 6/12/16.
 */
public class HistoryPageListContent {
    public static final List<RecordHolder> ITEMS = new ArrayList<>();

    /**
     * A map of items, by ID.
     */
    public static final Map<String, RecordHolder> ITEM_MAP = new HashMap<>();

    static void addItem(Bundle recordBundle) {
        addItem(new RecordHolder(recordBundle));
    }

    private static void addItem(RecordHolder item) {
        ITEMS.add(item);
        ITEM_MAP.put("#"+item.hashCode(), item);
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        RecordHolder rh = ITEMS.get(position);
        if (rh == null) {
            return "(null)";
        }
        Set<String> keys = rh.content.keySet();
        for (String key : keys) {
            builder.append(key);
            builder.append("\n");
        }
        /*
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        */
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */

    public static class RecordHolder {
        public final String id;
        public final Bundle content;
        public final String details;

        public RecordHolder(Bundle content) {
            this.id = String.format("%s\n%s",content.getString("timestamp"),content.getString("_type"));
            this.content = content;
            StringBuilder builder = new StringBuilder();
            for (String key : content.keySet()) {
                builder.append(key);
                builder.append("\n");
            }
            this.details = builder.toString();
        }

        @Override
        public String toString() {
            return content.getString("_type");
        }
    }

}

