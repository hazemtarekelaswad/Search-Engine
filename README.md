# Search Engine
A crawler-based search engine, consists of six main components that are crawler, indexer, search index, ranker, query processor, and an Android application for UI support.

## Technologies and Tools
- Java
- Apache Spark
- MongoDB
- Flutter

## Main Structure

```
Search-Engine   
│
└───apt_project
│   
└───Crawler
|   │   WebCrawler.java
|   │   Crawler_Main.java
|   |   seedlist.txt
|   └── output.txt
│   
└───Backend
    └──  src/main/java
         |   Indexer.java
         |   Ranker.java
         |   Server.java
         |   PageInfo.java
         |   PageJson.java
         |   WordInfo.java
         └── Utility.java
```

## Setup
Firstly, clone this repository
```sh
git clone --recurse-submodules https://github.com/hazemtarekelaswad/Search-Engine.git
cd Search-Engine
```
Then, build the project using Maven
```sh
mvn clean install
```
and you will see `./target` be added to the project main directory
> *If you don't have Maven installed in your machine, click [here](https://maven.apache.org/install.html) and follow the steps.*

At this point, you have four main components to run


1. **Crawler**
```sh
java -cp target/SearchEngine-1.0.jar Crawler
```
2. **Indexer**
```sh
java -cp target/SearchEngine-1.0.jar Indexer
```
3. **Server**
```sh
java -cp target/SearchEngine-1.0.jar Server
```
4. **Android Application**

Build
```sh
dart pub get
flutter pub get
flutter pub outdated
flutter pub upgrade
```
Then run
```sh
flutter run lib/main.dart
```

>*Please, check out the collaborators and contributers sections to get to know these awesome people who helped developing this project. 💪🏽👏🏾*
