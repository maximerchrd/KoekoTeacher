package koeko.Networking.OtherTransferables

import koeko.view.TransferPrefix
import koeko.view.TransferableObject

class SyncedIds : TransferableObject(groupPrefix = TransferPrefix.stateUpdate) {
    var ids = ArrayList<String>()
}