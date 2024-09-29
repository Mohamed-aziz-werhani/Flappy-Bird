// A class for a 3 layered neural network
package flappyBird;
import java.util.Random;

class Brain implements java.io.Serializable{
    int inputCount;
    int hiddenCount;
    int outputCount;
    Matrix weightsIH;
    Matrix weightsHO;
    Matrix biasH;
    Matrix biasO;

    public Brain(int inputCount, int hiddenCount, int outputCount){
        this.inputCount = inputCount;
        this.hiddenCount = hiddenCount;
        this.outputCount = outputCount;
        
        this.weightsIH = new Matrix(this.hiddenCount, this.inputCount);
        this.weightsHO = new Matrix(this.outputCount, this.hiddenCount);
        this.biasH = new Matrix(this.hiddenCount, 1);
        this.biasO = new Matrix(this.outputCount, 1);
        
        this.weightsIH.randomize();
        this.weightsHO.randomize();
        this.biasH.randomize();
        this.biasO.randomize();
    }

    public Matrix predict(Matrix inputMatrix) throws Exception{
        Matrix hiddenMatrix = Matrix.matrixMultiplication(this.weightsIH, inputMatrix);
        hiddenMatrix.add(biasH);
        hiddenMatrix.mapSigmoid();
        Matrix outputMatrix = Matrix.matrixMultiplication(this.weightsHO, hiddenMatrix);
        outputMatrix.add(biasO);
        outputMatrix.mapSigmoid();

        return outputMatrix;
    }

    public static Brain[] crossover(Brain dad, Brain mom) throws Exception{
        Random rand = new Random();
        Matrix[] dadSplit;
        Matrix[] momSplit;
        Brain baby1 = new Brain(dad.inputCount, dad.hiddenCount, dad.outputCount);
        Brain baby2 = new Brain(dad.inputCount, dad.hiddenCount, dad.outputCount);
        
        int cutIH = rand.nextInt(dad.weightsIH.rows);
        int cutHO = rand.nextInt(dad.weightsHO.rows);

        dadSplit = Matrix.transverseCut(dad.weightsIH, cutIH);
        momSplit = Matrix.transverseCut(mom.weightsIH, cutIH);
        
        baby1.weightsIH = Matrix.combineColumnsMatrices(dadSplit[0], momSplit[1]);
        baby2.weightsIH = Matrix.combineColumnsMatrices(momSplit[0], dadSplit[1]);

        dadSplit = Matrix.transverseCut(dad.weightsHO, cutHO);
        momSplit = Matrix.transverseCut(mom.weightsHO, cutHO);
        
        baby1.weightsHO = Matrix.combineColumnsMatrices(dadSplit[0], momSplit[1]);
        baby2.weightsHO = Matrix.combineColumnsMatrices(momSplit[0], dadSplit[1]);

        return new Brain[] {baby1, baby2};
    }
    public void mutate(double chance) throws Exception{
        this.weightsIH.mutate(chance);
        this.weightsHO.mutate(chance);
        this.biasH.mutate(chance);
        this.biasO.mutate(chance);   
    }
}