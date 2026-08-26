package domain.repository;
import domain.model.entity.Warehouse;
import java.util.Optional;
public interface WarehouseRepository { Warehouse save(Warehouse warehouse); Optional<Warehouse> findById(Long id); }
