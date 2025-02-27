package com.inkomoko.smarttask.user.repository;

import com.inkomoko.smarttask.user.models.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long>, JpaSpecificationExecutor<PasswordReset> {
}
