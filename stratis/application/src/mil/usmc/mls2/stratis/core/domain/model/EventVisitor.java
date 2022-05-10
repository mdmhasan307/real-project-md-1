package mil.usmc.mls2.stratis.core.domain.model;

public interface EventVisitor {

  default void visit(Event event) {}
}
