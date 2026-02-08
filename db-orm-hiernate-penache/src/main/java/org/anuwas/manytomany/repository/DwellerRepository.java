package org.anuwas.manytomany.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.anuwas.manytomany.entity.Dweller;

@ApplicationScoped
public class DwellerRepository implements PanacheRepository<Dweller> {
}
