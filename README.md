# HttpMock
An visual tool for mocking and proxying http requests

![Main window of the HttpMock](docs/images/httpmock.png?raw=true)

# Preparing the Environment
Include following lines in hosts archive:<br/>
__127.0.0.1 localhost.estatico.com.br<br/>
  127.0.0.1 localhost.dinamico.com.br<br/>
  127.0.0.1 offline.estatico.com.br<br/>
  127.0.0.1 offline.dinamico.com.br<br/>
  127.0.0.1 estatico.com.br<br/>
  127.0.0.1 dinamico.com.br__<br/>
Import the project in Eclipse IDE 
  
# 1.1 Running Online mode showcase at Eclipse IDE:
1. Click on Run Configurations...
2. Right click on Java Application
3. New Configuration
4. Type "HttpMock (ONLINE)" in "name" field
5. Type httpmock in "Project"
6. Select br.com.httpmock.Main in "Main class" field
7. Click on Arguments
8. Type --config online.conf
9. Apply
10. Run
11. Access http://localhost.dinamico.com to view the google page proxied and to get offline stubs
12. Stop tool clicking on "X"

# 1.2 Running Offline mode showcase at Eclipse IDE:
1. Run Online mode showcase at once
2. Click on Run Configurations...
3. Right click on Java Application
4. New Configuration
5. Type "HttpMock (OFFLINE)" in "name" field
6. Type httpmock in "Project"
7. Select br.com.httpmock.Main in "Main class" field
8. Click on Arguments
9. Type --config offline.conf
10. Apply
11. Run
12. Access http://localhost.dinamico.com to view the google page proxied in OFFLINE mode (without accessing the network)
13. Stop tool clicking on "X"

# Features availables on config file

| KEYWORD | DESCRIPTION | EXAMPLE | ONLINE MODE | OFFLINE MODE |
|:---:|:---:|:---:|:---:|:---:|
| VirtualServer | Name of the virtual server that will be mapped on the local machine to serve http/https requests | https://localhost.com.br:443 | X **(required)** | X **(required)** |
| ProxyPassAndReverse | Origin path and target serverName mapping (with path and separated by a space) in order to proxy and proxy reverse urls. When this keyword has any value automatically the tool changes to ONLINE MODE. Optionally, including the keywords ONLINE, OFFLINE or ONLINE,RECORDING it changes de operating mode of this mapping | ```/abc https://localhost.com.br:443/abc/def``` 

or

```/abc https://localhost.com.br:443/abc/def OFFLINE```

or

```/abc https://localhost.com.br:443/abc/def ONLINE```

or

```/abc https://localhost.com.br:443/abc/def ONLINE,RECORDING``` | X **(required)** |   |
| RecordingDirectory | Relative or absolute path of directory to use to write the network traffic file stubs when in ONLINE MODE or to read network traffic file stubs when in OFFLINE MODE | /servidor/http_www.google.com.br | X | X **(required)** |
| PreserveHostHeader | Preserves host header when proxying the http request to the target server | /servidor/http_www.google.com.br | X |   |
| HttpsKeystore | Relative or absolute filename path where to find ssl private key for https server protocol | /servidor/private_cert.pkcs12 | X | X |
| KeystorePassword | Password for keystore file | private_cert_secret | X | X |
| HttpsTruststore | Relative or absolute filename path where to find ssl public keys to trust client connections when serving https connections | /servidor/clients_certs.pkcs12 | X |   |
| TruststorePassword | Password for truststore file | clients_certs_secret | X |   |
| HttpsRequireClientCert | Indicates that the tool will require client ssl connection cert to serve https requests |   | X |   |
| OfflineMatchHeaders | Comma separated list of header names to match when in OFFLINE MODE (default: ignore header match) | Host,Content-Type |   | X |
| OfflineIgnoreParameters | Comma separated list of names to ignore match when in query parameters, body parameters and json property keys, or the keyword ALL to ignore this matching filter (default: ALL, match all query parameters, body parameteres and json property keys) | ALL or parameter1,parameter2,parameter3 |   | X |
| OfflineCyclicResponses | When enabled, a request that have one or more response stubs associateds are served cyclically |   |   | X |
| OfflineIgnoreHttpStatus | Comma separated list of http statuses in recorded stubs not to serve in http requests OFFLINE MODE | 300,301,302,304,400,500 |   | X |

# Contributing
We welcome bug fixes and new features in the form of pull requests.
If you'd like to contribute, you are welcome too.
