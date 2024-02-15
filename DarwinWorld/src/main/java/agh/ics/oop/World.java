package agh.ics.oop;

import agh.ics.oop.model.*;
import javafx.application.Application;

public class World {


    public static void main(String[] args) throws InterruptedException {
        System.out.println("Start");
        Application.launch(SimulationApp.class);
//        try {
//            Simulation simulation = new Simulation(5,14,13,3,6, 25, 100, 10, 5, 30, 10 ,1,2, true, true);
//            SimulationEngine simulationEngine = new SimulationEngine();
//            simulationEngine.runAsyncInThreadPool(List.of(simulation));
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        }
        System.out.println("Stop");
    }


}
