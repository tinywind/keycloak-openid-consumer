# keycloak-openid-test-consumer-app

# How to Play: Method 1
* Install maven3, jdk1.8
* compile command: `$ mvn compile`
* run command: 
```
mvn compile exec:java \ 
        -Dfile.encoding=utf-8 \
        -Dservice.domain=http://localhost:8000 \
        -Dkeycloak.domain=http://{keycloak ip}/auth \
        -Dkeycloak.realm={realm} \
        -Dkeycloak.client.id={client id} \
        -Dkeycloak.client.secret={client secret} \
        [-Dexec.args="--server.port={http listening port}"]
```

# LICENSE
**Licensed under the MIT**
