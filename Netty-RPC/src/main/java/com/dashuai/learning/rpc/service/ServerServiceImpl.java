package com.dashuai.learning.rpc.service;

import com.dashuai.learning.rpc.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Server service
 * <p/>
 * Created in 2019.06.22
 * <p/>
 *
 * @author Liaozihong
 */
public class ServerServiceImpl implements com.dashuai.learning.rpc.service.ServerService {
    @Override
    public String fn(String data) {
        return "RPC invoke process " + data;
    }

    private List<String> list = new ArrayList<>();

    @Override
    public List<String> getList(String data) {
        list.add(data);
        return list;
    }

    private List<User> userList = new ArrayList<>();

    @Override
    public List<User> getAllUser(User user) {
        userList.add(user);
        return userList;
    }
}
