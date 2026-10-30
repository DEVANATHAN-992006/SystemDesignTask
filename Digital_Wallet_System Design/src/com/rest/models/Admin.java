
package com.rest.models;

import com.rest.models.enums.KycStatus;
import com.rest.models.enums.UserRole;
import com.rest.models.enums.UserStatus;

public class Admin extends User {

    public Admin(int userId, String name, String phone,
                 String email, String pin, UserStatus status) {

        super(userId, name, phone, email, pin,
                KycStatus.FULL, status, UserRole.ADMIN);
    }
}
 