package alison.customeheadware.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import alison.customeheadware.entity.StyleDecorationPriceTier;

public interface StyleDecorationPriceTierRepository extends JpaRepository<StyleDecorationPriceTier,Long> {
    
}
