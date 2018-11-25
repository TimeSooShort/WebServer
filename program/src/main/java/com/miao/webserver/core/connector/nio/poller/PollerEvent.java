package com.miao.webserver.core.connector.nio.poller;

import com.miao.webserver.core.wrapper.nio.SocketChannelWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

@Slf4j
@AllArgsConstructor
public class PollerEvent {

    private SocketChannelWrapper socketWrapper;

    public void registerSocketChannel() {
        log.info("将SocketChannel注册到属于它的Poller的selector上");
        try {
            SocketChannel socketChannel = socketWrapper.getSocketChannel();
            if (socketChannel.isOpen()) {
                socketChannel.register(socketWrapper.getSelector(), SelectionKey.OP_READ, this.socketWrapper);
            } else {
                log.error("socket已经被关闭，无法注册到Poller", socketWrapper.getSocketChannel());
            }

        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }
}
