package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.Customer;

import java.util.Optional;

public interface CustomerService {

  Optional<Customer> getByAac(String aac);

  void save(Customer customer);
}
