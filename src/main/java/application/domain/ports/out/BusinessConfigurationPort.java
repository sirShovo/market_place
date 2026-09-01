package application.domain.ports.out;

/**
 * Externally configurable business parameters that must not be hardcoded in the
 * domain.
 */
public interface BusinessConfigurationPort {

    /** Minutes an unpaid order may remain before the Phase 7 scheduler expires it. */
    int getUnpaidOrderExpirationMinutes();
}
