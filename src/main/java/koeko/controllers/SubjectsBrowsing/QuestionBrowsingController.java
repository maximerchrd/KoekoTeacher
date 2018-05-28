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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import koeko.Koeko;
import koeko.controllers.CreateQuestionController;
import koeko.database_management.DbTableRelationQuestionQuestion;
import koeko.database_management.DbTableRelationQuestionSubject;
import koeko.database_management.DbTableRelationQuestionTest;
import koeko.database_management.DbTableSubject;
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
    private TreeItem<Subject> root;
    private Subject draggedSubject;

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
        //create root
        root = new TreeItem<>(new Subject());
        root.setExpanded(true);
        subjectsTree.setShowRoot(false);
        Task<Void> loadQuestions = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                populateSubjectTree(root);
                return null;
            }
        };
        new Thread(loadQuestions).start();
        subjectsTree.setRoot(root);
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
                        if (treeCell.getTreeItem().getValue().get_subjectId() > 0) {
                            draggedSubject = treeCell.getTreeItem().getValue();
                            Dragboard db = subjectsTree.startDragAndDrop(TransferMode.ANY);

                            /* Put a string on a dragboard */
                            ClipboardContent content = new ClipboardContent();
                            content.putString(treeCell.getText());
                            db.setContent(content);

                            mouseEvent.consume();
                        }
                    }
                });

                treeCell.setOnDragOver(new EventHandler<DragEvent>() {
                    public void handle(DragEvent event) {
                        /* data is dragged over the target */
                        /* accept it only if it is not dragged from the same node
                         * and if it has a string data */
                        if (event.getGestureSource() != treeCell &&
                                event.getDragboard().hasString()) {
                            /* allow for both copying and moving, whatever user chooses */
                            event.acceptTransferModes(TransferMode.COPY);
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
                            treeCell.setTextFill(Color.GREEN);
                        }
                        event.consume();
                    }
                });
                treeCell.setOnDragExited(new EventHandler<DragEvent>() {
                    public void handle(DragEvent event) {
                        /* mouse moved away, remove the graphical cues */
                        treeCell.setTextFill(Color.BLACK);
                        event.consume();
                    }
                });


                treeCell.setOnDragDropped(new EventHandler<DragEvent>() {
                    public void handle(DragEvent event) {
                        /* data dropped */
                        /* if there is a string data on dragboard, read it and use it */
                        /*if (treeCell.getTreeItem().getValue().get_subjectMUID() < 0) {

                            //add a horizontal relation with the question before in the list
                            int bigBrotherIndex = treeCell.getTreeItem().getChildren().size() - 1;
                            TreeItem<Subject> questionBefore = null;
                            if (bigBrotherIndex >= 0) {
                                questionBefore = treeCell.getTreeItem().getChildren().get(bigBrotherIndex);
                            }
                            if (questionBefore != null) {
                                DbTableRelationQuestionQuestion.addRelationQuestionQuestion(String.valueOf(questionBefore.getValue().getGlobalID()),
                                        String.valueOf(draggedSubject.getGlobalID()), treeCell.getTreeItem().getValue().getQuestion(), "");
                            }

                            //add the node to the tree
                            treeCell.getTreeItem().getChildren().add(new TreeItem<>(draggedSubject));
                            DbTableRelationQuestionTest.addRelationQuestionTest(String.valueOf(draggedSubject.getGlobalID()),
                                    treeCell.getTreeItem().getValue().getQuestion());
                            event.setDropCompleted(true);
                            treeCell.getTreeItem().setExpanded(true);
                            event.consume();
                        } else if (treeCell.getTreeItem().getChildren() != draggedSubject) {
                            TreeItem<Subject> treeItemTest = treeCell.getTreeItem();
                            while (treeItemTest.getParent() != root) {
                                treeItemTest = treeItemTest.getParent();
                            }
                            if (treeItemTest.getValue().getGlobalID() < 0) {
                                int bigBrotherIndex = treeCell.getTreeItem().getChildren().size() - 1;
                                TreeItem<Subject> questionBefore = null;
                                if (bigBrotherIndex >= 0) {
                                    questionBefore = treeCell.getTreeItem().getChildren().get(bigBrotherIndex);
                                }
                                if (questionBefore != null) {
                                    DbTableRelationQuestionQuestion.addRelationQuestionQuestion(String.valueOf(questionBefore.getValue().getGlobalID()),
                                            String.valueOf(draggedSubject.getGlobalID()), treeItemTest.getValue().getQuestion(), "");
                                }

                                //add the node to the tree and set the vertical relation
                                treeCell.getTreeItem().getChildren().add(new TreeItem<>(draggedSubject));
                                DbTableRelationQuestionQuestion.addRelationQuestionQuestion(String.valueOf(treeCell.getTreeItem().getValue().getGlobalID()),
                                        String.valueOf(draggedSubject.getGlobalID()), treeItemTest.getValue().getQuestion(), "EVALUATION<60");
                                event.setDropCompleted(true);
                                treeCell.getTreeItem().setExpanded(true);
                                event.consume();
                            }
                        } else {
                            System.out.println("Trying to drag on self or on question not belonging to any test");
                        }
                        draggedSubject = null;*/
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
        //retrieve data from the db
        Vector<Subject> subjects = DbTableSubject.getAllSubjects();

        for (Subject subject : subjects) {
            TreeItem subjectTreeItem = new TreeItem<>(subject);
            root.getChildren().add(subjectTreeItem);
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
        CreateQuestionController controller = fxmlLoader.getController();
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Create a New Subject");
        stage.setScene(new Scene(root1));
        stage.show();
    }

    public void filterQuestionsWithSubject() {
        Subject subject = subjectsTree.getSelectionModel().getSelectedItem().getValue();
        Vector<String> questionIds = DbTableRelationQuestionSubject.getQuestionsIdsForSubject(subject.get_subjectName());
        Koeko.questionSendingControllerSingleton.populateTree(questionIds);
    }
}
