package mil.usmc.mls2.stratis.common.domain.model;

/**
 * Domain Entity
 * <p>
 * Note: version will not be represented in the persistence as it is purely an infrastructural concept (persistence)
 */
public interface Entity<I extends EntityId> {

  I id();
}
