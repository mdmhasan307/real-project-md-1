package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.InvSerialLotNumSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.InvSerialLotNumEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QInvSerialLotNumEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
public class InvSerialLotNumEntityRepositoryCustomImpl implements InvSerialLotNumEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Long count(InvSerialLotNumSearchCriteria criteria) {
    val qSerial = QInvSerialLotNumEntity.invSerialLotNumEntity;
    val query = selectFrom(qSerial, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qSerial));
    return query.fetchCount();
  }

  @Override
  public List<InvSerialLotNumEntity> search(InvSerialLotNumSearchCriteria criteria) {
    val qSerial = QInvSerialLotNumEntity.invSerialLotNumEntity;
    val query = selectFrom(qSerial, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qSerial));
    return query.fetch();
  }

  private Predicate createSearchPredicate(InvSerialLotNumSearchCriteria criteria, QInvSerialLotNumEntity qEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qEntity.invSerialLotNumId, criteria.getInvSerialLotNumId(), expressions);
    match(qEntity.lotConNum, criteria.getLotConNum(), expressions);
    match(qEntity.niinId, criteria.getNiinId(), expressions);
    matchIgnoreCase(qEntity.serialNumber, criteria.getSerialNumber(), expressions); //serial number can be user input, so ignore case for matches. old code uppered both sides
    match(qEntity.cc, criteria.getCc(), expressions);
    match(qEntity.locationId, criteria.getLocationId(), expressions);
    notmatch(qEntity.locationId, criteria.getNotThisLocationId(), expressions);
    match(qEntity.inventoryItemId, criteria.getInventoryItemId(), expressions);
    return expressions.getExpression();
  }
}