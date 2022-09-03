import java.util.Random;

public class GameLogic {
    private  int solutions=0;
    private  boolean deadEndExit=false;

    private  int[] sudoku = new int[81];
    private  int[] customerSudoku = new int[81];
    private  int[] solved = new int[81];

    public  void solveSudoku(int n)
    {
        int i;
        if(n==81)
        {
            //setSolved(sudoku);
            //setSudoku(sudokuForCreation);
            solutions++;
            return;
        }
        else if(sudoku[n]!=0)
        {
            solveSudoku(n+1);
            if(solutions==2)
            {
                return;
            }
        }
        else
        {
            for(i=1;i<10;i++)
            {
                sudoku[n]=i;
                if(checkingSudoku(n)==true)
                {
                    solveSudoku(n+1);
                }
                if(solutions==2)
                {
                    return;
                }
            }
            sudoku[n]=0;
            return;
        }

    }
    public  boolean checkingSudoku(int n)
    {
        int i,x,x1,x2,y,z,z1,z2;

        // raw
        x1=(n/9)*9;
        x2=x1+9;
        for(x=x1;x<x2;x++)
        {
            if(sudoku[x] == sudoku[n] && n!=x)
            {
                return false;
            }
        }

        // column
        for(y=n%9;y<81;y=y+9)
        {
            if(sudoku[y] == sudoku[n] && n!=y)
            {
                return false;
            }
        }

        //Square
        z1 = ((n/27)*27)+(((n%9)/3)*3);
        z2 = z1+21;
        for(z=z1;z<z2;z=z+9)
        {
            for(i=0;i<3;i++)
            {
                if(sudoku[i+z] == sudoku[n] && n!=(i+z))
                {
                    return false;
                }
            }
        }
        //if it's all right
        return true;
    }
    public  int[] createSudoku(int givenNum) {
        Random rand = new Random();
        int randomIndex, randomValue, i, eraseNum, givenNumCounter = 0, lastOne;
        int deadlock = 0;
        int[] sudoku1 = new int[81]; //i need to duplicate the arr sudoku in order not to change him

        for (i = 0; i < 81; i++) {
            sudoku[i] = 0;
        }
        System.out.println("help");
        solutions=0;
        setDeadEndExit();
        while ((solutions != 1 && !getDeadEndExit())){
            solutions = 0;
            //adding a random number in a random place
            randomIndex = rand.nextInt(81);
            randomValue = rand.nextInt(9) + 1;
            if (sudoku[randomIndex] == 0) {
                sudoku[randomIndex] = randomValue;
                sudoku1[randomIndex] = randomValue;
                givenNumCounter++;
                //if the random number that had been added is in wrong place, a new number is placing him
                while (checkingSudoku(randomIndex) == false) {
                    sudoku[randomIndex] = 0;
                    sudoku1[randomIndex] = 0;
                    givenNumCounter--;
                    randomValue = rand.nextInt(9) + 1;
                    randomIndex = rand.nextInt(81);
                    sudoku[randomIndex] = randomValue;
                    sudoku1[randomIndex] = randomValue;
                    givenNumCounter++;
                }
            }
            //just if enough numbers had been added
            if (givenNumCounter > givenNum) {
                solveSudoku(0);
                //System.out.println(solutions+"h");

                if (solutions == 1) {
                    //i 'clean' sudoku[] by paste him to sudoku1[]
                    for (i = 0; i < 81; i++) {
                        sudoku[i] = sudoku1[i];
                    }
                    if (givenNumCounter > givenNum && givenNumCounter < (givenNum+4)) {
                        System.out.println("there is just " + solutions + " solution!");
                        System.out.println("there are " + givenNumCounter + " numbers in this Sudoku!");
                        setCustomerSudoku(sudoku);
                        return getSudoku();
                    }
                    for (i = 0; i < 81; i++) {
                        sudoku1[i] = 0;
                        sudoku[i] = 0;
                    }
                } else if (solutions == 0) {
                    //i 'clean' sudoku[] by paste him to sudoku1[]
                    for (i = 0; i < 81; i++) {
                        sudoku[i] = sudoku1[i];
                    }
                    if (givenNumCounter == givenNum) {
                        while (sudoku[randomIndex] != 0) {
                            sudoku[randomIndex] = 0;
                            sudoku1[randomIndex] = 0;
                            givenNumCounter--;
                        }
                    }

                } else if (solutions == 2) {
                    givenNumCounter = 81;
                    for (i = 0; i < 81; i++) {
                        sudoku1[i] = sudoku[i];
                    }
                    //Erase from the solved Sudoku until it has just the Given number and one solution
                    for (eraseNum = 0; eraseNum < (81 - (givenNum)); eraseNum++) {
                        solutions = 0;
                        randomIndex = rand.nextInt(81);
                        while (sudoku1[randomIndex] == 0) {
                            randomIndex = rand.nextInt(81);
                        }
                        lastOne = sudoku1[randomIndex];
                        sudoku1[randomIndex] = 0;
                        sudoku[randomIndex] = 0;
                        givenNumCounter--;

                        solveSudoku(0);
                        deadlock++;
                        //System.out.println(solutions+"r");
                        //i 'clean' sudoku[] by paste him to sudoku1[]
                        for (i = 0; i < 81; i++) {
                            sudoku[i] = sudoku1[i];
                        }
                        if (solutions != 1) {
                            sudoku[randomIndex] = lastOne;
                            sudoku1[randomIndex] = lastOne;
                            eraseNum--;
                            givenNumCounter++;
                        } else if (givenNumCounter > givenNum && givenNumCounter < (givenNum+4)) {
                            //i 'clean' sudoku[] by paste him to sudoku1[]
                            for (i = 0; i < 81; i++) {
                                sudoku[i] = sudoku1[i];
                            }
                            System.out.println("there is just " + solutions + " solution!");
                            System.out.println("there are " + givenNumCounter + " numbers in this Sudoku!");
                            System.out.println(deadlock+"r");
                            setCustomerSudoku(sudoku);
                            return getSudoku();
                        }
                        if (deadlock >= 100) {
                            eraseNum = 81;
                            deadEndExit=true;
                            for (i = 0; i < 81; i++) {
                                //sudoku1[i] = 0;
                                sudoku[i] = 0;
                            }
                            System.out.println("hold on a sec... it will come");
                            givenNumCounter = 0;
                            deadlock = 0;
                        }
                    }
                }
            }
        }
        deadEndExit=false;
        sudoku[0]=10;
        return getSudoku();
    }

    public  boolean checkingSudokuInputSudoku(int n,int[] CheckedSudoku)
    {
        int i,x,x1,x2,y,z,z1,z2;

        // raw
        x1=(n/9)*9;
        x2=x1+9;
        for(x=x1;x<x2;x++)
        {
            if(CheckedSudoku[x] == CheckedSudoku[n] && n!=x)
            {
                return false;
            }
        }

        // column
        for(y=n%9;y<81;y=y+9)
        {
            if(CheckedSudoku[y] == CheckedSudoku[n] && n!=y)
            {
                return false;
            }
        }

        //Square
        z1 = ((n/27)*27)+(((n%9)/3)*3);
        z2 = z1+21;
        for(z=z1;z<z2;z=z+9)
        {
            for(i=0;i<3;i++)
            {
                if(CheckedSudoku[i+z] == CheckedSudoku[n] && n!=(i+z))
                {
                    return false;
                }
            }
        }
        //if it's all right
        return true;
    }
    public  boolean getDeadEndExit() {
        return deadEndExit;
    }

    private  void setDeadEndExit() {
        deadEndExit=false;
    }
    public  void setCustomerSudoku(int[] input) {
        for (int i = 0; i < 81; i++) {
            customerSudoku[i]=input[i];
        }
    }

    public  void setSudoku(int[] input) {
        for (int i = 0; i < 81; i++) {
            sudoku[i]=input[i];
        }
    }
    public  void setSolutions(int solutions) {
        this.solutions = solutions;
    }
    public  void setSolved(int[] input) {
        for (int i = 0; i < 81; i++) {
            solved[i]=input[i];
        }
    }
    public  int countSolutions(int[] sudokuToBeCounted){
        setSolutions(0);
        setSudoku(sudokuToBeCounted);
        solveSudoku(0);
        return solutions;
    }
    public  int getSolutions() {
        return solutions;
    }

    public  int[] getSudoku() {
        return sudoku;
    }
    public  int[] getSolved(int[] sudokuB) {

        setSolutions(1);
        setSudoku(sudokuB);
        solveSudoku(0);
        setSolved(sudoku);
        return solved;
    }
}
