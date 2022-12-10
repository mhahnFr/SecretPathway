# Welcome to the SecretPathway!
The SecretPathway is a MUD client, written in Java.

## Usage
To use this client, you can either download an executable [here](https://github.com/mhahnFr/SecretPathway/releases).
Make sure you have installed a Java Runtime Environment in version 15 or higher.
To run it simply open the `.jar` file.

Or you can build it from source:
- Clone: `git clone --recursive https://www.github.com/mhahnFr/SecretPathway.git`
- Build: `javac -sourcepath src/:JUtilities/ src/mhahnFr/SecretPathway/CLI.java`

To run it from the commandline: `java -cp src/:JUtilites/ mhahnFr.SecretPathway.CLI`

## Supported protocols
Basic ANSI escape codes are supported at the moment, including RGB colours.

The *SecretPathwayProtocol* (**SPP**) will be added soon.

### Final notes
The optimized **macOS** version can be found [here](https://www.github.com/mhahnFr/SecretPathway_macOS).

This project is licensed under the terms of the GPL 3.0.

© Copyright 2022 [mhahnFr](https://www.github.com/mhahnFr)
