package application.domain.ports.out;

import application.domain.models.Seller;
import application.domain.models.Warehouse;
import java.util.List;
import java.util.Optional;

/** Persistence contract for {@link Warehouse}. */
public interface WarehouseRepositoryPort {

    Warehouse save(Warehouse warehouse);

    Optional<Warehouse> findByIdentifier(Warehouse warehouse);

    List<Warehouse> findByOwner(Seller owner);

    void update(Warehouse warehouse);
}
