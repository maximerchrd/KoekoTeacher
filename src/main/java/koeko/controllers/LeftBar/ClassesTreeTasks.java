package koeko.controllers.LeftBar;

import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import koeko.Koeko;
import koeko.controllers.ClassesControlling.ClassTreeCell;
import koeko.controllers.SubjectsBrowsing.SubjectTreeCell;
import koeko.database_management.DbTableClasses;
import koeko.database_management.DbTableRelationSubjectSubject;
import koeko.students_management.Classroom;
import koeko.view.Subject;

import java.util.ArrayList;

public class ClassesTreeTasks {
    static private Classroom draggedClassroom = null;
    static private TreeItem<Classroom> draggedItem = null;

    static public void populateClassesTree(TreeView<Classroom> classroomTreeView) {
        Classroom classroom = new Classroom();
        classroom.setClassName("All Classes");
        Koeko.leftBarController.rootClassSingleton = new TreeItem<>(classroom);
        Koeko.leftBarController.rootClassSingleton.setExpanded(true);
        classroomTreeView.setShowRoot(true);
        Task<Void> loadSubjects = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                buildTree();
                return null;
            }
        };
        new Thread(loadSubjects).start();
        classroomTreeView.setRoot(Koeko.leftBarController.rootClassSingleton);
        classroomTreeView.setCellFactory(stringTreeView -> {
            ClassTreeCell treeCell = new ClassTreeCell();

            treeCell.setOnDragDetected(mouseEvent -> {
                draggedClassroom = treeCell.getTreeItem().getValue();
                draggedItem = treeCell.getTreeItem();
                Dragboard db = classroomTreeView.startDragAndDrop(TransferMode.ANY);

                /* Put a string on a dragboard */
                ClipboardContent content = new ClipboardContent();
                content.putString(treeCell.getText());
                db.setContent(content);

                mouseEvent.consume();
            });

            treeCell.setOnDragOver(event -> {
                /* data is dragged over the target */
                /* accept it only if it is not dragged from the same node
                 * and if it has a string data */
                if (event.getGestureSource() != treeCell &&
                        event.getDragboard().hasString()) {
                    //set the type of dropping: MOVE AND/OR COPY
                    event.acceptTransferModes(TransferMode.MOVE);
                }

                event.consume();
            });

            treeCell.setOnDragEntered(event -> {
                /* the drag-and-drop gesture entered the target */
                /* show to the user that it is an actual gesture target */
                if (event.getGestureSource() != treeCell &&
                        event.getDragboard().hasString()) {
                    //treeCell.setStyle(String.format("-fx-background-color: green"));
                    treeCell.setTextFill(Color.LIGHTGREEN);
                }
                event.consume();
            });
            treeCell.setOnDragExited(event -> {
                /* mouse moved away, remove the graphical cues */
                //treeCell.setStyle(String.format("-fx-background-color: white"));
                treeCell.setTextFill(Color.BLACK);
                event.consume();
            });


            treeCell.setOnDragDropped(event -> {
                /* data dropped */
                if (!treeCell.getTreeItem().getValue().getClassName().contentEquals(draggedClassroom.getClassName())) {
                    DbTableRelationSubjectSubject.addRelationSubjectSubject(treeCell.getTreeItem().getValue().getClassName(),
                            draggedItem.getValue().getClassName(), draggedItem.getParent().getValue().getClassName());
                    draggedItem.getParent().getChildren().remove(draggedItem);
                    treeCell.getTreeItem().getChildren().add(draggedItem);
                } else {
                    System.out.println("Trying to drag on self");
                }
                draggedClassroom = null;
            });


            return treeCell;
        });
    }

    static private void buildTree() {
        ArrayList<Classroom> classes = DbTableClasses.getAllClassrooms();
        for (Classroom classroom : classes) {
            TreeItem<Classroom> classroomTreeItem = new TreeItem<>(classroom);
            ArrayList<Classroom> groups = DbTableClasses.getClassroomGroupsFromClass(classroom.getClassName());
            for (Classroom group : groups) {
                classroomTreeItem.getChildren().add(new TreeItem<>(group));
            }
            Koeko.leftBarController.rootClassSingleton.getChildren().add(classroomTreeItem);
        }
    }
}
