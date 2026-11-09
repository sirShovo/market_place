package application.adapters.in.rest.responses;

public record WarehouseResponse(
        String identifier,
        String name,
        String type,
        String ownerIdentification,
        String location
) {
}
