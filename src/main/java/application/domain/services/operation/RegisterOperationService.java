package application.domain.services.operation;

import application.domain.models.Operation;
import application.domain.ports.out.OperationRepositoryPort;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Persists a business {@link Operation}, stamping its execution date when absent.
 */
@Service
@RequiredArgsConstructor
public class RegisterOperationService {

    private final OperationRepositoryPort operationRepositoryPort;

    public Operation execute(Operation operation) {
        if (operation.getExecutionDate() == null) {
            operation.setExecutionDate(LocalDateTime.now());
        }
        return operationRepositoryPort.save(operation);
    }
}
