package DataStructure;

import MathCalculate.Arithmetic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.IntStream;

import static Setup.Config.ATTRIBUTEVALUE_NUM;
import static Setup.Config.UNKNOWNVALUE;

/**
 * Created by jack on 2017/3/20.
 */
public class Attribute{

    private int index;                                       //Attribute index
    private Boolean attributeTypeisStr;                      //Attribute type, string as true, digital as false
    private StringBuilder attributeName;                     //Attribute name
    private ArrayList<StringBuilder> attrvalueTrainStringList;    //Attribute value list for all value (includes string and digital)
    private ArrayList<StringBuilder> attrvalueTTStringList;    //Attribute value list for all value from train and test instance(includes string and digital)
    private ArrayList<Double> attrvalueDigitList;            //Attribute value list for only digital type
    private HashMap<String, Integer> attrvalueMap;           //Attribute value set in train or k-fold validation mode
    private HashMap<String, Integer> attrvalueMapforTest;    //Attribute value set in test mode

    private double formissingNum;              //For replacing The missing number in train or k-fold validation mode
    private String formissingStr;              //For replacing The missing string in train or k-fold validation mode
    private double formissingNumForTest;       //For replacing The missing number in test mode
    private String formissingStrForTest;       //For replacing The missing string in test mode

    ArrayList<Double> thresholdList;    //For double type attribute, calculating membership degree

    public Attribute(StringBuilder attributeName){
        //Set attribute index and name
        this.attributeName = attributeName;
        attrvalueTrainStringList = new  ArrayList(ATTRIBUTEVALUE_NUM);
        attrvalueTTStringList = new  ArrayList(ATTRIBUTEVALUE_NUM);
        attrvalueDigitList = new  ArrayList(ATTRIBUTEVALUE_NUM);
        attrvalueMap = new HashMap(ATTRIBUTEVALUE_NUM);
        attrvalueMapforTest = new HashMap(ATTRIBUTEVALUE_NUM);
    }


    public void setIndex(int index){
        //Set attribute index
        this.index = index;
    }

    public void setForMissingValue(double formissingNum, boolean checkIsTest){
        //Set missing digital value
        if(checkIsTest){
            this.formissingNumForTest = formissingNum;
            return;
        }
        this.formissingNum = formissingNum;
    }

    public void setForMissingValue(String formissingStr, boolean checkIsTest){
        //Set missing string value
        if(checkIsTest){
            this.formissingStrForTest = formissingStr;
            return;
        }
        this.formissingStr = formissingStr;
    }

    public void setAttributeType(boolean attributeTypeisStr){
        //Set attribute index
        this.attributeTypeisStr = attributeTypeisStr;
    }

    public void setAttributeValue(String attrvalue,boolean checkIsTest){
        //Set attribute value list
        if(UNKNOWNVALUE.equals(attrvalue)){
            return;
        }

        if(checkIsTest){
            //set Test attribute value
            this.attrvalueMapforTest.put(attrvalue, autoItemFrequencyCounter(this.attrvalueMapforTest.get(attrvalue)));
            return;
        }

        //set Train or k-fold validation attribute value
        this.attrvalueMap.put(attrvalue, autoItemFrequencyCounter(this.attrvalueMap.get(attrvalue)));
    }

    public void setThresholdList(ArrayList<Double> thresholdList){
        //Set threshold for MEPA
        this.thresholdList = thresholdList;
    }


    public int getIndex(){
        //Get attribute index
        return index;
    }

    public String getMissingValue(){
        //Get value to replace the original missing value
        if(getAttributeType()){
            return this.formissingStr;
        }
        return String.valueOf(this.formissingNum);
    }

    public String getMissingValueTest(){
        //Get value to replace the original missing value
        if(getAttributeType()){
            return this.formissingStrForTest;
        }
        return String.valueOf(this.formissingNumForTest);
    }

    public StringBuilder getAttributeName(){
        //Get attribute name
        return attributeName;
    }

    public StringBuilder getTrainValue(int index){
        //Get 'attribute value' by index
        return attrvalueTrainStringList.get(index);
    }

    public StringBuilder getTTValue(int index){
        //Get 'attribute value' by index, TT is TrainTest, it can find all the attribute values in Train and Test
        //Mainly, just for checking problem use
        return attrvalueTTStringList.get(index);
    }

    public int getAllTrainValueSize(){
        //Get attribute value num
        return attrvalueTrainStringList.size();
    }

    public int getAllTTValueSize(){
        //Get attribute value num, for checking problem used
        return attrvalueTTStringList.size();
    }

    public int getAttrValueIndByString(String value){
        //get 'attribute value' index by string value, error return -1
        return IntStream.range(0, attrvalueTrainStringList.size())
                .filter(i -> value.equals(attrvalueTrainStringList.get(i)))
                .findFirst()
                .orElse(-1);
    }

    public String getAttrValueStrByIndex(int valueInd){
        //get 'attribute value' string by index
        return attrvalueTrainStringList.get(valueInd).toString();
    }

    public boolean getAttributeType(){
        //Get attribute type
        //Return attribute type, String as true, Digital as false
        return attributeTypeisStr;
    }

    public ArrayList<StringBuilder> getAllValue(){
        //Get 'attribute value' string list
        return attrvalueTrainStringList;
    }

    public HashMap<String, Integer>  getAttrValueMap(boolean checkIsTest){
        //Get attribute value map
        if(checkIsTest){
            return attrvalueMapforTest;
        }
        return attrvalueMap;
    }

    public ArrayList<Double> getAllTrainValueInDigital(){
        //Get 'attribute value' digital list
        return attrvalueDigitList;
    }

    public ArrayList<Double> getThresholdList(){
        //Get threshold for MEPA
        return thresholdList;
    }


    public void transTrainAttrValueSet2AttrValueList(){
        //transfer the hash set to arraylist
        attrvalueMap.forEach((attrValue, attrValuefrequency)->{
            attrvalueTrainStringList.add(new StringBuilder(attrValue));
            attrvalueTTStringList.add(new StringBuilder(attrValue));
        });

        attrvalueMapforTest.forEach((attrValue, attrValuefrequency)->{
            attrvalueTTStringList.add(new StringBuilder(attrValue));
        });
    }

    public void createDoubleList(boolean isDigitalType){
        //transfer all the value into double type
        if(isDigitalType){
            attrvalueMap.forEach((attrValue, attrValueFrequency) -> getAllTrainValueInDigital().add(Arithmetic.createDouble(attrValue)));
        }
    }

    private int autoItemFrequencyCounter(Integer itemFrequency){
        //Auto calculate the attribute value frequency
        return Optional.ofNullable(itemFrequency).orElse(new Integer(0)) + 1;
    }
}
