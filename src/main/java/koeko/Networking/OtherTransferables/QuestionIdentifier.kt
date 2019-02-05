package koeko.Networking.OtherTransferables

import koeko.view.TransferPrefix
import koeko.view.TransferableObject

class QuestionIdentifier : TransferableObject(groupPrefix = TransferPrefix.stateUpdate) {
    var identifier = ""
    var correctionMode = -1
}