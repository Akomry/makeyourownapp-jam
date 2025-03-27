# Make your own app! - Yet Another Collaborative Whiteboard App

## What is it ?
@akomry's submission to `Make your own app!` game jam.

Basically, this is an online drawing app. It only has the most basic tools for now, pen and eraser with custom color, but I hope to be able to add more.

It's entirely written in JavaFX and uses TCP/IP protocol for communication. Any client is able to host, by default on port 8090. Once you've hosted, you might have to port forward (cf. nat forwarding on your box), but then it's self-hosted: no distant server is required to host!

For now, anyone can join a whiteboard, but i plan to add a basic authentication system.

Then, you can just create a new canvas and start drawing!


## Dependencies
Should be standalone, but it's possible you neew **[Java 23](https://adoptium.net/temurin/releases/?version=23)** up and running.


## Installation guide
It will depend on if we manage to generate artifacts using the Maven CI/CD workflows, but you'll probably have to 
install a packaged zipped file. Then execute `[extract dir]/bin/app`.


## -Roadmap-
- [x] Brainstorming
- [x] Find a name
- [x] List issues
- [x] Experiment with javafx Canvas
- [x] Implement Canvas creation
- [x] Implement TCP/IP server/client
- [x] Implement event callback
- [x] Implement brush, its size and color
- [x] Implement eraser
- [ ] Implement zoom control (WIP, sketchy zoom)
- [ ] Implement layering system

## License
TBD


## Contributors
* @akomry
