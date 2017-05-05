package FileIO;

/**
 * Created by jack on 2017/3/28.
 */
public class DataIOException {

    protected boolean switchSecondTime = false;      //For whether train or test command first,only use in train-test mode
    protected boolean switchTrainTest = false;       //For train-test mode, check current stage is train or test
    protected boolean switchKFoldValidation = false; //For k-fold validation mode, check current stage is train or test
    protected boolean switchAttributeFound = false;  //For all mode(train-test and k-fold), check attribute is already prepared
    protected int errorLineCount = -1;               //For error input, check which line in dataset have some problem, init as -1

    protected boolean errorAttributeBetweenTrainTest(int errorcode){
        //Calling error code that occurs in the processing of data input
        try {
            errorAttributeBetweenTrainTestException(errorcode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    protected void errorAttributeBetweenTrainTestException(int errorcode) throws Exception{
        //Error code statement
        switch (errorcode){
            case 0:
                throw new Exception("Train and Test Attribute no match! check by length");
            case 1:
                throw new Exception("Train and Test Attribute no match! check by attribute value");
            case 2:
                throw new Exception("Instance item has some problem, may lose some item, line : "+geterrorLine());
            case 3:
                throw new Exception("Instance item has some problem, missing value occurs, line : "+geterrorLine());
        }
    }

    protected int geterrorLine(){
        return errorLineCount;
    }

    protected void errorLineCountPlus(){
        errorLineCount += 1;
    }

    protected void errorLineCountInit(){
        errorLineCount = -1;
    }


    protected void switchSecondTime(){
        //Only for train-test, this function makes sure all the data source(includes train and test data) have been scanned
        //The attribute value will be set in the second time (switchSecondTime == true).
        //To avoid add the same attributes again.
        switchSecondTime = !switchSecondTime;
    }

    protected void switchOnlyFirstTime(){
        //Only for k-fold validation, the function just scan in one data source at a time
        switchSecondTime = true;
    }

    protected void switchForKFoldvalidation(boolean switchKFoldValidation){
        //Set current to k-fold validation mode, true as k-fold mode, alternatively, false as train-test mode
        this.switchKFoldValidation = switchKFoldValidation;
    }

    protected void switchForTrainTest(boolean switchTrainTest){
        //Set current to train-test mode, true as train, false as test.
        this.switchTrainTest = switchTrainTest;
    }

    protected void switchAttributeFound(){
        //Attribute is already prepared
        switchAttributeFound = true;
    }


    protected boolean checkModeAndIsTest(boolean currentMode, boolean isTrain){
        //Check current mode is train-test mode, and only in test mode return true.
        return (currentMode == true) && !isTrain;
    }

    protected boolean checkCurrentMode(){
        //Check current mode, train-test mode as true, k-fold validation mode as false
        return !checkIsKFoldvalidation();
    }

    protected boolean checkIsSecondTime(){
        return switchSecondTime;
    }

    protected boolean checkIsTrainTest(){
        //Train as true, test as false
        return switchTrainTest;
    }

    protected boolean checkIsKFoldvalidation(){
        return switchKFoldValidation;
    }

    protected boolean checkIsAttributeFound(){
        return switchAttributeFound;
    }
}
