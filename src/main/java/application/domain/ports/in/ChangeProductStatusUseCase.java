package application.domain.ports.in;

import application.domain.models.Product;
import application.domain.models.User;

/** Moves a product between PUBLISHED / SUSPENDED / DISCONTINUED. */
public interface ChangeProductStatusUseCase {

    Product changeStatus(User requester, Product product);
}
