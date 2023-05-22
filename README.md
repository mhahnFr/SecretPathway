# Welcome to the SecretPathway!
The SecretPathway is a MUD client, written in Java. It features a fully
functional LPC source code editor.

### Screenshots
<p align="center">
    <picture>
        <source srcset="https://raw.githubusercontent.com/mhahnFr/SecretPathway/main/screenshots/main_view-light.png" media="(prefers-color-scheme: light), (prefers-color-scheme: no-preference)" />
        <source srcset="https://raw.githubusercontent.com/mhahnFr/SecretPathway/main/screenshots/main_view-dark.png" media="(prefers-color-scheme: dark)" />
        <img src="https://raw.githubusercontent.com/mhahnFr/SecretPathway/main/screenshots/main_view-light.png" alt="Main View" />
    </picture>
    <picture>
        <source srcset="https://raw.githubusercontent.com/mhahnFr/SecretPathway/main/screenshots/editor-light.png" media="(prefers-color-scheme: light), (prefers-color-scheme: no-preference)" />
        <source srcset="https://raw.githubusercontent.com/mhahnFr/SecretPathway/main/screenshots/editor-dark.png" media="(prefers-color-scheme: dark)" />
        <img src="https://raw.githubusercontent.com/mhahnFr/SecretPathway/main/screenshots/editor-light.png" alt="Editor" />
    </picture>
</p>

## Usage
To use this client, you can download an executable or an installation image [here][1].

To use a `.jar` executable, make sure you have installed a Java Runtime Environment
in version 17 or higher.

You can also build it from source:
- Clone: `git clone --recursive https://www.github.com/mhahnFr/SecretPathway.git && cd SecretPathway`
- Build: `./gradlew jar` *(on Windows: `gradlew.bat jar`)*
- Run: `java -jar build/libs/SecretPathway-1.0.0.jar`

You can also create an installation image for your operating system by running
`./gradlew deploy` *(on Windows: `gradlew.bat deploy`)*.

## Supported protocols
Basic ANSI escape codes are supported, including **256-Bit**, **RGB** and **default** colours.

The *SecretPathwayProtocol* (**SPP**) is also supported, read its documentation in the [wiki][4].

### Final notes
For **macOS**, an optimized version is available [here][3].

This project is licensed under the terms of the GPL 3.0.

© Copyright 2022 - 2023 [mhahnFr][2]

[1]: https://github.com/mhahnFr/SecretPathway/releases
[2]: https://www.github.com/mhahnFr
[3]: https://www.github.com/mhahnFr/SecretPathway_macOS
[4]: https://www.github.com/mhahnFr/SecretPathway/wiki