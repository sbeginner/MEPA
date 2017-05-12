package Preprocess;

import DataStructure.Instance;
import DataStructure.Instances;
import MathCalculate.Arithmetic;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static MathCalculate.Arithmetic.createDouble;
import static MathCalculate.Arithmetic.mul;
import static Setup.Config.*;

/**
 * Created by JACK on 2017/5/12.
 */
public class MissingValue {
    Instances instances;

    public MissingValue(Instances instances){
        this.instances = instances;
    }

    public void mode(boolean checkIsTest){
        IntStream.range(0, ATTRIBUTE_NUM)
                .filter(currentAttributeInd -> instances.getAttributeMap().get(currentAttributeInd).getAttributeType())
                .forEach(currentAttributeInd -> {
                    String modeAttrvalue = instances.getAttributeMap()
                            .get(currentAttributeInd)
                            .getAttrValueMap(checkIsTest)
                            .entrySet()
                            .stream()
                            .max(Comparator.comparing(Map.Entry::getValue))
                            .get()
                            .getKey();

                    instances.getAttributeMap()
                            .get(currentAttributeInd)
                            .setForMissingValue(modeAttrvalue, checkIsTest);
                });
    }

    public void average(boolean checkIsTest){
        Map<Integer, Long> attributeMissingFrequency = instances.getmissingValueMap(checkIsTest)
                .entrySet()
                .stream()
                .map(item -> item.getValue())
                .flatMap(item -> item.stream())
                .collect(Collectors.groupingBy(item -> item, Collectors.counting()));

        IntStream.range(0, ATTRIBUTE_NUM)
                .filter(currentAttributeInd -> !instances.getAttributeMap().get(currentAttributeInd).getAttributeType())
                .forEach(currentAttributeInd -> {

                    double countValueFrequency = instances.getAttributeMap()
                            .get(currentAttributeInd)
                            .getAttrValueMap(checkIsTest)
                            .entrySet()
                            .stream()
                            .mapToDouble(item -> mul(createDouble(item.getKey()), item.getValue())).sum();

                    double totalnum = setTotalnum(checkIsTest, attributeMissingFrequency, currentAttributeInd);

                    double average = Arithmetic.div(countValueFrequency, totalnum);

                    instances.getAttributeMap().get(currentAttributeInd).setForMissingValue(average, checkIsTest);
                });
    }

    private double setTotalnum(boolean checkIsTest,  Map<Integer, Long> attributeMissingFrequency, int currentAttributeInd){
        if(instances.getCurrentMode()){
            if(checkIsTest){
                return setTotalnumSelectInMode(INSTANCE_NUM_TEST, attributeMissingFrequency, currentAttributeInd);
            }else{
                return setTotalnumSelectInMode(INSTANCE_NUM_TRAIN, attributeMissingFrequency, currentAttributeInd);
            }
        }else {
            return setTotalnumSelectInMode(INSTANCE_NUM, attributeMissingFrequency, currentAttributeInd);
        }
    }

    private double setTotalnumSelectInMode(double instancenum, Map<Integer, Long> attributeMissingFrequency, int currentAttributeInd){
        if(attributeMissingFrequency.containsKey(currentAttributeInd)){
            return Arithmetic.sub(instancenum, attributeMissingFrequency.get(currentAttributeInd));
        }else{
            return instancenum;
        }
    }
}
