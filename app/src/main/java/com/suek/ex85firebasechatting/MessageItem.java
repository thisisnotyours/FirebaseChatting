package com.suek.ex85firebasechatting;

public class MessageItem {    //firebase 로 전송할..

    String name;    //닉네임
    String message; //메세지
    String time;    //작성시간
    String profileUrl;  //Storage 에 저장되어있는 프로필이미지의 URL (http://~~~)


    public MessageItem() {
    }


    public MessageItem(String name, String message, String time, String profileUrl) {
        this.name = name;
        this.message = message;
        this.time = time;
        this.profileUrl = profileUrl;
    }


    //VO 클래스

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }
}
