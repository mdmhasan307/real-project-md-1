package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.Customer;

import java.util.Optional;

public interface CustomerRepository {

  Optional<Customer> getByAac(String aac);

  Optional<Customer> findById(Integer customerId);

  void save(Customer customer);
}
