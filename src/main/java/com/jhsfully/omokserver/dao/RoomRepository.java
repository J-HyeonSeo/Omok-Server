package com.jhsfully.omokserver.dao;

import com.jhsfully.omokserver.entity.Room;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends CrudRepository<Room, String> {

}
