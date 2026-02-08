package org.anuwas.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.anuwas.domain.Desktop;

@ApplicationScoped
public class DesktopRepository implements PanacheRepository<Desktop> {

}
