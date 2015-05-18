package com.bearapps.ground_control.model;

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

    public ContactObject(String googleid, String name, String email,String Status ) {
        this.googleidId = googleid;
        this.name = name;
        this.email = email;
        this.status = Status;

    }

    public ContactObject(String googleid, String name, String email,String Status, String photoPath ) {
        this.googleidId = googleid;
        this.name = name;
        this.email = email;
        this.status = Status;
        this.photoPath = photoPath;

    }

    public String getGoogleId() {
        return googleidId;
    }
    public void setId(int New_Id) { Id = New_Id;   }
    public Integer getId() { return Id;}
    public String getName() { return name; }
    public String getStatus() { return status;}
    public String getEmail() { return email;  }
    public String getPhotoPath() { return photoPath; }
    public void setChoosed() { this.choosed = true; return ;}
    public Boolean IsChoosed() { return this.choosed; }
    public void setUnChoosed() { this.choosed = false; return ;}

}
