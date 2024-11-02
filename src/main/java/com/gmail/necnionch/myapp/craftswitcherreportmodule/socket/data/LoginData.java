package com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.data;

import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.SerializableData;

public class LoginData implements SerializableData {
    public String serverName;
    public String reportType;

    //    @Override
//    public String getDataKey() {
//        return "login";
//    }


    public LoginData(String serverName, String reportType) {
        this.serverName = serverName;
        this.reportType = reportType;
    }

}
