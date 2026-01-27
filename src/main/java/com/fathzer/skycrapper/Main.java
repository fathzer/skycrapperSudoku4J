package com.fathzer.skycrapper;

import static com.fathzer.skycrapper.SkyscraperSolver.*;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

public class Main {
    public static void main(String[] args) throws ContradictionException, TimeoutException {
    	int nbLoops = Integer.getInteger("loops", 1);
    	int warmup = Integer.getInteger("warmup",0);
    	
    	String input = args[0];
    	
    	for (int i=0;i<warmup;i++) {
    		solve(input);
    	}
    	
    	final long start = System.currentTimeMillis();
    	for (int i=0;i<nbLoops;i++) {
    		int[][] solution = solve(input);
    		if (i==nbLoops-1) {
    			final long end = System.currentTimeMillis();
                System.out.println("Time: " + (end - start) / nbLoops + " ms");
                printSolution(solution);
    		}
    	}
    }
    
    private static int[][] solve(String input) throws ContradictionException, TimeoutException {
        InputDataParser parser = new InputDataParser();
        InputData data = parser.parse(input);
        SkyscraperSolver solver = new SkyscraperSolver(data.size());
        add(solver, DIRECTION_UP, data.up());
        add(solver, DIRECTION_DOWN, data.down());
        add(solver, DIRECTION_LEFT, data.left());
        add(solver, DIRECTION_RIGHT, data.right());
        return solver.solve();
    }

    private static void add(SkyscraperSolver solver, int direction, int[] clues) {
        for (int i = 0; i < clues.length; i++) {
            solver.setVisibilityConstraint(direction, i, clues[i]);
        }
    }

    private static void printSolution(int[][] solution) {
        for (int i = 0; i < solution.length; i++) {
            for (int j = 0; j < solution[i].length; j++) {
                System.out.print(solution[i][j] + " ");
            }
            System.out.println();
        }
    }
}
