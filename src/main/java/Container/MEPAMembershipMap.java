package Container;


import DataStructure.Attribute;
import DataStructure.Instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static Setup.Config.ATTRIBUTEVALUE_NUM;
import static Setup.Config.INSTANCE_NUM;
import static Setup.Config.TARGET_ATTRIBUTE;

/**
 * Created by jack on 2017/4/2.
 */
public class MEPAMembershipMap {
    private Instances instances;
    private HashMap<Attribute, ArrayList<MEPAMembership>> MEPAMembershipMap;
    private HashMap<Attribute, ArrayList<String>> attributeValueMap;
    private HashMap<Attribute, PriorProbabilityAttr> priorProbabilityMap;
    private HashMap<Attribute, ArrayList<Double>> bestThresholdMap;

    private boolean isTargetAttribute = false;

    public MEPAMembershipMap(Instances instances){
        MEPAMembershipMap = new HashMap(INSTANCE_NUM);
        attributeValueMap = new HashMap(ATTRIBUTEVALUE_NUM);
        priorProbabilityMap = new HashMap(ATTRIBUTEVALUE_NUM);
        bestThresholdMap = new HashMap(ATTRIBUTEVALUE_NUM);
        this.instances = instances;
    }

    public void setMEPAMembershipMap(Attribute curAttr, ArrayList<MEPAMembership> member){
        MEPAMembershipMap.put(curAttr, member);
    }

    public void setbestThresholdMap(Attribute curAttribute, ArrayList<Double> bestThresholdList){
        if(curAttribute.getIndex() == TARGET_ATTRIBUTE){
            return;
        }

        bestThresholdMap.put(curAttribute, bestThresholdList);
        //System.out.println(curAttribute.getIndex() + " " +bestThresholdList);
    }

    private void setAttributeValue(Attribute curAttr, ArrayList<String> attributeValueList){
        //Set Attribute Value
        attributeValueMap.put(curAttr, attributeValueList);
    }

    private void setTargetValue(Attribute curAttr, ArrayList<StringBuilder> targetValueList){
        //Set Target attribute value (Only Once), cause all the attribute list are the same
        if(isTargetAttribute == true){
            return;
        }

        isTargetAttribute = true;

        ArrayList<String> targetListtmp = targetValueList.stream()
                .map(item -> item.toString())
                .collect(Collectors.toCollection(ArrayList::new));
        setAttributeValue(curAttr, targetListtmp);
    }

    private void setPriorProbabilityMap(Attribute curAttribute, PriorProbabilityAttr priorProbabilityAttr){
        priorProbabilityMap.put(curAttribute, priorProbabilityAttr);
    }


    public ArrayList<MEPAMembership> getAllInstanceByAttr(Attribute curAttr){
        return MEPAMembershipMap.get(curAttr);
    }

    public ArrayList<MEPAMembership> getAllInstanceByAttr(int curAttrInd){
        return getAllInstanceByAttr(instances.getAttribute(curAttrInd));
    }

    public ArrayList<String> getAttributeValue(Attribute curAttr){
        return attributeValueMap.get(curAttr);
    }

    public ArrayList<String> getAttributeValue(int curAttrInd){
        return getAttributeValue(instances.getAttribute(curAttrInd));
    }

    public HashMap<Attribute, PriorProbabilityAttr> getPriorProbabilityMap(){
        return priorProbabilityMap;
    }

    public PriorProbabilityAttr getPriorProbabilityValueByAttr(Attribute curAttr){
        return priorProbabilityMap.get(curAttr);
    }

    public PriorProbabilityAttr getPriorProbabilityValueByAttr(int curAttrInd){
        return getPriorProbabilityValueByAttr(instances.getAttribute(curAttrInd));
    }


    public void priorProbabilityCalc(Attribute curAttribute){
        //Calculate the Prior probability for each attribute
        //The Prior probability use for support the situation that classification couldn't classify (e.g. The predict number is all in 0)

        if(curAttribute.getIndex() == TARGET_ATTRIBUTE){
            return;
        }

        ArrayList<MEPAConcernAttr> concernAttrArrayList = trnansConcernAttrList(curAttribute).getConcernAttrList();
        ArrayList<String> attributeValueList = new ArrayList(ATTRIBUTEVALUE_NUM);
        PriorProbabilityAttr priorProbabilityAttr = new PriorProbabilityAttr();

        priorProbabilityAttr.setTargetValue(getAttributeValue(instances.getAttribute(TARGET_ATTRIBUTE)));

        //Group the attribute value with target attribute value, e.g. V1 => {TargetV1(freq1), TargetV2(freq2),..., TargetVn(freqn)}
        groupByLevelFunc(concernAttrArrayList, true)
                .forEach((attributeValue,attrValue2TargetFrequency) ->{
                    attributeValueList.add(attributeValue);
                    priorProbabilityAttr.setAttributeValueMap(attributeValue, attrValue2TargetFrequency);
                });

        setAttributeValue(curAttribute, attributeValueList);
        setPriorProbabilityMap(curAttribute, priorProbabilityAttr);
    }

    private MEPAConcernAttrList trnansConcernAttrList(Attribute curAttribute){
        //After MEPA method processing
        //Cause of finding out the probability, we need to check the attribute value occurs with target attribute value
        ArrayList<MEPAMembership>  MEPAMemberList = instances.getMEPAMembership(curAttribute, false);
        MEPAConcernAttrList concernAttrList = new MEPAConcernAttrList();

        instances.getTrainInstanceMap()
                .entrySet()
                .stream()
                .forEach(instance -> {
                    StringBuilder concernAttribute = new StringBuilder(MEPAMemberList.get(instance.getKey()).getMembership());
                    StringBuilder targetAttribute = instance.getValue().getInstanceValue(TARGET_ATTRIBUTE);
                    concernAttrList.addMEPAConcernAttr(concernAttribute, targetAttribute);
                });

        setTargetValue(instances.getAttribute(TARGET_ATTRIBUTE), instances.getAttribute(TARGET_ATTRIBUTE).getAllValue());

        return concernAttrList;
    }

    private Map<String, Map<String, Long>> groupByLevelFunc(ArrayList<MEPAConcernAttr> concernAttrArrayList, boolean isProcessed){
        //After MEPA processed
        //Use for calculate Prior probability
        return concernAttrArrayList.stream()
                .collect(Collectors.groupingBy(MEPAConcernAttr::getConcernAttribute,
                        Collectors.groupingBy(MEPAConcernAttr::getTargetAttributeString, Collectors.counting())));
    }
}
