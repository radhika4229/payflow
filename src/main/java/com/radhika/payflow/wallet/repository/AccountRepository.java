package com.radhika.payflow.wallet.repository;

import com.radhika.payflow.wallet.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

}
