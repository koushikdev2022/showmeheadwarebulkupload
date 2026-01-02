package alison.customeheadware.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import alison.customeheadware.entity.HatSizeVariant;

public interface HatSizeVariantRepository extends JpaRepository<HatSizeVariant, Long> {
    
   Optional<HatSizeVariant>  findTop1ByHatColorAndVariantNameIgnoreCase(Long id,String name);
}

