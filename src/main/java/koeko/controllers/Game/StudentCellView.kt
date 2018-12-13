package koeko.controllers.Game

import koeko.students_management.Student

class StudentCellView {
    var student: Student = Student()
    var score = 0.0

    constructor(student: Student, initialScore: Double = 0.0) {
        this.student = student
        this.score = initialScore
    }
}