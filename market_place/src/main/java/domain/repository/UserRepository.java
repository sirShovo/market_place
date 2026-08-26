package domain.repository;
import domain.model.entity.User;
import java.util.Optional;
public interface UserRepository { User save(User user); Optional<User> findById(Long id); }
