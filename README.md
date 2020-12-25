# whmj.java_server

威海地方玩法麻将，Java 服务器端。
如果是在 IntelliJ IDEA 中运行服务器代码，需要分别启动两个服务器：
- proxyserver
- bizserver

# 启动 proxyserver
启动 proxyserver 时，需要在 IDEA 中添加以下参数：
```
--server_id=1001
--server_name=proxy_server_1001
-h 0.0.0.0
-p 20480
-c ../etc/proxyserver_all.conf.json
```

# 启动 bizserver
启动 proxyserver 时，需要在 IDEA 中添加以下参数：
```
--server_id=2001
--server_name=biz_server_2001
--server_job_type_set=PASSPORT,HALL,GAME,CLUB,CHAT,RECORD
-h 127.0.0.1
-p 40960
-c ../etc/bizserver_all.conf.json
```
