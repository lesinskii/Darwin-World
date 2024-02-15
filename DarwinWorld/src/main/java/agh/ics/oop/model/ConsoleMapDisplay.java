package agh.ics.oop.model;

import java.util.List;

public class ConsoleMapDisplay implements MapChangeListener{
    private int updateCount=0;
    public String lastMessage;
    @Override
    public void mapChanged(WorldMap worldMap, List<String> message,List<Vector2d> list) {
        System.out.println("Map update #" + (++updateCount) + ": " + message);
        // Wyświetlanie informacji o energii zwierząt
        if (worldMap instanceof GameMap) {
            GameMap gameMap = (GameMap) worldMap;

            //zmienione pod to że trzymamy listę zwierząt
            ((GameMap) worldMap).numAnimals();
            ((GameMap) worldMap).numGrass();
            ((GameMap) worldMap).numFreePlaces();
            ((GameMap) worldMap).numAverageEnergy();
            ((GameMap) worldMap).getMostPopularGenotype();
            for (List<Animal> animalList : gameMap.getAnimals().values()) {
                for (Animal animal : animalList) {
                    System.out.println("Animal at " + animal.getPosition() + " has energy: " + animal.getEnergy());
                }
            }

            // Wyświetlanie informacji o kaloryczności roślin
//            for (Grass grass : gameMap.grassMap().values()) {
//                System.out.println("Grass at " + grass.getPosition() + " has caloricity: " + grass.getCaloricity());
//            }
        }
        System.out.println(worldMap.toString());



        lastMessage="Map update #" + (updateCount) + ": " + message+" "+"Total updates received: " + updateCount;
    }


}
