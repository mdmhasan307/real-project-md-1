package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.SerialLotNumTrackSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QSerialLotNumTrackEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SerialLotNumTrackEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
public class SerialLotNumTrackEntityRepositoryCustomImpl implements SerialLotNumTrackEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<SerialLotNumTrackEntity> search(SerialLotNumTrackSearchCriteria criteria) {
    val qSerial = QSerialLotNumTrackEntity.serialLotNumTrackEntity;
    val query = selectFrom(qSerial, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qSerial));
    return query.fetch();
  }

  private Predicate createSearchPredicate(SerialLotNumTrackSearchCriteria criteria, QSerialLotNumTrackEntity qEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qEntity.serialLotNumTrackId, criteria.getSerialLotNumTrackId(), expressions);
    match(qEntity.lotConNum, criteria.getLotConNum(), expressions);
    match(qEntity.niinId, criteria.getNiinId(), expressions);
    matchIgnoreCase(qEntity.serialNumber, criteria.getSerialNumber(), expressions); //serial number can be user input, so ignore case for matches. old cold uppered both sides
    match(qEntity.cc, criteria.getCc(), expressions);
    match(qEntity.locationId, criteria.getLocationId(), expressions);
    match(qEntity.issuedFlag, criteria.getIssueFlag(), expressions);
    notmatch(qEntity.locationId, criteria.getNotLocationId(), expressions);
    return expressions.getExpression();
  }
}