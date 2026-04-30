package org.example.backend.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class UsersRest {
    @Autowired
    private UsersService usersService;


}
