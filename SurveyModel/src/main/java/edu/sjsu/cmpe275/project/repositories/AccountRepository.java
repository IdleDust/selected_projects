package edu.sjsu.cmpe275.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.sjsu.cmpe275.project.entities.Account;

public interface AccountRepository extends JpaRepository<Account, Integer>{

	public Account findAccountByEmail(String email);
}
