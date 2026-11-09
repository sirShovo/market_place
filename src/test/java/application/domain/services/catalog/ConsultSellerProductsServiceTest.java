package application.domain.services.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.DigitalProduct;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.services.authorization.ValidateSellerOwnershipService;
import application.domain.services.authorization.ValidateUserStatusService;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.UserStatus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsultSellerProductsServiceTest {

    private ConsultSellerProductsService service;
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new DigitalProduct();
        sampleProduct.setIdentifier("SKU-DIG-1");

        ProductRepositoryPort repo = new ProductRepositoryPort() {
            @Override
            public Product save(Product product) {
                return product;
            }

            @Override
            public Optional<Product> findByIdentifier(Product product) {
                return Optional.of(sampleProduct);
            }

            @Override
            public List<Product> findBySeller(Seller seller) {
                return List.of(sampleProduct);
            }

            @Override
            public List<Product> findPublished() {
                return List.of(sampleProduct);
            }

            @Override
            public void update(Product product) {
            }
        };

        service = new ConsultSellerProductsService(
                repo,
                new ValidateUserStatusService(),
                new ValidateSellerOwnershipService());
    }

    private User user(UserRole role, String doc) {
        User u = new User();
        u.setRole(role);
        u.setStatus(UserStatus.ACTIVE);
        u.setIdentification(new DocumentId(doc));
        return u;
    }

    private Seller seller(String doc) {
        Seller s = new Seller();
        s.setIdentification(new DocumentId(doc));
        return s;
    }

    @Test
    void sellerListsItsOwnProducts() {
        List<Product> result = service.consultBySeller(user(UserRole.SELLER, "900555"), seller("900555"));
        assertEquals(1, result.size());
        assertEquals("SKU-DIG-1", result.get(0).getIdentifier());
    }

    @Test
    void sellerCannotListAnotherSellersProducts() {
        assertThrows(UnauthorizedOperationException.class,
                () -> service.consultBySeller(user(UserRole.SELLER, "900999"), seller("900555")));
    }

    @Test
    void adminCanListAnySellersProducts() {
        List<Product> result = service.consultBySeller(user(UserRole.ADMIN, "1"), seller("900555"));
        assertEquals(1, result.size());
    }
}
