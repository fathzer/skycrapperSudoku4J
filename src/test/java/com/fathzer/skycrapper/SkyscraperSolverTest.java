package com.fathzer.skycrapper;

import org.junit.jupiter.api.Test;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class SkyscraperSolverTest {

    @Test
    void testSimple2x2NoConstraints() throws ContradictionException, TimeoutException {
        SkyscraperSolver solver = new SkyscraperSolver(2);
        int[][] solution = solver.solve();
        
        assertNotNull(solution, "Solution should exist");
        assertEquals(2, solution.length);
        assertEquals(2, solution[0].length);
        
        // Verify it's a valid Sudoku
        assertValidSudoku(solution);
    }
    
    @Test
    void testSimple2x2WithInitialValue() throws ContradictionException, TimeoutException {
        SkyscraperSolver solver = new SkyscraperSolver(2);
        solver.setInitialValue(0, 0, 1);
        
        int[][] solution = solver.solve();
        
        assertNotNull(solution, "Solution should exist");
        assertEquals(1, solution[0][0], "Initial value should be preserved");
        assertValidSudoku(solution);
    }
    
    @Test
    void test4x4NoConstraints() throws ContradictionException, TimeoutException {
        SkyscraperSolver solver = new SkyscraperSolver(4);
        int[][] solution = solver.solve();
        
        assertNotNull(solution, "Solution should exist");
        assertEquals(4, solution.length);
        assertValidSudoku(solution);
    }
    
    @Test
    void test4x4WithVisibilityConstraints() throws ContradictionException, TimeoutException {
        // Test with the original failing input: "4 3 2 1 1 2 2 2 4 3 2 1 1 2 2 2"
        // up=[4,3,2,1], down=[1,2,2,2], left=[4,3,2,1], right=[1,2,2,2]
        SkyscraperSolver solver = new SkyscraperSolver(4);
        
        // Up constraints (columns from top)
        solver.setVisibilityConstraint(SkyscraperSolver.DIRECTION_UP, 0, 4);
        solver.setVisibilityConstraint(SkyscraperSolver.DIRECTION_UP, 1, 3);
        solver.setVisibilityConstraint(SkyscraperSolver.DIRECTION_UP, 2, 2);
        solver.setVisibilityConstraint(SkyscraperSolver.DIRECTION_UP, 3, 1);
        
        // Down constraints (columns from bottom)
        solver.setVisibilityConstraint(SkyscraperSolver.DIRECTION_DOWN, 0, 1);
        solver.setVisibilityConstraint(SkyscraperSolver.DIRECTION_DOWN, 1, 2);
        solver.setVisibilityConstraint(SkyscraperSolver.DIRECTION_DOWN, 2, 2);
        solver.setVisibilityConstraint(SkyscraperSolver.DIRECTION_DOWN, 3, 2);
        
        // Left constraints (rows from left)
        solver.setVisibilityConstraint(SkyscraperSolver.DIRECTION_LEFT, 0, 4);
        solver.setVisibilityConstraint(SkyscraperSolver.DIRECTION_LEFT, 1, 3);
        solver.setVisibilityConstraint(SkyscraperSolver.DIRECTION_LEFT, 2, 2);
        solver.setVisibilityConstraint(SkyscraperSolver.DIRECTION_LEFT, 3, 1);
        
        // Right constraints (rows from right)
        solver.setVisibilityConstraint(SkyscraperSolver.DIRECTION_RIGHT, 0, 1);
        solver.setVisibilityConstraint(SkyscraperSolver.DIRECTION_RIGHT, 1, 2);
        solver.setVisibilityConstraint(SkyscraperSolver.DIRECTION_RIGHT, 2, 2);
        solver.setVisibilityConstraint(SkyscraperSolver.DIRECTION_RIGHT, 3, 2);
        
        int[][] solution = solver.solve();
        
        assertNotNull(solution, "Solution should exist");
        assertValidSudoku(solution);
    }
    
    @Test
    void test4x4AllZeroConstraints() throws ContradictionException, TimeoutException {
        // All zeros means no visibility constraints
        SkyscraperSolver solver = new SkyscraperSolver(4);
        
        for (int i = 0; i < 4; i++) {
            solver.setVisibilityConstraint(SkyscraperSolver.DIRECTION_UP, i, 0);
            solver.setVisibilityConstraint(SkyscraperSolver.DIRECTION_DOWN, i, 0);
            solver.setVisibilityConstraint(SkyscraperSolver.DIRECTION_LEFT, i, 0);
            solver.setVisibilityConstraint(SkyscraperSolver.DIRECTION_RIGHT, i, 0);
        }
        
        int[][] solution = solver.solve();
        
        assertNotNull(solution, "Solution should exist");
        assertValidSudoku(solution);
    }
    
    @Test
    void test3x3Solver() throws ContradictionException, TimeoutException {
        SkyscraperSolver solver = new SkyscraperSolver(3);
        int[][] solution = solver.solve();
        
        assertNotNull(solution, "Solution should exist");
        assertEquals(3, solution.length);
        assertValidSudoku(solution);
    }
    
    @Test
    void testContradictoryInitialValues() throws ContradictionException, TimeoutException {
        // Set two cells in the same row to the same value - should have no solution
        SkyscraperSolver solver = new SkyscraperSolver(4);
        solver.setInitialValue(0, 0, 1);
        solver.setInitialValue(0, 1, 1); // Same value in same row
        
        int[][] solution = solver.solve();
        
        assertNull(solution, "Should have no solution due to contradiction");
    }
    
    @Test
    void testMultipleInitialValues() throws ContradictionException, TimeoutException {
        SkyscraperSolver solver = new SkyscraperSolver(4);
        solver.setInitialValue(0, 0, 1);
        solver.setInitialValue(0, 1, 2);
        solver.setInitialValue(1, 0, 2);
        solver.setInitialValue(1, 1, 1);
        
        int[][] solution = solver.solve();
        
        assertNotNull(solution, "Solution should exist");
        assertEquals(1, solution[0][0]);
        assertEquals(2, solution[0][1]);
        assertEquals(2, solution[1][0]);
        assertEquals(1, solution[1][1]);
        assertValidSudoku(solution);
    }
    
    /**
     * Validates that a solution is a valid Sudoku:
     * - Each row contains each value exactly once
     * - Each column contains each value exactly once
     */
    private void assertValidSudoku(int[][] solution) {
        int n = solution.length;
        
        // Check each row
        for (int i = 0; i < n; i++) {
            boolean[] seen = new boolean[n + 1];
            for (int j = 0; j < n; j++) {
                int val = solution[i][j];
                assertTrue(val >= 1 && val <= n, 
                    String.format("Value at [%d][%d] is %d, should be between 1 and %d", i, j, val, n));
                assertFalse(seen[val], 
                    String.format("Value %d appears twice in row %d", val, i));
                seen[val] = true;
            }
        }
        
        // Check each column
        for (int j = 0; j < n; j++) {
            boolean[] seen = new boolean[n + 1];
            for (int i = 0; i < n; i++) {
                int val = solution[i][j];
                assertFalse(seen[val], 
                    String.format("Value %d appears twice in column %d", val, j));
                seen[val] = true;
            }
        }
    }
    
    /**
     * Counts how many buildings are visible from a given direction
     */
    private int countVisible(int[] buildings) {
        int visible = 0;
        int maxHeight = 0;
        for (int height : buildings) {
            if (height > maxHeight) {
                visible++;
                maxHeight = height;
            }
        }
        return visible;
    }
    
    /**
     * Validates visibility constraints for a solution
     */
    private void assertVisibilityConstraints(int[][] solution, int[][] constraints) {
        int n = solution.length;
        
        // Check left visibility (rows from left)
        for (int i = 0; i < n; i++) {
            if (constraints[SkyscraperSolver.DIRECTION_LEFT][i] > 0) {
                int expected = constraints[SkyscraperSolver.DIRECTION_LEFT][i];
                int actual = countVisible(solution[i]);
                assertEquals(expected, actual, 
                    String.format("Row %d from left: expected %d visible, got %d", i, expected, actual));
            }
        }
        
        // Check right visibility (rows from right)
        for (int i = 0; i < n; i++) {
            if (constraints[SkyscraperSolver.DIRECTION_RIGHT][i] > 0) {
                int expected = constraints[SkyscraperSolver.DIRECTION_RIGHT][i];
                int[] reversed = new int[n];
                for (int j = 0; j < n; j++) {
                    reversed[j] = solution[i][n - 1 - j];
                }
                int actual = countVisible(reversed);
                assertEquals(expected, actual, 
                    String.format("Row %d from right: expected %d visible, got %d", i, expected, actual));
            }
        }
        
        // Check top visibility (columns from top)
        for (int j = 0; j < n; j++) {
            if (constraints[SkyscraperSolver.DIRECTION_UP][j] > 0) {
                int expected = constraints[SkyscraperSolver.DIRECTION_UP][j];
                int[] column = new int[n];
                for (int i = 0; i < n; i++) {
                    column[i] = solution[i][j];
                }
                int actual = countVisible(column);
                assertEquals(expected, actual, 
                    String.format("Column %d from top: expected %d visible, got %d", j, expected, actual));
            }
        }
        
        // Check bottom visibility (columns from bottom)
        for (int j = 0; j < n; j++) {
            if (constraints[SkyscraperSolver.DIRECTION_DOWN][j] > 0) {
                int expected = constraints[SkyscraperSolver.DIRECTION_DOWN][j];
                int[] column = new int[n];
                for (int i = 0; i < n; i++) {
                    column[i] = solution[n - 1 - i][j];
                }
                int actual = countVisible(column);
                assertEquals(expected, actual, 
                    String.format("Column %d from bottom: expected %d visible, got %d", j, expected, actual));
            }
        }
    }
}
