package agh.ics.oop.model;


public enum MoveDirection {
    FORWARD(0),
    FORWARD_RIGHT(1),
    RIGHT(2),
    BACKWARD_RIGHT(3),
    BACKWARD(4),
    BACKWARD_LEFT(5),
    LEFT(6),
    FORWARD_LEFT(7);
    private final int value;

    MoveDirection(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MoveDirection fromValue(int value) {
        for (MoveDirection direction : MoveDirection.values()) {
            if (direction.value == value) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Invalid MoveDirection value: " + value);
    }
}
