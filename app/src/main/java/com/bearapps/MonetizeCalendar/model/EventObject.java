package com.bearapps.MonetizeCalendar.model;

import com.google.api.services.calendar.model.EventDateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by ursow on 15/4/24.
 */
public class EventObject {

    protected Integer Id;
    protected String googleid;
    protected List<Integer> ContactsId = new ArrayList<Integer>();
    protected List<String> ContactsEmail = new ArrayList<String>();
    protected String sumary;
    protected EventDateTime BeginEvent;
    protected EventDateTime EndEvent;
    protected String where;
    protected int status;

    public EventObject(String googleid, String createtion_sumary, EventDateTime BeginEvent, EventDateTime EndEvent, String where, List ContactsId, List ContactsEmail) {
        this.googleid = googleid;
        this.sumary = createtion_sumary;
        this.BeginEvent = BeginEvent;
        this.EndEvent = EndEvent;
        this.where = where;
        this.ContactsId = ContactsId;
        this.ContactsEmail = ContactsEmail;
    }

    public EventObject(String googleid, String createtion_sumary, EventDateTime BeginEvent, EventDateTime EndEvent, String where) {
        this.googleid = googleid;
        this.sumary = createtion_sumary;
        this.BeginEvent = BeginEvent;
        this.EndEvent = EndEvent;
        this.where = where;

    }

    public String getGoogleId() {
        return googleid;
    }

    public List<Integer> getContactsId() {
        return ContactsId;
    }

    public Integer getId() {
        return Id;
    }

    public void setId(int New_Id) {
        Id = New_Id;
    }

    public String getBeginEventDate() {
        SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyyy");
        String result = date.format(new Date(BeginEvent.getDateTime().getValue()));

        return result;
    }

    public String getBeginEventTime() {
        //DateTime date = DateTime.parseRfc3339(BeginEvent.getDateTime(),DateTimeFormat)
        //String result = DateTime.
        SimpleDateFormat time = new SimpleDateFormat("HH:mm");
        String result = time.format(new Date(BeginEvent.getDateTime().getValue()));

        return result;
    }

    public EventDateTime getBeginEvent() {

        EventDateTime result = BeginEvent;
        return result;
    }

    public EventDateTime getEndEvent() {
        EventDateTime result = EndEvent;
        return result;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int newStatus) {
        this.status = newStatus;
    }

    public String getWhere() {
        return where;
    }

    public String getSumary() {
        return sumary;
    }

    public Integer getFirstContact() {
        Integer result;

        if (this.ContactsId != null && !this.ContactsId.isEmpty() && this.ContactsId.size() > 0) {
            result = this.ContactsId.get(0);
        } else {
            result = 0;
        }

        return result;
    }

    public String getFirstContactEmail() {
        String result;

        if (this.ContactsEmail != null && !this.ContactsEmail.isEmpty() && this.ContactsEmail.size() > 0) {
            result = this.ContactsEmail.get(0);
        } else {
            result = "";
        }

        return result;
    }


    public void AddContact(Integer ContactId) {
        this.ContactsId.add(ContactId);
    }

    public void AddContactEmail(String email) {
        this.ContactsEmail.add(email);
    }
}

