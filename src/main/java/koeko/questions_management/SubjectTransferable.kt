package koeko.questions_management

import koeko.view.TransferPrefix
import koeko.view.TransferableObject

class SubjectTransferable: TransferableObject(groupPrefix = TransferPrefix.resource) {
    var _subjectName = ""
    var questionId = ""


}