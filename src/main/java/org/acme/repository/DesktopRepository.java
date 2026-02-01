/**
 * 
 */
package org.acme.repository;

import org.acme.entity.Desktop;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * @author ambigo
 * 25-Jan-2026 1:12:36 pm
 */
@ApplicationScoped
public class DesktopRepository implements PanacheRepository<Desktop> {
	

}
