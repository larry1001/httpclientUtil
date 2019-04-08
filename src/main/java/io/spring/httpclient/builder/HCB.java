package io.spring.httpclient.builder;

import io.spring.httpclient.common.SSLs;
import io.spring.httpclient.common.SSLs.SSLProtocolVersion;
import io.spring.httpclient.exception.HttpProcessException;
import io.spring.httpclient.model.ProxyEntity;
import io.spring.httpclient.model.ProxyType;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class HCB extends HttpClientBuilder {

    /**
     * 记录是否设置了连接池
     */
    public boolean isSetPool = false;
    /**
     * ssl 协议版本
     */
    private SSLProtocolVersion sslpv = SSLProtocolVersion.SSLv3;

    /**
     * 用于配置ssl
     */
    private SSLs ssls = SSLs.getInstance();

    private HCB() {

    }

    public static HCB custom() {
        return new HCB();
    }

    /**
     * 设置超时时间
     *
     * @param timeout 超市时间，单位-毫秒
     * @return 返回当前对象
     */
    public HCB timeout(int timeout) {
        return timeout(timeout, true);
    }

    /**
     * 设置超时时间以及是否允许网页重定向（自动跳转 302）
     *
     * @param timeout        超时时间，单位-毫秒
     * @param redirectEnable 自动跳转
     * @return 返回当前对象
     */
    public HCB timeout(int timeout, boolean redirectEnable) {
        // 配置请求的超时设置
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(timeout)
                .setConnectTimeout(timeout)
                .setSocketTimeout(timeout)
                .setRedirectsEnabled(redirectEnable)
                .build();
        return (HCB) this.setDefaultRequestConfig(config);
    }

    /**
     * 设置ssl安全链接
     *
     * @return 返回当前对象
     * @throws HttpProcessException http处理异常
     */
    public HCB ssl() throws HttpProcessException {
        return (HCB) this.setSSLSocketFactory(ssls.getSSLCONNSF(sslpv));
    }


    /**
     * 设置自定义sslcontext
     *
     * @param keyStorePath 密钥库路径
     * @return 返回当前对象
     * @throws HttpProcessException http处理异常
     */
    public HCB ssl(String keyStorePath) throws HttpProcessException {
        return ssl(keyStorePath, "nopassword");
    }

    /**
     * 设置自定义sslcontext
     *
     * @param keyStorePath 密钥库路径
     * @param keyStorepass 密钥库密码
     * @return 返回当前对象
     * @throws HttpProcessException http处理异常
     */
    public HCB ssl(String keyStorePath, String keyStorepass) throws HttpProcessException {
        this.ssls = SSLs.custom().customSSL(keyStorePath, keyStorepass);
        return ssl();
    }


    /**
     * 设置连接池（默认开启https）
     *
     * @param maxTotal 最大连接数
     * @return 返回当前对象
     * @throws HttpProcessException http处理异常
     */
    public HCB pool(int maxTotal) throws HttpProcessException {
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                .<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", ssls.getSSLCONNSF(sslpv)).build();
        //设置连接池大小
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        connManager.setMaxTotal(maxTotal);
        isSetPool = true;
        return (HCB) this.setConnectionManager(connManager);
    }

    /**
     * 设置连接池（默认开启https）
     *
     * @param maxTotal           最大连接数
     * @param defaultMaxPerRoute 每个路由默认连接数
     * @return 返回当前对象
     * @throws HttpProcessException http处理异常
     */
    public HCB pool(int maxTotal, int defaultMaxPerRoute) throws HttpProcessException {
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                .<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", ssls.getSSLCONNSF(sslpv)).build();
        //设置连接池大小
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        connManager.setMaxTotal(maxTotal);
        connManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
        isSetPool = true;
        return (HCB) this.setConnectionManager(connManager);
    }

    /**
     * 设置代理
     *
     * @param hostOrIP 代理host或者ip
     * @param port     代理端口
     * @return 返回当前对象
     */
    public HCB proxy(String hostOrIP, int port) {
        // 依次是代理地址，代理端口号，协议类型
        HttpHost proxy = new HttpHost(hostOrIP, port);
        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
        return (HCB) this.setRoutePlanner(routePlanner);
    }

    /**
     * 重试（如果请求是幂等的，就再次尝试）
     *
     * @param tryTimes 重试次数
     * @return 返回当前对象
     */
    public HCB retry(final int tryTimes) {
        return retry(tryTimes, false);
    }

    /**
     * 重试（如果请求是幂等的，就再次尝试）
     *
     * @param tryTimes               重试次数
     * @param retryWhenInterruptedIO 连接拒绝时，是否重试
     * @return 返回当前对象
     */
    public HCB retry(final int tryTimes, final boolean retryWhenInterruptedIO) {
        // 请求重试处理
        HttpRequestRetryHandler httpRequestRetryHandler = (exception, executionCount, context) -> {
            /**
             * 如果已经重试了n次，就放弃
             */
            if (executionCount >= tryTimes) {
                return false;
            }
            /**
             * 如果服务器丢掉了连接，那么就重试
             */
            if (exception instanceof NoHttpResponseException) {
                return true;
            }
            /**
             * 不要重试SSL握手异常
             */
            if (exception instanceof SSLHandshakeException) {
                return false;
            }
            /**
             * 超时
             */
            if (exception instanceof InterruptedIOException) {
                //return false;
                return retryWhenInterruptedIO;
            }
            /**
             * 目标服务器不可达
             */
            if (exception instanceof UnknownHostException) {
                return true;
            }
            /**
             * 连接被拒绝
             */
            if (exception instanceof ConnectTimeoutException) {
                return false;
            }
            /**
             * SSL握手异常
             */
            if (exception instanceof SSLException) {
                return false;
            }

            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            // 如果请求是幂等的，就再次尝试
            if (!(request instanceof HttpEntityEnclosingRequest)) {
                return true;
            }
            return false;
        };
        this.setRetryHandler(httpRequestRetryHandler);
        return this;
    }

    /**
     * 设置代理
     * @param proxyEntity
     * @param type
     * @return
     */
    public HCB proxy(ProxyEntity proxyEntity, ProxyType type) {
        SSLContext sslContext = null;
        try {
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        PoolingHttpClientConnectionManager connManager;
        switch (type) {
            case NONE:
                return this;
            case HTTP:
                Registry<ConnectionSocketFactory> httpFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.INSTANCE)
                        .register("https", new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
                        .build();

                //设置连接池大小
                connManager = new PoolingHttpClientConnectionManager(httpFactoryRegistry);
                connManager.setMaxTotal(200);
//                connManager.setDefaultMaxPerRoute(50);
                AuthScope authScope = new AuthScope(proxyEntity.getHost(), proxyEntity.getPort());
                Credentials credentials = new UsernamePasswordCredentials(proxyEntity.getUsername(), proxyEntity.getPassword());
                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(authScope, credentials);

                HttpHost proxy = new HttpHost(proxyEntity.getHost(), proxyEntity.getPort());
                return (HCB) this.setConnectionManager(connManager).setProxy(proxy).setDefaultCredentialsProvider(credsProvider);
            case SOCKS5:
                Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", new SocksPlainConnectionSocketFactory(proxyEntity))
                        .register("https", new SocksSSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE, proxyEntity))
                        .build();
                //设置连接池大小
                connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
                connManager.setMaxTotal(200);
                this.setConnectionManager(connManager);
                if (StringUtils.isNotBlank(proxyEntity.getUsername())) {
                    /**
                     * 设置授权信息
                     */
                    ThreadLocalProxyAuthenticator.getInstance().setCredentials(proxyEntity.getUsername(), proxyEntity.getPassword());
                }
                return this;
            default:
                return this;
        }
    }

    /**
     * 设置ssl版本<br>
     * 如果您想要设置ssl版本，必须<b><span style="color:red">先调用此方法，再调用ssl方法</span><br>
     * 仅支持 SSLv3，TSLv1，TSLv1.1，TSLv1.2</b>
     *
     * @param sslpv 版本号
     * @return 返回当前对象
     */
    public HCB sslpv(String sslpv) {
        return sslpv(SSLProtocolVersion.find(sslpv));
    }

    /**
     * 设置ssl版本<br>
     * 如果您想要设置ssl版本，必须<b>先调用此方法，再调用ssl方法<br>
     * 仅支持 SSLv3，TSLv1，TSLv1.1，TSLv1.2</b>
     *
     * @param sslpv 版本号
     * @return 返回当前对象
     */
    public HCB sslpv(SSLProtocolVersion sslpv) {
        this.sslpv = sslpv;
        return this;
    }
}