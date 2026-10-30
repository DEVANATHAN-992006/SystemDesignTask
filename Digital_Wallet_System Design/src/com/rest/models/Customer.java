
package com.rest.models;

import com.rest.models.enums.KycStatus;
import com.rest.models.enums.UserRole;
import com.rest.models.enums.UserStatus;

public class Customer extends User {

    public Customer(int userId, String name, String phone,
                    String email, String pin,
                    KycStatus kycStatus, UserStatus status) {

        super(userId, name, phone, email, pin,
                kycStatus, status, UserRole.CUSTOMER);
    }
}
