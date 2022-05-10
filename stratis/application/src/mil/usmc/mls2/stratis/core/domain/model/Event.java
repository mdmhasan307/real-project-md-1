package mil.usmc.mls2.stratis.core.domain.model;

public interface Event extends Message {

  EventMeta getMeta();

  default void accept(EventVisitor visitor) { visitor.visit(this);}
}
