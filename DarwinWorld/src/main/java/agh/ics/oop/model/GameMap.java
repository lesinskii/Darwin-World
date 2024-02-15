package agh.ics.oop.model;

import agh.ics.oop.Simulation;
import agh.ics.oop.model.util.MapVisualizer;

import java.util.*;

public class GameMap implements WorldMap {
    final Map<Vector2d, ArrayList<Animal>> animals;
    private final Map<Vector2d, Grass> grassMap;
    private final int width;
    private final int height;
    private final Random random = new Random();
    private final Object mapLock = new Object();
    private final List<MapChangeListener> observers = new ArrayList<>();
    private int energyPerGrass;
    private int energyUsedForReproduction;
    private int energyForBeingFull;
    private int genomeLength;
    private final Simulation simulation;
    private final int maxMutations;
    private final int minMutations;
    private int equatorMinHeight;
    private int equatorMaxHeight;

    private boolean walkingJungle;
    private boolean thereAndBack;
    private int totalDeadAnimalsAge = 0;
    private int totalDeadAnimalsCount = 0;

    //potrzebne do znajdowania najpopularniejszego genotypu
    private final Map<List<Integer>, Integer> genotypeCounter = new HashMap<>();
    private List<Integer> mostPopularGenotype = new ArrayList<>();

    //grassCount to startowa liczba roslin
    public GameMap(Simulation simulation, int width, int height, int grassCount, int energyPerGrass, int energyForBeingFull, int energyUsedForReproduction, int genomeLength, int minMutations, int maxMutations, boolean walkingJungle, boolean thereAndBack) {
        if(grassCount > width*height) {
            throw new IllegalArgumentException("Ilość trawy nie może być większa niż ilość dostępnych pól na mapie.");
        }
        this.width = width;
        this.height = height;
        this.animals = new HashMap<>();
        this.grassMap = new HashMap<>();
        this.energyPerGrass = energyPerGrass;
        this.energyForBeingFull = energyForBeingFull;
        this.energyUsedForReproduction = energyUsedForReproduction;
        this.genomeLength = genomeLength;
        this.simulation = simulation;
        this.minMutations=minMutations;
        this.maxMutations=maxMutations;
        this.walkingJungle = walkingJungle;
        this.thereAndBack = thereAndBack;

        setEquatorPosition(width, height);
        placeNewGrass(grassCount);
    }


    public void placeNewGrass(int addGrassNumber) {
        RandomPositionGenerator randomPositionGenerator = new RandomPositionGenerator(width, height, addGrassNumber,equatorMinHeight,equatorMaxHeight, new HashSet<>(grassMap.keySet()),true, walkingJungle);
        Iterator<Vector2d> positionsIterator = randomPositionGenerator.iterator();

        while (positionsIterator.hasNext()) {
            Vector2d grassPosition = positionsIterator.next();
            grassMap.put(grassPosition, new Grass(grassPosition, energyPerGrass));
        }
//        mapChanged("Putted grass");
    }
    //ustalenie obszaru równika
    private void setEquatorPosition(int width, int height) {
        int equatorPlace=(int) Math.round( (float) (width*height)*0.2);
        int eqLayers = (int) Math.round( (float) equatorPlace/width);
        equatorMinHeight=(int) Math.round( (float) (height-eqLayers)/2);
        equatorMaxHeight=equatorMinHeight+eqLayers-1;
    }



    public void addObserver(MapChangeListener observer) {
        observers.add(observer);
    }

    public void removeObserver(MapChangeListener observer) {
        observers.remove(observer);
    }

    protected void notifyObservers(List<String> message) {
        for (MapChangeListener observer : observers) {

            List<String> messages=new ArrayList<>();
            messages.add(String.valueOf(simulation.mapVersion()));
            messages.add(String.valueOf(numAnimals()));
            messages.add(String.valueOf(numGrass()));
            messages.add(String.valueOf(numAverageEnergy()));
            messages.add(String.valueOf(numFreePlaces()));
            List<Integer> mostPopularGenotype = getMostPopularGenotype();
            if (mostPopularGenotype.isEmpty()) {
                messages.add("-");
            } else {
                messages.add(String.valueOf(mostPopularGenotype));
            }
            messages.add(String.valueOf(getAverageDeadAnimalsAge()));

            messages.add(String.valueOf(getAvgOffspring()));


            StringBuilder dominantPositionsText = new StringBuilder();
            List<Vector2d> dominantGenotypePositions = this.getPositionsOfDominantGenotypeAnimals();
            int count=0;
            dominantPositionsText.append("{");
            for (Vector2d position : dominantGenotypePositions) {
                if (count>0){
                    dominantPositionsText.append(", ");
                }
                dominantPositionsText.append(position);
                count=1;
            }
            dominantPositionsText.append("}");
            messages.add(dominantPositionsText.toString());


            observer.mapChanged(this, messages,dominantGenotypePositions);
        }
    }

    public void mapChanged(List<String> message) {
        synchronized (mapLock) {
            notifyObservers(message);
        }
    }

    @Override
    public boolean place(Animal newAnimal) {

        if(animals.containsKey(newAnimal.getPosition())) {
            animals.get(newAnimal.getPosition()).add(newAnimal);
        }
        else {
            animals.put(newAnimal.getPosition(), new ArrayList<>(List.of(newAnimal)));
        }

        //genotyp
        List<Integer> genotype = newAnimal.getGenome().getGenome();
        genotypeCounter.put(genotype, genotypeCounter.getOrDefault(genotype, 0) + 1);

        // Sprawdzenie, czy aktualny genotyp jest bardziej popularny
        if (genotypeCounter.get(genotype) > genotypeCounter.getOrDefault(mostPopularGenotype, 0)) {
            mostPopularGenotype = genotype;
        }

//        mapChanged("Animal putted on "+newAnimal.getPosition());
        return true;
    }

    private ArrayList<Animal> resolveConflict(List<Animal> animalList) {
        ArrayList<Animal> sortedList = new ArrayList<>(animalList);
        sortedList.sort(new Comparator<Animal>() {
            @Override
            public int compare(Animal o1, Animal o2) {
                if (o1.getEnergy() > o2.getEnergy()) return -1;
                if (o1.getEnergy() < o2.getEnergy()) return 1;

                if (o1.getAge() > o2.getAge()) return -1;
                if (o1.getAge() < o2.getAge()) return 1;

                if (o1.getChildren() > o2.getChildren()) return -1;
                if (o1.getChildren() < o2.getChildren()) return 1;

                Random random = new Random();
                int randomInt = random.nextInt(2);
                return (randomInt == 0) ? -1 : 1;
            }
        });
        return sortedList;
    }

    //usuwanie martwych zwierzat
    public void deleteDeadAnimals() {
        Iterator<Map.Entry<Vector2d, ArrayList<Animal>>> iterator = animals.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Vector2d, ArrayList<Animal>> entry = iterator.next();
            List<Animal> animalList = entry.getValue();

            for (Animal animal : animalList) {
                if (animal.getEnergy() <= 0) {
                    totalDeadAnimalsAge += animal.getAge();
                    totalDeadAnimalsCount++;
                    animal.setDead();
                    List<Integer> genotype = animal.getGenome().getGenome();
                    genotypeCounter.put(genotype, genotypeCounter.getOrDefault(genotype, 0) - 1);

                }
            }
            animalList.removeIf(animal -> animal.getEnergy() <= 0);

            if (animalList.isEmpty()) {
                iterator.remove(); // Bezpieczne usunięcie pary z mapy
            }
        }
    }


    public void consumption() {
        Iterator<Map.Entry<Vector2d, ArrayList<Animal>>> iterator = animals.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Vector2d, ArrayList<Animal>> entry = iterator.next();
            if(isOccupiedGrass(entry.getKey())) {
                Grass eatenGrass = grassMap.remove(entry.getKey());
                ArrayList<Animal> animalList = entry.getValue();
                Animal happyAnimal;
                if(animalList.size() == 1) {
                    happyAnimal = animalList.get(0);
                }
                else {
                    happyAnimal = resolveConflict(animalList).get(0);
                }
                happyAnimal.changeEnergy(eatenGrass.getCaloricity());
                happyAnimal.incrementEatenGrass();
//                mapChanged("Animal at " + happyAnimal.getPosition() + " ate grass and gained " + eatenGrass.getCaloricity() + " energy.");
            }

        }
    }

    public void reproduction() {
        List<Animal> newAnimalList = new ArrayList<>();
        Iterator<Map.Entry<Vector2d, ArrayList<Animal>>> iterator = animals.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Vector2d, ArrayList<Animal>> entry = iterator.next();
            if(entry.getValue().size() > 1) {
                ArrayList<Animal> animalList = entry.getValue();
                ArrayList<Animal> sortedList = resolveConflict(animalList);
                Animal happyAnimal1 = sortedList.get(0);
                Animal happyAnimal2 = sortedList.get(1);
                if(happyAnimal1.getEnergy() >= energyForBeingFull && happyAnimal2.getEnergy() >= energyForBeingFull) {
                    Random random = new Random();

                    //tyle genów biorę od happyAnimal1:
                    double ratio = (double) (happyAnimal1.getEnergy()) /(happyAnimal1.getEnergy() + happyAnimal2.getEnergy());
                    int strongerGenes = (int) Math.round(ratio * genomeLength);
                    int orientation = random.nextInt(8);
                    int activeGene = random.nextInt(genomeLength);
                    int strongerSite = random.nextInt(2); //0 to lewa, 1 to prawa

                    List<Integer> genome = new ArrayList<>();
                    if(strongerSite == 0) {
                        for(int i = 0; i < strongerGenes; i++) {
                            genome.add(happyAnimal1.getGenome().getGenome().get(i));
                        }
                        for(int i = strongerGenes; i < genomeLength; i++) {
                            genome.add(happyAnimal2.getGenome().getGenome().get(i));
                        }
                    }
                    else {
                        for(int i = 0; i < genomeLength - strongerGenes; i++) {
                            genome.add(happyAnimal2.getGenome().getGenome().get(i));
                        }
                        for(int i = genomeLength - strongerGenes; i < genomeLength; i++) {
                            genome.add(happyAnimal1.getGenome().getGenome().get(i));
                        }
                    }

                    //System.out.println("Parent 1 Genome: " + happyAnimal1.getGenome().getGenome());
                    //System.out.println("Parent 2 Genome: " + happyAnimal2.getGenome().getGenome());
                    //System.out.println("Child Genome: " + genome);

                    // Mutacja genów
                    //TODO Mozna dac jakis błąd jeśli ktos da min > max ale nie wiem czy potrzebne
                    if (minMutations<=maxMutations){
                        int mutations = minMutations + random.nextInt(maxMutations - minMutations + 1);

                        List<Integer> genomeIndexes = new ArrayList<>();
                        for (int i = 0; i < genomeLength; i++) {
                            genomeIndexes.add(i);
                        }
                        Collections.shuffle(genomeIndexes, new Random());

                        for (int i = 0; i < mutations; i++) {
                            int mutatedIndex = genomeIndexes.get(i);
                            genome.set(mutatedIndex, random.nextInt(8)); // Pełna losowość - mutacja zmienia gen na dowolny inny gen
                        }
                    }
                    //System.out.println("Child Genome: " + genome);


                    happyAnimal1.changeEnergy(-1*energyUsedForReproduction);
                    happyAnimal2.changeEnergy(-1*energyUsedForReproduction);

                    //czy gen ma iśc do przodu czy do tyłu w przypadku thereAndBack
                    boolean goingForward = random.nextBoolean();

                    Animal newAnimal = new Animal(happyAnimal1.getPosition(), genome, energyUsedForReproduction*2, orientation, activeGene, thereAndBack, goingForward);
                    happyAnimal1.addOffspring(newAnimal);
                    happyAnimal2.addOffspring(newAnimal);
                    happyAnimal1.incrementChildren();
                    happyAnimal2.incrementChildren();
                    newAnimalList.add(newAnimal);



                }
            }
        }
        simulation.addAnimals(newAnimalList);
    }


    @Override
    public void move(Animal animal, MoveDirection direction) {
        if (animals.containsKey(animal.getPosition())) {
            Vector2d oldPosition = animal.getPosition();
            MapDirection oldOrientation = animal.getOrientation();

            // jeśli jest tylko jedno zwierzę na opuszczanej pozycji, to usuwam całe pole mapy, ale jeśli
            // pozostały tam jeszcze jakieś zwierzęta, to usuwam tylko dane zwierzę z listy
            if(animals.get(oldPosition).size() == 1) animals.remove(oldPosition);
            else animals.get(oldPosition).remove(animal);

            animal.move(direction, this);

            //jeśli na nowej pozycji już jest jakieś zwierzę, to dopisujemy nowe zwierzę do listy
            //w przeciwnym wypadku tworzymy nową pozycję
            if(animals.containsKey(animal.getPosition())) {
                animals.get(animal.getPosition()).add(animal);
            }
            else {
                animals.put(animal.getPosition(), new ArrayList<Animal>(List.of(animal)));
            }

//            mapChanged("Moved animal from [(" + oldPosition.getX() + ", " + oldPosition.getY() + "), " + oldOrientation + "] to [(" + animal.getPosition().getX() + ", " + animal.getPosition().getY()+ "), " + animal.getOrientation() + "]");
            //checkIfAnimalAteGrass(animal);

        }
    }


    @Override
    public boolean isOccupied(Vector2d position) {
        return animals.containsKey(position) || grassMap.containsKey(position);
    }

    //niepotrzebne
//    public boolean isOccupiedAnimal(Vector2d position) {
//        return animals.containsKey(position);
//    }
    @Override
    public boolean isOccupiedGrass(Vector2d position) {
        return grassMap.containsKey(position);
    }

    @Override
    public WorldElement objectAt(Vector2d position) {
        List<Animal> animalList = animals.get(position);
        if (animalList != null) {
            return animalList.get(0);
        }
        return grassMap.get(position);
    }


    public Map<Vector2d, ArrayList<Animal>> getAnimals() {
        return animals;
    }

    public Map<Vector2d, Grass> grassMap() {
        return grassMap;
    }

    @Override
    public int getHeight() {
        return height;
    }
    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getEquatorMinHeight() {
        return equatorMinHeight;
    }

    @Override
    public int getEquatorMaxHeight() {
        return equatorMaxHeight;
    }

    @Override
    public String toString(){
        MapVisualizer visualizer = new MapVisualizer(this);
        return visualizer.draw(new Vector2d(0, 0), new Vector2d(width - 1, height - 1));
    }
    public int numAnimals() {
        int totalAnimals = 0;
        for (ArrayList<Animal> animalList : animals.values()) {
            for (Animal animal : animalList) {
                if (animal.getEnergy() > 0) {
                    totalAnimals++;
                }
            }
        }
        return totalAnimals;
        //System.out.println("Liczba zwierząt: "+ totalAnimals);
    }
    public int numGrass() {
        //System.out.println("Liczba trawy: "+grassMap.size());
        return grassMap.size();
    }
    public int numFreePlaces(){
        //System.out.println("Liczba wolnych pol: "+((width*height)-grassMap.size()-animals.size()));
        return ((width*height)-grassMap.size()-animals.size());
    }
    public double numAverageEnergy() {
        double totalEnergy = 0;
        int livingAnimalsCount = 0;
        double avg;

        for (List<Animal> animalList : animals.values()) {
            for (Animal animal : animalList) {
                if (animal.getEnergy() > 0) {
                    totalEnergy += animal.getEnergy();
                    livingAnimalsCount++;
                }
            }
        }

        if (livingAnimalsCount > 0) {
            return Math.round((totalEnergy / livingAnimalsCount) * 100.0) / 100.0;
        } else {
            return 0;
        }
    }
    public List<Integer> getMostPopularGenotype() {
        return mostPopularGenotype;
        /*
        if (genotypeCounter.values().stream().allMatch(count -> count == 1)) {
            return Collections.emptyList();
        } else {
            return mostPopularGenotype;
        }

         */
    }
    public double getAverageDeadAnimalsAge() {
        return totalDeadAnimalsCount > 0
                ? Math.round((totalDeadAnimalsAge / (double) totalDeadAnimalsCount) * 100.0) / 100.0
                : 0;
    }

    public List<Vector2d> getPositionsOfDominantGenotypeAnimals() {
        List<Vector2d> positions = new ArrayList<>();

        for (Map.Entry<Vector2d, ArrayList<Animal>> entry : animals.entrySet()) {
            List<Animal> animalList = entry.getValue();
            for (Animal animal : animalList) {
                if (!animal.isDead() && animal.getGenome().getGenome().equals(mostPopularGenotype)) {
                    positions.add(animal.getPosition());
                }
            }
        }

        return positions;
    }

    public double getAvgOffspring() {
        double totalOffspringCount = 0;
        int totalAnimalsCount = 0;

        for (List<Animal> animalList : animals.values()) {
            for (Animal animal : animalList) {
                totalOffspringCount += animal.getCountOffspring();
                totalAnimalsCount++;
            }
        }

        return totalAnimalsCount > 0
                ? Math.round((totalOffspringCount / totalAnimalsCount) * 100.0) / 100.0
                : 0;
    }

}