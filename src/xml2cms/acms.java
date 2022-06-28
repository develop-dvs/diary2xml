package xml2cms;

/**
 *
 * @author dcrm
 */
public abstract class acms {

    public acms() {
        this.save();
    }
    
    public void load() {
    }
    
    abstract protected void save();
}
