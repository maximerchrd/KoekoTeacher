package koeko.controllers.LeftBar.HomeworkControlling

import java.time.LocalDate

class Homework {
    var name = ""
    var idCode = ""
    var dueDate = LocalDate.now()
    var questions = ArrayList<String>()
    var resources = ArrayList<String>()
}