package com.wtrwx.blog;

class ContactInfo {

    private String title;
    private String text;
    ContactInfo(String title, String text){
        this.title = title;
        this.text = text;
    }

    String getTitle(){
        return title;
    }
    String getText(){
        return text;
    }
}