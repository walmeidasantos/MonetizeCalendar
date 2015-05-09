package com.bearapps.ground_control.model;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;

import java.util.List;


/**
 * Created by ursow on 15/4/24.
 */
public class EventObject {

    protected Integer Id;
    protected String googleid;
    protected List<String> contacts_googleid;
    protected String sumary;
    protected EventDateTime BeginEvent;
    protected EventDateTime EndEvent;
    protected String where;

    public EventObject(String googleid,String createtion_sumary,EventDateTime BeginEvent, EventDateTime EndEvent, String where, List Contacts ) {
        this.googleid = googleid;
        this.sumary = createtion_sumary;
        this.BeginEvent = BeginEvent;
        this.EndEvent = EndEvent;

    }

    public String getGoogleId() {
        return googleid;
    }
    public List<String> getContactsId() { return contacts_googleid; }
    public void setId(int New_Id) { Id = New_Id;   }
    public Integer getId() { return Id;}
    public DateTime getBeginEvent() { return BeginEvent.getDateTime(); }
    public DateTime getEndEvent() { return EndEvent.getDateTime(); }
    public String getWhere() { return where;}
    public String getSumary() { return sumary;  }

    public void AddContact(String contact_googleid){
        contacts_googleid.add(contact_googleid);
    }
}

