package application;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import application.adapters.in.rest.controllers.AuthController;
import application.adapters.in.rest.controllers.OrderController;
import application.adapters.out.persistence.mongodb.repositories.AuditLogMongoRepository;
import application.adapters.out.persistence.mysql.repositories.BuyerJpaRepository;
import application.adapters.out.persistence.mysql.repositories.CartJpaRepository;
import application.adapters.out.persistence.mysql.repositories.CategoryJpaRepository;
import application.adapters.out.persistence.mysql.repositories.InventoryItemJpaRepository;
import application.adapters.out.persistence.mysql.repositories.InventoryMovementJpaRepository;
import application.adapters.out.persistence.mysql.repositories.OperationJpaRepository;
import application.adapters.out.persistence.mysql.repositories.OrderJpaRepository;
import application.adapters.out.persistence.mysql.repositories.ProductJpaRepository;
import application.adapters.out.persistence.mysql.repositories.SellerJpaRepository;
import application.adapters.out.persistence.mysql.repositories.UserJpaRepository;
import application.adapters.out.persistence.mysql.repositories.WarehouseJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Full Spring Boot application-context integration test verifying that all domain
 * services, input/output ports, persistence adapters, security adapters, and REST
 * controllers wire together cleanly.
 */
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration,"
                + "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration,"
                + "org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration,"
                + "org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration"
})
class NexusMarketApplicationTests {

    @MockitoBean
    private UserJpaRepository userJpaRepository;
    @MockitoBean
    private BuyerJpaRepository buyerJpaRepository;
    @MockitoBean
    private SellerJpaRepository sellerJpaRepository;
    @MockitoBean
    private WarehouseJpaRepository warehouseJpaRepository;
    @MockitoBean
    private CategoryJpaRepository categoryJpaRepository;
    @MockitoBean
    private ProductJpaRepository productJpaRepository;
    @MockitoBean
    private InventoryItemJpaRepository inventoryItemJpaRepository;
    @MockitoBean
    private InventoryMovementJpaRepository inventoryMovementJpaRepository;
    @MockitoBean
    private CartJpaRepository cartJpaRepository;
    @MockitoBean
    private OrderJpaRepository orderJpaRepository;
    @MockitoBean
    private OperationJpaRepository operationJpaRepository;
    @MockitoBean
    private AuditLogMongoRepository auditLogMongoRepository;
    @MockitoBean
    private JavaMailSender javaMailSender;

    @Autowired
    private AuthController authController;

    @Autowired
    private OrderController orderController;

    @Test
    void contextLoads() {
        assertNotNull(authController);
        assertNotNull(orderController);
    }
}
