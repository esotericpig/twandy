# Twandy

[![Source Code](https://img.shields.io/badge/code-github-%23211F1F.svg)](https://github.com/esotericpig/twandy)
[![License](https://img.shields.io/github/license/esotericpig/twandy.svg)](COPYING.LESSER)

Twitch bot for playing games.

## Setup

Install **Java 17+** from [Adoptium](https://adoptium.net) or with [SDKMAN!](https://sdkman.io) or whatever.

TODO: Download Release and run.

## Build

Install **Java 17+** and **Gradle v8.10+** with [SDKMAN!](https://sdkman.io) or whatever.

The root-level Bash script, [twandy](twandy), will build the Jar (as fast as possible) if it doesn't exist, and then run it. If the Jar already exists, it will not rebuild it.

```
$ ./twandy
$ ./twandy --help
```

Or build manually (use `./gradlew.bat` on Windows):

```
$ ./gradlew build
```

The `twandy` script uses `./gradlew` if it exists, else it uses `gradle`. So if you don't want `twandy` to use `./gradlew`, just delete `./gradlew`.

## Run

Run the app using one of these methods:

```
## 0) Use the top-level script.
$ ./twandy

## 1) Use the Jar.
$ java -jar app/build/libs/twandy.jar

## 2) Use one of the build scripts.
$ app/build/install/twandy/bin/twandy
$ app/build/install/twandy/bin/twandy.bat

## 3) Use Gradle.
$ ./gradlew run
```

## Usage

These examples use the `twandy` script, but any of the other running methods will also work with these same options/commands.

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
> Copyright (c) 2021 Bradley Whited  
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
