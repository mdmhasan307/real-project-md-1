package mil.usmc.mls2.stratis.common.domain.model;

/**
 * Standard Type
 */
public interface StandardType<I> {

  I id();

  String name();

  String label();
}
