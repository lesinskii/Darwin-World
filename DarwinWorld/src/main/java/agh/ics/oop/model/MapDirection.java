package agh.ics.oop.model;

public enum MapDirection {
    NORTH(0),
    NORTHEAST(1),
    EAST(2),
    SOUTHEAST(3),
    SOUTH(4),
    SOUTHWEST(5),
    WEST(6),
    NORTHWEST(7);

    private final int value;

    MapDirection(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MapDirection fromValue(int value) {
        for (MapDirection direction : MapDirection.values()) {
            if (direction.value == value) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Invalid MapDirection value: " + value);
    }

    public Vector2d toUnitVector() {
        return switch (value) {
            case 0 -> new Vector2d(0,1);
            case 1 -> new Vector2d(1,1);
            case 2 -> new Vector2d(1,0);
            case 3 -> new Vector2d(1,-1);
            case 4 -> new Vector2d(0,-1);
            case 5 -> new Vector2d(-1,-1);
            case 6 -> new Vector2d(-1,0);
            case 7 -> new Vector2d(-1,1);

            default -> throw new IllegalStateException("Unexpected value: " + value);
        };
    }

}
