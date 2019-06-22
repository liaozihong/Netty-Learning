package com.dashuai.learning.rpc.server.handler;

import com.dashuai.commons.utils.JasksonUtils;
import com.dashuai.learning.rpc.model.ClassInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Invoke handler
 * <p/>
 * Created in 2019.06.22
 * <p/>
 *
 * @author Liaozihong
 */
public class InvokeHandler extends ChannelInboundHandlerAdapter {

    /**
     * The constant classMap.
     */
    public static ConcurrentHashMap<String, Object> classMap = new ConcurrentHashMap<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ClassInfo classInfo = (ClassInfo) msg;
        Object clazz = null;
        if (!classMap.containsKey(classInfo.getClassName())) {
            try {
                clazz = Class.forName(classInfo.getClassName()).getDeclaredConstructor().newInstance();
                classMap.put(classInfo.getClassName(), clazz);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            clazz = classMap.get(classInfo.getClassName());
        }
        assert clazz != null;
        Method method = clazz.getClass().getMethod(classInfo.getMethodName(), classInfo.getTypes());
        Object result = method.invoke(clazz, classInfo.getObjects());
        System.out.println("服务器接受客户端的数据:" + JasksonUtils.toJson(result));
        ctx.write(result);
        ctx.flush();
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
