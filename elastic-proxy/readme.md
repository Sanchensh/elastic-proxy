启动参数，设置堆外内存大小
-Xmx1024m -XX:MaxDirectMemorySize=200m


嗅探对部署在容器里面的集群似乎无效，因为得到的数据IP都为127.0.0.1，这显然不符合要求



发送请求例子：
    POST: localhost:8081/proxy/search
    Body: 
    {
        "sql":"select * from test where 1=1"
    }