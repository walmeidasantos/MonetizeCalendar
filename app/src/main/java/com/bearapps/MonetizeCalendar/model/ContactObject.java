package com.bearapps.MonetizeCalendar.model;

import com.bearapps.MonetizeCalendar.utility.Storage;

/**
 * Created by ursow on 15/4/24.
 */
public class ContactObject {

    protected Integer Id;
    protected String googleidId;
    protected String name;
    protected String email;
    protected String status;
    protected String photoPath;
    protected Boolean choosed = false;
    protected long amount = 0;
    protected String period;
    protected Boolean stored = false;

    public ContactObject(String googleid, String name, String email, String Status, String photoPath) {
        this.googleidId = googleid;
        this.name = name;
        this.email = email;
        this.status = Status;
        this.photoPath = photoPath;

    }

    public ContactObject(String googleid, String name, String email, String Status, String photoPath, long amount, String period) {
        this.googleidId = googleid;
        this.name = name;
        this.email = email;
        this.status = Status;
        this.photoPath = photoPath;
        this.period = period;
        this.amount = amount;

    }

    public String getGoogleId() {
        return googleidId;
    }

    public Integer getId() {
        return Id;
    }

    public void setId(int New_Id) {
        Id = New_Id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getEmail() {
        return email;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setChoosed() {
        this.choosed = true;
        return;
    }

    public Boolean IsChoosed() {
        return this.choosed;
    }

    public void setUnChoosed() {
        this.choosed = false;
        return;
    }

    public long getAmount() {
        return this.amount;
    }

    public String getPeriod() {
        if (this.period == null) {
            this.period = Storage.CHR_TYPE_MONTHLY;
        }

        return this.period;

    }

    public Boolean StatusStore() {
        return this.stored;
    }

    public void ToggleStore() {
        this.stored = !stored;
    }

}

