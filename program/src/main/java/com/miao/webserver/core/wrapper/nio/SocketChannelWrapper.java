package com.miao.webserver.core.wrapper.nio;

import com.miao.webserver.core.connector.nio.poller.NioPoller;
import com.miao.webserver.core.wrapper.SocketWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

@Getter
public class SocketChannelWrapper implements SocketWrapper {

    private SocketChannel socketChannel;
    private Selector selector;
    private NioPoller poller;
    private volatile boolean isWorking; //socketChannel是否正在运行
    private volatile long waitBegin; // 进入poller中队列的时间

    public SocketChannelWrapper(SocketChannel socketChannel,
                                Selector selector, NioPoller poller) {
        this.socketChannel = socketChannel;
        this.selector = selector;
        this.poller = poller;
        this.isWorking = false;
    }

    @Override
    public void close() throws IOException {
        socketChannel.keyFor(poller.getSelector()).cancel();
        socketChannel.close();
    }

    public void setWorking(boolean working) {
        isWorking = working;
    }

    public void setWaitBegin(long waitBegin) {
        this.waitBegin = waitBegin;
    }

    @Override
    public String toString() {
        return "SocketChannelWrapper{" +
                "socketChannel=" + socketChannel +
                '}';
    }
}
