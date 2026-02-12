package com.tejas.splitspend.common;

import com.tejas.splitspend.user.User;

public class fixtures {

    public static User getValidUser() {
        return new User(
                "Tejas",
                "tejas@example.com",
                "9876543210",
                "hashed");
    }
}
