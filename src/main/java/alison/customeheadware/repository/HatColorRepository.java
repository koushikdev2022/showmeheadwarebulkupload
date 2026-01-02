package alison.customeheadware.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import alison.customeheadware.entity.Hat;
import alison.customeheadware.entity.HatColor;

import java.util.List;
import java.util.Optional;

public interface HatColorRepository extends JpaRepository<HatColor, Long> {
    
    // Derived query methods
    Optional<HatColor> findTop1ByHatAndNameIgnoreCase(Long id,String name);
}
