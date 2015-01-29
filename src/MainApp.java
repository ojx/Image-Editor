import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;

/*
Program to create simple bitmap image manipulations
 */
public class MainApp extends Application {
    public static final String INITIAL_IMAGE_FILE = "initial_image.jpg";
    public static final int MARGIN_VERTICAL = 45;
    public static final double CHANGE_FACTOR = 0.1;

    //Layout containers:
    private BorderPane root;
    private StackPane stackPane; //to add any dialogs to the application
    private Stage stage;
    private Scene scene;

    //Image controls:
    private ImageView imageView;
    private WritableImage writableImage;
    private PixelReader pixelReader;
    private PixelWriter pixelWriter;

    //The 'start' method is called automatically when the UI is ready.
    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        root = new BorderPane();
        stackPane = new StackPane(root);
        scene = new Scene(stackPane);
        stage.setScene(scene);
        stage.setTitle("Image Editor");  //text for the title bar of the window

        scene.getStylesheets().add("styles.css");
        configureUI();

        loadImage(new Image(INITIAL_IMAGE_FILE));

        stage.show();
    }

    // initialize variables and configure the UI controls, including the menu system
    private void configureUI() {
        stage.setResizable(false);

        imageView = new ImageView();
        root.setCenter(imageView);

        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open ...");
        MenuItem saveItem = new MenuItem("Save ...");
        MenuItem quitItem = new MenuItem("Quit");
        fileMenu.getItems().addAll(openItem, saveItem, quitItem);

        Menu colorMenu = new Menu("Color Adjustments");
        MenuItem saturationItem = new MenuItem("Saturate");
        MenuItem deSaturationItem = new MenuItem("DeSaturate");
        MenuItem brighterItem = new MenuItem("Brighten");
        MenuItem darkerItem = new MenuItem("Darken");
        MenuItem greyscaleItem = new MenuItem("Greyscale");

        Menu redMenu = new Menu("Red");
        Menu greenMenu = new Menu("Green");
        Menu blueMenu = new Menu("Blue");

        MenuItem increaseRedItem = new MenuItem("Increase");
        MenuItem decreaseRedItem = new MenuItem("Decrease");
        redMenu.getItems().addAll(increaseRedItem, decreaseRedItem);

        MenuItem increaseGreenItem = new MenuItem("Increase");
        MenuItem decreaseGreenItem = new MenuItem("Decrease");
        greenMenu.getItems().addAll(increaseGreenItem, decreaseGreenItem);

        MenuItem increaseBlueItem = new MenuItem("Increase");
        MenuItem decreaseBlueItem = new MenuItem("Decrease");
        blueMenu.getItems().addAll(increaseBlueItem, decreaseBlueItem);

        colorMenu.getItems().addAll(saturationItem, deSaturationItem, new SeparatorMenuItem(),
                brighterItem, darkerItem, new SeparatorMenuItem(), greyscaleItem,
                new SeparatorMenuItem(), redMenu, greenMenu, blueMenu);

        Menu imageMenu = new Menu("Image Adjustments");
        MenuItem flipHorizontalItem = new MenuItem("Flip Horizontally");
        MenuItem flipVerticalItem = new MenuItem("Flip Vertically");
        MenuItem rotateClockwiseItem = new MenuItem("Rotate 90\u00b0 Clockwise");
        MenuItem rotateAntiClockwiseItem = new MenuItem("Rotate 90\u00b0 Anti-Clockwise");
        MenuItem rotate180Item = new MenuItem("Rotate 180\u00b0");

        imageMenu.getItems().addAll(flipHorizontalItem, flipVerticalItem, new SeparatorMenuItem(),
                rotateClockwiseItem, rotateAntiClockwiseItem, rotate180Item);

        menuBar.getMenus().addAll(fileMenu, colorMenu, imageMenu);

        openItem.setOnAction(event -> {
            openFile();
        });
        saveItem.setOnAction(event -> {
            saveImage();
        });
        quitItem.setOnAction(event -> {
            quitApp();
        });
        saturationItem.setOnAction(event -> {
            saturate();
        });
        increaseRedItem.setOnAction(event -> {
            increaseRed();
        });
        flipHorizontalItem.setOnAction(event -> {
            flipHorizontally();
        });
        rotateClockwiseItem.setOnAction(event -> {
            rotateClockwise();
        });

        root.setTop(menuBar);
    }

    //loads an image and adjusts the screen and image viewport based on the dimensions of the loaded image
    private void loadImage(Image img) {
        imageView.setImage(img);
        imageView.setPreserveRatio(true);

        double width, height;

        if (imageView.getImage().getWidth() > Screen.getPrimary().getVisualBounds().getWidth() && imageView.getImage().getHeight() > Screen.getPrimary().getVisualBounds().getHeight()) {
            if (Screen.getPrimary().getVisualBounds().getWidth() / imageView.getImage().getWidth() > Screen.getPrimary().getVisualBounds().getHeight() / imageView.getImage().getHeight()) {
                width = Screen.getPrimary().getVisualBounds().getWidth();
                height = width / imageView.getImage().getWidth() * imageView.getImage().getHeight();
            } else {
                height = Screen.getPrimary().getVisualBounds().getHeight();
                width = height / imageView.getImage().getHeight() * imageView.getImage().getWidth();
            }

        } else if (imageView.getImage().getWidth() > Screen.getPrimary().getVisualBounds().getWidth() && imageView.getImage().getWidth() > imageView.getImage().getHeight()) {
            width = Screen.getPrimary().getVisualBounds().getWidth();
            height = width / imageView.getImage().getWidth() * imageView.getImage().getHeight();
        } else if (imageView.getImage().getHeight() > Screen.getPrimary().getVisualBounds().getHeight() && imageView.getImage().getWidth() < imageView.getImage().getHeight()) {
            height = Screen.getPrimary().getVisualBounds().getHeight();
            width = height / imageView.getImage().getHeight() * imageView.getImage().getWidth();
        } else {
            width = imageView.getImage().getWidth();
            height = imageView.getImage().getHeight();
        }

        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        stage.setWidth(width);
        stage.setHeight(height + MARGIN_VERTICAL);

        if (width + stage.getX() > Screen.getPrimary().getVisualBounds().getWidth()) {
            stage.setX(Screen.getPrimary().getVisualBounds().getWidth() - width);
        }

        if (height + MARGIN_VERTICAL + stage.getY() > Screen.getPrimary().getVisualBounds().getHeight()) {
            stage.setY(Screen.getPrimary().getVisualBounds().getHeight() - height - MARGIN_VERTICAL);
        }

        writableImage = new WritableImage(imageView.getImage().getPixelReader(), 0, 0, (int) imageView.getImage().getWidth(), (int) imageView.getImage().getHeight());
        pixelReader = writableImage.getPixelReader();
        pixelWriter = writableImage.getPixelWriter();
    }

    //Save the current writable Image in pPNG format
    public void saveImage() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png"));
        File file = fc.showSaveDialog(stage);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
        } catch (Exception s) {
        }
    }


    //Oopen an image file from local drives
    private void openFile() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png"));

        try {
            File file = fc.showOpenDialog(imageView.getScene().getWindow());

            if (file != null) {
                Image im = new Image("file://localhost" + file.getPath());

                loadImage(im);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //Exit application, closing windows
    private void quitApp() {
        stage.close();
        Platform.exit();
        System.exit(0);
    }

    //apply the saturtate method to each pixel in the current twriateble image
    private void saturate() {
        for (int x = 0; x < writableImage.getWidth(); x++) {
            for (int y = 0; y < writableImage.getHeight(); y++) {
                pixelWriter.setColor(x, y, pixelReader.getColor(x, y).saturate());
            }
        }

        imageView.setImage(writableImage);
    }

    //increase the amount of the RED color chanel in the current writable image by a factor of the predefined CHANGE_FACTOR
    private void increaseRed() {
        for (int x = 0; x < writableImage.getWidth(); x++) {
            for (int y = 0; y < writableImage.getHeight(); y++) {

                double red = pixelReader.getColor(x, y).getRed();
                double green = pixelReader.getColor(x, y).getGreen();
                double blue = pixelReader.getColor(x, y).getBlue();

                red *= 1 + CHANGE_FACTOR;

                Color temp = new Color(Math.min(red, 1),Math.min(green, 1),Math.min(blue, 1),1);

                pixelWriter.setColor(x, y, temp);
            }
        }

        imageView.setImage(writableImage);
    }

    //swaps the color of pixels from left to right in the current writable image
    private void flipHorizontally() {
        for (int x = 0; x < writableImage.getWidth() / 2; x++) {
            for (int y = 0; y < writableImage.getHeight(); y++) {
                int width  = (int)writableImage.getWidth()-1;

                Color temp = pixelReader.getColor(width - x, y);
                pixelWriter.setColor(width - x, y, pixelReader.getColor(x, y));
                pixelWriter.setColor(x, y, temp);
            }
        }

        imageView.setImage(writableImage);
    }

    private void rotateClockwise() {
        //create a new writable image, exchanging the width and height dimensions of the original writableImage.
        WritableImage newWritableImage = new WritableImage((int)writableImage.getHeight(), (int)writableImage.getWidth());


        // loop through and rotate pixels

        writableImage = newWritableImage;
        imageView.setImage(writableImage);
    }


    public static void main(String[] args) {
        launch(args);
    }
}