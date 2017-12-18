package com.sucaiji.cjpan.entity;

public class User {
    private Integer id;
    private String count;
    private String password;
    private String name;
    private String role;

    public User() {
    }

    public User(String count, String password, String name, String role) {
        this.count = count;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public User(Integer id, String count, String password, String name, String role) {
        this.id = id;
        this.count = count;
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

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
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
