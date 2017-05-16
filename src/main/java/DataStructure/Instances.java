package DataStructure;


import Container.MEPAMembership;
import Container.MEPAMembershipMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import static Setup.Config.*;

/**
 * Created by jack on 2017/3/20.
 */
public class Instances{

    private boolean currentMode = false;    //Train-test as true, k fold validation as false
    private int currentFoldValid = 0;

    private HashMap<Integer, Attribute> attributesMap;
    private HashMap<Integer, Instance> instanceMap;
    private HashMap<Integer, Instance> shuffleInstanceMap;    //Optional. shuffle the elements in instanceMap
    private HashMap<Integer, Instance> trainInstanceMap;
    private HashMap<Integer, Instance> testInstanceMap;

    private HashMap<Integer, ArrayList<Integer>> missingValueMap;    //HashMap(lineNum, attributeInd list)
    private HashMap<Integer, ArrayList<Integer>> missingValueMapforTest;    //HashMap(lineNum, attributeInd list)

    private MEPAMembershipMap MEPAMembershipForTrain;    //The MEPA-processed train instances, can get some information, e.g. 'MEPA membership degree'
    private MEPAMembershipMap MEPAMembershipForTest;    //The MEPA-processed test instances, can get some information, same as MEPAMembershipForTrain


    public Instances(){
        attributesMap = new HashMap(ATTRIBUTE_NUM);
        instanceMap = new HashMap(INSTANCE_NUM);
        trainInstanceMap = new HashMap(INSTANCE_NUM);
        testInstanceMap = new HashMap(INSTANCE_NUM);

        missingValueMap = new HashMap(ATTRIBUTE_NUM);
        missingValueMapforTest = new HashMap(ATTRIBUTE_NUM);

        MEPAMembershipForTrain = new MEPAMembershipMap(this);
        MEPAMembershipForTest = new MEPAMembershipMap(this);
    }


    public void setMissingValueMap(Integer errorLine, ArrayList<Integer> missingValueAttrInd, boolean checkIsTrainTestmodeAndIsTest){
        //train-test mode: train and k-fold mode as same process, train-test mode: test might use other container to store it.
        if(checkIsTrainTestmodeAndIsTest){
            this.missingValueMapforTest.put(errorLine, missingValueAttrInd);
            return;
        }

        this.missingValueMap.put(errorLine, missingValueAttrInd);
    }

    public void setAttribute(String attributeName){
        Attribute attr = new Attribute(new StringBuilder(attributeName));
        attr.setIndex(autoResizeIndex(attributesMap));
        attributesMap.put(autoResizeIndex(attributesMap), attr);
    }

    public void setInstance(Instance inst){
        inst.setInstances(this);
        instanceMap.put(autoResizeIndex(instanceMap), inst);
    }

    public void setTrainInstance(Instance inst){
        inst.setInstances(this);
        trainInstanceMap.put(autoResizeIndex(trainInstanceMap), inst);
    }

    public void resetTestInstance(){
        testInstanceMap.clear();
    }

    public void setTestInstance(Instance inst){
        inst.setInstances(this);
        testInstanceMap.put(autoResizeIndex(testInstanceMap), inst);
    }

    public void setCurrentMode(boolean currentMode){
        //Train-test as true, k fold validation as false
        this.currentMode = currentMode;
    }

    public void setRandSeed(int randseed){
        //For fixed
        RANDOM_SEED = randseed;
    }

    public void setMaxFoldNum(int maxfoldnum){
        //Set the max fold num
        MAX_FOLDNUM = maxfoldnum;
    }

    public void setMEPAMembership(Attribute curAttr, ArrayList<MEPAMembership> MEPAMembershipList, boolean isTest){
        //Set train or test MEPAMembership by attribute index
        if(isTest){
            MEPAMembershipForTest.setMEPAMembershipMap(curAttr, MEPAMembershipList);
        }else
            MEPAMembershipForTrain.setMEPAMembershipMap(curAttr, MEPAMembershipList);
    }


    public Attribute getAttribute(int index){
        return this.attributesMap.get(index);
    }

    public Instance getInstance(int index){
        return this.instanceMap.get(index);
    }

    private Instance getTrainInstance(int index){
        return this.trainInstanceMap.get(index);
    }

    private Instance getTestInstance(int index){
        return this.testInstanceMap.get(index);
    }

    public Instance getInstance(boolean isTest, int index){
        if(isTest){
            return getTestInstance(index);
        }

        return getTrainInstance(index);
    }

    public  HashMap<Integer, Attribute> getAttributeMap(){
        return this.attributesMap;
    }

    public  HashMap<Integer, Instance> getInstanceMap(){
        return this.instanceMap;
    }

    public HashMap<Integer, Instance> getTrainInstanceMap(){
        return this.trainInstanceMap;
    }

    public HashMap<Integer, Instance> getTestInstanceMap(){
        return this.testInstanceMap;
    }

    public HashMap<Integer, ArrayList<Integer>> getmissingValueMap( boolean checkIsTrainTestmodeAndIsTest){
        if(checkIsTrainTestmodeAndIsTest){
            return this.missingValueMapforTest;
        }
        //System.out.println(missingValueMap);
        return this.missingValueMap;
    }

    public MEPAMembershipMap getMEPAMembershipMap(boolean isTest){
        //Get train or test MEPAMembershipMap
        if(isTest){
            return MEPAMembershipForTest;
        }else
            return MEPAMembershipForTrain;
    }

    public ArrayList<MEPAMembership> getMEPAMembership(Attribute curAttr, boolean isTest){
        //Get train or test MEPAMembership by attribute
        if(isTest){
            return MEPAMembershipForTest.getAllInstanceByAttr(curAttr);
        }else
            return MEPAMembershipForTrain.getAllInstanceByAttr(curAttr);
    }

    public ArrayList<MEPAMembership> getMEPAMembership(int curAttrInd, boolean isTest){
        //Get train or test MEPAMembership by attribute index
        return getMEPAMembership(this.getAttribute(curAttrInd), isTest);
    }

    public boolean getCurrentMode(){
        return this.currentMode;
    }

    public int getCurrentFoldValid(){
        return this.currentFoldValid;
    }


    private void splitTrainTestInEachFold(int valid){
        //Only for k-fold validation, this function splits train and test data for classification
        HashMap<Integer, Instance> currentInstanceMap;
        this.trainInstanceMap.clear();
        this.testInstanceMap.clear();

        if(!INSTANCEORDER_SHUFFLE_BTN){
            //For no shuffle
            currentInstanceMap = instanceMap;
        }else {
            currentInstanceMap = shuffleInstanceMap;
        }
        //front [0]; back [1]
        int[] frontback = splitMethodInWeka(valid);
        splitTrainTest(currentInstanceMap, frontback);
    }

    private void splitTrainTest(HashMap<Integer, Instance> currentInstanceMap, int[] frontback){
        //train
        currentInstanceMap.entrySet()
                .stream()
                .filter(item -> !(item.getKey() >= frontback[0] && item.getKey() < frontback[1]))
                .forEach(item -> {
                    this.trainInstanceMap.put(autoResizeIndex(trainInstanceMap), item.getValue());
                });
        INSTANCE_NUM_TRAIN =  this.trainInstanceMap.size();

        //test
        currentInstanceMap.entrySet()
                .stream()
                .filter(item -> item.getKey() >= frontback[0] && item.getKey() < frontback[1])
                .forEach(item -> {
                    this.testInstanceMap.put(autoResizeIndex(testInstanceMap), item.getValue());
                });
        INSTANCE_NUM_TEST =  this.testInstanceMap.size();
    }

    private int[] splitMethodInWeka(int valid){
        //A way reference from weka CV method
        //find front [0] and back [1]
        int numInstForFold = INSTANCE_NUM / MAX_FOLDNUM;
        int offset, front, back;

        if(valid < INSTANCE_NUM % MAX_FOLDNUM){
            numInstForFold++;
            offset = valid;
        }else {
            offset = INSTANCE_NUM % MAX_FOLDNUM;
        }

        front = valid * numInstForFold + offset;
        back = front + numInstForFold;

        return new int[]{front, back};
    }


    public void autoShuffleInstanceOrder(){
        //Only for k-fold validation, this function shuffles the original instance item order by random (random seed is set).
        if(!INSTANCEORDER_SHUFFLE_BTN){
            return;
        }

        shuffleInstanceMap = new HashMap(INSTANCE_NUM);
        ArrayList<Integer> shufflekeylist = new ArrayList(instanceMap.keySet());
        Collections.shuffle(shufflekeylist, new Random(RANDOM_SEED));
        shufflekeylist.stream().forEach(orderedkey -> shuffleInstanceMap.put(orderedkey, instanceMap.get(orderedkey)));
    }

    public void autoCVInKFold(int valid){
        //Only for k-fold validation, split the train and test instance in each fold.
        this.currentFoldValid = valid;
        splitTrainTestInEachFold(valid);
    }

    private int autoResizeIndex(HashMap targetmap){
        return targetmap.size();
    }
}
