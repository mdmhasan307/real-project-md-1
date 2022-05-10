package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
@SuppressWarnings("squid:S00119")
public interface EntityRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

  // marker for now
}
