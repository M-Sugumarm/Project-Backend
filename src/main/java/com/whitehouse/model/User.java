package com.whitehouse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String clerkId;
    private String email;
    private String name;
    private Boolean isAdmin = false;
    private Boolean emailSubscribed = false;
}
