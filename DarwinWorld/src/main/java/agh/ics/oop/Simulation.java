package agh.ics.oop;

import agh.ics.oop.model.*;
import agh.ics.oop.presenter.SingleSimulationPresenter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Simulation implements Runnable {



    private List<Animal> animals = new ArrayList<>();
    private GameMap worldMap;
    private int genomeLength;
    private int day; //kalendarz, na bierząco przechowuje ile "dni" upłynęło od początku symulacji
    private int animalEnergy;
    private int sleepMillisTime;
    private int energyPerGrass;
    private int addGrassNumber;
    private int energyUsedForReproduction;
    private boolean mapVersion;



    private int energyForBeingFull;
    private SingleSimulationPresenter singleSimulationPresenter;
    private boolean paused = false;
    private final Object lock = new Object(); //zmienna do synchronizacji

    public int getEnergyForBeingFull() {
        return energyForBeingFull;
    }
    public Simulation(int numOfAnimals, int width, int height, int grassCount, int genomeLength, int animalEnergy, int sleepMillisTime, int energyPerGrass, int addGrassNumber, int energyForBeingFull, int energyUsedForReproduction, int minMutations, int maxMutations, boolean walkingJungle, boolean thereAndBack, SingleSimulationPresenter singleSimulationPresenter) throws IllegalArgumentException {
        if(energyUsedForReproduction >= energyForBeingFull) throw new IllegalArgumentException("Energia zużywana przy reprodukcji nie może być większa, niż energia najedzenia");

        this.worldMap = new GameMap(this, width, height, grassCount, energyPerGrass, energyForBeingFull, energyUsedForReproduction, genomeLength,minMutations,maxMutations, walkingJungle, thereAndBack);
        this.genomeLength = genomeLength;
        this.day = 0;
        this.animalEnergy = animalEnergy;
        this.sleepMillisTime = sleepMillisTime;
        this.energyPerGrass = energyPerGrass;
        this.addGrassNumber = addGrassNumber;
        this.energyForBeingFull = energyForBeingFull;
        this.energyUsedForReproduction = energyUsedForReproduction;
        this.singleSimulationPresenter = singleSimulationPresenter;
        this.mapVersion=walkingJungle;


        // ustawianie zwierząt na losowych pozycjach na mapie
        RandomPositionGenerator randomPositionGenerator = new RandomPositionGenerator(width, height, numOfAnimals, worldMap.getEquatorMinHeight(), worldMap.getEquatorMaxHeight(), false, walkingJungle);
        Iterator<Vector2d> positionsIterator = randomPositionGenerator.iterator();
        while (positionsIterator.hasNext()) {
            Vector2d animalPosition = positionsIterator.next();
            animals.add(new Animal(animalPosition, genomeLength, animalEnergy, thereAndBack));
        }

        //ustawianie obserwatorów
//        ConsoleMapDisplay consoleMapDisplay = new ConsoleMapDisplay();
//        worldMap.addObserver(consoleMapDisplay);
        worldMap.addObserver(singleSimulationPresenter);

    }

    public void addAnimals(List<Animal> animalList) {
        animals.addAll(animalList);
        for(Animal newAnimal : animalList) {
            worldMap.place(newAnimal);
        }

    }

    public void run() {
        Iterator<Animal> iterator = animals.iterator();



        while(iterator.hasNext()) {
            Animal animal = iterator.next();
            worldMap.place(animal);

        }

        //sprawdzenie czy zwierzęta pojawiły się na trawie
        worldMap.consumption();


        while(true) {
            try {
                Thread.sleep(sleepMillisTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            synchronized (lock) {
                while (paused) {
                    try {
                        // Czekaj, dopóki paused jest true
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            // sprzątanie mapy po martwych zwierzętach
            animals.removeIf(animal -> animal.getEnergy() <= 0);
            worldMap.deleteDeadAnimals();

            //przemieszczanie zwierząt zgodnie z genami
            for(Animal animal : animals) {
                worldMap.move(animal, MoveDirection.fromValue(animal.getGenome().getActiveGene()));
            }
            //konsumpcja roślin
            worldMap.consumption();

            //rozmnażanie
            worldMap.reproduction();



            //wyrastanie nowych roślin
            worldMap.placeNewGrass(addGrassNumber);



            //inkrementacja danych w Animal
            for(Animal animal : animals) {
                animal.incrementAge();
                animal.getGenome().incrementActiveGene();
            }
            day++;

            //rysowanie nowej mapy
            worldMap.mapChanged(new ArrayList<>(List.of("", "")));
        }


    }

    public int getDay() {
        return day;
    }
    public boolean mapVersion(){ return mapVersion; }

    public List<Animal> getAnimals() {
        return animals;
    }
    public WorldMap getWorldMap() {
        return worldMap;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        if(!paused) {
            synchronized (lock) {
                lock.notify();
            }
        }
    }
}
