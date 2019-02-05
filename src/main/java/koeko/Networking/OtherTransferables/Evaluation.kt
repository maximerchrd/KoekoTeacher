package koeko.Networking.OtherTransferables

import koeko.view.TransferPrefix
import koeko.view.TransferableObject

class Evaluation : TransferableObject(groupPrefix = TransferPrefix.stateUpdate) {
    var evaluationType = 0
    var identifier = ""
    var name = ""
    var evaluation = 0.0
    var evalUpdate = false
    var testIdentifier = ""
    var testName = ""
}

object EvaluationTypes {
    const val questionEvaluation = 0
    const val objectiveEvaluation = 1
}