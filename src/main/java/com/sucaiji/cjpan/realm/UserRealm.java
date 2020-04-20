package com.sucaiji.cjpan.realm;

import com.sucaiji.cjpan.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.realm.Realm;
import org.springframework.beans.factory.annotation.Autowired;

public class UserRealm implements Realm {
    @Autowired
    private UserService userService;

    @Override
    public String getName() {
        return "userrealm";
    }

    @Override
    public boolean supports(AuthenticationToken authenticationToken) {
        return true;
    }

    @Override
    public AuthenticationInfo getAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String account = (String) authenticationToken.getPrincipal();
        String password = new String((char[])authenticationToken.getCredentials());
        if(!userService.login(account,password)){
            throw new IncorrectCredentialsException();
        }
        return new SimpleAuthenticationInfo(account,password,getName());
    }
}
