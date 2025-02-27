package com.inkomoko.smarttask.user.repository;

import com.inkomoko.smarttask.user.models.User;
import com.inkomoko.smarttask.user.models.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {
    UserCredential findByUser(User user);
}
