import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessing {
    private  ArrayList eightyOneDigitsRec = new ArrayList<Rectangle>();
    private  int [] corners = new int[4];
    private  int YCoordinate;
    private  int XCoordinate;
    private  boolean hasDigitInTheImage = false;
    private   boolean[] digitsInTheSquare = new boolean[81];;
    private  int digitCounter=0;
    private  int longestLine;
    private  int longestLineBottomLine;
    private int bottomLongestLine;
    private  int[][][] cutImage;
    private  int[][][] pic;
    private GameLogic gameLogic;
    public ImageProcessing(GameLogic gameLogic){
        this.gameLogic=gameLogic;
    }
    public  int[] sudokuArrayFromImage(BufferedImage BI) {
        int[][][] image = readImageFromFile(BI);
        setCutImage(image);
        int[][][] blackImage = blackAndWhite(image);
        int[] loadSudoku = divideTo81AndReadThePhoto(BI,image,blackImage);

        //check it and remove contradictions
        loadSudoku = findAllContradictionsAndRemoveThem(loadSudoku);
        int numCounter=0;
        boolean readItRight = true;
        for (int i = 0; i < 81; i++) {
            if (loadSudoku[i]!=0)
                numCounter++;
        }
        //if it recognizes less than 20 numbers is not trying to solve it
        if (numCounter<20)
            readItRight = false;
        else
            readItRight = true;

        if (readItRight){
            setCorners(new int[]{image[0][0].length, 0, image[0][0].length, 0});
            loadSudoku = emphasize(loadSudoku);
            return loadSudoku;
        }
        //if it didn't read it right it return array with one number
        return new int[1];

    }

    private void setCutImage(int[][][] image) {
        this.cutImage = image;
    }

    private  int[][][] blackAndWhite(int[][][] image) {
        int[][][] newPic = new int[3][image[0].length][image[0][0].length];
        int[] AVERAGE=averageColor(image);
        for (int i = 0; i < image[0].length; i++) {
            for (int j = 0; j < image[0][0].length; j++) {
                if(image[0][i][j] > 0.85*AVERAGE[0] && image[1][i][j] > 0.85*AVERAGE[1] && image[2][i][j] > 0.85*AVERAGE[2]) {
                    newPic[0][i][j] = 250;
                    newPic[1][i][j] = 250;
                    newPic[2][i][j] = 250;
                }
                else {
                    newPic[0][i][j] = 0;
                    newPic[1][i][j] = 0;
                    newPic[2][i][j] = 0;
                }
            }
        }
        return newPic;
    }
    private  int[] emphasize(int[] loadSudoku) {
        int[] emphasizeTheGivenNum = new  int[81];

        for (int i = 0; i < 81; i++) {
            emphasizeTheGivenNum[i]=loadSudoku[i];
        }
        loadSudoku=getGameLogic().getSolved(loadSudoku);

        for (int i = 0; i < 81; i++) {
            if(emphasizeTheGivenNum[i]!=0)
            {
                loadSudoku[i]+=10;
            }
        }
        return loadSudoku;
    }


    private  int[] divideTo81AndReadThePhoto(BufferedImage colorfulImage,int[][][] image,int[][][] blackImage){
        int counter=0;
        BufferedImage blackAndWhiteImage = writeImageToFile("src/photos/black&white",blackImage);

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                //start point
                int x1 = (image[0].length/9)*i;
                int y1 = (image[0][0].length/9)*j;
                //stop point
                int x2 = (image[0].length/9)*(i+1);
                int y2 = (image[0][0].length/9)*(j+1);
                int[][][] relevantDigit = new int[3][(image[0].length/9)][(image[0][0].length/9)];

                Rectangle relevantRec = new Rectangle(y1,x1,y2-y1,x2-x1);
                int cX=0;
                for (int x=x1;x<x2;x++){
                    int cY=0;
                    for (int y=y1;y<y2;y++){
                        relevantDigit[0][cX][cY]=image[0][x][y];
                        relevantDigit[1][cX][cY]=image[1][x][y];
                        relevantDigit[2][cX][cY]=image[2][x][y];
                        cY++;
                    }
                    cX++;
                }
                eightyOneDigitsRec.add(counter,relevantRec);
                counter++;
            }
        }
        int[] loadSudoku = new int[81];
        for (int i = 0; i < 81; i++) {
            loadSudoku[i]=0;
        }

        ITesseract iTesseract = new Tesseract();
        iTesseract.setDatapath("src/tessdata");
        for (int i = 0; i < 81; i++) {
            try {
                Rectangle rectangle = improveRec((Rectangle) eightyOneDigitsRec.get(i));
                eightyOneDigitsRec.set(i,rectangle);
                digitsInTheSquare[digitCounter] = isHasDigitInTheImage();
                digitCounter++;
                //in the beginning it tries to do the OCR with the black&white image
                //and if did not succeed is trying with the regular image
                if (isHasDigitInTheImage() && loadSudoku[i]==0){
                    String s1 = iTesseract.doOCR(blackAndWhiteImage, (Rectangle) eightyOneDigitsRec.get(i));
                    if(!s1.equals("")){
                    for (int j = 0; j < s1.length(); j++) {
                        int x = Character.getNumericValue(s1.charAt(j));
                        if (x < 10 && x > 0) {
                                loadSudoku[i] = Character.getNumericValue(s1.charAt(j));
                        }
                    }
                }
                    //regular image OCR
                    if (loadSudoku[i]==0){
                        s1 = iTesseract.doOCR(colorfulImage, (Rectangle) eightyOneDigitsRec.get(i));
                        if(!s1.equals("")){
                            for (int j = 0; j < s1.length(); j++) {
                                int x = Character.getNumericValue(s1.charAt(j));
                                if (x < 10 && x > 0) {
                                    loadSudoku[i] = Character.getNumericValue(s1.charAt(j));
                                }
                            }
                        }
                    }
                }
            } catch (TesseractException ex) {
                throw new RuntimeException(ex);
            }

        }
        return loadSudoku;
    }
    public  int[] findAllContradictionsAndRemoveThem(int[] loadSudoku) {

        int [] noContradict = new int[81];
        for (int i = 0; i < 81; i++) {
            noContradict[i]=loadSudoku[i];
        }

        for (int i = 0; i < 81; i++) {
            if (loadSudoku[i]!=0) {
                if (!getGameLogic().checkingSudokuInputSudoku(i,loadSudoku)){
                    noContradict[i]=0;
                }
            }
        }
        return noContradict;
    }
    public  int[] removeAllNumThatWithOutThemThereIsSolution(int[] loadSudoku,boolean noSolution) {
        int[] copy = new int[81];
        for (int i = 0; i < 81; i++) {
            copy[i]=loadSudoku[i];
            if(loadSudoku[i]!=0){
                loadSudoku[i] -= 10;
            }
        }
        noSolution = true;
        for (int i = 0; i < 81; i++) {
            if(loadSudoku[i]!=0) {
                int temp = loadSudoku[i];
                loadSudoku[i] = 0;
                if (getGameLogic().countSolutions(loadSudoku) >= 1) {
                    copy[i] = 0;
                    noSolution = false;
                }
                loadSudoku[i] = temp;
            }
        }

        return copy;
    }

    public  List<BufferedImage> fileToListOfImages(File file) throws IOException {
        List<BufferedImage> imageList = new ArrayList<>();
        //return file with the cut image
        nu.pattern.OpenCV.loadLocally();

        BufferedImage im = ImageIO.read(file);

        Mat matObject = BufferedImage2Mat(im);

        Mat processed = processImage(matObject);

        List<Point> corners = findListOfCorners(processed);
        if (corners.size()==0){
            imageList.add(im);
            return imageList;
        }
        List<Point> rotated = new ArrayList<>();
        int counter=0;
        for (int i = 0; i < corners.size()/4; i++) {
            rotated.clear();
            for (int j = 0; j < 4; j++) {
                rotated.add(corners.get(counter));
                counter++;
            }
            rotated = rotatesIfNeeded(rotated);

            Mat startM = Converters.vector_Point2f_to_Mat(rotated);

            Mat result;

            result = warp(matObject, startM);

            BufferedImage bufferedImage = convertMatToBufferedImage(result);

            imageList.add(bufferedImage);
        }
        imageList.add(im);
        return imageList;
    }
    private  List<Point> rotatesIfNeeded(List<Point> corners) {
        int topLeft=0;
        double tempSum;
        double first=0;
        boolean firstBool=true;
        for (int i = 0; i < 4; i++) {
            tempSum = corners.get(i).x+corners.get(i).y;
            if (tempSum<first || firstBool){
                first=tempSum;
                topLeft=i;
                firstBool=false;
            }
        }
        List<Point> rotated = new ArrayList<Point>();
        for (int i = 0; i < 4; i++) {
            rotated.add(corners.get((topLeft+i)%4));
        }
        return rotated;
    }
    private  Mat processImage(final Mat mat) {
        final Mat processed = new Mat(mat.height(), mat.width(), mat.type());
        Imgproc.GaussianBlur(mat, processed, new Size(7, 7), 1);
        Imgproc.cvtColor(processed, processed, Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(processed, processed, 200, 25);
        Imgproc.dilate(processed, processed, new Mat(), new org.opencv.core.Point(-1, -1), 1);

        return processed;
    }
    public  List<Point> findListOfCorners(final Mat processedImage) {
        // Find contours of an image
        final List<MatOfPoint> allContours = new ArrayList<>();
        Imgproc.findContours(
                processedImage,
                allContours,
                new Mat(processedImage.size(), processedImage.type()),
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_NONE
        );
        List<Point> source = new ArrayList<Point>();

        for (int i = 0; i < allContours.size(); i++) {
            double value = Imgproc.contourArea(allContours.get(i));
            if(value>50000){
                MatOfPoint2f dst = new MatOfPoint2f();
                allContours.get(i).convertTo(dst, CvType.CV_32F);
                Imgproc.approxPolyDP(dst, dst, 0.02 * Imgproc.arcLength(dst, true), true);

                if (dst.total()==4) {
                    double[] temp_double;
                    temp_double = dst.get(0, 0);
                    Point p1 = new Point(temp_double[0], temp_double[1]);
                    temp_double = dst.get(1, 0);
                    Point p2 = new Point(temp_double[0], temp_double[1]);
                    temp_double = dst.get(2, 0);
                    Point p3 = new Point(temp_double[0], temp_double[1]);
                    temp_double = dst.get(3, 0);
                    Point p4 = new Point(temp_double[0], temp_double[1]);
                    source.add(p1);
                    source.add(p2);
                    source.add(p3);
                    source.add(p4);
                }
            }
        }

        return source;
    }
    public  Mat warp(Mat inputMat, Mat startM) {
        int resultWidth = 1000;
        int resultHeight = 1000;

        Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC4);



        Point ocvPOut1 = new Point(0, 0);
        Point ocvPOut2 = new Point(0, resultHeight);
        Point ocvPOut3 = new Point(resultWidth, resultHeight);
        Point ocvPOut4 = new Point(resultWidth, 0);
        List<Point> dest = new ArrayList<Point>();
        dest.add(ocvPOut1);
        dest.add(ocvPOut2);
        dest.add(ocvPOut3);
        dest.add(ocvPOut4);
        Mat endM = Converters.vector_Point2f_to_Mat(dest);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

        Imgproc.warpPerspective(inputMat,
                outputMat,
                perspectiveTransform,
                new Size(resultWidth, resultHeight),
                Imgproc.INTER_CUBIC);

        return outputMat;
    }
    public  Mat BufferedImage2Mat(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", byteArrayOutputStream);
        byteArrayOutputStream.flush();
        return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), 1);
    }
    private  BufferedImage convertMatToBufferedImage(final Mat mat) {
        // Create buffered image
        final BufferedImage bufferedImage = new BufferedImage(
                mat.width(),
                mat.height(),
                mat.channels() == 1 ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR
        );

        // Write data to image
        final WritableRaster raster = bufferedImage.getRaster();
        final DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        mat.get(0, 0, dataBuffer.getData());

        return bufferedImage;
    }

    private  int[] averageColor(int[][][] pic) {
        int [] average = new int[3];
        int sumR=0;
        int sumG=0;
        int sumB=0;
        for (int i = 0; i < pic[0].length; i++) {
            for (int j = 0; j < pic[0][0].length; j++) {
                sumR += pic[0][i][j];
                sumG += pic[1][i][j];
                sumB += pic[2][i][j];
            }
        }
        average[0] = (int) (sumR/(pic[0].length*pic[0][0].length)*0.8);
        average[1] = (int) (sumG/(pic[0].length*pic[0][0].length)*0.8);
        average[2] = (int) (sumB/(pic[0].length*pic[0][0].length)*0.8);

        return average;
    }

//    public  int[][][] getCutImage() {
//        return cutImage;
//    }

    public  int getYCoordinate() {
        return YCoordinate;
    }


    public  void setCorners(int[] corners) {
        this.corners = corners;
    }

    public  int[] getCorners() {
        return corners;
    }
    public  boolean[] getDigitsInTheSquare() {
        return digitsInTheSquare;
    }


    private  Rectangle improveRec(Rectangle rectangle) {
        //
        hasDigitInTheImage = false;
        int JCounter=0,ICounter=0;
        int[] AVERAGECOLOUR = averageColorRec(rectangle);
        boolean gotTheBottom=false;
        boolean gotTheRight=false;
        int blackCounter=0;
        int EDGE = (int) (0.08*(rectangle.width+rectangle.height)/2);

        for (int i = 0; i < rectangle.height; i++) {
            JCounter=0;
            for (int j = 0; j < rectangle.width; j++) {
                if (cutImage[0][i+rectangle.y][j+rectangle.x]<AVERAGECOLOUR[0]){
                    {
                        JCounter++;
                    }
                }

            }

            //all line was black
            if (JCounter>=rectangle.width*0.7 && !gotTheBottom){
                //line from top
                if(i < ((double)rectangle.height * 0.333)){

                    rectangle.y = rectangle.y + i + EDGE;
                    rectangle.height = rectangle.height - i - EDGE;
                }
                //line from bottom
                else if (i > ((double)rectangle.height * 0.666)) {

                    rectangle.height = i - EDGE;
                    gotTheBottom = true;
                }
                //if it's on the edge
            } else if (JCounter>=rectangle.width*0.3 && !gotTheBottom
                    && i < ((double)rectangle.height * 0.1)) {
                rectangle.y = rectangle.y + i + EDGE;
                rectangle.height = rectangle.height - i - EDGE;
            }
        }


        //taking care for the left right
        for (int j = 0; j < rectangle.width; j++) {
            ICounter=0;
            for (int i = 0; i < rectangle.height; i++) {
                if (cutImage[0][i+rectangle.y][j+rectangle.x]<AVERAGECOLOUR[0]){
                    ICounter++;
                }
            }
            //most row was black
            if (ICounter>=rectangle.height*0.7 && !gotTheRight){
                //row from left
                if(j < ((double)rectangle.width * 0.333)){
                    rectangle.x = rectangle.x + j + EDGE;
                    rectangle.width = rectangle.width - j - EDGE;
                }
                //row from right
                else if (j > ((double)rectangle.width * 0.666)) {
                    rectangle.width = j-EDGE;
                    gotTheRight=true;
                }
            }
            //cut the edge more sensitive
            else if (ICounter>=rectangle.height*0.3 && !gotTheRight
                    && j < ((double) rectangle.width * 0.15)) {
                rectangle.x = rectangle.x + j + EDGE;
                rectangle.width = rectangle.width - j - EDGE;
            }
            else if (ICounter>=rectangle.height*0.3 && !gotTheRight
                    && j > ((double) rectangle.width * 0.85)) {
                rectangle.width = j-EDGE;
                gotTheRight = true;
            }

        }


        for (int i =(int) (rectangle.height*0.2); i < 0.8*rectangle.height; i++) {
            for (int j = (int) (0.2*rectangle.width); j < 0.8*rectangle.width; j++) {
                if (cutImage[0][i+rectangle.y][j+rectangle.x]<AVERAGECOLOUR[0]){
                    blackCounter++;
                }
            }
        }
        //if there are more them 2.1% black pixels probably there is a digit in the image;
        if (blackCounter>0.021*(rectangle.height*rectangle.width))
            hasDigitInTheImage = true;

        return rectangle;
    }

    private  int[] averageColorRec(Rectangle rectangle) {
        int [] average = new int[3];
        int sumR=0;
        int sumG=0;
        int sumB=0;
        for (int i = 0; i < rectangle.height; i++) {
            for (int j = 0; j < rectangle.width; j++) {
                if (i+rectangle.y<cutImage[0].length && j+rectangle.x<cutImage[0][0].length){
                sumR += cutImage[0][i+rectangle.y][j+rectangle.x];
                sumG += cutImage[1][i+rectangle.y][j+rectangle.x];
                sumB += cutImage[2][i+rectangle.y][j+rectangle.x];
            }
            }
        }
        average[0] = (int) (sumR/(rectangle.height*rectangle.width)*0.8);
        average[1] = (int) (sumG/(rectangle.height*rectangle.width)*0.8);
        average[2] = (int) (sumB/(rectangle.height*rectangle.width)*0.8);

        return average;
    }


    public  int[][][] readImageFromFile(BufferedImage im) {
        int[][][] img=new int[3][im.getHeight()][im.getWidth()];
        for (int i = 0; i < im.getHeight(); i++) {
            for (int j = 0; j < im.getWidth(); j++) {
                Color c = new Color(im.getRGB(j, i));
                img[0][i][j] = c.getRed();
                img[1][i][j] = c.getGreen();
                img[2][i][j] = c.getBlue();
            }
        }
        return img;
    }

    public  BufferedImage writeImageToFile(String fileName, int[][][] pixels) {
        BufferedImage im = new BufferedImage(pixels[0][0].length, pixels[0].length, 1);
        Graphics g = im.createGraphics();

        for(int i = 0; i < pixels[0].length; ++i) {
            for(int j = 0; j < pixels[0][0].length; ++j) {
                g.setColor(new Color(pixels[0][i][j], pixels[1][i][j], pixels[2][i][j]));
                g.fillRect(j, i, 1, 1);
            }
        }

        try {
            fileName = fileName + ".png";
            ImageIO.write(im, "png", new File(fileName));
        } catch (IOException var6) {
            var6.printStackTrace();
        }
        return im;
    }

    public  ArrayList getEightyOneDigitsRec() {
        return eightyOneDigitsRec;
    }


    public GameLogic getGameLogic() {
        return gameLogic;
    }

    public  boolean isHasDigitInTheImage() {
        return hasDigitInTheImage;
    }

    public  void setEightyOneDigitsRec(ArrayList eightyOneDigitsRec) {
        this.eightyOneDigitsRec = eightyOneDigitsRec;
    }

    public  void setDigitCounter(int digitCounter) {
        this.digitCounter = digitCounter;
    }

    //get the cut part of the relevant square
    public  int[][][] getSquare(int index) {
        Rectangle r = (Rectangle) getEightyOneDigitsRec().get(index);
        int[][][] sq = new int[3][r.height][r.width];
        for (int i = 0; i < r.height; i++) {
            for (int j = 0; j < r.width; j++) {
                sq[0][i][j]=cutImage[0][i+r.y][j+r.x];
                sq[1][i][j]=cutImage[1][i+r.y][j+r.x];
                sq[2][i][j]=cutImage[2][i+r.y][j+r.x];
            }
        }
        return sq;
    }

    //    public  int[][][] cutAndScale(int[][][] pic) {
//        setPic(pic);
//        int tempXCoordinate=0;
//        int XCoordinate=0;
//        int YCoordinate=0;
//        int longestLine=0;
//        int sequenceBreaker=0;
//        boolean sequenceBroke = false;
//        int[] AVERAGECOLOUR = averageColor(pic);
//
//
//        //linesXandY[0] = X top
//        //linesXandY[1] = y top
//        //linesXandY[2] = X bottom
//        //linesXandY[3] = Y bottom
//        //ArrayList<int[]> lines = fileToListOfImages(pic);
//
//        for (int i = 0; i < pic[0].length; i++) {
//            int LineLength=0;
//            for (int j = 0; j < pic[0][0].length; j++) {
//                if (pic[0][i][j]< AVERAGECOLOUR[0] && pic[1][i][j]< AVERAGECOLOUR[1] && pic[2][i][j]< AVERAGECOLOUR[2]){
////                    if (i>0 && j>0 && i<pic[0].length-1 && j < pic[0][0].length-1)
////                        if ((pic[0][i-1][j]<120 && pic[0][i][j-1]<120) || (pic[0][i+1][j]<120 && pic[0][i][j+1]<120))
//                    {
//                        pic[0][i][j]=0;
//                        pic[1][i][j]=0;
//                        pic[2][i][j]=0;
//                        if(LineLength==0 || sequenceBroke){
//                            tempXCoordinate=j;
//                        }
//                        sequenceBroke=false;
//                        LineLength++;
//                        sequenceBreaker=0;
//                    }
//                }
//                else{
//                    pic[0][i][j]=250;
//                    pic[1][i][j]=250;
//                    pic[2][i][j]=250;
//                    sequenceBreaker++;
//                    //the sequence broke if we had 3 white pixels in sequence and the line is shorter then quarter
//                    if(sequenceBreaker>2 && LineLength<pic[0][0].length/4){
//                        sequenceBroke=true;
//                    }
//                }
//            }
//            //find the top line which is most black
//            if (LineLength>=(double)pic[0][0].length/100*40)
//                if( LineLength>longestLine
//                    && i < ((double)pic[0].length * 0.333)
//                    && (i < YCoordinate+2 || YCoordinate==0)){
//                //line from top
//                //gotTheLine=true;
//                XCoordinate = tempXCoordinate;
//                YCoordinate=i;
//                longestLine=LineLength-2;
//            }
//        }
//
//        //looking for the bottom line
//        int XCoordinateBottomLine = XCoordinate;
//        int YCoordinateBottomLine = pic[0].length;
//        int longestLineBottomLine = 0;
//        sequenceBroke = false;
//
//        for (int i = pic[0].length-1; i > ((double)pic[0].length * 0.666); i--) {
//            int bottomLineLength=0;
//            for (int j = 0; j < pic[0][0].length; j++) {
//                //black pixel
//                if(pic[0][i][j]==0){
//                    if(bottomLineLength==0 || sequenceBroke){
//                        tempXCoordinate=j;
//                    }
//                    sequenceBroke=false;
//                    bottomLineLength++;
//                    sequenceBreaker=0;
//                }
//                //white pixel
//                else {
//                    sequenceBreaker++;
//                    //the sequence broke if we had 3 white pixels in sequence and the line is shorter then quarter
//                    if(sequenceBreaker>2 && bottomLineLength<pic[0][0].length/4){
//                        sequenceBroke=true;
//                    }
//                }
//            }
//
//            //find the bottom line which is most black
//            if (bottomLineLength >= (double)pic[0][0].length/100*40)
//                if(bottomLineLength >= longestLineBottomLine
//                        && (i >= YCoordinateBottomLine-2 || YCoordinateBottomLine==pic[0].length)){
//                  //the bottom line
//                    XCoordinateBottomLine = tempXCoordinate;
//                    YCoordinateBottomLine = i;
//                    longestLineBottomLine = bottomLineLength;
//                }
//        }
//        setLongestLine(longestLine);
//        setBottomLongestLine(longestLineBottomLine);
//        setXCoordinate(XCoordinate);
//        setYCoordinate(YCoordinate);
//        int[][][] cutImg =new int[3][YCoordinateBottomLine-YCoordinate][Math.max(longestLine,longestLineBottomLine)];
//
//
//        //if the top line smaller flip the image
//        boolean flipped = false;
//        if (longestLine < longestLineBottomLine){
//        pic = flipImage(pic);
//            flipped = true;
//        }
//        //corners[0] = bottom right
//        //corners[1] = bottom left
//        //corners[2] = upper right
//        //corners[3] = upper left
//        corners = findCorners(pic,YCoordinate,YCoordinateBottomLine);
//        //int upRightXCoordinate = XCoordinate + longestLine;
//        //int downrightXCoordinate = XCoordinateBottomLine + longestLineBottomLine;
//        int rightGap = corners[2] - corners[0];
//        int leftGap = corners[1] - corners[3];
//        //int[][][] secondCutImg = new int[3][cutImg[0].length][cutImg[0][0].length];
//        if(cutImg[0].length + 3 < pic[0].length || cutImg[0][0].length + 3 < pic[0][0].length) {
//
//
//            //stretch the pic
//            for (int i = 1; i < cutImg[0].length; i++) {
//                //I_percent to know the ratio between "i" and the length
//                double I_percent = i / (double) cutImg[0].length;
//                //the wide necessary
//                double w = (corners[0]+(rightGap * ((1-I_percent))) - (leftGap * I_percent) - corners[3]);
//                //the ratio
//                double factor_w = w / (cutImg[0][0].length -(int) w);
//                if (w > cutImg[0][0].length){
//                    factor_w=cutImg[0][0].length;
//                    w=1;
//                }
//                //how many times to add each time
//                double addTimes = 1. /(factor_w);
//
//                int upAddTimes = (int) Math.ceil(addTimes);
//                //the relative ratio between the left side gap and the line (i)
//                long relativeRatio = Math.round((corners[1]-corners[3])*((I_percent)));
//
//                //creating lineSum to count the pixels in each line
//                int lineSum = 0;
//                for (int j = 0; j <= (cutImg[0][0].length - w); j++) {
//
//                    for (int k = 0; k < factor_w ; k++) {
//
//                        double l = (corners[3] + relativeRatio + lineSum - (j * (upAddTimes)));
//                        int lo =(int) Math.ceil(l);
//                        if (lo < pic[0][0].length && lineSum < cutImg[0][0].length && lo > 0) {
//
//                            cutImg[0][i][lineSum] = pic[0][i+YCoordinate][lo];
//
//                            cutImg[1][i][lineSum] = pic[1][i+YCoordinate][lo];
//
//                            cutImg[2][i][lineSum] = pic[2][i+YCoordinate][lo];
//
//                            lineSum++;
//                        }
//
//                    }
//
//                    for (int k1 = 0; k1 < addTimes; k1++) {
//                        double y = corners[3] + relativeRatio + (lineSum - 1 - k1)- (j * (upAddTimes));
//                        int yo = (int) Math.ceil(y)-1;
//                        if (yo < pic[0][0].length && yo > 0 && lineSum < cutImg[0][0].length) {
//                            //copy the pixel before addTimes times
//                            cutImg[0][i][(lineSum)] = pic[0][i + YCoordinate][yo];
//
//                            cutImg[1][i][(lineSum)] = pic[1][i + YCoordinate][yo];
//
//                            cutImg[2][i][(lineSum)] = pic[2][i + YCoordinate][yo];
//
//                            lineSum++;
//                        }
//                    }
//                }
//            }
//        }
//        else {
//            return pic;
//        }
//        if(flipped){
//            cutImg = flipImage(cutImg);
//        }
//        return cutImg;
//    }
//    public  void setPic(int[][][] pic) {
//        this.pic = pic;
//    }
//    public  int[][][] getPic() {
//        return pic;
//    }

    //    public  int getDigitCounter() {
//        return digitCounter;
//    }
//
//    private  int[][][] flipImage(int[][][] image) {
//
//        int[][][] flip = new int[3][image[0].length][image[0][0].length];
//        for (int i = 0; i < image[0].length; i++) {
//            for (int j = 0; j < image[0][0].length; j++) {
//                flip[0][i][j]=image[0][image[0].length-1-i][image[0][0].length-1-j];
//                flip[1][i][j]=image[1][image[0].length-1-i][image[0][0].length-1-j];
//                flip[2][i][j]=image[2][image[0].length-1-i][image[0][0].length-1-j];
//            }
//        }
//        writeImageToFile("src/flip.jpg",flip);
//        return flip;
//        //return image;
//    }
//
//    private  int[] findCorners(int[][][] pic, int YCoordinate, int YCoordinateBottomLine) {
//        //corners[0] = bottom right
//        //corners[1] = bottom left
//        //corners[2] = upper right
//        //corners[3] = upper left
//        int[] corners = new int[4];
//        int temp=0;
//
//        //bottom right corner
//        for (int i = pic[0][0].length-1; i >= 0; i--) {
//            //checking last line
//            if (pic[0][YCoordinateBottomLine][i]==0){
//                temp++;
//                if (temp>0.1*pic[0][0].length){
//                    corners[0]=(int) (i+0.1*pic[0][0].length);
//                    break;
//                }
//            }
//            else {
//                temp=0;
//            }
//        }
//        temp=0;
//        //bottom left corner
//        for (int i = 0; i < pic[0][0].length ; i++) {
//            //checking last line
//            if (pic[0][YCoordinateBottomLine][i]==0){
//                temp++;
//                if (temp>0.1*pic[0][0].length){
//                    corners[1] = (int) (i-0.1*pic[0][0].length)+1;
//                    break;
//                }
//            }
//            else {
//                temp=0;
//            }
//        }
//        temp=0;
//        //upper right corner
//        for (int i = pic[0][0].length-1; i >= 0; i--) {
//            //checking last line
//            if (pic[0][YCoordinate][i]==0){
//                temp++;
//                if (temp>0.1*pic[0][0].length){
//                    corners[2]= (int) (i+0.1*pic[0][0].length);
//                    break;
//                }
//            }
//            else {
//                temp=0;
//            }
//        }
//        temp=0;
//        //upper left corner
//        for (int i = 0; i < pic[0][0].length ; i++) {
//            //checking last line
//            if (pic[0][YCoordinate][i]==0){
//                temp++;
//                if (temp>0.1*pic[0][0].length){
//                    corners[3] = (int) (i-0.1*pic[0][0].length)+1;
//                    break;
//                }
//            }
//            else {
//                temp=0;
//            }
//        }
//
//        return corners;
//    }

//    public void setYCoordinate(int YCoordinate) {
//        this.YCoordinate = YCoordinate;
//    }
//
//    public int getXCoordinate() {
//        return XCoordinate;
//    }
//
//    public void setXCoordinate(int XCoordinate) {
//        this.XCoordinate = XCoordinate;
//    }
//
//    public int getLongestLine() {
//        return longestLine;
//    }
//
//    public void setLongestLine(int longestLine) {
//        this.longestLine = longestLine;
//    }
//
//    public int getLongestLineBottomLine() {
//        return longestLineBottomLine;
//    }
//
//    public int getBottomLongestLine() {
//        return bottomLongestLine;
//    }
//
//    public void setBottomLongestLine(int bottomLongestLine) {
//        this.bottomLongestLine = bottomLongestLine;
//    }
//
//    public void setLongestLineBottomLine(int longestLineBottomLine) {
//        this.longestLineBottomLine = longestLineBottomLine;
//    }


//    public void setGameLogic(GameLogic gameLogic) {
//        this.gameLogic = gameLogic;
//    }
    //    public  void setDigitsInTheSquare(boolean[] digitsInTheSquare) {
//        this.digitsInTheSquare = digitsInTheSquare;
//    }
//
//    public  void setHasDigitInTheImage(boolean hasDigitInTheImage) {
//        this.hasDigitInTheImage = hasDigitInTheImage;
//    }

}
