package com.sucaiji.cjpan.model;

public class User {
    private Integer id;
    private String account;
    private String password;
    private String name;
    private String role;

    public User() {
    }

    public User(String account, String password, String name, String role) {
        this.account = account;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public User(Integer id, String account, String password, String name, String role) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
