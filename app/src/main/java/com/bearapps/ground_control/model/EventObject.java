package com.bearapps.ground_control.model;

import com.google.api.client.util.DateTime;
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
    protected List<String> contactsEmail = new ArrayList<String>();
    protected String sumary;
    protected EventDateTime BeginEvent;
    protected EventDateTime EndEvent;
    protected String where;

    public EventObject(String googleid,String createtion_sumary,EventDateTime BeginEvent, EventDateTime EndEvent, String where, List Contacts ) {
        this.googleid = googleid;
        this.sumary = createtion_sumary;
        this.BeginEvent = BeginEvent;
        this.EndEvent = EndEvent;
        this.where = where;
        this.contactsEmail = Contacts;
    }
    public EventObject(String googleid,String createtion_sumary,EventDateTime BeginEvent, EventDateTime EndEvent, String where ) {
        this.googleid = googleid;
        this.sumary = createtion_sumary;
        this.BeginEvent = BeginEvent;
        this.EndEvent = EndEvent;
        this.where = where;

    }

    public String getGoogleId() {
        return googleid;
    }
    public List<String> getContactsEmails() { return contactsEmail; }
    public void setId(int New_Id) { Id = New_Id;   }
    public Integer getId() { return Id;}
    public String getBeginEventDate() {
        SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyyy");
        String result = date.format( new Date(BeginEvent.getDateTime().getValue()) );

        return result; }

    public String getBeginEventTime() {
        //DateTime date = DateTime.parseRfc3339(BeginEvent.getDateTime(),DateTimeFormat)
        //String result = DateTime.
        SimpleDateFormat time = new SimpleDateFormat("HH:mm");
        String result = time.format( new Date(BeginEvent.getDateTime().getValue()) );

        return result; }
    public DateTime getBeginEvent() {
        DateTime result = BeginEvent.getDateTime();
        return result ; }
    public DateTime getEndEvent() {
        DateTime result = EndEvent.getDateTime();
        return result ; }
    public String getWhere() { return where;}
    public String getSumary() { return sumary;  }
    public String getFirstContact() {
        String result ;

        if (  this.contactsEmail != null && !this.contactsEmail.isEmpty() && this.contactsEmail.size() > 0) {
            result = this.contactsEmail.get(0);
        }
        else {
            result = "";
        }

        return result;
    }

    public void AddContact(String contact_googleid){
        this.contactsEmail.add(contact_googleid);
    }
}

