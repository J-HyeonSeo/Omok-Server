package com.jhsfully.omokserver.dao;

import com.jhsfully.omokserver.entity.Player;
import com.jhsfully.omokserver.entity.Room;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends CrudRepository<Player, String> {

}
