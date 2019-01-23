package koeko.view

import java.io.Serializable
import java.time.LocalDate

class Homework : Serializable {
    var uid = ""
    var name = ""
    var idCode = ""
    var dueDate = LocalDate.now()
    var questions = ArrayList<String>()
    var resources = ArrayList<String>()
}