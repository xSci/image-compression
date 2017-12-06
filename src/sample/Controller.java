package sample;


import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.SepiaTone;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class Controller implements Initializable{

    @FXML
    private HBox mainwindow;
    @FXML
    private HBox top;
    @FXML
    private HBox mid;
    @FXML
    private VBox bot;

    @FXML
    public Button load;

    @FXML
    private ImageView original;
    @FXML
    private ImageView modified;
    @FXML
    private ListView algorithms;

    @FXML
    public RadioButton source;
    @FXML
    public RadioButton path;
    @FXML
    public TextField path_input;
    @FXML
    public Button browse;
    @FXML
    public Button save;
    @FXML
    public Text messages;
    @FXML
    public Button about;

    private String original_path = null;
    private BufferedImage modified_image_raw = null;

    //TODO: Further we can adjust as require, best way to know is by testing out!
    private final double WINDOW_HEIGHT = 300.0;
    private final double WINDOW_WIDTH = 1000.0;
    private final double PADDING = 10;
    private String separator;

    private void setupOS(){
        separator = File.separator;

        if (separator.equals("\\")) separator = separator.replace("\\","\\\\");
    }
    /**
     * Layouts all the components relative to the window height and width specified.
     * Doesn't need adjusting, can leave it as it is.
     */
    private void setResponsiveLayout(){
        mainwindow.setMinHeight(WINDOW_HEIGHT);
        mainwindow.setMinWidth(WINDOW_WIDTH);
        mainwindow.setTranslateY(PADDING);

        top.setMinHeight(WINDOW_HEIGHT/10);
        top.setMinWidth(WINDOW_WIDTH);
        top.setTranslateX(PADDING);
        mid.setMinHeight(8 * WINDOW_HEIGHT/10);
        mid.setMinWidth(WINDOW_WIDTH);
        mid.setTranslateX(PADDING);
        bot.setMinHeight(WINDOW_HEIGHT/10);
        bot.setMinWidth(WINDOW_WIDTH);
        bot.setTranslateX(PADDING);


        algorithms.setPrefHeight(mid.getMinHeight()/3);
        algorithms.setPrefWidth(mid.getMinWidth()/3);

        original.setFitHeight(mid.getMinHeight());
        original.setFitWidth(mid.getMinWidth()/3);

        modified.setFitHeight(mid.getMinHeight());
        modified.setFitWidth(mid.getMinWidth()/3);
    }

    /**
     * Write all algorithms that will be available.
     */
    private void setListItems(){
        //TODO: Name the items as require. We can map them later in setupList().
        algorithms.setItems(FXCollections.observableArrayList (
                "Huffman", "Second", "Third", "There can be more"));
    }

    /**
     * Loads the image in the place holder specified
     * @param image_holder the place holder for the image
     * @param file the image
     */
    private void setImageFromFile(ImageView image_holder, File file) {
        Image image = new Image(file.toURI().toString());
        image_holder.setImage(image);
    }

    /**
     * Function for load button.
     * It opens a file chooser and loads image into original image holder.
     * Saves original path for later use.
     */
    private void setupLoad(){
        load.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(Main.stage);
            if (file != null) {
                setImageFromFile(original, file);
                modified.setImage(null);
                modified_image_raw = null;
                original_path = file.toString();
                messages.setText("Successfully loaded!");
            }
        });
    }

    /**
     * Function for list item clicks.
     * Here we should map all the strings to their functions.
     */
    private void setupList(){
        algorithms.setOnMouseClicked(event -> {
            if (original_path != null){
                String selected = (String) algorithms.getSelectionModel().getSelectedItem();
                messages.setText("Modifying right now...");
                (new Thread(() -> {
                    //TODO: Map strings from the list to their algorithm function
                    //Our main (Huffman and Haarwavelet) algorithms should return BufferedImage or its descendants.
                    //don't forget to call SwingFXUtils.toFXImage(modified_image_raw, null) when setting image.

                    //We require we can return Image from our other algorithm, and set image to what you returned.
                    if (selected.equals("Huffman")){
                        modified_image_raw = new DummyAlgorithms(original.getImage()).huffman();
                        modified.setImage(SwingFXUtils.toFXImage(modified_image_raw, null));
                        messages.setText("Done!");
                    }
                })).start();


            }
            else messages.setText("No image loaded!");
        });
    }

    /**
     * Function to save images.
     * It does checks for image to save and outputs to specified path(s).
     */
    private void setupSave(){
        save.setOnAction(event -> {
            if (original_path != null && modified_image_raw != null){
                File source_path = null, custom_path = null;
                if (source.isSelected())
                    source_path = new File(original_path.split("\\.")[0] + "-"
                            + algorithms.getSelectionModel().getSelectedItem() + "." + original_path.split("\\.")[1]);


                if (path.isSelected()){
                    custom_path = new File(path_input.getText());
                }

                try {
                    if (source_path != null)
                        ImageIO.write(modified_image_raw, original_path.split("\\.")[1], source_path);
                    if (custom_path != null)
                        ImageIO.write(modified_image_raw, original_path.split("\\.")[1], custom_path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                finally {
                    messages.setText("Image Saved!");
                }
            }
            else messages.setText("No image to save");
        });
    }

    /**
     * Function to set custom path. We can load a folder and automatically generates a picture name based on the
     * algorithm we used. The path is, however, free to input.
     */
    private void setupBrowse(){
        browse.setOnAction(event -> {
            //TODO: Check for original
            DirectoryChooser directoryChooser = new DirectoryChooser();
            System.out.println(separator);
            File file = directoryChooser.showDialog(Main.stage);
            if (file != null)
                path_input.setText(file.toString() + separator + original_path.split(separator)[original_path.split(separator).length - 1].split("\\.")[0] +
                        "-" + algorithms.getSelectionModel().getSelectedItem() + "." + original_path.split("\\.")[1]);
        });
    }

    private void setupAbout(){
        about.setTranslateX(WINDOW_WIDTH - WINDOW_WIDTH/10);
        about.setTranslateY(-PADDING);
        about.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About");
            alert.setHeaderText(null);
            String info = "Credits: Umair, ..., ..., ..."
            		+ "\n\n\nLicense: \nI don't know what license to choose!\nYou guys can decide."
            		+ "\n\n\nCopyrightę 2017";
            alert.setContentText(info);

            alert.showAndWait();
        });
    }


    /**
     * Sets up all the button actions. In this case, a click on the list will be considered a button.
     */
    private void setupButtons(){
        setupLoad();
        setupList();
        setupSave();
        setupBrowse();
        setupAbout();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setResponsiveLayout();
        setupOS();
        setupButtons();
        setListItems();
    }

}