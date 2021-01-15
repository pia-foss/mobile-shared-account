[![PIA logo][pia-image]][pia-url]

# Private Internet Access

Private Internet Access is the world's leading consumer VPN service. At Private Internet Access we believe in unfettered access for all, and as a firm supporter of the open source ecosystem we have made the decision to open source our VPN clients. For more information about the PIA service, please visit our website [privateinternetaccess.com][pia-url] or check out the [Wiki][pia-wiki].

# Regions common library for Android and Apple platforms

With this library, clients from iOS and Android can communicate easily with the Private Internet Access region's services.

## Installation

### Requirements
 - Git (latest)
 - Xcode (latest)
 - IntelliJ IDEA (latest)
 - Gradle (latest)
 - ADB installed
 - NDK (latest)
 - Android 4.1+
 - Cocoapods

#### Download Codebase
Using the terminal:

`git clone https://github.com/pia-foss/mobile-common-regions.git *folder-name*`

type in what folder you want to put in without the **

#### Building

Once the project is cloned, you can build the binaries by running `./gradlew bundleDebugAar` or `./gradlew bundleReleaseAar` for Android. And, `./gradlew iOSBinaries` for iOS. You can find the binaries at `[PROJECT_DIR]/regions/build/outputs/aar` and `[PROJECT_DIR]/regions/build/bin/iOS` accordingly

## Usage

### Android 

To use this project in your Android apps, you need to import the generated AAR module and include the following dependencies in your application's gradle. See the project's `build.gradle` for the specific versions.

`
implementation 'io.ktor:ktor-client-okhttp:x.x.x'
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:x.x.x'
implementation 'org.jetbrains.kotlinx:kotlinx-serialization-core:x.x.x'
`

### iOS

To use this project in your iOS apps, just add the library as a pod

`pod "PIARegions", :git => "http://github.com/pia-foss/mobile-common-regions`

After the pod install is completed, when you run your app, the PIARegions pod will generate the `Regions.framework`.

### Add new classes or change iOS project structure

When adding new classes or if you need to change the project structure of the `PIARegions` module you will need to update the `PIARegions.podspec` file. This file is located in the root path of the project.

## Documentation

#### Architecture

The library is formed by two layers. The common layer. Containing the business logic for all platforms. And, the bridging layer. Containing the platform specific logic being injected into the common layer.

Code structure via packages:

* `commonMain` - Common business logic.
* `main` - Android's bridging layer, providing the platform specific dependencies.
* `iosApp` - iOS's bridging layer, providing the platform specific dependencies.

#### Significant Classes and Interfaces

* `RegionsBuilder` - Public builder class responsible for creating an instance of an object conforming to the `RegionsAPI` interface for the client side.
* `RegionsCommonBuilder` - Internal builder class responsible for creating an instance of an object conforming to the `RegionsAPI` interface and injecting the platform specific dependencies.
* `RegionsAPI` - Public interface defining the API to be offered by the library to the clients.
* `RegionsResponse` - Public data class representing the serialized data from our regions service.
* `MessageVerificationHandler` - Handler conforming to the interface `MessageVerificator` and providing the defined capabilities to the common logic.
* `PingRequestHandler` - Handler conforming to the interface `PingRequest` and providing the defined capabilities to the common logic.

## Contributing

By contributing to this project you are agreeing to the terms stated in the Contributor License Agreement (CLA) [here](/CLA.rst).

For more details please see [CONTRIBUTING](/CONTRIBUTING.md).

Issues and Pull Requests should use these templates: [ISSUE](/.github/ISSUE_TEMPLATE.md) and [PULL REQUEST](/.github/PULL_REQUEST_TEMPLATE.md).

## Authors

- Jose Blaya - [ueshiba](https://github.com/ueshiba)
- Juan Docal - [tatostao](https://github.com/tatostao) 

## License

This project is licensed under the [MIT (Expat) license](https://choosealicense.com/licenses/mit/), which can be found [here](/LICENSE).

## Acknowledgements

- Ktor - © 2020 (http://ktor.io)

[pia-image]: https://www.privateinternetaccess.com/assets/PIALogo2x-0d1e1094ac909ea4c93df06e2da3db4ee8a73d8b2770f0f7d768a8603c62a82f.png
[pia-url]: https://www.privateinternetaccess.com/
[pia-wiki]: https://en.wikipedia.org/wiki/Private_Internet_Access
