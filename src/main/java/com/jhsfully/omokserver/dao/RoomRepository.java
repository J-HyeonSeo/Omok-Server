package com.jhsfully.omokserver.dao;

import com.jhsfully.omokserver.entity.Room;
import com.jhsfully.omokserver.type.State;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends CrudRepository<Room, String> {

    List<Room> findByNowState(State state);

}
