package application.domain.models;

import application.domain.valueobjects.BuyerCommercialStatus;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Commercial participant that browses the catalog and places orders (spec Domain 2).
 * A buyer never manages information belonging to another buyer, nor inventory
 * (spec RG03).
 */
@Getter
@Setter
@NoArgsConstructor
public class Buyer extends Person {

    private BuyerCommercialStatus commercialStatus;
    private String mainAddress;
    private List<String> additionalAddresses = new ArrayList<>();
}
