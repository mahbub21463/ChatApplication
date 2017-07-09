/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatprogram;

import java.io.*;
import java.util.ArrayList;

/**
 *
 * @author mahbub
 */
public class ChatMessage implements Serializable {

    private final int login = 1, signup = 0, msgtype = 2, file = 3, activeFriend = 4, activeAnonymous = 5, inactiveFriend = 6;
    private final int singleActiveFriend = 7, singleAnonymous = 8, logout = 9, frequest = 10, prevMsg = 11, accepted = 12;
    private final int rejected = 13, offline = 14,fileReached = 15;
    private int type;
    private String message;
    private String destUser;
    private String source;
    private ArrayList<String> friendList;
    private String friend;
    private byte[] fileContent;

    public ChatMessage() {
    }

    public ChatMessage(int typ) {
        type = typ;
    }

    public ChatMessage(String friend, int typ) {
        this.friend = friend;
        type = typ;
    }

    public ChatMessage(int typ, String msg) {
        type = typ;
        message = msg;
    }

    public ChatMessage(int typ, ArrayList<String> frlist) {
        type = typ;
        friendList = frlist;
    }

    public ChatMessage(int typ, String msg, String src, String dest) {
        type = typ;
        message = msg;
        destUser = dest;
        source = src;
    }

    public ChatMessage(int typ, String msg, byte[] content, String src, String dest) {
        type = typ;
        message = msg;
        destUser = dest;
        source = src;
        fileContent = content;
    }

    public String getSource() {
        return source;
    }

    public String getDest() {
        return destUser;
    }

    public int getType() {
        return type;
    }

    public byte[] getFileContents() {
        return fileContent;
    }

    public String getMessage() {
        return message;
    }

    public String getFriend() {
        return friend;
    }

    public ArrayList<String> getFriendList() {
        return friendList;
    }

    public void setType(int typ) {
        type = typ;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void addFriend(String f) {
        friend = f;
    }

    public void setDest(String dst) {
        destUser = dst;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
