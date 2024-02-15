package agh.ics.oop.presenter;

import agh.ics.oop.Simulation;
import agh.ics.oop.SimulationApp;
import agh.ics.oop.SimulationEngine;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SettingsPresenter extends Stage {

    public Label finalInfoSaving;
    public Label finalInfoOpening;
    public CheckBox showAnimalEnergyCheckbox;
    public CheckBox saveEveryDayToCSV;
    public Label finalInfoChoosingEveryDayPath;
    @FXML
    private Spinner<Integer> numOfAnimals = new Spinner<Integer>();
    @FXML
    private Spinner<Integer> width = new Spinner<Integer>();
    @FXML
    private Spinner<Integer> height = new Spinner<Integer>();
    @FXML
    private Spinner<Integer> grassCount = new Spinner<Integer>();
    @FXML
    private Spinner<Integer> genomeLength = new Spinner<Integer>();
    @FXML
    private Spinner<Integer> animalEnergy = new Spinner<Integer>();
    @FXML
    private Spinner<Integer> sleepMillisTime = new Spinner<Integer>();
    @FXML
    private Spinner<Integer> energyPerGrass = new Spinner<Integer>();
    @FXML
    private Spinner<Integer> addGrassNumber = new Spinner<Integer>();

    @FXML
    private Spinner<Integer> energyForBeingFull = new Spinner<Integer>();
    @FXML
    private Spinner<Integer> energyUsedForReproduction = new Spinner<Integer>();
    @FXML
    private Spinner<Integer> minMutations = new Spinner<Integer>();
    @FXML
    private Spinner<Integer> maxMutations = new Spinner<Integer>();
    @FXML
    private ComboBox<String> walkingJungle = new ComboBox<String>();
    @FXML
    private ComboBox<String> thereAndBack = new ComboBox<String>();

    public Label infoError;

    private FileWriter saveEveryDayWriter = null;


    public void startNewWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("singleSimulation.fxml"));
        BorderPane viewRoot = loader.load();

        SingleSimulationPresenter additionalPresenter = loader.getController();

        Stage additionalStage = new Stage();

        SimulationApp.configureStage(additionalStage, viewRoot);
        additionalStage.show();
        initSimulation(additionalPresenter);
    }

    public void onSimulationStartClicked() throws IOException {

        if (isInputValid()){
            startNewWindow();
        }
    }

    private boolean isInputValid(){
        if (genomeLength.getValue()<maxMutations.getValue()){
            infoError.setText("The number of min mutations must be less or equal than the genome length! ");
            return false;
        }
        if (numOfAnimals.getValue()>(height.getValue()*width.getValue())){
            infoError.setText("The number of animals must be less than the available space! ");
            return false;
        }
        if (grassCount.getValue()>(height.getValue()*width.getValue())){
            infoError.setText("The number of grass must be less than the available space! ");
            return false;
        }
        if (minMutations.getValue()>maxMutations.getValue()){
            infoError.setText("The number of min mutations must be greater than the number of max mutations! ");
            return false;
        }
        if (energyForBeingFull.getValue()<=energyUsedForReproduction.getValue()){
            infoError.setText("The number of energy for being full must be greater than the energy used for reproduction! ");
            return false;
        }

        infoError.setText("");
        return true;
    }


    private void initSimulation(SingleSimulationPresenter presenter) {
        try {
            Simulation simulation = new Simulation(numOfAnimals.getValue(), width.getValue(), height.getValue(), grassCount.getValue(), genomeLength.getValue(), animalEnergy.getValue(), sleepMillisTime.getValue(), energyPerGrass.getValue(), addGrassNumber.getValue(), energyForBeingFull.getValue(), energyUsedForReproduction.getValue() , minMutations.getValue(), maxMutations.getValue(), Objects.equals(walkingJungle.getValue(), "Walking jungle"), Objects.equals(thereAndBack.getValue(), "There and back"), presenter);

            presenter.setSaveEveryDayToCSV(saveEveryDayToCSV.isSelected());
            presenter.setWriter(saveEveryDayWriter);
            presenter.setShowEnergy(showAnimalEnergyCheckbox.isSelected());
            presenter.setSimulation(simulation);
            presenter.setWorldMap(simulation.getWorldMap());
            presenter.setHeight(simulation.getWorldMap().getHeight());
            presenter.setWidth(simulation.getWorldMap().getWidth());
            presenter.drawEmptyMap();


            SimulationEngine simulationEngine = new SimulationEngine();
            simulationEngine.runAsyncInThreadPool(List.of(simulation));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void saveToCSV() throws IOException {
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Zapisz jako");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Pliki CSV (*.csv)", "*.csv")
        );

        File selectedFile = fileChooser.showSaveDialog(this);
        String finalPathName = selectedFile.getAbsolutePath();

        try {
            FileWriter writer = new FileWriter(selectedFile);

            writer.write("numOfAnimals;" + numOfAnimals.getValue().toString() + "\n");
            writer.write("width;" + width.getValue().toString() + "\n");
            writer.write("height;" + height.getValue().toString() + "\n");
            writer.write("grassCount;" + grassCount.getValue().toString() + "\n");
            writer.write("genomeLength;" + genomeLength.getValue().toString() + "\n");
            writer.write("animalEnergy;" + animalEnergy.getValue().toString() + "\n");
            writer.write("sleepMillisTime;" + sleepMillisTime.getValue().toString() + "\n");
            writer.write("energyPerGrass;" + energyPerGrass.getValue().toString() + "\n");
            writer.write("addGrassNumber;" + addGrassNumber.getValue().toString() + "\n");
            writer.write("energyForBeingFull;" + energyForBeingFull.getValue().toString() + "\n");
            writer.write("energyUsedForReproduction;" + energyUsedForReproduction.getValue().toString() + "\n");
            writer.write("minMutations;" + minMutations.getValue().toString() + "\n");
            writer.write("maxMutations;" + maxMutations.getValue().toString() + "\n");
            writer.write("walkingJungle;" + walkingJungle.getValue() + "\n");
            writer.write("thereAndBack;" + thereAndBack.getValue() + "\n");
            writer.write("showAnimalEnergyCheckbox;" + showAnimalEnergyCheckbox.isSelected() + "\n");
            writer.write("saveEveryDayToCSV;" + saveEveryDayToCSV.isSelected() + "\n");

            finalInfoSaving.setText("Succesfuly saved: " + finalPathName);

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV file");

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Pliki CSV (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);

        File selectedFile = fileChooser.showOpenDialog(this);

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(selectedFile.getAbsolutePath()));
            String line;

            List<String> parameters = new ArrayList<>();
            while((line = reader.readLine()) != null) {
                String[] fields = line.split(";");
                parameters.add(fields[1]);
            }

            numOfAnimals.getValueFactory().setValue(Integer.parseInt(parameters.get(0)));
            width.getValueFactory().setValue(Integer.parseInt(parameters.get(1)));
            height.getValueFactory().setValue(Integer.parseInt(parameters.get(2)));
            grassCount.getValueFactory().setValue(Integer.parseInt(parameters.get(3)));
            genomeLength.getValueFactory().setValue(Integer.parseInt(parameters.get(4)));
            animalEnergy.getValueFactory().setValue(Integer.parseInt(parameters.get(5)));
            sleepMillisTime.getValueFactory().setValue(Integer.parseInt(parameters.get(6)));
            energyPerGrass.getValueFactory().setValue(Integer.parseInt(parameters.get(7)));
            addGrassNumber.getValueFactory().setValue(Integer.parseInt(parameters.get(8)));
            energyForBeingFull.getValueFactory().setValue(Integer.parseInt(parameters.get(9)));
            energyUsedForReproduction.getValueFactory().setValue(Integer.parseInt(parameters.get(10)));
            minMutations.getValueFactory().setValue(Integer.parseInt(parameters.get(11)));
            maxMutations.getValueFactory().setValue(Integer.parseInt(parameters.get(12)));
            walkingJungle.setValue(parameters.get(13));
            thereAndBack.setValue(parameters.get(14));
            showAnimalEnergyCheckbox.setSelected(Boolean.parseBoolean(parameters.get(15)));
            saveEveryDayToCSV.setSelected(Boolean.parseBoolean(parameters.get(16)));

            finalInfoOpening.setText("Succesfuly opened: " + selectedFile.getAbsolutePath());

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void chooseDirectoryForEveryDay() {
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Zapisz jako");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Pliki CSV (*.csv)", "*.csv")
        );

        File selectedFile = fileChooser.showSaveDialog(this);

        if(selectedFile != null) {
            try {
                saveEveryDayWriter = new FileWriter(selectedFile);
                finalInfoChoosingEveryDayPath.setText("Selected succesfully" + selectedFile.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

}