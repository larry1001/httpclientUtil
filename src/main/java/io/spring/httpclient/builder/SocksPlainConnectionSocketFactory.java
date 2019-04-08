package io.spring.httpclient.builder;

import io.spring.httpclient.model.ProxyEntity;
import org.apache.http.HttpHost;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

public class SocksPlainConnectionSocketFactory extends PlainConnectionSocketFactory {

    private ProxyEntity proxyEntity;

    public SocksPlainConnectionSocketFactory(ProxyEntity entity) {
        super();
        this.proxyEntity = entity;
    }

    @Override
    public Socket createSocket(HttpContext context) throws IOException {
        InetSocketAddress socksaddr = new InetSocketAddress(proxyEntity.getHost(), proxyEntity.getPort());
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
        Socket socket = new Socket(proxy);
        return socket;
    }

    @Override
    public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context) throws IOException {
        InetSocketAddress unresolvedRemote = InetSocketAddress.createUnresolved(host.getHostName(), remoteAddress.getPort());
        return super.connectSocket(connectTimeout, socket, host, unresolvedRemote, localAddress, context);
    }
}
