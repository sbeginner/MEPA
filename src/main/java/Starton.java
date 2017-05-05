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
        /** K-fold validation data **/
        setConfig();
        MEPA_crossValidateModel(10,1);


        /** Train-Test data **/
        setConfig();
        MEPA_TrainTestModel(1);
    }

    public static void MEPA_crossValidateModel(int maxfoldnum, int randseed) throws IOException {
        DataInput dt = new DataInput();
        dt.forKfoldValidationInstance();
        dt.completeData();
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
        dt.completeData();
        Instances instances = dt.getInstances();    //get data

        instances.autoShuffleInstanceOrder();    //Optional, shuffle the instance item
        instances.setRandSeed(randseed);         //Optional
        Filter.useFilter(instances, new MEPA());
    }

    public static void setConfig(){
        //Root
        Setup.Config.FILEPATH = "C:/Data/test";

        //K-fold validation data
        Setup.Config.FILENAME = "valid_USED.txt";// k-fold validation data

        //Train-Test data
        Setup.Config.FILETRAINNAME = "train_USED.txt";// Train data
        Setup.Config.FILETESTNAME = "test_USED.txt";// Test data
    }

}
