ckan4j
======
A java library to extend and access core functionalities of CKAN.

Why a CKAN java library
-----------------------
[CKAN]() is a complete and performant open-source Data Management System (DMS) built in
Python by a quite large community behind the Open Knowledge Foundation.
It powers many data catalogs across the globe including datahub.io,
catalog.data.gov and data.gov.uk among many other sites.
CKAN provide a well designed and powerful RPC-style API that expose
pratically all its core functionalities but when it comes to integration
with Java based or enterprise scale applications it present some lack of support. 
So, when we built the opendatahub.it federated catalog we decided to create a
Java based client library to ease such integration and to decouple our portal and
data harvester architecture (based on Java tecnologies like Hadoop and Storm)

CKAN4J is the core library of such work released as open-source so that many
other can benefit from it


Features
--------
This is the first Java client library that provide access to the CKAN
core functionalities and APIs using Java language.
It also contains extensions to the CKAN API to perform common operations.
In particular it includes the following features that are not part of CKAN:

* CKAN dataset and organizations statistics exposed as API
* Complete Dataset rating mechanism
* Social Login (supported GitHub, Google+ and Facebook).
  It include capabilities to implement SSO with many other portal software 


Contribute
----------
SciamLab is maintaining the library but we are happy to consider your feedbacks
and contributions and we want this to be the result of a community effort.
To partecipate you can simply fork the repo and follow the
typical GitHub flow considering that every pull request must be associated with
an issue on GitHub
You can collaborate with [code](https://github.com/sciamlab/ckan4j/pulls) or
[ideas and bugs](https://github.com/sciamlab/ckan4j/issues)


License
-------

    Copyright 2014 Sciamlab s.r.l.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

