package agh.ics.oop.presenter;

import agh.ics.oop.Simulation;
import agh.ics.oop.model.*;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SingleSimulationPresenter extends Stage implements MapChangeListener {


    @FXML
    public Button pauseResumeButton;
    public Label followedGenome;
    public Label followedEnergy;
    public Label followedEatenGrass;
    public Label followedHeader;
    public Label followedAge;
    public Label currentDay;
    public Label followedDateOfDeath;
    public Label followedChildrenNumber;
    public Label followedOffspringrenNumber;
    public Button popularGenomeButton;
    public Slider zoomSlider;
    public BorderPane rootPane;
    public Button highlightGrassButton;
    private WorldMap worldMap;
    private Simulation simulation;
    @FXML
    private GridPane mapGrid = new GridPane();
    private int height;
    private int width;
    private boolean showEnergy;
    @FXML
    List<List<ImageView>> gridLabels = new ArrayList<>();

    List<Vector2d> listToHighlight;

    @FXML
    private Label animalsCountLabel;
    @FXML
    private Label grassCountLabel;
    @FXML
    private Label avgEnergyLabel;
    @FXML
    private Label freeSpaceLabel;
    @FXML
    private Label genotypeLabel;
    @FXML
    private Label avgAgeLabel;
    @FXML
    private Label avgOffspringLabel;
    @FXML
    private Label dominantGenotypePositionsLabel;
    @FXML
    private VBox infoVBox;
    @FXML
    private HBox deathHBox;


    private boolean paused = false;
    private boolean saveEveryDayToCSV = false;
    private boolean highlight = false;
    private boolean highlightEQ=true;
    private boolean mapVersion;

    private Animal followedAnimal = null;


    private Image dirt = new Image("dirt.png");
    private Image grass = new Image("grass.png");
    private List<Image> animalImages = new ArrayList<>();
    private int energyForBeingFull;


    //przesunięcie GridPane
    private double translateX = 0;
    private double translateY = 0;

    private FileWriter writer = null;



    public void setHeight(int height) {
        this.height = height;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public void setWorldMap(WorldMap map) {
        this.worldMap = map;
    }
    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
        this.energyForBeingFull = simulation.getEnergyForBeingFull();
    }

    public void setWriter(FileWriter writer) {
        this.writer = writer;
    }

    public void setShowEnergy(boolean showEnergy) {
        this.showEnergy = showEnergy;
    }

    public void setSaveEveryDayToCSV(boolean saveEveryDayToCSV) {
        this.saveEveryDayToCSV = saveEveryDayToCSV;
    }

    private void updateAboutFollowedAnimal() {
        if(followedAnimal == null) return;
        followedHeader.setText("Animal at: " + followedAnimal.getPosition());
        followedGenome.setText("Genome:" + followedAnimal.getGenome());
        followedEnergy.setText("Energy: " + followedAnimal.getEnergy());
        followedEatenGrass.setText("Eaten grass: " + followedAnimal.getEatenGrass());
        followedChildrenNumber.setText("Children number: " + followedAnimal.getChildren());
        followedOffspringrenNumber.setText("Offspring number: "+followedAnimal.getCountOffspring());
        followedAge.setText("Age: " + followedAnimal.getAge());

        //ustawianie podświetlenia
        infoVBox.setVisible(true);
        turnOffHighlighting();
        gridLabels.get(height - 1 - followedAnimal.getPosition().getY()).get(followedAnimal.getPosition().getX()).setStyle("-fx-effect: innershadow(gaussian, rgba(255, 255, 255, 0.4), 10, 0.8, 0, 0);");


        if(followedAnimal.isDead()) {
            deathHBox.setVisible(true);
            followedDateOfDeath.setText("Date of death: " + simulation.getDay());
            followedAnimal = null;
        }
        else deathHBox.setVisible(false);;
    }

    private void handleMouseClick(int y, int x) {
        if(!paused || highlight) return; //nie pozwalamy na kliknięcie w zwierzę gdy program nie jest spauzowany
        Vector2d position = new Vector2d(x,height-1-y);

        if(worldMap.objectAt(position) != null && worldMap.objectAt(position) instanceof Animal) {

            //temu poprzedniemu ustawiam kolor jako energia
            if(followedAnimal != null) {
                ImageView imageView = gridLabels.get(height - 1 - followedAnimal.getPosition().getY()).get(followedAnimal.getPosition().getX());
                if (showEnergy) {
                    int animalEnergy = followedAnimal.getEnergy();
                    if (animalEnergy >= 2 * energyForBeingFull) {
                        imageView.setStyle("-fx-effect: innershadow(gaussian, rgba(0, 255, 0, 0.4), 10, 0.8, 0, 0);");
                    } else {
                        double value = ((double) animalEnergy / (2 * energyForBeingFull));
                        imageView.setStyle("-fx-effect: innershadow(gaussian, rgba(" + 255 * (1 - value) + ", " + 255 * value + ", 0, 0.4), 10, 0.8, 0, 0);");
                    }
                } else {
                    imageView.setStyle(null);
                }
            }

            followedAnimal = (Animal) worldMap.objectAt(position);
            updateAboutFollowedAnimal();
        }

    }

    public void drawEmptyMap() throws IOException {
        highlightGrassButton.setVisible(false);

        zoomSlider.setValue(39); //ustawiam tyle, bo to jest domyślna wartość skali

        rootPane.addEventFilter(KeyEvent.ANY, event -> {
            if (event.getEventType() == KeyEvent.KEY_RELEASED && event.getCode() == KeyCode.SPACE) {
                pauseResume();
            } else {
                movePane(event.getCode());
            }
            event.consume();
        });

        if(saveEveryDayToCSV) {
            if(writer == null) {
                File selectedFile = new File(System.getProperty("user.dir") + "/simulation" + LocalTime.now() + ".csv");
                writer = new FileWriter(selectedFile);
            }

            writer.write("Day;Animals;Grass;Avg energy;Free spaces;Popular genome;Avg age;Avg offspring;Dominant genome positions\n");
            writer.flush();
        }

        for(int i=0; i<8; i++) {
            Image newImageView = new Image("a" + i + ".png");
            animalImages.add(newImageView);
        }

        for (int row = 0; row < height; row++) {
            List<ImageView> rowLabels = new ArrayList<>();
            for (int col = 0; col < width; col++) {
                ImageView imageView = new ImageView(dirt);

                imageView.setFitWidth(15);
                imageView.setFitHeight(15);
                int finalRow = row;
                int finalCol = col;
                imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        handleMouseClick(finalRow, finalCol);
                    }
                });
                rowLabels.add(imageView);
                mapGrid.add(imageView, col, row);
            }
            gridLabels.add(rowLabels);
        }
        for (int row=0; row < height; row++) {
            mapGrid.getRowConstraints().add(new RowConstraints(15));
        }
        for (int col=0; col< width; col++) {
            mapGrid.getColumnConstraints().add(new ColumnConstraints(15));
        }
    }

    public void drawMap() {
        for (int y = 0; y<height; y++) {
            for (int x = 0; x<width; x++) {
                ImageView imageView = gridLabels.get(y).get(x);

                if(worldMap.objectAt(new Vector2d(x,height-1-y)) != null) {
                    if (worldMap.objectAt(new Vector2d(x, height - 1 - y)) instanceof Grass) {
                        imageView.setImage(grass);

                        if(showEnergy) {
                            imageView.setStyle(null);
                        }

                    } else {
                        Animal newAnimal = (Animal) worldMap.objectAt(new Vector2d(x, height - 1 - y));
                        int orientation = newAnimal.getOrientation().getValue();
                        imageView.setImage(animalImages.get(orientation));





                        if(showEnergy && newAnimal != followedAnimal) {
                            int animalEnergy = newAnimal.getEnergy();
                            if(animalEnergy >= 2*energyForBeingFull) {
                                imageView.setStyle("-fx-effect: innershadow(gaussian, rgba(0, 255, 0, 0.4), 10, 0.8, 0, 0);");
                            }
                            else {
                                double value = ((double) animalEnergy / (2*energyForBeingFull));
                                imageView.setStyle("-fx-effect: innershadow(gaussian, rgba(" + 255*(1-value) + ", " + 255*value +", 0, 0.4), 10, 0.8, 0, 0);");

                            }

                        }


                    }
                }
                else {
                    imageView.setImage(dirt);
                    if(showEnergy) {
                        imageView.setStyle(null);
                    }
                }

            }
        }
    }

    public void exportDataToCSV(List<String> messages) throws IOException {
        writer.write(simulation.getDay() + ";");
        boolean firstElementSkipped = false;
        for(String message : messages) {
            if(firstElementSkipped) {
                writer.write(message + ";");
            }
            else {
                firstElementSkipped = true;
            }
        }
        writer.write("\n");
        writer.flush();
    }

    private void turnOffHighlighting() {
        {
            popularGenomeButton.setText("Show popular genome");

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    ImageView imageView = gridLabels.get(y).get(x);
                    imageView.setStyle(null);
                    if (worldMap.objectAt(new Vector2d(x, height - 1 - y)) != null) {
                        if (worldMap.objectAt(new Vector2d(x, height - 1 - y)) instanceof Animal) {
                            Animal newAnimal = (Animal) worldMap.objectAt(new Vector2d(x, height - 1 - y));

                            if(newAnimal == followedAnimal) {
                                imageView.setStyle("-fx-effect: innershadow(gaussian, rgba(255, 255, 255, 0.4), 10, 0.8, 0, 0);");
                            }

                            if(showEnergy) {
                                if (newAnimal != followedAnimal) {
                                    int animalEnergy = newAnimal.getEnergy();
                                    if (animalEnergy >= 2 * energyForBeingFull) {
                                        imageView.setStyle("-fx-effect: innershadow(gaussian, rgba(0, 255, 0, 0.4), 10, 0.8, 0, 0);");
                                    } else {
                                        double value = ((double) animalEnergy / (2 * energyForBeingFull));
                                        imageView.setStyle("-fx-effect: innershadow(gaussian, rgba(" + 255 * (1 - value) + ", " + 255 * value + ", 0, 0.4), 10, 0.8, 0, 0);");
                                    }
                                }
                            }
                            else {
                                if (newAnimal == followedAnimal) {
                                    imageView.setStyle("-fx-effect: innershadow(gaussian, rgba(200, 200, 200, 0.4), 10, 0.8, 0, 0);");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //jeśli bez argumentu, to z False
    public void highlightPopularGenome() {
        this.highlightPopularGenome(false);
    }

    public void highlightPopularGenome (boolean force) {

        if(force) {
            highlight = false;
            turnOffHighlighting();
            return;
        }
        turnOffHighlighting();


        if(!paused) return;
        highlightGrassButton.setVisible(true);
        highlight = !highlight;
        if (highlight) {
            popularGenomeButton.setText("Hide popular genome");
            for (int y = 0; y<height; y++) {
                for (int x = 0; x<width; x++) {
                    ImageView imageView = gridLabels.get(y).get(x);
                    if(worldMap.objectAt(new Vector2d(x,height-1-y)) != null) {
                        if (worldMap.objectAt(new Vector2d(x, height - 1 - y)) instanceof Animal) {
                            Animal newAnimal = (Animal) worldMap.objectAt(new Vector2d(x, height - 1 - y));

                            if(listToHighlight.contains(newAnimal.getPosition())) {
                                imageView.setStyle("-fx-effect: innershadow(gaussian, rgba(200, 200, 200, 0.4), 10, 0.8, 0, 0);");
                            }
                            else {
                                imageView.setStyle(null);
                            }
                        }
                    }
                }
            }

        } else {
            turnOffHighlighting();
        }

    }

    public void highlightDominantGrass() {
        highlightGrassButton.setVisible(false);
        if (!paused) return;
        if (!mapVersion){
            this.highlightEquator();
        }else{
            this.highlightWalkingJungle();
        }
    }
    public void highlightEquator () {
        if(!paused) return;
        for (int y = worldMap.getEquatorMinHeight(); y<= worldMap.getEquatorMaxHeight(); y++) {
            for (int x = 0; x < width; x++) {
                ImageView imageView = gridLabels.get(y).get(x);
                imageView.setStyle("-fx-effect: innershadow(gaussian, rgba(0, 200, 0, 0.25), 10, 0.8, 0, 0);");
            }
        }

    }
    public void highlightWalkingJungle () {
        if(!paused) return;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ImageView imageView = gridLabels.get(y).get(x);
                if (worldMap.isOccupiedGrass(new Vector2d(x, height - 1 - y))) {
                    imageView.setStyle("-fx-effect: innershadow(gaussian, rgba(0, 200, 0, 0.25), 10, 0.8, 0, 0);");
                    for (int i = 0; i < 8; i++) {
                        Vector2d maybePopular = new Vector2d(x, height - 1 - y).add(MapDirection.fromValue(i).toUnitVector());


                        if (maybePopular.getX() < 0) {
                            maybePopular = new Vector2d(width - 1, maybePopular.getY());
                        } else if (maybePopular.getX() > width - 1) {
                            maybePopular = new Vector2d(0, maybePopular.getY());
                        }

                        if (maybePopular.getY() >= 0 && maybePopular.getY() <= height - 1 ) {

                            ImageView imageView2 = gridLabels.get(height - 1 - maybePopular.getY()).get(maybePopular.getX());
                            imageView2.setStyle("-fx-effect: innershadow(gaussian, rgba(0, 200, 0, 0.25), 10, 0.8, 0, 0);");

                        }
                    }
                }
            }
        }
    }

    @Override
    public void mapChanged(WorldMap worldMap, List<String> messages,List<Vector2d> list) {
        Platform.runLater(()-> {
//            messageLabel.setText(message);
            mapVersion=Boolean.valueOf(messages.get(0));
            animalsCountLabel.setText(messages.get(1));
            grassCountLabel.setText(messages.get(2));
            avgEnergyLabel.setText(messages.get(3));
            freeSpaceLabel.setText(messages.get(4));
            genotypeLabel.setText(messages.get(5));
            avgAgeLabel.setText(messages.get(6));
            avgOffspringLabel.setText(messages.get(7));
            dominantGenotypePositionsLabel.setText("Dominant Genotype Positions: \n"+messages.get(8));

            listToHighlight = list;

            updateAboutFollowedAnimal();
            if(saveEveryDayToCSV) {
                try {
                    exportDataToCSV(messages);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }


            currentDay.setText("DAY: " + simulation.getDay());
            drawMap();
        });
    }

    public void pauseResume() {
        paused = !paused;
        if (paused) {
            pauseResumeButton.setText("Resume");
            highlightGrassButton.setVisible(true);
        } else {
            pauseResumeButton.setText("Pause");
            highlightGrassButton.setVisible(false);
        }
        simulation.setPaused(paused);
        highlightPopularGenome(true);

    }

    public void changeZoom() {
        mapGrid.setScaleX((zoomSlider.getValue()+1)/40);
        mapGrid.setScaleY((zoomSlider.getValue()+1)/40);
    }

    public void movePane(KeyCode keyCode) {
        switch (keyCode) {
            case LEFT -> translateX+=(zoomSlider.getValue()+1)/4;
            case RIGHT -> translateX-=(zoomSlider.getValue()+1)/4;
            case UP -> translateY+=(zoomSlider.getValue()+1)/4;
            case DOWN -> translateY-=(zoomSlider.getValue()+1)/4;
        }
        mapGrid.setTranslateX(translateX);
        mapGrid.setTranslateY(translateY);
    }

}
