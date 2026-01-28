package com.fathzer.skycrapper;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

/**
 * Résolveur de Skyscraper Sudoku utilisant Sat4j
 * Approche par order encoding + contraintes pseudo-booléennes
 */
public class SkyscraperSolver {
    public static final int DIRECTION_LEFT = 0;
    public static final int DIRECTION_RIGHT = 1;
    public static final int DIRECTION_UP = 2;
    public static final int DIRECTION_DOWN = 3;

    private final int n; // Taille de la grille (typiquement 4, 5, 6, ou 9)
    private final ISolver solver;
    private int nextVar = 1; // Prochain numéro de variable SAT
    
    // Mapping des variables
    // cell[i][j][v] : vrai si case (i,j) a une valeur > v
    // Avec order encoding : valeur = k ssi cell[i][j][k-1] ET NOT cell[i][j][k]
    private int[][][] cellOrder;
    
    // Variables auxiliaires pour la visibilité
    // visible[dir][line][pos] : case visible depuis direction dir
    private int[][][] visible;
    
    // Contraintes de visibilité [direction][ligne] = nombre attendu
    // direction: 0=gauche, 1=droite, 2=haut, 3=bas
    private int[][] constraints;
    
    // Grille initiale (0 = vide)
    private int[][] initial;
    
    public SkyscraperSolver(int n) {
        this.n = n;
        this.solver = SolverFactory.newDefault();
        solver.newVar(1000000); // Pré-allouer des variables
        solver.setExpectedNumberOfClauses(100000);
        
        this.cellOrder = new int[n][n][n]; // N valeurs: 1..N
        this.visible = new int[4][n][n];
        this.constraints = new int[4][n];
        this.initial = new int[n][n];
    }
    
    /**
     * Définit les contraintes de visibilité
     * @param direction 0=gauche, 1=droite, 2=haut, 3=bas
     * @param line numéro de ligne/colonne
     * @param count nombre de cases visibles attendu (0 = pas de contrainte)
     */
    public void setVisibilityConstraint(int direction, int line, int count) {
        constraints[direction][line] = count;
    }
    
    /**
     * Définit une valeur initiale dans la grille
     */
    public void setInitialValue(int row, int col, int value) {
        initial[row][col] = value;
    }
    
    /**
     * Construit et résout le problème SAT
     */
    public int[][] solve() throws ContradictionException, TimeoutException {
        allocateVariables();
        addOrderEncodingConstraints();
        addSudokuConstraints();
        addVisibilityConstraints();
        addInitialValues();
        
        IProblem problem = solver;
        if (problem.isSatisfiable()) {
            return extractSolution();
        }
        return null;
    }
    
    /**
     * Alloue toutes les variables SAT nécessaires
     */
    private void allocateVariables() {
        // Variables order encoding pour les cellules
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int v = 0; v < n; v++) {
                    cellOrder[i][j][v] = nextVar++;
                }
            }
        }
        
        // Variables de visibilité
        for (int dir = 0; dir < 4; dir++) {
            for (int line = 0; line < n; line++) {
                for (int pos = 0; pos < n; pos++) {
                    visible[dir][line][pos] = nextVar++;
                }
            }
        }
    }
    
    /**
     * Ajoute les contraintes d'order encoding
     * Si cell[i][j][v] est vrai, alors cell[i][j][v-1] doit être vrai
     */
    private void addOrderEncodingConstraints() throws ContradictionException {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                // Chaque cellule doit avoir au moins la valeur 1 (cellOrder[i][j][0] = vrai)
                solver.addClause(new VecInt(new int[]{cellOrder[i][j][0]}));
                
                // Ordre décroissant: si >v alors >v-1
                for (int v = 1; v < n; v++) {
                    // cellOrder[i][j][v] => cellOrder[i][j][v-1]
                    // ¬cellOrder[i][j][v] ∨ cellOrder[i][j][v-1]
                    solver.addClause(new VecInt(new int[]{
                        -cellOrder[i][j][v], 
                        cellOrder[i][j][v-1]
                    }));
                }
                
                // Chaque cellule a au plus la valeur N (¬cellOrder[i][j][N-1] peut être faux)
                // Pas de contrainte supplémentaire nécessaire
            }
        }
    }
    
    /**
     * Ajoute les contraintes Sudoku classiques
     */
    private void addSudokuConstraints() throws ContradictionException {
        // Chaque cellule a exactement une valeur
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                addExactlyOneValueInCell(i, j);
            }
        }
        
        // Chaque valeur apparaît exactement une fois par ligne
        for (int i = 0; i < n; i++) {
            for (int val = 1; val <= n; val++) {
                addExactlyOneValueInLine(i, val);
            }
        }
        
        // Chaque valeur apparaît exactement une fois par colonne
        for (int j = 0; j < n; j++) {
            for (int val = 1; val <= n; val++) {
                addExactlyOneValueInColumn(j, val);
            }
        }
    }
    
    /**
     * Encode: la cellule (row, col) contient exactement une valeur parmi 1..n
     */
    private void addExactlyOneValueInCell(int row, int col) throws ContradictionException {
        int[] hasValueVars = new int[n];
        for (int val = 1; val <= n; val++) {
            int hasVal = nextVar++;
            hasValueVars[val-1] = hasVal;
            
            if (val == 1) {
                if (n > 1) {
                    solver.addClause(new VecInt(new int[]{-hasVal, -cellOrder[row][col][1]}));
                    solver.addClause(new VecInt(new int[]{cellOrder[row][col][1], hasVal}));
                } else {
                    solver.addClause(new VecInt(new int[]{hasVal}));
                }
            } else if (val == n) {
                solver.addClause(new VecInt(new int[]{-hasVal, cellOrder[row][col][n-1]}));
                solver.addClause(new VecInt(new int[]{-cellOrder[row][col][n-1], hasVal}));
            } else {
                solver.addClause(new VecInt(new int[]{-hasVal, cellOrder[row][col][val-1]}));
                solver.addClause(new VecInt(new int[]{-hasVal, -cellOrder[row][col][val]}));
                solver.addClause(new VecInt(new int[]{-cellOrder[row][col][val-1], cellOrder[row][col][val], hasVal}));
            }
        }
        
        addExactlyOne(hasValueVars);
    }
    
    /**
     * Encode: exactement une case de la ligne i contient la valeur val
     */
    private void addExactlyOneValueInLine(int row, int val) throws ContradictionException {
        // hasValue[row][j] = cellOrder[row][j][val-1] ∧ ¬cellOrder[row][j][val]
        // Mais pour simplifier avec at-least-one et at-most-one:
        
        int[] hasValueVars = new int[n];
        for (int j = 0; j < n; j++) {
            // Variable auxiliaire pour "case (row,j) a la valeur val"
            int hasVal = nextVar++;
            hasValueVars[j] = hasVal;
            
            // hasVal <=> (cellOrder[row][j][val-1] ∧ (val==N ∨ ¬cellOrder[row][j][val]))
            if (val == 1) {
                // Valeur 1: hasVal <=> ¬cellOrder[row][j][1]
                // (since cellOrder[row][j][0] is always TRUE, value 1 means NOT (value > 1))
                if (n > 1) {
                    // hasVal => ¬cellOrder[row][j][1]
                    solver.addClause(new VecInt(new int[]{-hasVal, -cellOrder[row][j][1]}));
                    // ¬cellOrder[row][j][1] => hasVal
                    solver.addClause(new VecInt(new int[]{cellOrder[row][j][1], hasVal}));
                } else {
                    // For n=1, value 1 is always true
                    solver.addClause(new VecInt(new int[]{hasVal}));
                }
            } else if (val == n) {
                // Valeur N: hasVal <=> cellOrder[row][j][N-1]
                solver.addClause(new VecInt(new int[]{-hasVal, cellOrder[row][j][n-1]}));
                solver.addClause(new VecInt(new int[]{-cellOrder[row][j][n-1], hasVal}));
            } else {
                // Valeur intermédiaire: hasVal <=> (cellOrder[row][j][val-1] ∧ ¬cellOrder[row][j][val])
                // hasVal => cellOrder[row][j][val-1]
                solver.addClause(new VecInt(new int[]{-hasVal, cellOrder[row][j][val-1]}));
                // hasVal => ¬cellOrder[row][j][val]
                solver.addClause(new VecInt(new int[]{-hasVal, -cellOrder[row][j][val]}));
                // (cellOrder[row][j][val-1] ∧ ¬cellOrder[row][j][val]) => hasVal
                solver.addClause(new VecInt(new int[]{-cellOrder[row][j][val-1], cellOrder[row][j][val], hasVal}));
            }
        }
        
        // Exactement un doit être vrai
        addExactlyOne(hasValueVars);
    }
    
    /**
     * Encode: exactement une case de la colonne j contient la valeur val
     */
    private void addExactlyOneValueInColumn(int col, int val) throws ContradictionException {
        int[] hasValueVars = new int[n];
        for (int i = 0; i < n; i++) {
            int hasVal = nextVar++;
            hasValueVars[i] = hasVal;
            
            if (val == 1) {
                // Valeur 1: hasVal <=> ¬cellOrder[i][col][1]
                if (n > 1) {
                    solver.addClause(new VecInt(new int[]{-hasVal, -cellOrder[i][col][1]}));
                    solver.addClause(new VecInt(new int[]{cellOrder[i][col][1], hasVal}));
                } else {
                    solver.addClause(new VecInt(new int[]{hasVal}));
                }
            } else if (val == n) {
                solver.addClause(new VecInt(new int[]{-hasVal, cellOrder[i][col][n-1]}));
                solver.addClause(new VecInt(new int[]{-cellOrder[i][col][n-1], hasVal}));
            } else {
                solver.addClause(new VecInt(new int[]{-hasVal, cellOrder[i][col][val-1]}));
                solver.addClause(new VecInt(new int[]{-hasVal, -cellOrder[i][col][val]}));
                solver.addClause(new VecInt(new int[]{-cellOrder[i][col][val-1], cellOrder[i][col][val], hasVal}));
            }
        }
        
        addExactlyOne(hasValueVars);
    }
    
    /**
     * Ajoute une contrainte "exactement un parmi ces variables"
     */
    private void addExactlyOne(int[] vars) throws ContradictionException {
        // Au moins un
        solver.addClause(new VecInt(vars));
        
        // Au plus un (toutes les paires s'excluent mutuellement)
        for (int i = 0; i < vars.length; i++) {
            for (int j = i + 1; j < vars.length; j++) {
                solver.addClause(new VecInt(new int[]{-vars[i], -vars[j]}));
            }
        }
    }
    
    /**
     * Ajoute les contraintes de visibilité pour chaque direction
     */
    private void addVisibilityConstraints() throws ContradictionException {
        for (int line = 0; line < n; line++) {
            if (constraints[0][line] > 0) addVisibilityLeft(line, constraints[0][line]);
            if (constraints[1][line] > 0) addVisibilityRight(line, constraints[1][line]);
            if (constraints[2][line] > 0) addVisibilityTop(line, constraints[2][line]);
            if (constraints[3][line] > 0) addVisibilityBottom(line, constraints[3][line]);
        }
    }
    
    /**
     * Contrainte de visibilité depuis la gauche pour une ligne
     */
    private void addVisibilityLeft(int row, int expected) throws ContradictionException {
        // visible[0][row][0] est toujours vrai (premier élément toujours visible)
        solver.addClause(new VecInt(new int[]{visible[0][row][0]}));
        
        for (int j = 1; j < n; j++) {
            // visible[0][row][j] <=> cell[row][j] > max(cell[row][0..j-1])
            // Simplifié: visible ssi pour tout k < j, cell[row][j] > cell[row][k]
            addVisibilityLogic(0, row, j, row, 0, j-1, true);
        }
        
        // Contrainte de cardinalité: somme des visible[0][row][*] = expected
        addCardinalityConstraint(visible[0][row], expected);
    }
    
    /**
     * Contrainte de visibilité depuis la droite pour une ligne
     */
    private void addVisibilityRight(int row, int expected) throws ContradictionException {
        solver.addClause(new VecInt(new int[]{visible[1][row][n-1]}));
        
        for (int j = n-2; j >= 0; j--) {
            addVisibilityLogic(1, row, j, row, j+1, n-1, true);
        }
        
        addCardinalityConstraint(visible[1][row], expected);
    }
    
    /**
     * Contrainte de visibilité depuis le haut pour une colonne
     */
    private void addVisibilityTop(int col, int expected) throws ContradictionException {
        solver.addClause(new VecInt(new int[]{visible[2][col][0]}));
        
        for (int i = 1; i < n; i++) {
            addVisibilityLogic(2, col, i, col, 0, i-1, false);
        }
        
        addCardinalityConstraint(visible[2][col], expected);
    }
    
    /**
     * Contrainte de visibilité depuis le bas pour une colonne
     */
    private void addVisibilityBottom(int col, int expected) throws ContradictionException {
        solver.addClause(new VecInt(new int[]{visible[3][col][n-1]}));
        
        for (int i = n-2; i >= 0; i--) {
            addVisibilityLogic(3, col, i, col, i+1, n-1, false);
        }
        
        addCardinalityConstraint(visible[3][col], expected);
    }
    
    /**
     * Encode la logique: une case est visible ssi elle est plus grande que toutes les précédentes
     */
    private void addVisibilityLogic(int dir, int line, int pos, int fixedIdx, int start, int end, boolean isRow) 
            throws ContradictionException {
        // visible[dir][line][pos] => pour tout k in [start..end]: current > cell[k]
        
        for (int k = start; k <= end; k++) {
            // Si visible, alors pour chaque valeur v, si current <= v alors prev > v est impossible
            // Approche simplifiée: visible => current est strictement plus grand
            
            for (int v = 0; v < n-1; v++) {
                // visible ∧ prev[k] > v => current > v
                // ¬visible ∨ ¬cellOrder[prev][v] ∨ cellOrder[current][v]
                int currentVar = isRow ? cellOrder[fixedIdx][pos][v] : cellOrder[pos][fixedIdx][v];
                int prevVar = isRow ? cellOrder[fixedIdx][k][v] : cellOrder[k][fixedIdx][v];
                
                solver.addClause(new VecInt(new int[]{
                    -visible[dir][line][pos], 
                    -prevVar, 
                    currentVar
                }));
            }
        }
    }
    
    /**
     * Ajoute une contrainte de cardinalité: somme = expected
     */
    private void addCardinalityConstraint(int[] vars, int expected) throws ContradictionException {
        // Exactly 'expected' variables must be true
        // This is equivalent to: at least 'expected' AND at most 'expected'
        
        VecInt vecVars = new VecInt(vars);
        
        // At least 'expected' variables are true
        solver.addAtLeast(vecVars, expected);
        
        // At most 'expected' variables are true  
        solver.addAtMost(vecVars, expected);
    }
    
    /**
     * Ajoute les valeurs initiales de la grille
     */
    private void addInitialValues() throws ContradictionException {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (initial[i][j] > 0) {
                    int val = initial[i][j];
                    
                    if (val == 1) {
                        // Valeur 1: ¬cellOrder[i][j][1]
                        if (n > 1) {
                            solver.addClause(new VecInt(new int[]{-cellOrder[i][j][1]}));
                        }
                        // For n=1, no constraint needed (always satisfied)
                    } else if (val == n) {
                        // Valeur N: cellOrder[i][j][N-1]
                        solver.addClause(new VecInt(new int[]{cellOrder[i][j][n-1]}));
                    } else {
                        // Valeur val: cellOrder[i][j][val-1] ∧ ¬cellOrder[i][j][val]
                        solver.addClause(new VecInt(new int[]{cellOrder[i][j][val-1]}));
                        solver.addClause(new VecInt(new int[]{-cellOrder[i][j][val]}));
                    }
                }
            }
        }
    }
    
    /**
     * Extrait la solution depuis le modèle SAT
     */
    private int[][] extractSolution() {
        int[] model = solver.model();
        int[][] solution = new int[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                // Trouver la valeur: première transition de vrai à faux dans cellOrder
                solution[i][j] = n; // Par défaut, valeur max
                
                for (int v = 0; v < n; v++) {
                    int variable = cellOrder[i][j][v];
                    boolean isTrue = false;
                    for (int lit : model) {
                        if (Math.abs(lit) == variable) {
                            isTrue = (lit > 0);
                            break;
                        }
                    }
                    
                    // First FALSE value determines the cell value
                    // cellOrder[v]=false means value is NOT > v, i.e., value <= v
                    // Since cellOrder is ordered and cellOrder[0] is always true (value > 0),
                    // the first false at index v means value = v
                    if (!isTrue) {
                        solution[i][j] = v;
                        break;
                    }
                }
            }
        }
        
        return solution;
    }
}
