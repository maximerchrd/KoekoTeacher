package koeko.Networking

class SubNet {
    var name = ""
    var password = ""
    var ipAddress = ""
    var online = false
    var saturated = false
    var advertiser: DeviceInfo = DeviceInfo()
    var discoverer: DeviceInfo = DeviceInfo()
    var clients = mutableListOf<DeviceInfo>()
}