package alison.customeheadware.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import alison.customeheadware.entity.Hat;
import alison.customeheadware.entity.HatSizeVariant;

import java.util.List;
import java.util.Optional;

public interface HatSizeVariantRepository extends JpaRepository<HatSizeVariant, Long> {
    
   Optional<HatSizeVariant>  findTop1ByVariantNameIgnoreCase(String name);
}

