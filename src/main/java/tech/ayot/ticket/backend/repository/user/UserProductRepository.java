package tech.ayot.ticket.backend.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tech.ayot.ticket.backend.model.user.UserProduct;

@Repository
public interface UserProductRepository extends JpaRepository<UserProduct, Long> {

    @Transactional
    void deleteAllByProductId(Long productId);
}
