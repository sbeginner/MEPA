package Preprocess;


import DataStructure.Instances;

import static Setup.Config.MEPADATA_OUTPUT_BTN;

/**
 * Created by jack on 2017/3/31.
 */
public class Filter{
    public static Instances useFilter(Instances instances, MEPA MEPAFilter){
        //This filter uses MEPA method
        MEPAFilter.setInstances(instances);
        MEPAFilter.useFilter();

        if(MEPADATA_OUTPUT_BTN) MEPAFilter.MEPAInstancesOutput();

        return MEPAFilter.getInstances();
    }
}
