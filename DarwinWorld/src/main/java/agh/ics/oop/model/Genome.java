package agh.ics.oop.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Genome {

    private List<Integer> genome = new ArrayList<>();
    private int activeGene;
    private final int genomeLength;
    private final boolean thereAndBack;
    private boolean goingForward;


    //dwa konstruktory; jeden - przy reprodukcji; drugi przy tworzeniu zwierząt na starcie mapy
    public Genome(List<Integer> genome, int activeGene, boolean thereAndBack, boolean goingForward) {
        this.genome = genome;
        genomeLength = genome.size();
        this.activeGene = activeGene;
        this.thereAndBack = thereAndBack;
        this.goingForward = goingForward;
    }

    public Genome(int length, boolean thereAndBack) {
        activeGene = 0;
        genomeLength = length;
        this.thereAndBack = thereAndBack;
        this.goingForward = true;
        Random random = new Random();

        for(int i=0; i<length; i++) {
            int gene = random.nextInt(8);
            genome.add(gene);
        }
    }

    public List<Integer> getGenome() {
        return genome;
    }

    @Override
    public String toString() {
        String genomeString = "";
        for(int i=0; i<genomeLength; i++) {
            if(activeGene == i) {
                genomeString += " [" + genome.get(i) + "]";
            }
            else {
                genomeString += " " + genome.get(i);
            }
        }

        return genomeString;
    }

    public void incrementActiveGene() {
        if(!thereAndBack) {
            this.activeGene = (this.activeGene + 1) % genomeLength;
        }
        else {
            int tempGene;
            tempGene = goingForward ? this.activeGene+1 : this.activeGene-1;
            if(tempGene < 0) {
                tempGene = 0;
                goingForward = !goingForward;
            } else if (tempGene > genomeLength-1) {
                tempGene = genomeLength-1;
                goingForward = !goingForward;
            }

            this.activeGene=tempGene;
        }
    }

    public int getActiveGene() {
        return genome.get(activeGene);
    }
}
