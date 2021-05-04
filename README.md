# Twandy

[![Source Code](https://img.shields.io/badge/code-github-%23211F1F.svg)](https://github.com/esotericpig/twandy)
[![License](https://img.shields.io/github/license/esotericpig/twandy.svg)](COPYING.LESSER)

Twitch bot for playing games.

## Setup

Install **Java 16+** with [AdoptOpenJDK](https://adoptopenjdk.net), IntelliJ IDEA, or [SDKMAN!](https://sdkman.io).

(See commented-out code in [app/build.gradle](app/build.gradle) for automatically downloading this version of Java using Gradle.)

Build the project (use `./gradlew.bat` for Windows):

```
$ ./gradlew build
```

Run the app, using one of these methods:

```
## 1) Use the Jar.
$ java -jar app/build/libs/twandy.jar

## 2) Use the script.
$ app/build/install/twandy/bin/twandy
$ app/build/install/twandy/bin/twandy.bat

## 3) Use Gradle, but slow.
$ ./gradlew run
```

## Usage

Replace *twandy* with one of the run methods from the *Setup* section.

```
$ twandy --help
```

TODO: user/pass

TODO: play Solarus; download and run DX file

TODO: play lichess

TODO: run fhat

## License

[GNU LGPL v3+](COPYING.LESSER)

> Twandy [<https://github.com/esotericpig/twandy>]  
> Copyright (c) 2021 Jonathan Bradley Whited  
>
> Twandy is free software: you can redistribute it and/or modify  
> it under the terms of the GNU Lesser General Public License as published by  
> the Free Software Foundation, either version 3 of the License, or  
> (at your option) any later version.  
>
> Twandy is distributed in the hope that it will be useful,  
> but WITHOUT ANY WARRANTY; without even the implied warranty of  
> MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the  
> GNU Lesser General Public License for more details.  
>
> You should have received a copy of the GNU Lesser General Public License  
> along with Twandy. If not, see <https://www.gnu.org/licenses/>.  
