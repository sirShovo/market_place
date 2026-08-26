package domain.repository;
import domain.model.aggregate.Product;
import java.util.Optional;
public interface ProductRepository { Product save(Product product); Optional<Product> findById(Long id); }
