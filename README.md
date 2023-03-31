# Welcome to the SecretPathway!
The SecretPathway is a MUD client, written in Java. It features a fully
functional LPC source code editor.

## Usage
To use this client, you can either download an executable [here][1].
Make sure you have installed a Java Runtime Environment in version 15 or higher.
To run it simply open the `.jar` file.

Or you can build it from source:
- Clone: `git clone --recursive https://www.github.com/mhahnFr/SecretPathway.git && cd SecretPathway`
- Build: `./gradlew jar` *(on Windows: `gradlew.bat jar`)*
- Run: `java -jar build/libs/SecretPathway-1.0.0.jar`

## Supported protocols
Basic ANSI escape codes are supported, including **256-Bit**, **RGB** and **default** colours.

The *SecretPathwayProtocol* (**SPP**) is also supported, read its documentation in the [wiki][4].

### Final notes
For **macOS**, an optimized version is available [here][3]

This project is licensed under the terms of the GPL 3.0.

© Copyright 2022 - 2023 [mhahnFr][2]

[1]: https://github.com/mhahnFr/SecretPathway/releases
[2]: https://www.github.com/mhahnFr
[3]: https://www.github.com/mhahnFr/SecretPathway_macOS
[4]: https://www.github.com/mhahnFr/SecretPathway/wiki