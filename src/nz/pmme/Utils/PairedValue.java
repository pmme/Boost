package nz.pmme.Utils;

public class PairedValue<V1,V2>
{
    private V1 v1;
    public V1 getV1() { return v1; }

    private V2 v2;
    public V2 getV2() { return v2; }

    public PairedValue( V1 v1, V2 v2 ) {
        this.v1 = v1;
        this.v2 = v2;
    }
}
