package com.dashuai.learning.rpc.model;

import java.io.Serializable;

/**
 * User
 * <p/>
 * Created in 2019.06.22
 * <p/>
 *
 * @author Liaozihong
 */
public class User implements Serializable {
    private static final long serialVersionUID = -5017352506794834362L;
    private String name;
    private String mark;


    /**
     * Builder user.
     *
     * @return the user
     */
    public static User builder() {
        return new User();
    }

    /**
     * Name user.
     *
     * @param name the name
     * @return the user
     */
    public User name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Mark user.
     *
     * @param mark the mark
     * @return the user
     */
    public User mark(String mark) {
        this.mark = mark;
        return this;
    }

    /**
     * Build user.
     *
     * @return the user
     */
    public User build() {
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }
}
