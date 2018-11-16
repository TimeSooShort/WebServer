package com.miao.webserver.core.wrapper.bio;

import com.miao.webserver.core.wrapper.SocketWrapper;
import lombok.Getter;

import java.io.IOException;
import java.net.Socket;

/**
 * Bio的socket包装类，只有一个方法close，关闭socket
 */
@Getter
public class BioSocketWrapper implements SocketWrapper {

    private Socket socket;

    public BioSocketWrapper(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
