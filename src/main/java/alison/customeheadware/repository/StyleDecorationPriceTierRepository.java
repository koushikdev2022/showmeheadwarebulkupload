package alison.customeheadware.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import alison.customeheadware.entity.StyleDecorationPriceTier;

public interface StyleDecorationPriceTierRepository extends JpaRepository<StyleDecorationPriceTier,Long> {
   Optional<StyleDecorationPriceTier> findTop1ByHatIdAndDecorationTypeIdAndMinQty(Long hatId,Long id,Integer qty);
}
