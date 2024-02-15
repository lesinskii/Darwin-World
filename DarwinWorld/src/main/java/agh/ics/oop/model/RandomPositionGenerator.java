package agh.ics.oop.model;
import java.util.*;

public class RandomPositionGenerator implements Iterable<Vector2d> {

    private final int maxWidth;
    private final int maxHeight;
    private final int elementCount;
    private final int equatorMinHeight;
    private final int equatorMaxHeight;
    private final HashSet<Vector2d> bannedPlaces;
    private int lastIndex;
    private final boolean isGrass;
    private final boolean walkingJungle;

    public RandomPositionGenerator(int maxWidth, int maxHeight, int elementCount, int equatorMinHeight, int equatorMaxHeight, HashSet<Vector2d> bannedPlaces, boolean isGrass, boolean walkingJungle) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.elementCount = elementCount;
        this.equatorMinHeight = equatorMinHeight;
        this.equatorMaxHeight = equatorMaxHeight;
        this.bannedPlaces = bannedPlaces;
        this.isGrass=isGrass;
        this.walkingJungle = walkingJungle;
    }

    public RandomPositionGenerator(int maxWidth, int maxHeight, int elementCount, int equatorMinHeight, int equatorMaxHeight, boolean isGrass, boolean walkingJungle) {
        this(maxWidth, maxHeight, elementCount, equatorMinHeight, equatorMaxHeight, new HashSet<>(),isGrass, walkingJungle);
    }

    //generowanie miejsc dla trawy
    private List<Vector2d> generateRandomPositionsGrassStandard() {
        List<Vector2d> positions = new ArrayList<>();
        List<Vector2d> equatorPositions = new ArrayList<>();
        List<Vector2d> otherPositions = new ArrayList<>();

        for (int i = 0; i < maxWidth; i++) {
            for (int j = 0; j < maxHeight; j++) {
                Vector2d position = new Vector2d(i, j);

                // Jeśli dane miejsce jest niedozwolone, pomijamy je
                if (!bannedPlaces.contains(position)) {
                    if (isOnEquator(j)) {
                        equatorPositions.add(position);
                    } else {
                        otherPositions.add(position);
                    }
                }
            }
        }

        Random random = new Random();

        for (int i = 0; i < elementCount; i++) {
            // 80% szans na wybór pozycji na równiku
            if (!equatorPositions.isEmpty() && (random.nextDouble() < 0.8)) {
                positions.add(equatorPositions.remove(random.nextInt(equatorPositions.size())));
            } else if (!otherPositions.isEmpty()) {
                positions.add(otherPositions.remove(random.nextInt(otherPositions.size())));
            } else {
                // Oba zbiory są puste, nie ma więcej miejsc
                break;
            }
        }

        lastIndex = positions.size();
        return positions;
    }

    private List<Vector2d> generateRandomPositionsGrassWalkingJungle() {
        List<Vector2d> positions = new ArrayList<>();
        List<Vector2d> popularPositions = new ArrayList<>();
        List<Vector2d> otherPositions = new ArrayList<>();

        for(Vector2d position : bannedPlaces) {
            for(int i = 0; i < 8; i++) {
                Vector2d maybePopular = position.add(MapDirection.fromValue(i).toUnitVector());

                //te zmiany musza być najpierw, aby zadziałały warunki contains na odpowiednich wektorach
                if(maybePopular.getX()<0) {
                    maybePopular = new Vector2d(maxWidth-1, maybePopular.getY());
                }
                else if(maybePopular.getX()>maxWidth-1) {
                    maybePopular = new Vector2d(0, maybePopular.getY());
                }

                //sprawdzam czy pozycja nie wybiega ponad bieguny, czy już nie ma trawy na tej pozycji oraz czy jeszcze nie ma jej w liście popularnych miejsc
                if(maybePopular.getY()>=0 && maybePopular.getY()<=maxHeight-1 && !bannedPlaces.contains(maybePopular) && !popularPositions.contains(maybePopular)) {
                    popularPositions.add(maybePopular);
                }
            }
        }

        for(int i=0; i<maxWidth; i++) {
            for(int j=0; j<maxHeight; j++) {
                //jeśli dane miejsce jest niedozwolone, pomijamy je
                if(!bannedPlaces.contains(new Vector2d(i,j)) && !popularPositions.contains(new Vector2d(i,j))) {
                    otherPositions.add(new Vector2d(i,j));
                }

            }
        }

        Random random = new Random();

        for (int i = 0; i < elementCount; i++) {
            // 80% szans na wybór pozycji przy roślinie
            if (!popularPositions.isEmpty() && (random.nextDouble() < 0.8)) {
                positions.add(popularPositions.remove(random.nextInt(popularPositions.size())));
            } else if (!otherPositions.isEmpty()) {
                positions.add(otherPositions.remove(random.nextInt(otherPositions.size())));
            } else {
                // Oba zbiory są puste, nie ma więcej miejsc
                break;
            }
        }

        lastIndex = positions.size();
        return positions;
    }

    private List<Vector2d> generateRandomPositionsGrass() {
        if(walkingJungle) return generateRandomPositionsGrassWalkingJungle();
        return generateRandomPositionsGrassStandard();
    }

    //generowanie miejsc dla zwierząt
    private List<Vector2d> generateRandomPositions() {
        List<Vector2d> positions = new ArrayList<>();

        for(int i=0; i<maxWidth; i++) {
            for(int j=0; j< maxHeight; j++) {
                //jeśli dane miejsce jest niedozwolone, pomijamy je
                if(!bannedPlaces.contains(new Vector2d(i,j))) {
                    positions.add(new Vector2d(i,j));
                }

            }
        }

        Collections.shuffle(positions, new Random());

        lastIndex = Math.min(elementCount, positions.size());
        return positions.subList(0, lastIndex);

    }


    private boolean isOnEquator(int height) {
        return height >= equatorMinHeight && height <= equatorMaxHeight;
    }

    @Override
    public Iterator<Vector2d> iterator() {
        return new Iterator<Vector2d>() {

            private int current = -1;
            private final List<Vector2d> positions = isGrass ? generateRandomPositionsGrass() : generateRandomPositions();

            @Override
            public boolean hasNext() {
                return current + 1 < lastIndex;
            }

            @Override
            public Vector2d next() {
                current++;
                return positions.get(current);
            }
        };
    }

}
