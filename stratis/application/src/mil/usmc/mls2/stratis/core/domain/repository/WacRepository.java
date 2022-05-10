package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.Wac;

import java.util.List;
import java.util.Optional;

public interface WacRepository {

  Optional<Wac> findById(Integer id);

  List<Wac> getAll();
}
