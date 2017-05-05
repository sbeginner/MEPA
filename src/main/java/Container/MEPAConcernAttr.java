package Container;

/**
 * Created by jack on 2017/3/31.
 */
public class MEPAConcernAttr {
    double concernAttributeD;
    StringBuilder targetAttribute;
    StringBuilder concernAttribute;

    public MEPAConcernAttr(StringBuilder concernAttribute, StringBuilder targetAttribute){
        this.concernAttribute = concernAttribute;
        this.targetAttribute = targetAttribute;
    }

    public MEPAConcernAttr(double concernAttribute, StringBuilder targetAttribute){
        this.concernAttributeD = concernAttribute;
        this.targetAttribute = targetAttribute;
    }

    public String getConcernAttribute(){
        //For string type
        return concernAttribute.toString();
    }

    public double getConcernAttributeD(){
        //For double type
        return concernAttributeD;
    }

    public String getTargetAttributeString(){
        return targetAttribute.toString();
    }
}
