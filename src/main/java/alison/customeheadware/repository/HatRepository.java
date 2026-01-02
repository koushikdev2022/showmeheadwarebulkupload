package alison.customeheadware.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import alison.customeheadware.entity.Hat;

import java.util.List;
import java.util.Optional;

public interface HatRepository extends JpaRepository<Hat, Long> {
    
    
    Optional<Hat> findTop1ByNameIgnoreCase(String name);
    
    
    // Custom queries with pagination
}
