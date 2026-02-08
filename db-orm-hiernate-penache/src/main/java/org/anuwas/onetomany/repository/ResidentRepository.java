package org.anuwas.onetomany.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.anuwas.onetomany.entity.Resident;

@ApplicationScoped
public class ResidentRepository implements PanacheRepository<Resident> {
}
