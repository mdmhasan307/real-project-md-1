package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QRefSlcEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefSlcEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
public class RefSlcEntityRepositoryCustomImpl implements RefSlcEntityRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<RefSlcEntity> findByCode(String code) {
        val qRefSlc = QRefSlcEntity.refSlcEntity;

        val query = selectFrom(qRefSlc, entityManager);

        val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
        match(qRefSlc.shelfLifeCode, code, expressions);

        configurePredicate(query, expressions.getExpression());
        val results = query.fetch();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        else {
            return Optional.of(results.get(0));
        }
    }
}