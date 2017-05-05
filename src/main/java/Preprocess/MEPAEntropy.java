package Preprocess;

import Container.MEPAConcernAttr;
import MathCalculate.Arithmetic;

import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static Setup.Config.DIVIDE_CONSTRAINTNUM;

/**
 * Created by jack on 2017/4/1.
 */
public class MEPAEntropy {
    //Entropy calculate

    private int divConstraintNumCnt = 0;   //The counter, up to limited split number
    protected Queue<ArrayList<MEPAConcernAttr>> queueForChildInstance;    //Queue for storing the child data set(instance) split
    protected ArrayList<Double> bestThresholdList;    //Best threshold list


    protected void initTargetValueNumInEachAttribute(){
        //Init
        initDivConstraintNumCnt();
        initQueueForChildInstance();
        initBestThresholdList();
    }

    private void initQueueForChildInstance(){
        getQueueForChildInstance().clear();
    }

    private void initBestThresholdList(){
        this.bestThresholdList.clear();
    }

    private void initDivConstraintNumCnt(){
        this.divConstraintNumCnt = 0;
    }


    protected void calcEntropy(ArrayList<MEPAConcernAttr> concernAttrArrayList, ArrayList<Double> attrValueList){
        //calculate Entropy and find out the best threshold
        double bestThreshold = findBestThreshold(groupByTargetLevelFunc(concernAttrArrayList), groupByLevelFunc(concernAttrArrayList), attrValueList);
        bestThresholdList.add(bestThreshold);
        attrValueList.remove(bestThreshold);    //It's redundant to travel this threshold again

        autoDivConstraintNumCnt();

        //Split child instance and offer them to the queue
        //BFS is used
        splitChildInstance(concernAttrArrayList, bestThreshold);

        if(checkDivConstraintNumCnt() || getQueueForChildInstance().isEmpty() || attrValueList.isEmpty()){
            //Don't need to find the next threshold, cause of this limitation
            initQueueForChildInstance();    //Remove all item in queue;
            return;
        }


        calcEntropy(getQueueForChildInstance().poll(), attrValueList);
    }


    private double findBestThreshold(Map<String, Long> groupByTargetLevel, Map<Double, Map<String, Long>> groupByLevel, ArrayList<Double> attrValueList){

        int bestThresholdInd = IntStream.range(0, attrValueList.size())
                .reduce((maxThresholdInd, curThresholdInd) -> {
                    double fatherEntropy = calcFatherEntropy(groupByTargetLevel);
                    double childsEntropyMax = calcSplitChildEntropy(groupByLevel, attrValueList.get(maxThresholdInd));
                    double childsEntropyCur = calcSplitChildEntropy(groupByLevel, attrValueList.get(curThresholdInd));

                    double maxThresholdPerformance = Arithmetic.round(evalInformationGain(fatherEntropy, childsEntropyMax));
                    double curThresholdPerformance = Arithmetic.round(evalInformationGain(fatherEntropy, childsEntropyCur));

                    //Return the Information Gain larger one and the index
                    return maxThresholdPerformance < curThresholdPerformance ? curThresholdInd : maxThresholdInd;
                }).getAsInt();


        return attrValueList.get(bestThresholdInd);
    }

    private double calcFatherEntropy(Map<String, Long> targetAttrValueFrequency){
        return entropyCalculate(new ArrayList(targetAttrValueFrequency.values()));
    }

    private double entropyCalculate(ArrayList<Long> targetAttrValueFrequency){
        //Process target attribute has multi attribute-value (multi-class)
        double totalFrequency = sumCalculate(targetAttrValueFrequency);
        double totalEntropy = targetAttrValueFrequency.stream().mapToDouble(frequency -> {
            double divFreqInFreqtotal = Arithmetic.div(frequency, totalFrequency);
            double nonsignUnitEntropy = Arithmetic.mul(divFreqInFreqtotal, Arithmetic.ln(divFreqInFreqtotal));
            double unitEntropy = Arithmetic.mul(-1, nonsignUnitEntropy);

            return unitEntropy;
        }).sum();

        return Arithmetic.round(totalEntropy);
    }

    private double calcSplitChildEntropy(Map<Double, Map<String, Long>> groupByLevel, double curThreshold){
        //Split child Entropy calculate
        Map<String, Long> upperSplitEntropy = upperSplitChildEntropyFunc(groupByLevel, curThreshold);
        Map<String, Long> lowerSplitEntropy = lowerSplitChildEntropyFunc(groupByLevel, curThreshold);
        ArrayList<ArrayList<Long>> arrayListforAvg = addSplitChildList(upperSplitEntropy, lowerSplitEntropy);

        return avgEntropyCalculate(arrayListforAvg);
    }

    private double avgEntropyCalculate(ArrayList<ArrayList<Long>> arrayListforAvg){
        //Average Entropy calculate
        double allSum = arrayListforAvg.stream()
                .mapToDouble(item -> sumCalculate(item))
                .sum();

        double avgEntropy = arrayListforAvg.stream().mapToDouble(arrayItem -> {
            double unitWeight = Arithmetic.div(sumCalculate(arrayItem), allSum);
            double unitEntropy = entropyCalculate(arrayItem);
            return Arithmetic.mul(unitWeight, unitEntropy);
        }).sum();

        return Arithmetic.round(avgEntropy);
    }

    private double sumCalculate(ArrayList<Long> targetAttrValueFrequency){
        return Arithmetic.round(targetAttrValueFrequency.stream()
                .mapToDouble(Long::doubleValue)
                .sum());
    }

    private Map<String, Long> upperSplitChildEntropyFunc(Map<Double, Map<String, Long>> groupByLevel, double curThreshold){
        return splitChildEntropyFunc(groupByLevel, curThreshold, true);
    }

    private Map<String, Long> lowerSplitChildEntropyFunc(Map<Double, Map<String, Long>> groupByLevel, double curThreshold){
        return splitChildEntropyFunc(groupByLevel, curThreshold, false);
    }

    private Map<String, Long> splitChildEntropyFunc(Map<Double, Map<String, Long>> groupByLevel, double curThreshold, boolean isUpper){

        Map<String, Long> splitEntropy = groupByLevel.entrySet()
                .stream()
                .filter(TargetLevelGroupByDigital -> (TargetLevelGroupByDigital.getKey() >= curThreshold) == isUpper)
                .map(TargetLevelMap -> TargetLevelMap.getValue())
                .flatMap(TargetLevelMap -> TargetLevelMap.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.summingLong(frequency -> frequency))));

        return splitEntropy;
    }

    private ArrayList<ArrayList<Long>> addSplitChildList(Map<String, Long> upperSplitEntropy, Map<String, Long> lowerSplitEntropy){

        ArrayList<ArrayList<Long>> arrayListforAvg = new ArrayList();
        arrayListforAvg.add(new ArrayList(upperSplitEntropy.values()));
        arrayListforAvg.add(new ArrayList(lowerSplitEntropy.values()));

        return arrayListforAvg;
    }

    private double evalInformationGain(double fatherEntropy, double childsEntropy){
        return Arithmetic.sub(fatherEntropy, childsEntropy);
    }


    private Map<Double, Map<String, Long>> groupByLevelFunc(ArrayList<MEPAConcernAttr> concernAttrArrayList){
        //E.g. "Attribute value" Attribute[1] - a0 ,a1,...,an, "Target attribute value" Attribute[T] - t0 ,t1,...,tn
        //In this structure will look like => {(a0 => t0 ,t1,...,tn),(a1 => t0 ,t1,...,tn),...,(a2 => t0 ,t1,...,tn)}
        //,and keep the "Target attribute value" frequency in each attribute value an occurs
        return concernAttrArrayList.stream()
                .collect(Collectors.groupingBy(MEPAConcernAttr::getConcernAttributeD,
                        Collectors.groupingBy(MEPAConcernAttr::getTargetAttributeString, Collectors.counting())));
    }

    private Map<String, Long> groupByTargetLevelFunc(ArrayList<MEPAConcernAttr> concernAttrArrayList){
        //"Target attribute value" Attribute[T] - t0 ,t1,...,tn, and keep the frequency
        return concernAttrArrayList.stream()
                .collect(Collectors.groupingBy(MEPAConcernAttr::getTargetAttributeString, Collectors.counting()));
    }


    private void splitChildInstance(ArrayList<MEPAConcernAttr> concernAttrArrayList, double bestThreshold){
        //Split into child instance
        ArrayList<MEPAConcernAttr> upperChildList = upperSplitChildInstance(concernAttrArrayList, bestThreshold);
        ArrayList<MEPAConcernAttr> lowerChildList = lowerSplitChildInstance(concernAttrArrayList, bestThreshold);

        if(checkChildListExist(upperChildList, lowerChildList)){
            //Offer into queue
            //It is non-meaningful that occurs the list return null
            offerChildInstanceQueue(upperChildList, lowerChildList);
        }
    }

    private ArrayList<MEPAConcernAttr> upperSplitChildInstance(ArrayList<MEPAConcernAttr> concernAttrArrayList, double bestThreshold){
        //The left side
        return splitChildInstance(concernAttrArrayList, bestThreshold, true);
    }

    private ArrayList<MEPAConcernAttr> lowerSplitChildInstance(ArrayList<MEPAConcernAttr> concernAttrArrayList, double bestThreshold){
        //The right side
        return splitChildInstance(concernAttrArrayList, bestThreshold, false);
    }

    private ArrayList<MEPAConcernAttr> splitChildInstance(ArrayList<MEPAConcernAttr> concernAttrArrayList, double bestThreshold, boolean isUpper){
        //Split into two child instance
        return concernAttrArrayList.stream()
                .filter(item -> item.getConcernAttributeD() >= bestThreshold == isUpper)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void offerChildInstanceQueue(ArrayList<MEPAConcernAttr> upperChildList, ArrayList<MEPAConcernAttr> lowerChildList){
        getQueueForChildInstance().offer(upperChildList);
        getQueueForChildInstance().offer(lowerChildList);
    }


    protected Queue<ArrayList<MEPAConcernAttr>> getQueueForChildInstance(){
        return queueForChildInstance;
    }

    private boolean checkDivConstraintNumCnt(){
        return divConstraintNumCnt > DIVIDE_CONSTRAINTNUM;
    }

    private boolean checkChildListExist(ArrayList<MEPAConcernAttr> upperChildList, ArrayList<MEPAConcernAttr> lowerChildList){
        return !upperChildList.isEmpty() && !lowerChildList.isEmpty();
    }

    private int autoDivConstraintNumCnt(){
        return divConstraintNumCnt++;
    }
}
