package agh.ics.oop.model;

public class Grass implements WorldElement {
    private final Vector2d position;
    private int caloricity;
    public Grass(Vector2d position, int energyPerGrass){
        this.position=position;
        this.caloricity=energyPerGrass;
    }

    @Override
    public Vector2d getPosition(){
        return this.position;
    }

    @Override
    public String toString(){
        return "*";
    }

    public int getCaloricity() {
        return caloricity;
    }

}
