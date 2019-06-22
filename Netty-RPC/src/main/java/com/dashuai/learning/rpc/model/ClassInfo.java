package com.dashuai.learning.rpc.model;

import java.io.Serializable;

/**
 * Class info
 * <p/>
 * Created in 2019.06.22
 * <p/>
 *
 * @author Liaozihong
 */
public class ClassInfo implements Serializable {

    private static final long serialVersionUID = 8193170354562066641L;

    /**
     * 类名
     */
    private String className;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 参数类型
     */
    private Class<?>[] types;
    /**
     * 参数列表
     */
    private Object[] objects;

    /**
     * Gets class name.
     *
     * @return the class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets class name.
     *
     * @param className the class name
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Gets method name.
     *
     * @return the method name
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets method name.
     *
     * @param methodName the method name
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Get types class [ ].
     *
     * @return the class [ ]
     */
    public Class<?>[] getTypes() {
        return types;
    }

    /**
     * Sets types.
     *
     * @param types the types
     */
    public void setTypes(Class<?>[] types) {
        this.types = types;
    }

    /**
     * Get objects object [ ].
     *
     * @return the object [ ]
     */
    public Object[] getObjects() {
        return objects;
    }

    /**
     * Sets objects.
     *
     * @param objects the objects
     */
    public void setObjects(Object[] objects) {
        this.objects = objects;
    }
}
