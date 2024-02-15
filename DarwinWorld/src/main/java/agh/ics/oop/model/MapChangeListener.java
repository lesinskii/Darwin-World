package agh.ics.oop.model;

import java.util.List;

public interface MapChangeListener {
    void mapChanged(WorldMap worldMap, List<String> messages, List<Vector2d> list);


}

