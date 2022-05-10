package mil.usmc.mls2.stratis.common.model.enumeration;

public interface PersistableEnum<Integer, String> {

  public Integer getId();

  public String getLabel();
}
