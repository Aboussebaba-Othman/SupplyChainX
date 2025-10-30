package com.supplychainx.supply.entity;

import com.supplychainx.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "raw_materials")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class RawMaterial extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Le code matière est obligatoire")
    @Size(max = 50, message = "Le code ne peut pas dépasser 50 caractères")
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "Le nom de la matière première est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String name;

    @Column(name = "stock", nullable = false)
    @NotNull(message = "Le stock est obligatoire")
    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    private Integer stock;

    @Column(name = "stock_min", nullable = false)
    @NotNull(message = "Le stock minimum est obligatoire")
    @Min(value = 0, message = "Le stock minimum ne peut pas être négatif")
    private Integer stockMin;

    @Column(name = "unit", nullable = false, length = 20)
    @NotBlank(message = "L'unité de mesure est obligatoire")
    @Size(max = 20, message = "L'unité ne peut pas dépasser 20 caractères")
    private String unit;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "material_suppliers",
        joinColumns = @JoinColumn(name = "material_id"),
        inverseJoinColumns = @JoinColumn(name = "supplier_id")
    )
    @Builder.Default
    private List<Supplier> suppliers = new ArrayList<>();

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SupplyOrderLine> orderLines = new ArrayList<>();


    public boolean isLowStock() {
        return stock < stockMin;
    }
}
