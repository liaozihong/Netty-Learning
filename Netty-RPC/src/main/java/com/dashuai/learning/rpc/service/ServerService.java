package com.dashuai.learning.rpc.service;

import com.dashuai.learning.rpc.model.User;

import java.util.List;

public interface ServerService {
    String fn(String data);

    List<String> getList(String data);

    List<User> getAllUser(User user);
}
