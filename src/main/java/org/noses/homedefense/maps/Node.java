package org.noses.homedefense.maps;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;

@Entity
@Data
public class Node {

    @GeneratedValue(strategy= GenerationType.AUTO)
    @Id
    private Long id;

    private BigDecimal lat;

    private BigDecimal lon;

}
