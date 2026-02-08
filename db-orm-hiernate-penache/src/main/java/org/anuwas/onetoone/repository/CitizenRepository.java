package org.anuwas.onetoone.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.anuwas.onetoone.entity.Citizen;

@ApplicationScoped
public class CitizenRepository implements PanacheRepository<Citizen> {
}
