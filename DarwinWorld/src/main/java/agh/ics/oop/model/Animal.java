package agh.ics.oop.model;

import java.util.ArrayList;
import java.util.List;

public class Animal implements WorldElement {
    private MapDirection orientation;
    private Vector2d position;
    private final Genome genome;
    private List<Animal> offspring;

    private int energy;
    private int age;
    private int children;
    private int eatenGrass = 0;
    private boolean isDead = false;

    public Animal(Vector2d pos, List<Integer> genome, int animalEnergy, int orientation, int activeGene, boolean thereAndBack, boolean goingForward) {
        this.orientation = MapDirection.fromValue(orientation);
        position = pos;
        this.genome = new Genome(genome, activeGene, thereAndBack, goingForward);
        energy = animalEnergy;
        age = 0;
        this.offspring=new ArrayList<>();
    }

    public Animal(Vector2d pos, int genomeLength, int animalEnergy, boolean thereAndBack) {
        orientation = MapDirection.NORTH;
        position = pos;
        genome = new Genome(genomeLength, thereAndBack);
        energy = animalEnergy;
        age = 0;
        this.offspring=new ArrayList<>();

    }

    public void setDead() {
        isDead = true;
    }

    public boolean isDead() {
        return isDead;
    }
    public void addOffspring(Animal child) {
        offspring.add(child);
    }
    public List<Animal> getOffspring() {
        List<Animal> offspringList = new ArrayList<>();

        for (Animal child : offspring) {
            offspringList.add(child);
            offspringList.addAll(child.getOffspring());
        }
        return offspringList;
    }

    @Override
    public String toString() {
        return switch(orientation.getValue()) {
            case 0 -> "↑";
            case 1 -> "↗";
            case 2 -> "→";
            case 3 -> "↘";
            case 4 -> "↓";
            case 5 -> "↙";
            case 6 -> "←";
            case 7 -> "↖";

            default -> throw new IllegalStateException("Unexpected value: " + orientation);
        };
    }
    public void move(MoveDirection direction, WorldMap moveValidator) {
        energy--;


        this.orientation = MapDirection.fromValue((this.orientation.getValue() + direction.getValue()) % 8);
        Vector2d newPosition = this.position.add(this.orientation.toUnitVector());

        // Sprawdzanie, czy zwierzę znajduje się na krańcach mapy
        if (newPosition.getY() < 0 || newPosition.getY() >= moveValidator.getHeight()) {
            this.orientation = MapDirection.fromValue((this.orientation.getValue() + 4)%8);
            newPosition = this.position;
        }
        else if (newPosition.getX() < 0) {
            newPosition = new Vector2d(moveValidator.getWidth()-1, newPosition.getY());
        }
        else if (newPosition.getX() >= moveValidator.getWidth()) {
            newPosition = new Vector2d(0, newPosition.getY());
        }

        this.position = newPosition;
    }


    @Override
    public Vector2d getPosition() {
        return position;
    }

    public MapDirection getOrientation() {
        return orientation;
    }

    public Genome getGenome() {
        return genome;
    }

    public int getEnergy() {
        return energy;
    }
    public void changeEnergy(int energy){
        this.energy += energy;
    }

    public int getAge() {
        return age;
    }

    public void incrementAge() {
        this.age++;
    }

    public int getChildren() {
        return children;
    }

    public int getEatenGrass() { return eatenGrass; }
    public int getCountOffspring(){
        if (offspring.isEmpty()){
            return 0;
        }
        return this.getOffspring().size();
    }

    public void incrementEatenGrass() {
        eatenGrass++;
    }

    public void incrementChildren() {
        this.children++;
    }

}