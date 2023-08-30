[![PIA logo][pia-image]][pia-url]

# Private Internet Access

Private Internet Access is the world's leading consumer VPN service. At Private Internet Access we believe in unfettered access for all, and as a firm supporter of the open source ecosystem we have made the decision to open source our VPN clients. For more information about the PIA service, please visit our website [privateinternetaccess.com][pia-url] or check out the [Wiki][pia-wiki].

# Account common library for Android and Apple platforms

With this library, clients from iOS and Android can communicate easily with the Private Internet Access account's services.

## Installation

### Requirements
 - Git (latest)
 - Xcode (latest)
 - Android Studio (latest)
 - Gradle (latest)
 - ADB installed
 - NDK (latest)
 - Android 4.1+

#### Download Codebase
Using the terminal:

`git clone https://github.com/pia-foss/mobile-common-account.git *folder-name*`

type in what folder you want to put in without the **

#### Building

Once the project is cloned, you can build the binaries by running the tasks `./gradlew bundleDebugAar` or `./gradlew bundleReleaseAar` for Android. And, `./gradlew assembleAccountDebugXCFramework` or `./gradlew assembleAccountReleaseXCFramework` for iOS. You can find the binaries at `[PROJECT_DIR]/account/build/outputs/aar` and `[PROJECT_DIR]/account/build/XCFrameworks` accordingly.

## Usage

### Android 

To use this project in Android, you can run the task `./gradlew publishAndroidReleasePublicationToMavenLocal`. This will publish the package to your maven local (Make sure to have included `mavenLocal()` as part of your gradle repositories). Once successful, you can set the dependency as per any other package, e.g.:
```
implementation("com.kape.android:account:[version_number]")
```
where `[version_number]` is the version as set in `account/build.gradle.kts`.

### iOS

To use this project in iOS, once you have built `account.xcframework`. You can go to your project target. Build Phases. Link Binary With Libraries. (or alternatively drag the file there and skip the rest) Click the `+`. Add Other. Add Files. And look for `account.xcframework`.

## Documentation

#### Architecture

The library is formed by two layers. The common layer. Containing the business logic for all platforms. And, the bridging layer. Containing the platform specific logic being injected into the common layer.

Code structure via packages:

* `commonMain` - Common business logic.
* `androidMain` - Android's bridging layer, providing the platform specific dependencies.
* `iosMain` - iOS's bridging layer, providing the platform specific dependencies.

#### Significant Classes and Interfaces

* `AccountBuilder` - Public builder class responsible for creating an instance of an object conforming to either the `IOSAccountAPI` or `AndroidAccountAPI` interface for the client side.
* `AccountAPI` - Public interfaces defining the API to be offered by the library to the clients.
* `AccountHttpClient` - Class defining the certificate pinning logic on each platform.

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

[pia-image]: https://assets-cms.privateinternetaccess.com/img/frontend/pia_menu_logo_light.svg
[pia-url]: https://www.privateinternetaccess.com/
[pia-wiki]: https://en.wikipedia.org/wiki/Private_Internet_Access
