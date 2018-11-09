package koeko.Networking

class DeviceInfo {
    var uniqueId = ""
    var os = ""
    var sdkLevel = 0
    var ble = false
    var googleServicesVersion = 0L

    constructor() {
        this.uniqueId = ""
        this.os = ""
        this.sdkLevel = 0
        this.ble = false
        this.googleServicesVersion = 0L
    }

    constructor(uid: String, os: String, sdkLevel: Int, ble: Boolean, googleServicesVersion: Long) {
        this.uniqueId = uid
        this.os = os
        this.sdkLevel = sdkLevel
        this.ble = ble
        this.googleServicesVersion = googleServicesVersion
    }
}