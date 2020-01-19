package skyglass.data.service.generic;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public class GenericService extends BaseGenericService implements IBaseGenericService {

}
