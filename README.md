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
12. Access http://localhost.dinamico.com to view the google page proxied
13. Stop tool clicking on "X"

# Contributing
We welcome bug fixes and new features in the form of pull requests.
If you'd like to contribute, you are welcome too.
