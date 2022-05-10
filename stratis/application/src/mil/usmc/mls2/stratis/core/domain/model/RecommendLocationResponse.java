package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Data;

import java.io.Serializable;


@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RecommendLocationResponse implements Serializable {
    private String location;
}
