# SkyscrapperSudoku4J
A skyscrapper Sudoku solver based on SAT4J

## Requirements

- Maven in order to compile
- JDK 21+ in order to compile & run (JRE is enough to run)

## How to build it:

```bash
mvn clean package
```

## How to run it

Here is an example that runs the solver on a 9x9 problem with 40 warmup loops and 10 measured loops:

```bash
java -Dwarmup=40 -DnbLoops=10 -jar target/skyscrapper-solver.jar "9 8 7 6 5 4 3 2 1 1 2 2 2 2 2 2 2 2 9 8 7 6 5 4 3 2 1 1 2 2 2 2 2 2 2 2"
```

### Settings

The following settings are avalable through java system properties:
- warmup: Number of warmup loops (the problem is solved again and again in a loop, before chronometer started).
- nbLoops: Number of loops (the problem is also solved in that loop, the compute time displayed is the average one).

## Examples

9x9:  
9 8 7 6 5 4 3 2 1 1 2 2 2 2 2 2 2 2 9 8 7 6 5 4 3 2 1 1 2 2 2 2 2 2 2 2

Solution:  
1 2 3 4 5 6 7 8 9   
2 3 4 5 6 7 8 9 1  
3 4 5 6 7 8 9 1 2  
4 5 6 7 1 9 2 3 8  
5 6 7 8 9 1 3 2 4  
6 7 2 9 8 3 1 4 5  
7 8 9 1 3 2 4 5 6  
9 1 8 2 4 5 6 7 3  
8 9 1 3 2 4 5 6 7  

4x4: 
4 3 2 1 1 2 2 2 4 3 2 1 1 2 2 2

Solution:
1 2 3 4  
2 1 4 3  
3 4 1 2  
4 3 2 1  

## TODO
- [ ] Use assertVisibilityConstraints in tests (This method currently seems buggy)
