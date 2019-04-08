package io.spring.httpclient.builder;

import io.spring.httpclient.model.ProxyEntity;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

public class SocksSSLConnectionSocketFactory extends SSLConnectionSocketFactory {
    public ProxyEntity proxyInfo;

    public SocksSSLConnectionSocketFactory(final SSLContext sslContext, HostnameVerifier hostnameVerifier, ProxyEntity proxyInfo) {
        super(sslContext, hostnameVerifier);
        this.proxyInfo = proxyInfo;
    }

    @Override
    public Socket createSocket(HttpContext context) throws IOException {
        InetSocketAddress socksaddr = new InetSocketAddress(proxyInfo.getHost(), proxyInfo.getPort());

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
