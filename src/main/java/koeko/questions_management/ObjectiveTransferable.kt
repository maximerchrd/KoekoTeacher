package koeko.questions_management

import koeko.view.TransferPrefix
import koeko.view.TransferableObject


class ObjectiveTransferable: TransferableObject(groupPrefix = TransferPrefix.resource) {
    var _objectiveName = ""
    var _objectiveLevel = -1
    var questionId = ""
}