package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.Wac;

import java.util.List;
import java.util.Optional;

public interface WacService {

  Optional<Wac> findById(Integer id);
  List<Wac> getAll();
}
