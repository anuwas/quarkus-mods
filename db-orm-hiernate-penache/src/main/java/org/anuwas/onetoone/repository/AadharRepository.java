package org.anuwas.onetoone.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.anuwas.onetoone.entity.Aadhar;

@ApplicationScoped
public class AadharRepository implements PanacheRepository<Aadhar> {
}
