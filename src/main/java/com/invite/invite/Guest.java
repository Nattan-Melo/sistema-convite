package com.invite.invite;

public class Guest
{
    private String id;
    private String name = "";
    private int status = 0;
    private String link = "";
    private int aggregate = 0;
    
    public Guest(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getAggregate() {
        return aggregate;
    }

    public void setAggregate(int aggregate) {
        this.aggregate = aggregate;
    }


    


}