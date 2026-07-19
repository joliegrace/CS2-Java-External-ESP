# CS2 External ESP Overlay

Just a pet project, poor feature no GUI. This using java awt to drawing buffer image frame by frame so it very weird FPS, anyway I just make it for fun.

*You will get VAC Ban immediately without -insecure flag at steam launch property because it still using Windows API to read another process memory(via JNA),*

> ->**never cheating in any multiplayer game :)** 


## Basic ESP feature

 - [x] ESP box
 - [x] Health & Armor bar
 - [x] Player name & health
 - [x] Line to box(snap line)

## Necessary library to run

add to pom.xml(if using Maven like me)

    <dependency>
        <groupId>net.java.dev.jna</groupId>
        <artifactId>jna</artifactId>
        <version>VERSION</version>
    </dependency>
    <dependency>
        <groupId>net.java.dev.jna</groupId>
        <artifactId>jna-platform</artifactId>
        <version>VERSION</version>
    </dependency>

## Demo
![Alt text](Pictures/1.png)
![Alt text](Pictures/2.png)
