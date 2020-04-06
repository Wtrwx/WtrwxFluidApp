package com.wtrwx.blog;

public class ContactInfo {

    protected String title;
    protected String text;
    public ContactInfo(String title,String text){
        this.title = title;
        this.text = text;
    }

    public String getTitle(){
        return title;
    }
    public String getText(){
        return text;
    }
}