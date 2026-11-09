package application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sellers")
@Getter
@Setter
@NoArgsConstructor
public class SellerJpaEntity {

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

    @Column(name = "status", nullable = false, length = 40)
    private String status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "onboarded_by_user_id")
    private UserJpaEntity onboardedBy;
}
