package alison.customeheadware.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import alison.customeheadware.entity.InventoryItem;
import alison.customeheadware.entity.PrimaryDecorationType;

public interface PrimaryDecorationTypeRepository extends JpaRepository<PrimaryDecorationType, Long>{

    
} 
