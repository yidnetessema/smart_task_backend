package com.inkomoko.smarttask.email.repositories;

import com.inkomoko.smarttask.email.models.EmailModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmailRepository extends JpaRepository<EmailModel, Long>, JpaSpecificationExecutor<EmailModel> {
}
