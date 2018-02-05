package TextModels;

/**
 *
 * @author G.A.P. II
 */
public interface ITextModel {
    
    int DATASET_1 = 0;
    int DATASET_2 = 1;
    
    public String getInstanceName();
    
    public void finalizeModel();
    
    public double getSimilarity(ITextModel oModel);
    
    public void updateModel(String text);
}
