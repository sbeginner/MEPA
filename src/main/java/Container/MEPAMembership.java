package Container;

/**
 * Created by jack on 2017/4/1.
 */
public class MEPAMembership {
    String membership;    //Membership name
    double membershipDegree;    //Membership degree

    public MEPAMembership(String membership, double membershipDegree){
        this.membership = membership;
        this.membershipDegree = membershipDegree;
    }

    public String getMembership(){
        return this.membership;
    }

    public double getMembershipDegree(){
        return this.membershipDegree;
    }
}
