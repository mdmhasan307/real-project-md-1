package mil.stratis.model.entity.walkThru;

import lombok.NoArgsConstructor;
import oracle.jbo.server.EntityDefImpl;
import oracle.jbo.server.EntityImpl;

@NoArgsConstructor
public class WalkThruQueueImpl extends EntityImpl {

  /**
   * @return the definition object for this instance class.
   */
  public static synchronized EntityDefImpl getDefinitionObject() {
    return EntityDefImpl.findDefObject("mil.stratis.model.entity.walkThru.WalkThruQueue");
  }
}
