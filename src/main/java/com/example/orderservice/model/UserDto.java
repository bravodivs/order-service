package com.example.orderservice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {

    private UUID userId;

    private String username;

    private String password;

    private String email;

    private List<String> role;

    private String mobileNumber;

    private Address address;

    private String lastUpdatedBy;

    private Date createdAt;

    private Date modifiedAt;

    private Boolean isEnabled = Boolean.TRUE;

    public UserDto(String username, String password, String email, List<String> role, String mobileNumber, Address address) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.mobileNumber = mobileNumber;
        this.address = address;
    }

    @Override
    public String toString() {
        return "UserDto{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", address='" + address + '\'' +
                ", lastUpdatedBy='" + lastUpdatedBy + '\'' +
                ", createdAt=" + createdAt +
                ", modifiedAt=" + modifiedAt +
                ", isEnabled=" + isEnabled +
                '}';
    }
}
