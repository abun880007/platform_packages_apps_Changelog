package com.bytehamster.changelog;

import android.content.Context;
import android.view.View;
import java.util.HashMap;

final class Change {

    public static final int TYPE_ITEM   = 0;
    public static final int TYPE_HEADER = 1;

    public String id       = "";
    public String branch   = "";
    public String number   = "";
    public String project  = "";
    public String dateFull = "";
    public String dateDay  = "";
    public String owner    = "";
    public String title    = "";
    public String message  = "";
    public long   date     = 0;
    public long   lastModified  = 0;
    public boolean isNew   = false;

    HashMap<String, Object> getHashMap(Context c) {
        HashMap<String, Object> newItem = new HashMap<String, Object>();
        newItem.put("title", title);
        newItem.put("secondline", c.getResources().getString(R.string.owner_and_date)
                .replace("%o", owner)
                .replace("%d", dateFull));
        newItem.put("owner", owner );
        newItem.put("dateFull", dateFull);
        newItem.put("project", project);
        newItem.put("number", number);
        newItem.put("type", TYPE_ITEM);
        newItem.put("branch", branch);
        newItem.put("change_id", id);
        newItem.put("is_new", isNew);
        newItem.put("message", message);
        newItem.put("expand", c.getResources().getString(R.string.expanded_message)
                .replace("%project", project)
                .replace("%message", message)
                .replace("%branch", branch ));
        newItem.put("visibility", View.GONE);
        return newItem;
    }

    void calculateDate() {
        dateFull = Main.mDateFormat.format(date);
        dateDay  = Main.mDateDayFormat.format(date);
    }
}