package org.noses.homedefense.maps;

import javax.persistence.*;
import java.util.List;

@Entity
public class Way {

    @GeneratedValue(strategy= GenerationType.AUTO)
    @Id
    private Long id;

    @OneToMany
    private List<Node> nodes;

    private String name;

    private Integer lanes;

    private Integer maxSpeed;

    private Boolean oneWay;

}
