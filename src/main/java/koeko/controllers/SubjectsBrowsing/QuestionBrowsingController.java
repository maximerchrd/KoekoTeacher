package koeko.controllers.SubjectsBrowsing;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import koeko.Koeko;
import koeko.database_management.*;
import koeko.students_management.Subject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * Created by maximerichard on 13.03.18.
 */
public class QuestionBrowsingController implements Initializable {
    static public TreeItem<Subject> rootSubjectSingleton;
    private Subject draggedSubject;
    private TreeItem<Subject> draggedItem;

    private Vector<Subject> subjects;
    private Vector<String> subjectsIds;
    private Vector<String> parentIds;
    private Vector<String> childIds;

    @FXML private Label labelIP;
    @FXML private TreeView<Subject> subjectsTree;

    public void initialize(URL location, ResourceBundle resources) {
        final String[] ip_address = {""};
        Task<Void> getIPTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                ip_address[0] = InetAddress.getLocalHost().getHostAddress();
                Platform.runLater(() -> labelIP.setText("studentGroupsAndClass should connect \nto the following address: " + ip_address[0]));
                return null;
            }
        };
        new Thread(getIPTask).start();


        //build the subjects tree
        subjectsTree.getStylesheets().add("/style/treeview.css");

        //create rootSubjectSingleton
        Subject subject = new Subject();
        subject.set_subjectName("All subjects");
        rootSubjectSingleton = new TreeItem<>(subject);
        rootSubjectSingleton.setExpanded(true);
        subjectsTree.setShowRoot(true);
        Task<Void> loadQuestions = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                populateSubjectTree(rootSubjectSingleton);
                return null;
            }
        };
        new Thread(loadQuestions).start();
        subjectsTree.setRoot(rootSubjectSingleton);
        subjectsTree.setCellFactory(new Callback<TreeView<Subject>, TreeCell<Subject>>() {
            @Override
            public TreeCell<Subject> call(TreeView<Subject> stringTreeView) {
                TreeCell<Subject> treeCell = new TreeCell<Subject>() {
                    @Override
                    protected void updateItem(Subject item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty && item != null) {
                            setText(item.get_subjectName());
                        } else {
                            setText(null);
                            setGraphic(null);
                        }
                    }
                };

                treeCell.setOnDragDetected(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                            draggedSubject = treeCell.getTreeItem().getValue();
                            draggedItem = treeCell.getTreeItem();
                            Dragboard db = subjectsTree.startDragAndDrop(TransferMode.ANY);

                            /* Put a string on a dragboard */
                            ClipboardContent content = new ClipboardContent();
                            content.putString(treeCell.getText());
                            db.setContent(content);

                            mouseEvent.consume();
                    }
                });

                treeCell.setOnDragOver(new EventHandler<DragEvent>() {
                    public void handle(DragEvent event) {
                        /* data is dragged over the target */
                        /* accept it only if it is not dragged from the same node
                         * and if it has a string data */
                        if (event.getGestureSource() != treeCell &&
                                event.getDragboard().hasString()) {
                            //set the type of dropping: MOVE AND/OR COPY
                            event.acceptTransferModes(TransferMode.MOVE);
                        }

                        event.consume();
                    }
                });

                treeCell.setOnDragEntered(new EventHandler<DragEvent>() {
                    public void handle(DragEvent event) {
                        /* the drag-and-drop gesture entered the target */
                        /* show to the user that it is an actual gesture target */
                        if (event.getGestureSource() != treeCell &&
                                event.getDragboard().hasString()) {
                            //treeCell.setStyle(String.format("-fx-background-color: green"));
                            treeCell.setTextFill(Color.LIGHTGREEN);
                        }
                        event.consume();
                    }
                });
                treeCell.setOnDragExited(new EventHandler<DragEvent>() {
                    public void handle(DragEvent event) {
                        /* mouse moved away, remove the graphical cues */
                        //treeCell.setStyle(String.format("-fx-background-color: white"));
                        treeCell.setTextFill(Color.BLACK);
                        event.consume();
                    }
                });


                treeCell.setOnDragDropped(new EventHandler<DragEvent>() {
                    public void handle(DragEvent event) {
                        /* data dropped */
                        if (!treeCell.getTreeItem().getValue().get_subjectName().contentEquals(draggedSubject.get_subjectName())) {
                            DbTableRelationSubjectSubject.addRelationSubjectSubject(treeCell.getTreeItem().getValue().get_subjectName(),draggedItem.getValue().get_subjectName(),
                                    draggedItem.getParent().getValue().get_subjectName());
                            draggedItem.getParent().getChildren().remove(draggedItem);
                            treeCell.getTreeItem().getChildren().add(draggedItem);
                        } else {
                            System.out.println("Trying to drag on self");
                        }
                        draggedSubject = null;
                    }
                });


                return treeCell;
            }
        });

        subjectsTree.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    //filter with subject
                }
            }
        });

    }

    private void populateSubjectTree(TreeItem<Subject> root) {
        //BEGIN retrieve data from the db and prepare the vectors
        subjects = DbTableSubject.getAllSubjects();
        subjectsIds = new Vector<>();
        for (Subject subject : subjects) {
            subjectsIds.add(String.valueOf(subject.get_subjectMUID()));
        }

        Vector<Vector<String>> idsRelationPairs = DbTableRelationSubjectSubject.getAllSubjectIDsRelations();
        parentIds = new Vector<>();
        childIds = new Vector<>();
        for (Vector<String> pair : idsRelationPairs) {
            parentIds.add(pair.get(0));
            childIds.add(pair.get(1));
        }

        Vector<String> topSubjects = new Vector<>();
        for (String subjectId : subjectsIds) {
            if (!childIds.contains(subjectId)) {
                topSubjects.add(subjectId);
            }
        }
        //END retrieve data from the db and prepare the vectors

        for (String subjectId : topSubjects) {
            Subject subject = subjects.get(subjectsIds.indexOf(subjectId));
            TreeItem subjectTreeItem = new TreeItem<>(subject);
            root.getChildren().add(subjectTreeItem);

            populateWithChildren(subjectId, subjectTreeItem);
        }
    }

    private void populateWithChildren(String subjectID, TreeItem<Subject> subjectTreeItem) {
        Vector<String> childrenIds = new Vector<>();
        for (int i = 0; i < parentIds.size(); i++) {
            if (parentIds.get(i).contentEquals(subjectID)) {
                childrenIds.add(childIds.get(i));
            }
        }

        //
        for (String childrenId : childrenIds) {
            Subject subject = subjects.get(subjectsIds.indexOf(childrenId));
            TreeItem<Subject> newItem = new TreeItem<>(subject);
            subjectTreeItem.getChildren().add(newItem);
            populateWithChildren(childrenId, newItem);
        }
    }

    public void refreshIP() {
        final String[] ip_address = {""};
        Task<Void> getIPTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                ip_address[0] = InetAddress.getLocalHost().getHostAddress();
                Platform.runLater(() -> labelIP.setText("studentGroupsAndClass should connect \nto the following address: " + ip_address[0]));
                return null;
            }
        };
        new Thread(getIPTask).start();
    }

    public void createSubject() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/CreateSubject.fxml"));
        Parent root1 = null;
        try {
            root1 = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        CreateSubjectController controller = fxmlLoader.getController();

        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Create a New Subject");
        stage.setScene(new Scene(root1));
        stage.show();
    }

    public void editSubject() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/EditSubject.fxml"));
        Parent root1 = null;
        try {
            root1 = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        EditSubjectController controller = fxmlLoader.getController();
        controller.initializeSubject(subjectsTree.getSelectionModel().getSelectedItem().getValue().get_subjectName(), subjectsTree);
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Edit the Subject");
        stage.setScene(new Scene(root1));
        stage.show();
    }

    public void filterQuestionsWithSubject() {
        Subject subject = subjectsTree.getSelectionModel().getSelectedItem().getValue();
        Vector<String> questionIds;
        if (subject.get_subjectName().contentEquals("All subjects")) {
            questionIds = DbTableQuestionGeneric.getAllGenericQuestionsIds();
        } else {
            questionIds = DbTableRelationQuestionSubject.getQuestionsIdsForSubject(subject.get_subjectName());
        }
        Koeko.questionSendingControllerSingleton.populateTree(questionIds);
    }
}
