package com.bearapps.MonetizeCalendar.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ursow on 29/05/15.
 */
public class InvoiceObject {

    protected Integer Id;
    protected ContactObject contact;
    protected long amount;
    protected Date CreatedDate;
    protected String status = "1"; // 1=open , 2= close, 3=Cancel
    protected List<Integer> eventsId = new ArrayList<Integer>();


    public InvoiceObject(ContactObject contact, long amount, Date CreatedDate) {
        this.contact = contact;
        this.CreatedDate = CreatedDate;
        this.amount = amount;
    }

    public ContactObject getContact() {
        return contact;
    }

    public Integer getId() {
        return Id;
    }

    public void setId(int New_Id) {
        Id = New_Id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String newStatus) {
        this.status = newStatus;
        return;
    }

    public long getAmount() {
        return this.amount;
    }

    public void SetAmount(long amount) {
        this.amount = amount;
    }

    public void AddEventId(Integer eventId) {
        this.eventsId.add(eventId);
    }

    public List<Integer> getEventsId() {
        return this.eventsId;
    }
}

