package com.automation.ibinstallationteam.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.io.Serializable;

// Created by $USER_NAME on 2018/11/28/028.
public class UserInfo implements Serializable, Parcelable {

    //
    private String userId;
    private String userName;
    private String userPassword;
    private String userPhone;
    private String userRole; //用户角色

    private String userPerm; //用户权限
    private String userImage;
    private boolean checked;

    /*
     * 构造函数
     */
    public UserInfo() {
        super();
    }
    public UserInfo(String userName, @Nullable String userPhone, @Nullable String userPassword, @Nullable String userRole) {
        this.userName = userName;
        this.userPhone = userPhone;
        this.userPassword = userPassword;
        this.userRole = userRole;
        this.userPhone = userPhone;
    }
    public UserInfo(String userId, String userName, @Nullable String userPhone, @Nullable String userPassword, @Nullable String userRole) {
        this.userId = userId;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userPassword = userPassword;
        this.userRole = userRole;
    }
    public UserInfo(String userPhone, String userPassword) {
        this.userPhone = userPhone;
        this.userPassword = userPassword;
    }
    public UserInfo(String userId, String userRole, String userName) {
        this.userId = userId;
        this.userRole = userRole;
        this.userPhone = userName;
    }
    public UserInfo(String userId, String userName, String userPassword, String userPhone, String userRole, String userPerm, String userImage, boolean checked) {
        this.userId = userId;
        this.userName = userName;
        this.userPassword = userPassword;
        this.userPhone = userPhone;
        this.userRole = userRole;
        this.userPerm = userPerm;
        this.userImage = userImage;
        this.checked = checked;
    }

    /*
     * Bean 函数
     */
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserPerm() {
        return userPerm;
    }

    public void setUserPerm(String userPerm) {
        this.userPerm = userPerm;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public boolean getChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        checked = checked;
    }

    /*
     * Parcel 序列化
     */
    protected UserInfo(Parcel in) {
        userId = in.readString();
        userName = in.readString();
        userPassword = in.readString();
        userPhone = in.readString();
        userRole = in.readString();
        userPerm = in.readString();
        userImage = in.readString();
        // 将byte再转化回布尔值
        checked = in.readByte() != 0;
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(userName);
        dest.writeString(userPassword);
        dest.writeString(userPhone);
        dest.writeString(userRole);
        dest.writeString(userPerm);
        dest.writeString(userImage);
        // 布尔值这里是将之转化成byte进行序列化
        dest.writeByte((byte) (checked ? 1 : 0));
    }
}
