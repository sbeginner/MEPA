package Preprocess;

import Container.MEPAConcernAttr;
import Container.MEPAConcernAttrList;
import Container.MEPAMembership;
import Container.MEPAMembershipMap;
import DataStructure.Attribute;
import DataStructure.Instance;
import DataStructure.Instances;
import FileIO.DataOutput;
import MathCalculate.Arithmetic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static MathCalculate.Arithmetic.createDouble;
import static Setup.Config.*;

/**
 * Created by jack on 2017/3/30.
 */
public class MEPA extends MEPAEntropy {
    private Instances instances;
    private boolean isPrepared = false;

    public MEPA(){
        this.queueForChildInstance = new LinkedList();
        this.bestThresholdList = new ArrayList();
    }


    public void setInstances(Instances instances){
        this.instances = instances;
    }

    public void setDivConstraintNum(int divConstraintNum){
        //Set the number for the range to split, divide constraint number
        DIVIDE_CONSTRAINTNUM = divConstraintNum;
    }

    public void useFilter(){
        //Travel all attributes
        IntStream.range(0, ATTRIBUTE_NUM)
                .forEach(this::MEPAProcess);

        setPrepared();
        setNewTestDataNum();
    }

    private void setPrepared(){
        if(isPrepared){
            return;
        }
        isPrepared = true;
    }

    private void setNewTestDataNum(){
        DIFFERENT_TESTDATA++;
    }

    private void MEPAProcess(int attributeInd){

        Attribute curAttribute = instances.getAttribute(attributeInd);

        if(curAttribute.getAttributeType() == true){
            //If attribute type is string goes here
            //Membership degree is 1.0
            if(!isPrepared){
                remainInstanceInfo(curAttribute, true);
            }
            remainInstanceInfo(curAttribute, false);
        }else{
            //If attribute type is digital goes here
            //Train instance uses to find threshold, and their membership degree set 1.0
            //Test instance needs to calculate the membership degree for each
            if(!isPrepared){
                setTrainInstanceInfo(curAttribute);
            }
            setTestInstanceInfo(curAttribute);
        }

        setMEPAMembershipMapInfo(curAttribute);
    }


    private void remainInstanceInfo(Attribute curAttribute, boolean isTest){
        //If the attribute is string type, it can just skip the MEPA method processing
        HashMap<Integer, Instance> instanceMap;

        if(isTest){
            instanceMap = instances.getTestInstanceMap();
        }else
            instanceMap = instances.getTrainInstanceMap();

        ArrayList<MEPAMembership> MEPAMembershipList = instanceMap
                .entrySet()
                .stream()
                .map(item -> new MEPAMembership(item.getValue().getInstanceValue(curAttribute).toString(), 1))
                .collect(Collectors.toCollection(ArrayList::new));

        instances.setMEPAMembership(curAttribute, MEPAMembershipList, isTest);
    }

    private void setTrainInstanceInfo(Attribute curAttribute){
        //Find out the threshold by using Information Gain method
        //We use the training data set to find out those threshold, and use these threshold in testing data set
        calcTargetValueNumInEachAttribute(curAttribute.getIndex());

        //Sort the identified threshold, it can be more simple to group the data set (instance)
        bestThresholdListSort(curAttribute);

        ArrayList<MEPAMembership> MEPAMembershipList = instances.getTrainInstanceMap()
                .entrySet()
                .stream()
                .map(trainInstance -> detectValueRangeChangeToStr(createDouble(trainInstance.getValue().getInstanceValue(curAttribute).toString()),false))
                .collect(Collectors.toCollection(ArrayList::new));

        instances.setMEPAMembership(curAttribute, MEPAMembershipList, false);
    }

    private void calcTargetValueNumInEachAttribute(int attributeInd){
        //Init
        initTargetValueNumInEachAttribute();

        //Set attribute value list, and select the list item to travel the whole instances,
        //this work aims to find out the best split threshold (by using the concept of entropy).
        ArrayList<Double> curAttrValueList = new ArrayList(getInputInstances().getAttribute(attributeInd).getAllTrainValueInDigital());

        //transfer the whole train instances to only two columns(attributes), including considered column and target.
        ArrayList<MEPAConcernAttr> transTrainInstanceList = transTrainInstance(attributeInd);

        //Recursive call method by queue process, finding out the best split threshold list by evaluating the Information Gain
        //Each splitting work can produce two child, and the next splitting step can produce the other two child based on there father,
        //therefore, recursive call method used
        getQueueForChildInstance().offer(transTrainInstanceList);
        calcEntropy(getQueueForChildInstance().poll(), curAttrValueList); //The most important place for calculating (Entropy...)
    }

    private void bestThresholdListSort(Attribute curAttribute){
        //Sort the best threshold set, including distinct process
        bestThresholdList = bestThresholdList.stream()
                .distinct()
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));
        //Attribute set thresholdList
        curAttribute.setThresholdList(new ArrayList(bestThresholdList));
    }

    private ArrayList<MEPAConcernAttr> transTrainInstance(int attributeInd){
        //Only Current attribute and target attribute will be selected
        MEPAConcernAttrList concernAttrList = new MEPAConcernAttrList();

        getInputInstances().getTrainInstanceMap().entrySet().stream().forEach(instance -> {
            concernAttrList.addMEPAConcernAttr(instance.getValue().getInstanceDigitalValue(attributeInd), instance.getValue().getInstanceValue(TARGET_ATTRIBUTE));
        });

        return concernAttrList.getConcernAttrList();
    }

    private void setTestInstanceInfo(Attribute curAttribute){
        //Set the test instance by using the threshold that train data find
        ArrayList<MEPAMembership> MEPAMembershipList =  instances.getTestInstanceMap()
                .entrySet()
                .stream()
                .map(item -> detectValueRangeChangeToStr(createDouble(item.getValue().getInstanceValue(curAttribute).toString()), true))
                .collect(Collectors.toCollection(ArrayList::new));

        instances.setMEPAMembership(curAttribute, MEPAMembershipList, true);
    }

    private void setMEPAMembershipMapInfo(Attribute curAttribute){
        MEPAMembershipMap trainMEPAMembershipMap = instances.getMEPAMembershipMap(false);
        trainMEPAMembershipMap.priorProbabilityCalc(curAttribute);
        trainMEPAMembershipMap.setbestThresholdMap(curAttribute, bestThresholdList);
    }


    private MEPAMembership detectValueRangeChangeToStr(double curNum, boolean isTest){
        //The number categorize into its range
        String replaceStr;
        boolean degreeIsUnderEstimate;

        //Special case in divide num = 0
        if(bestThresholdList.size() == 1){
            if(bestThresholdList.get(0) >= curNum){
                replaceStr = String.valueOf("T0");
                return new MEPAMembership(replaceStr, 1);
            }else {
                replaceStr = String.valueOf("T1");
                return new MEPAMembership(replaceStr, 1);
            }
        }

        //minMargin
        if(detectValueRangeOverMinMargin(curNum)){
            replaceStr = String.valueOf("T0");
            return new MEPAMembership(replaceStr, 1);
        }

        //maxMargin
        if(detectValueRangeOverMaxMargin(curNum)){
            replaceStr = String.valueOf("T" + (bestThresholdList.size() - 1));
            return new MEPAMembership(replaceStr, 1);
        }

        //Others in middle side
        int frontInRange = IntStream.range(0, bestThresholdList.size() - 1)
                .filter(i -> bestThresholdList.get(i+1) >= curNum && curNum > bestThresholdList.get(i))
                .findFirst()
                .orElse(-99);
        int frontInRangeNext = frontInRange + 1;
        double leftThreshold = bestThresholdList.get(frontInRange);
        double rightThreshold = bestThresholdList.get(frontInRangeNext);

        if(Math.abs(curNum - bestThresholdList.get(frontInRangeNext)) >= Math.abs(curNum - bestThresholdList.get(frontInRange))){
            //curNum is close to left side
            degreeIsUnderEstimate = true;
            replaceStr = String.valueOf("T" + frontInRange);
        }else {
            degreeIsUnderEstimate = false;
            replaceStr = String.valueOf("T" + frontInRangeNext);
        }

        if(degreeIsUnderEstimate){
            return new MEPAMembership(replaceStr, Arithmetic.sub(1, membershipDegree(leftThreshold, rightThreshold, curNum)));
        }

        return new MEPAMembership(replaceStr, membershipDegree(leftThreshold, rightThreshold, curNum));
    }

    private boolean detectValueRangeOverMinMargin(double curNum){
        if(bestThresholdList.get(0) >= curNum){
            return true;
        }
        return false;
    }

    private boolean detectValueRangeOverMaxMargin(double curNum){
        if(curNum >= bestThresholdList.get(bestThresholdList.size() - 1)){
            return true;
        }
        return false;
    }

    private double membershipDegree(double leftThreshold, double rightThreshold, double currentNum){
        return Arithmetic.div(Arithmetic.sub(currentNum, leftThreshold), Arithmetic.sub(rightThreshold, leftThreshold));
    }



    private Instances getInputInstances(){
        return instances;
    }

    public Instances getInstances(){
        return instances;
    }

    public void MEPAInstancesOutput(){
        if(!MEPADATA_OUTPUT_BTN){
            return;
        }

        MEPATestInstancesOutput();
        MEPATrainInstancesOutput();
    }

    private void MEPATestInstancesOutput(){
        MEPAInstancesOutputProcess(true);
    }

    private void MEPATrainInstancesOutput(){
        MEPAInstancesOutputProcess(false);
    }

    private void MEPAInstancesOutputProcess( boolean isTest){
        DataOutput dataOutput = new DataOutput(instances.getCurrentMode(), instances.getCurrentFoldValid());
        MEPAMembershipMap mMEPAMembershipMap = instances.getMEPAMembershipMap(isTest);

        int maxInstanceNum;
        StringBuilder OriginData = setAttributeTitle();
        StringBuilder MembershipNameStr = setAttributeTitle();
        StringBuilder MembershipDegreeStr = setAttributeTitle();

        if(isTest){
            maxInstanceNum = INSTANCE_NUM_TEST;
        }else {
            maxInstanceNum = INSTANCE_NUM_TRAIN;
        }

        IntStream.range(0, maxInstanceNum)
                .peek(instanceNum -> {
                    IntStream.range(0, ATTRIBUTE_NUM).forEach(attributeNum ->{

                        OriginData.append(instances.getInstance(isTest, instanceNum).getInstanceValue(attributeNum));

                        if(attributeNum % ATTRIBUTE_NUM != (ATTRIBUTE_NUM - 1)){
                            OriginData.append(',');
                        }
                    });
                    OriginData.append(System.getProperty("line.separator"));
                })
                .boxed()
                .flatMap(instanceNum -> IntStream.range(0, ATTRIBUTE_NUM)
                        .mapToObj(attributeNum -> {

                            if(attributeNum % ATTRIBUTE_NUM == 0) {
                                if(instanceNum == 0){
                                    //do nothing
                                }else{
                                    MembershipNameStr.append(System.getProperty("line.separator"));
                                    MembershipDegreeStr.append(System.getProperty("line.separator"));
                                }
                            }else {
                                MembershipNameStr.append(',');
                                MembershipDegreeStr.append(',');
                            }

                            return mMEPAMembershipMap.getAllInstanceByAttr(attributeNum).get(instanceNum);    //MEPAMembership type
                        }))
                .forEach(MEPAMembershipItem ->{
                    MembershipNameStr.append(MEPAMembershipItem.getMembership());
                    MembershipDegreeStr.append(MEPAMembershipItem.getMembershipDegree());
                });

        if(isTest){
            dataOutput.outputTestMembership(OriginData, "_TestOriginData");
            dataOutput.outputTestMembership(MembershipNameStr, "_TestMembershipName");
            dataOutput.outputTestMembership(MembershipDegreeStr, "_TestMembershipDegree");
        }else {
            dataOutput.outputTrainMembership(OriginData, "_TrainOriginData");
            dataOutput.outputTrainMembership(MembershipNameStr, "_TrainMembershipName");
            dataOutput.outputTrainMembership(MembershipDegreeStr, "_TrainMembershipDegree");
            dataOutput.outputTrainMembership(setAttrThreshold(), "_AttrThreshold");
        }

    }

    private StringBuilder setAttributeTitle(){
        StringBuilder cpAppendStr = new StringBuilder();

        IntStream.range(0, ATTRIBUTE_NUM).forEach(attributeNum -> {
            cpAppendStr.append(instances.getAttribute(attributeNum).getAttributeName());
            if(attributeNum % ATTRIBUTE_NUM != (ATTRIBUTE_NUM-1)){
                cpAppendStr.append(',');
            }
        });

        cpAppendStr.append(System.getProperty("line.separator"));

        return cpAppendStr;
    }

    private StringBuilder setAttrThreshold(){

        StringBuilder cpAttrThresholdStr = new StringBuilder();
        IntStream.range(0, ATTRIBUTE_NUM).forEach(attributeNum ->{
            cpAttrThresholdStr.append("Attribute["+instances.getAttribute(attributeNum).getIndex()+"] ");
            cpAttrThresholdStr.append(instances.getAttribute(attributeNum).getAttributeName()+" => ");
            cpAttrThresholdStr.append(instances.getAttribute(attributeNum).getThresholdList()+" ");
            if(attributeNum == TARGET_ATTRIBUTE){
                cpAttrThresholdStr.append(" :: \" This is the target attribute! \"");
            }
            cpAttrThresholdStr.append(System.getProperty("line.separator"));
        });

        return cpAttrThresholdStr;
    }

}
