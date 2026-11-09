package application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "buyers")
@Getter
@Setter
@NoArgsConstructor
public class BuyerJpaEntity {

    @Id
    @Column(name = "identification", nullable = false, length = 64)
    private String identification;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "role", nullable = false, length = 40)
    private String role;

    @Column(name = "commercial_status", nullable = false, length = 40)
    private String commercialStatus;

    @Column(name = "main_address", nullable = false)
    private String mainAddress;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "buyer_additional_addresses",
            joinColumns = @JoinColumn(name = "buyer_identification"))
    @Column(name = "additional_address")
    private List<String> additionalAddresses = new ArrayList<>();
}
