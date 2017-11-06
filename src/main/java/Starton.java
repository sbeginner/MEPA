import DataStructure.Instances;
import FileIO.DataInput;
import Preprocess.Filter;
import Preprocess.MEPA;

import java.io.IOException;
import java.util.stream.IntStream;

import static Setup.Config.MAX_FOLDNUM;

/**
 * Created by jack on 2017/3/20.
 */
public class Starton {
    public static void main(String str[]) throws IOException {
        setConfig();
        MEPA_crossValidateModel(10,1);
//        MEPA_TrainTestModel(1);
    }

    public static void MEPA_crossValidateModel(int maxfoldnum, int randseed) throws IOException {
        DataInput dt = new DataInput();
        dt.forKfoldValidationInstance();
        Instances instances = dt.getInstances();    //get data

        instances.setRandSeed(randseed);         //Optional
        instances.autoShuffleInstanceOrder();    //Optional, shuffle the instance item
        instances.setMaxFoldNum(maxfoldnum);

        IntStream.range(0, MAX_FOLDNUM).forEach(curfoldInd -> {
            instances.autoCVInKFold(curfoldInd);
            Filter.useFilter(instances, new MEPA());
        });
    }

    public static void MEPA_TrainTestModel(int randseed) throws IOException {
        DataInput dt = new DataInput();
        dt.forTrainTestInstance();
        Instances instances = dt.getInstances();    //get data

        MEPA mepa =new MEPA();
        Filter.useFilter(instances, mepa);
    }

    public static void MEPA_TrainTestModeltest(int randseed) throws IOException {
        DataInput dt = new DataInput();
        dt.forTrainTestInstance();
        Instances instances = dt.getInstances();    //get data

        MEPA mepa =new MEPA();
        Filter.useFilter(instances, mepa);

        //Use current MEPA-threshold to handle the new test data
        dt.forTestInstance("C:/Data/test/test_USED.txt");
        instances = dt.getInstances();    //get data
        Filter.useFilter(instances, mepa);
        System.out.println(instances.getTestInstanceMap());


        //Use current MEPA-threshold to handle the new test data
        dt.forTestInstance("C:/Data/test/test_USED.txt");
        instances = dt.getInstances();    //get data
        Filter.useFilter(instances, mepa);
        System.out.println(instances.getTestInstanceMap());


        //Use current MEPA-threshold to handle the new test data
        dt.forTestInstance("C:/Data/test/test_USED.txt");
        instances = dt.getInstances();    //get data
        Filter.useFilter(instances, mepa);
        System.out.println(instances.getTestInstanceMap());


        //Use current MEPA-threshold to handle the new test data
        dt.forTestInstance("C:/Data/test/test_USED.txt");
        instances = dt.getInstances();    //get data
        Filter.useFilter(instances, mepa);
        System.out.println(instances.getTestInstanceMap());
    }

    public static void setConfig(){
        Setup.Config.DIVIDE_CONSTRAINTNUM = 5;//split num

        //Root
        Setup.Config.FILEPATH = "C:/Data/Biotset/liver";

        //K-fold validation data
        Setup.Config.FILENAME = "origin.txt";// k-fold validation data

        //Train-Test data
        Setup.Config.FILETRAINNAME = "weka_test.csv";// Train data
        Setup.Config.FILETESTNAME = "weka_test.csv";// Test data
    }
}
