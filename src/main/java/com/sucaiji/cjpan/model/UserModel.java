package com.sucaiji.cjpan.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {
    private Integer id;
    private String account;
    private String password;
    private String name;
    private String role;


    public UserModel(String account, String password, String name, String role) {
        this.account = account;
        this.password = password;
        this.name = name;
        this.role = role;
    }

}
